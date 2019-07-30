package com.orange.srs.refreport.provider.messaging;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA01InventoryFacade;
import com.orange.srs.refreport.business.SOA15PartitioningFacade;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryStatusTO;
import com.orange.srs.refreport.model.exception.InventoryException;
import com.orange.srs.refreport.technical.JMSConnectionHandler;
import com.orange.srs.refreport.technical.Statistics;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.TO.JobTO.JobMessageTO;
import com.orange.srs.statcommon.model.enums.ExportTypeEnum;
import com.orange.srs.statcommon.model.enums.JobEventCriticityEnum;
import com.orange.srs.statcommon.model.enums.JobEventTypeEnum;
import com.orange.srs.statcommon.model.enums.JobStateEnum;
import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;
import com.orange.srs.statcommon.model.parameter.ExportInventoryParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.OfferOptionSieveParameter;
import com.orange.srs.statcommon.model.parameter.inventory.SpecificInventoryParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.ExportEntitiesPartitionsJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.ExportInventoryJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.ExportReportInventoryJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.ExportReportingGroupsAndOfferOptionsJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.JobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.SpecificInventoryJobParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

public class ExportInventoryConsumer implements MessageListener {

	private static final Logger LOGGER = Logger.getLogger(ExportInventoryConsumer.class);

	@EJB
	private JMSConnectionHandler jmsConnectionHandler;

	@EJB
	private SOA01InventoryFacade inventoryFacade;

	@EJB
	private SOA15PartitioningFacade partitioningFacade;

	public ExportInventoryConsumer() {
	}

	@Override
	public void onMessage(Message message) {
		ObjectMessage omessage = (ObjectMessage) message;
		LOGGER.info("Message recu " + omessage.toString());

		try {
			JobParameter parameter = (JobParameter) omessage.getObject();

			// build SOA Context from parameter
			SOAContext soaContext = SOATools.buildSOAContext(parameter);

			JobMessageTO response = new JobMessageTO();
			response.date = new Date();
			response.buildMessageTO(parameter);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, " job Id " + parameter.getJobId()));
			Long start = Utils.getTime();

			LOGGER.info(SOATools.buildSOAStartLogMessage(soaContext, "onMessage", parameter.taskType.toString()));
			try {

				switch (parameter.taskType) {
				case EXPORT_INVENTORY:
					exportInventory(parameter, response, soaContext);
					break;
				case EXPORT_INVENTORY_REPORT:
					exportReportInventory(parameter, response, soaContext);
					break;
				case EXPORT_ENTITIES_PARTITIONS:
					exportEntitiesPartitions(parameter, response, soaContext);
					break;
				case EXPORT_REPORTING_GROUPS_AND_OFFER_OPTIONS:
					exportReportingGroupsAndOfferOptions(parameter, response, soaContext);
					break;
				case EXPORT_SPECIFIC_INVENTORY:
					exportSpecificInventory(parameter, response, soaContext);
					break;
				default:
					response.jobSummaryTO.state = JobStateEnum.ERROR;
					response.jobSummaryTO.stateDetails = "Unmanaged task type: " + parameter.taskType;
					response.jobSummaryTO.addEvent(JobEventTypeEnum.MISCELLANEOUS, JobEventCriticityEnum.FATAL,
							response.jobSummaryTO.stateDetails);
					break;
				}

			} catch (Exception e) {
				String errorMsg = "Error occurs when processing " + parameter.taskType + " Task. Send back JMS message "
						+ e.getMessage();

				LOGGER.error(SOATools.buildSOALogMessage(soaContext, errorMsg), e);
				response.jobSummaryTO.stateDetails = e.getMessage();
				response.jobSummaryTO.addEvent(JobEventTypeEnum.MISCELLANEOUS, JobEventCriticityEnum.FATAL, errorMsg);
				if (e instanceof BusinessException) {
					response.jobSummaryTO.exceptionCode = ((BusinessException) e).code;
				}
			}

			response.jobSummaryTO.computeAutoState();
			response.setJobDuration(start);
			response.setHostProcessJob(soaContext.server);
			jmsConnectionHandler.sendJMSMessage(response, parameter.responseCanal);
			LOGGER.info(SOATools.buildSOAEndLogMessage(soaContext, "onMessage", parameter.taskType.toString()));
		} catch (Exception e) {
			LOGGER.error("Error occurs when receiving Task " + e.getMessage(), e);
		}
	}

	private void exportInventory(JobParameter parameter, JobMessageTO jobMessageTO, SOAContext soaContext) {

		Long start = Utils.getTime();

		ExportInventoryJobParameter eijparameter = (ExportInventoryJobParameter) parameter;
		ExportInventoryParameter eiparameter = getExportInventoryParameter(eijparameter.reportingGroupRefId,
				eijparameter.origin, eijparameter.exportDate);
		// add new context to parameter
		SOATools.buildSOAParameter(soaContext, eiparameter);

		try {
			ExportInventoryStatusTO exportInventoryStatusTO = inventoryFacade.exportH2Inventory(eiparameter,
					soaContext);
			jobMessageTO.jobSummaryTO.jobOutput = exportInventoryStatusTO.fileName;
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					exportInventoryStatusTO.fileName);
			try {
				StringWriter resultWriter = new StringWriter();
				JAXBRefReportFactory.getMarshaller().marshal(exportInventoryStatusTO, resultWriter);
				jobMessageTO.jobSummaryTO.stateDetails = resultWriter.toString();
			} catch (JAXBException jaxbException) {
				LOGGER.error("Cannot write exportInventoryResult", jaxbException);
			}
		} catch (BusinessException e) {
			String errorMsg = e.getMessage();
			jobMessageTO.jobSummaryTO.jobOutput = "Inventory export not completed";
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.WARN, errorMsg);
			jobMessageTO.jobSummaryTO.stateDetails = errorMsg;
		}

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.EXPORT, Utils.getTime() - start,
				eijparameter.reportingGroupRefId, eijparameter.origin, eijparameter.taskType.toString());
	}

	private void exportReportInventory(JobParameter parameter, JobMessageTO jobMessageTO, SOAContext soaContext) {

		Long start = Utils.getTime();

		ExportReportInventoryJobParameter erijparameter = (ExportReportInventoryJobParameter) parameter;
		ExportInventoryParameter eiparameter = getExportInventoryParameter(erijparameter.reportingGroupRefId,
				erijparameter.origin, erijparameter.exportDate);
		// add new context to parameter
		SOATools.buildSOAParameter(soaContext, eiparameter);

		String reportFileLocation = "";
		String reportTemplateFileLocation = "";
		try {
			reportFileLocation = inventoryFacade.exportInventoryReport(eiparameter, soaContext,
					ReportOutputTypeEnum.INTERACTIVE);
			jobMessageTO.jobSummaryTO.jobOutput = reportFileLocation;
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO, reportFileLocation);
		} catch (InventoryException e) {
			reportFileLocation = e.getMessage();
			jobMessageTO.jobSummaryTO.jobOutput = "InteractiveReports export not completed";
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.ERROR, e.getMessage());
		}

		try {
			reportTemplateFileLocation = inventoryFacade.exportInventoryReport(eiparameter, soaContext,
					ReportOutputTypeEnum.TEMPLATE);
			jobMessageTO.jobSummaryTO.jobOutput += "\n" + reportTemplateFileLocation;
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					reportTemplateFileLocation);
		} catch (InventoryException e) {
			reportTemplateFileLocation = e.getMessage();
			jobMessageTO.jobSummaryTO.jobOutput += "\nTemplateReports export not completed";
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.ERROR, e.getMessage());
		}

		jobMessageTO.jobSummaryTO.stateDetails = reportFileLocation + "\n" + reportTemplateFileLocation;

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.EXPORT, Utils.getTime() - start,
				erijparameter.reportingGroupRefId, erijparameter.origin, erijparameter.taskType.toString());
	}

	private void exportEntitiesPartitions(JobParameter parameter, JobMessageTO jobMessageTO, SOAContext soaContext) {

		Long start = Utils.getTime();

		ExportEntitiesPartitionsJobParameter eepjparameter = (ExportEntitiesPartitionsJobParameter) parameter;
		// add new context to parameter
		SOATools.buildSOAParameter(soaContext, eepjparameter);

		try {
			jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
			jobMessageTO.jobSummaryTO.jobOutput = partitioningFacade.exportEntitiesPartitions(soaContext);
			jobMessageTO.jobSummaryTO.stateDetails = jobMessageTO.jobSummaryTO.jobOutput;
		} catch (InventoryException e) {
			jobMessageTO.jobSummaryTO.state = JobStateEnum.ERROR;
			jobMessageTO.jobSummaryTO.jobOutput = "EntitiesPartitions export not completed";
			jobMessageTO.jobSummaryTO.stateDetails = e.getMessage();
		}

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.EXPORT, Utils.getTime() - start,
				Statistics.ALL, null, eepjparameter.taskType.toString());
	}

	private static ExportInventoryParameter getExportInventoryParameter(String groupRefId, String origin,
			Calendar date) {
		ExportInventoryParameter parameter = new ExportInventoryParameter();
		parameter.reportingGroupRefId = groupRefId;
		parameter.origin = origin;
		parameter.date = date;

		return parameter;
	}

	private void exportReportingGroupsAndOfferOptions(JobParameter parameter, JobMessageTO jobMessageTO,
			SOAContext soaContext) {
		Long start = Utils.getTime();

		ExportReportingGroupsAndOfferOptionsJobParameter ergoojparameter = (ExportReportingGroupsAndOfferOptionsJobParameter) parameter;
		// add new context to parameter
		SOATools.buildSOAParameter(soaContext, ergoojparameter);

		try {
			jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
			jobMessageTO.jobSummaryTO.jobOutput = inventoryFacade
					.getReportingGroupsByOfferOptionType(ergoojparameter.offerOptionTypes, soaContext);
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					jobMessageTO.jobSummaryTO.jobOutput);
		} catch (BusinessException e) {
			jobMessageTO.jobSummaryTO.state = JobStateEnum.ERROR;
			jobMessageTO.jobSummaryTO.jobOutput = "ReportingGroupsAndOfferOptions export not completed";
			jobMessageTO.jobSummaryTO.stateDetails = e.getMessage();
		}

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.EXPORT, Utils.getTime() - start,
				Statistics.ALL, null, ergoojparameter.taskType.toString());
	}

	private void exportSpecificInventory(JobParameter parameter, JobMessageTO jobMessageTO, SOAContext soaContext) {

		Long start = Utils.getTime();

		SpecificInventoryJobParameter eiJobParameter = (SpecificInventoryJobParameter) parameter;
		SpecificInventoryParameter eiparameter = getExportSpecificInventoryParameter(eiJobParameter.sieveList,
				eiJobParameter.exportType);
		// add new context to parameter
		SOATools.buildSOAParameter(soaContext, eiparameter);

		try {
			jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
			final String fileName = inventoryFacade.exportH2SpecificInventory(eiparameter, soaContext);
			jobMessageTO.jobSummaryTO.jobOutput = fileName;
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO, fileName);
		} catch (InventoryException e) {
			jobMessageTO.jobSummaryTO.jobOutput = "Specific Inventory export not completed";
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.ERROR, e.getMessage());
			jobMessageTO.jobSummaryTO.stateDetails = e.getMessage();
		}

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.EXPORT_SPECIFIC_INVENTORY,
				Utils.getTime() - start, Statistics.ALL, null, eiparameter.getExportType(),
				eiparameter.getOfferOptionAliasesAsStringList().toArray());
	}

	private static SpecificInventoryParameter getExportSpecificInventoryParameter(
			List<OfferOptionSieveParameter> sieveList, ExportTypeEnum exportType) {
		SpecificInventoryParameter parameter = new SpecificInventoryParameter();
		parameter.setExportType(exportType);
		parameter.getSieveList().addAll(sieveList);

		return parameter;
	}
}
