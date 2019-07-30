package com.orange.srs.refreport.business.delegate;

import java.io.StringWriter;
import java.util.Date;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.ejb.TransactionRolledbackLocalException;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.applicative.helper.ProvisioningHelper;
import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.SOA16InventoryGraphFacade;
import com.orange.srs.refreport.model.TO.ProvisioningFileStatusTO;
import com.orange.srs.refreport.model.TO.inventory.GraphCreationStatusTO;
import com.orange.srs.refreport.model.TO.provisioning.ProvisioningStatusTO;
import com.orange.srs.refreport.model.parameter.RetrieveProvisioningFileParameter;
import com.orange.srs.refreport.model.parameter.RetrieveProvisioningFilesForASourceParameter;
import com.orange.srs.refreport.technical.JMSConnectionHandler;
import com.orange.srs.refreport.technical.Statistics;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.TO.JobTO.JobMessageTO;
import com.orange.srs.statcommon.model.enums.JobEventCriticityEnum;
import com.orange.srs.statcommon.model.enums.JobEventTypeEnum;
import com.orange.srs.statcommon.model.enums.JobStateEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.jobparameter.ExportGraphInventoryJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.FlushGraphDatabasesJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.JobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.ProvisioningJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.RetrieveProvisioningFileJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.RetrieveProvisioningFilesForASourceJobParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

@Stateless
public class ProvisioningConsumerDelegate {

	private static final Logger LOGGER = Logger.getLogger(ProvisioningConsumerDelegate.class);

	@EJB
	private JMSConnectionHandler jmsConnectionHandler;

	@EJB
	private ProvisioningHelper provisioningHelper;

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private SOA16InventoryGraphFacade inventoryGraphFacade;

	public ProvisioningConsumerDelegate() {
	}

	@Asynchronous
	public void handleMessageReception(JobParameter parameter, SOAContext soaContext) {
		JobMessageTO response = new JobMessageTO();
		response.buildMessageTO(parameter);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, " job Id " + parameter.getJobId()));
		Long start = Utils.getTime();

		LOGGER.info(SOATools.buildSOAStartLogMessage(soaContext, "onMessage", parameter.taskType.toString()));

		try {
			switch (parameter.taskType) {
			case DO_PROVISIONNING:
				doProvisioning(parameter, response, soaContext);
				break;
			case RETRIEVE_PROVISIONING_FILE:
				retrieveProvisioningFile(parameter, response, soaContext);
				break;
			case RETRIEVE_PROVISIONING_FILES_FOR_A_SOURCE_TYPE:
				retrieveProvisioningFilesForAType(parameter, response, soaContext);
				break;
			case EXPORT_GRAPH_INVENTORY:
				exportGraphInventory(parameter, response, soaContext);
				break;
			case FLUSH_GRAPH_DATABASES:
				flushGraphDatabases(parameter, response, soaContext);
				break;
			default:
				response.jobSummaryTO.state = JobStateEnum.ERROR;
				response.jobSummaryTO.stateDetails = "Unmanaged task type: " + parameter.taskType;
				response.jobSummaryTO.addEvent(JobEventTypeEnum.MISCELLANEOUS, JobEventCriticityEnum.FATAL,
						response.jobSummaryTO.stateDetails);
				break;
			}

		} catch (Exception e) {
			Throwable cause = e;
			while (cause != null && (cause instanceof EJBTransactionRolledbackException
					|| cause instanceof TransactionRolledbackLocalException || cause instanceof EJBException)) {
				cause = cause.getCause();
			}
			String errorMsg = "Error occurs when processing " + parameter.taskType + " Task. Send back JMS message "
					+ cause.getMessage();
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, errorMsg), e);
			response.jobSummaryTO.addEvent(JobEventTypeEnum.MISCELLANEOUS, JobEventCriticityEnum.FATAL, errorMsg);
			response.jobSummaryTO.state = JobStateEnum.ERROR;
			response.jobSummaryTO.stateDetails = errorMsg;
			if (e instanceof BusinessException) {
				response.jobSummaryTO.exceptionCode = ((BusinessException) e).code;
			}
		}

		response.setJobDuration(start);
		response.setHostProcessJob(soaContext.server);
		jmsConnectionHandler.sendJMSMessage(response, parameter.responseCanal);
		LOGGER.info(SOATools.buildSOAEndLogMessage(soaContext, "onMessage", parameter.taskType.toString()));
	}

	private void doProvisioning(JobParameter parameter, JobMessageTO jobMessageTO, SOAContext soaContext) {

		Long start = Utils.getTime();

		ProvisioningJobParameter pjparameter = (ProvisioningJobParameter) parameter;
		jobMessageTO.date = pjparameter.provisioningDate.getTime();
		// add new context to parameter
		SOATools.buildSOAParameter(soaContext, pjparameter);
		LOGGER.info(SOATools.buildSOAStartLogMessage(soaContext, "doProvisionning", pjparameter.toString()));

		ProvisioningStatusTO provisioningStatus = provisioningHelper.doProvisioning(soaContext, pjparameter,
				jobMessageTO.jobSummaryTO);
		jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
		jobMessageTO.jobSummaryTO.stateDetails = marshallStatusTO(provisioningStatus);
		jobMessageTO.jobSummaryTO.jobOutput = jobMessageTO.jobSummaryTO.stateDetails;
		if (provisioningStatus != null) {
			long nbActionInError = provisioningStatus.actions.stream().filter(p -> p.error).count();
			if (nbActionInError > 0) {
				jobMessageTO.jobSummaryTO.state = JobStateEnum.ERROR;
				jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.MISCELLANEOUS, JobEventCriticityEnum.ERROR,
						jobMessageTO.jobSummaryTO.stateDetails);
			}
		}

		LOGGER.info(SOATools.buildSOAEndLogMessage(soaContext, "doProvisionning", pjparameter.toString()));

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.PROVISIONING,
				Utils.getTime() - start, Statistics.ALL, null, pjparameter.provisioningType.toString());
	}

	private void retrieveProvisioningFile(JobParameter parameter, JobMessageTO jobMessageTO, SOAContext soaContext) {

		Long start = Utils.getTime();

		RetrieveProvisioningFileJobParameter rpfjparameter = (RetrieveProvisioningFileJobParameter) parameter;
		jobMessageTO.date = new Date();
		RetrieveProvisioningFileParameter rpfparameter = new RetrieveProvisioningFileParameter(
				rpfjparameter.ProvisioningSource);
		// add new context to parameter
		SOATools.buildSOAParameter(soaContext, rpfjparameter);

		ProvisioningFileStatusTO provisioningFileStatus = null;
		try {
			provisioningFileStatus = provisioningFacade.retrieveProvisioningFiles(rpfparameter, soaContext);
			if (provisioningFileStatus.nbExpectedRetrievedFile == provisioningFileStatus.nbEffectiveRetrievedFile) {
				jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
			} else {
				jobMessageTO.jobSummaryTO.state = JobStateEnum.FINISHED_WITH_WARNING;
			}
			if (provisioningFileStatus.nbExpectedRetrievedFile == 0) {
				jobMessageTO.jobSummaryTO.jobOutput = "No files to be retrieved";
			} else {
				jobMessageTO.jobSummaryTO.jobOutput = provisioningFileStatus.nbEffectiveRetrievedFile
						+ " files successfully retrieved and uncompress on a target of "
						+ provisioningFileStatus.nbExpectedRetrievedFile;
			}
			jobMessageTO.jobSummaryTO.stateDetails = marshallStatusTO(provisioningFileStatus);

		} catch (BusinessException be) {
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"[retrieveProvisioningFile] An error occurs for source => no files to be retieved", be));
			jobMessageTO.jobSummaryTO.jobOutput = "No files to be retrieved; An error occurs: " + be.getMessage();
			jobMessageTO.jobSummaryTO.state = JobStateEnum.FINISHED_WITH_WARNING;
			jobMessageTO.jobSummaryTO.stateDetails = be.getMessage();
			jobMessageTO.jobSummaryTO.exceptionCode = be.code;
		}

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.RETRIEVE_PROVISIONING_FILE,
				Utils.getTime() - start, Statistics.ALL, null, Statistics.UNSET,
				provisioningFileStatus.nbEffectiveRetrievedFile, provisioningFileStatus.nbExpectedRetrievedFile);
	}

	private void retrieveProvisioningFilesForAType(JobParameter parameter, JobMessageTO jobMessageTO,
			SOAContext soaContext) {

		Long start = Utils.getTime();

		RetrieveProvisioningFilesForASourceJobParameter rpffatjparameter = (RetrieveProvisioningFilesForASourceJobParameter) parameter;
		jobMessageTO.date = new Date();
		RetrieveProvisioningFilesForASourceParameter rpffatparameter = new RetrieveProvisioningFilesForASourceParameter(
				rpffatjparameter.SourceType, rpffatjparameter.date, rpffatjparameter.origin);
		// Add new context to parameter
		SOATools.buildSOAParameter(soaContext, rpffatjparameter);

		ProvisioningFileStatusTO provisioningFileStatus = null;
		try {
			provisioningFileStatus = provisioningFacade.retrieveProvisioningFilesForASource(rpffatparameter,
					soaContext);
			if (provisioningFileStatus.nbExpectedRetrievedFile == provisioningFileStatus.nbEffectiveRetrievedFile) {
				jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
			} else {
				jobMessageTO.jobSummaryTO.state = JobStateEnum.FINISHED_WITH_WARNING;
			}
			if (provisioningFileStatus.nbExpectedRetrievedFile == 0) {
				jobMessageTO.jobSummaryTO.jobOutput = "No files to be retrieved for source "
						+ rpffatparameter.getSource() + ",origin " + rpffatparameter.getOrigin();
			} else {
				jobMessageTO.jobSummaryTO.jobOutput = provisioningFileStatus.nbEffectiveRetrievedFile
						+ " files successfully retrieved and uncompress on a target of "
						+ provisioningFileStatus.nbExpectedRetrievedFile;
			}
			jobMessageTO.jobSummaryTO.stateDetails = marshallStatusTO(provisioningFileStatus);

		} catch (BusinessException be) {
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"[retrieveProvisioningFilesForAType] An error occurs for source => no files to be retieved", be));
			jobMessageTO.jobSummaryTO.jobOutput = "No files to be retrieved for source " + rpffatparameter.getSource()
					+ ",origin " + rpffatparameter.getOrigin() + "; An error occurs: " + be.getMessage();
			jobMessageTO.jobSummaryTO.state = JobStateEnum.FINISHED_WITH_WARNING;
			jobMessageTO.jobSummaryTO.stateDetails = be.getMessage();
			jobMessageTO.jobSummaryTO.exceptionCode = be.code;
		}

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.RETRIEVE_PROVISIONING_FILE,
				Utils.getTime() - start, Statistics.ALL, null,
				rpffatjparameter.SourceType + '_' + rpffatjparameter.origin,
				provisioningFileStatus.nbEffectiveRetrievedFile, provisioningFileStatus.nbExpectedRetrievedFile);
	}

	private void exportGraphInventory(JobParameter parameter, JobMessageTO jobMessageTO, SOAContext soaContext)
			throws JAXBException {

		Long start = Utils.getTime();

		ExportGraphInventoryJobParameter jparameter = (ExportGraphInventoryJobParameter) parameter;
		jobMessageTO.date = new Date();
		GraphCreationStatusTO resultGraphCreationStatus = inventoryGraphFacade
				.createAndActivateInventoryGraphSynchronously(jobMessageTO.jobSummaryTO,
						jparameter.exportDate.getTime(), soaContext);

		if (resultGraphCreationStatus.nbNodeCreationError != 0
				|| resultGraphCreationStatus.nbParentLinkCreationError != 0
				|| resultGraphCreationStatus.nbComplexLinkCreationError != 0) {
			jobMessageTO.jobSummaryTO.state = JobStateEnum.FINISHED_WITH_WARNING;
		} else {
			jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
		}

		jobMessageTO.jobSummaryTO.stateDetails = marshallStatusTO(resultGraphCreationStatus);

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.EXPORT_GRAPH_INVENTORY,
				Utils.getTime() - start, Statistics.ALL, null, resultGraphCreationStatus.nbNodeCreated,
				resultGraphCreationStatus.nbParentLinkCreated, resultGraphCreationStatus.nbComplexLinkCreated);
	}

	private String marshallStatusTO(Object o) {
		try {
			StringWriter resultWriter = new StringWriter();
			JAXBRefReportFactory.getMarshaller().marshal(o, resultWriter);
			return resultWriter.toString();
		} catch (JAXBException jaxbException) {
			LOGGER.error("Canno't marshall object", jaxbException);
		}
		return "";
	}

	/**
	 * Flush all the Neo4J graph databases with state NEW/INACTIVE/TO_GARBAGE from memory.<br/>
	 * Keep the ACTIVE ones.<br/>
	 * Do not delete the Neo4J graph database files on disk.<br/>
	 * 
	 * @param parameter
	 * @param jobMessageTO
	 * @param soaContext
	 * 
	 * @author Pascal Morvan (Atos)
	 * @see [GSD02314-2018-114206-Openstat-BaseGraphNeo4J--ProgrammerLeMenage, PR_02314_P228_003]
	 *      {@link SOA16InventoryGraphFacade#flushAllExceptActiveGraphDatabases(SOAContext)}
	 */
	private void flushGraphDatabases(JobParameter parameter, JobMessageTO jobMessageTO, SOAContext soaContext) {
		long startTime = Utils.getTime();

		FlushGraphDatabasesJobParameter jparameter = (FlushGraphDatabasesJobParameter) parameter;
		jobMessageTO.date = new Date();
		SOATools.buildSOAParameter(soaContext, jparameter);

		Integer count = inventoryGraphFacade.flushAllExceptActiveGraphDatabases(soaContext);
		String result = String.format("%s=%d", "flushedGraphDatabases", count);

		jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
		jobMessageTO.jobSummaryTO.stateDetails = result;
		jobMessageTO.jobSummaryTO.jobOutput = jobMessageTO.jobSummaryTO.stateDetails;

		Statistics.addStatistics(soaContext.uuid, Statistics.REFREPORT, Statistics.FLUSH_GRAPH_DATABASES,
				Utils.getTime() - startTime, Statistics.ALL, null, count);
	}

}
