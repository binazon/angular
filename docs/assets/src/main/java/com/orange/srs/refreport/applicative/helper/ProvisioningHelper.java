package com.orange.srs.refreport.applicative.helper;

import java.io.File;
import java.io.StringWriter;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA03CatalogFacade;
import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.SOA15PartitioningFacade;
import com.orange.srs.refreport.business.SOA18ReportingGroupAndOfferProvisioningFacade;
import com.orange.srs.refreport.model.TO.provisioning.ProvisioningStatusTO;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.TO.JobTO.JobSummaryTO;
import com.orange.srs.statcommon.model.enums.JobEventCriticityEnum;
import com.orange.srs.statcommon.model.enums.JobEventTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.jobparameter.ProvisioningJobParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.concurrent.DefaultExecutorService;

@Stateless
public class ProvisioningHelper {

	private static final Logger LOGGER = Logger.getLogger(ProvisioningHelper.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private SOA03CatalogFacade catalogFacade;

	@EJB
	private SOA15PartitioningFacade partitioningFacade;

	@EJB
	private SOA18ReportingGroupAndOfferProvisioningFacade reportingGroupAndOfferProvisioningFacade;

	@EJB
	private DefaultExecutorService executorService;

	private final static String PROVISIONING_PATH = "{ROOT}" + File.separatorChar + "{MOUNT}" + File.separatorChar
			+ "{PROVISIONING}" + File.separatorChar;
	private final static String CLIENTSTAT_FILE_NAME = "CLIENT-STAT{START_YYMMDD}.xml";
	private final static String REPORTING_CUSTOMER_REFERENCES_FILE_PATH = PROVISIONING_PATH
			+ "ReportingCustomerReferences{START_YYMMDD}.xml";
	private final static String SPECIFIC_STAT_GROUP_FILE_NAME = "SpecificStatGroup.xml";

	private final static String ALIAS_3G = "3G";
	private final static String ALIAS_IPER = "IPER";
	private final static String ALIAS_BTGHKH = "BTG-hkh";

	@Asynchronous
	public void doProvisioningAsynchronous(SOAContext soaContext, ProvisioningJobParameter parameter,
			JobSummaryTO jobSummaryTO) {
		ProvisioningStatusTO provisioningStatusTO = doProvisioning(soaContext, parameter, jobSummaryTO);
		try {
			StringWriter resultWriter = new StringWriter();
			JAXBRefReportFactory.getMarshaller().marshal(provisioningStatusTO, resultWriter);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, resultWriter.toString()));
		} catch (JAXBException jaxbException) {
			LOGGER.warn("Canno't marshall object", jaxbException);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ProvisioningStatusTO doProvisioning(SOAContext soaContext, ProvisioningJobParameter parameter,
			JobSummaryTO jobSummaryTO) {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Entering doProvisioning"));
		ProvisioningStatusTO result = new ProvisioningStatusTO("provisioning " + parameter.provisioningType);

		long start = Utils.getTime();

		switch (parameter.provisioningType) {
		case IMPORT_DIFF_AND_UPDATE:
			importProvisioningFilesAndUpdateData(soaContext, result, parameter, jobSummaryTO, true);
			break;
		case IMPORT_DIFF:
			result.addAction(provisioningFacade.importProvisioningFiles(soaContext, jobSummaryTO,
					parameter.provisioningDate, true));
			break;
		case IMPORT_FULL_AND_UPDATE:
			importProvisioningFilesAndUpdateData(soaContext, result, parameter, jobSummaryTO, false);
			break;
		case IMPORT_FULL:
			result.addAction(provisioningFacade.importProvisioningFiles(soaContext, jobSummaryTO,
					parameter.provisioningDate, false));
			break;
		case UPDATE_REPORTING_GROUPS_AND_OFFERS:
			result.addAction(reportingGroupAndOfferProvisioningFacade
					.updateReportingGroupsAndOffers(parameter.provisioningDate.getTime(), soaContext));
			break;
		case UPDATE_ENTITIES_DATA_LOCATIONS:
			result.addAction(provisioningFacade.updateEntitiesDataLocation(soaContext, parameter.provisioningDate));
			break;
		case UPDATE_GROUP_REPORT_CONFIGS:
			result.addAction(catalogFacade.updateGroupReportConfig(soaContext));
			break;
		case UPDATE_GROUPS_PARTITIONS:
			result.addAction(partitioningFacade.updateGroupsPartitions(soaContext));
			break;
		case UPDATE_ENTITIES_PARTITIONS:
			result.addAction(partitioningFacade.updateEntitiesPartitions(soaContext));
			break;
		default:
			jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.FATAL,
					"Provisioning Type unknown: " + parameter.provisioningType);
		}

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exiting doProvisioning"));

		result.duration = Utils.getTime() - start;
		return result;
	}

	@Asynchronous
	public void importProvisioningFilesAndUpdateDataAsynchronous(final SOAContext soaContext,
			ProvisioningJobParameter parameter, boolean provisioningDiff) {

		ProvisioningStatusTO provisioningStatusTO = new ProvisioningStatusTO("importRefObjectFilesAndUpateData");
		importProvisioningFilesAndUpdateData(soaContext, provisioningStatusTO, parameter, new JobSummaryTO(),
				provisioningDiff);
		try {
			StringWriter resultWriter = new StringWriter();
			JAXBRefReportFactory.getMarshaller().marshal(provisioningStatusTO, resultWriter);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, resultWriter.toString()));
		} catch (JAXBException jaxbException) {
			LOGGER.warn("Canno't marshall object", jaxbException);
		}
	}

	private void importProvisioningFilesAndUpdateData(final SOAContext soaContext, final ProvisioningStatusTO status,
			ProvisioningJobParameter parameter, JobSummaryTO jobSummaryTO, boolean provisioningDiff) {

		long startTime = Utils.getTime();

		// -- Import RefObject provisioning files
		status.addAction(provisioningFacade.importProvisioningFiles(soaContext, jobSummaryTO,
				parameter.provisioningDate, provisioningDiff));

		// -- Update reporting groups : remove useless reporting group (no offer),
		// update links to offer option, criteria and data location
		status.addAction(reportingGroupAndOfferProvisioningFacade
				.updateReportingGroupsAndOffers(parameter.provisioningDate.getTime(), soaContext));

		// -- Update entities dataLocation
		status.addAction(provisioningFacade.updateEntitiesDataLocation(soaContext, parameter.provisioningDate));

		// -- Update Group Report Config
		status.addAction(catalogFacade.updateGroupReportConfig(soaContext));

		// -- Update entities partition number
		status.addAction(partitioningFacade.updateEntitiesPartitions(soaContext));

		// -- Update Group partitions
		status.addAction(partitioningFacade.updateGroupsPartitions(soaContext));

		status.duration = Utils.getTime() - startTime;

	}

}
