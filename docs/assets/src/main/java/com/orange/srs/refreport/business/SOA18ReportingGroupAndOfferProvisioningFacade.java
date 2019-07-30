package com.orange.srs.refreport.business;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionRolledbackLocalException;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.delegate.OfferDelegate;
import com.orange.srs.refreport.business.delegate.ReportingGroupDelegate;
import com.orange.srs.refreport.consumer.dao.OfferOptionDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.model.TO.ProvisioningActionStatusTO;
import com.orange.srs.refreport.model.parameter.UpdateReportingGroupContext;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.business.commonFunctions.PatternResolutionDelegate;
import com.orange.srs.statcommon.model.TO.ReportingGroupKeyTO;
import com.orange.srs.statcommon.model.enums.OriginEnum;
import com.orange.srs.statcommon.model.parameter.PatternParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.provisioning.CreateReportingGroupOfferParameter;
import com.orange.srs.statcommon.model.parameter.provisioning.CreateReportingGroupParameter;
import com.orange.srs.statcommon.model.parameter.provisioning.CreateReportingGroupsAndOffersParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

@Stateless
public class SOA18ReportingGroupAndOfferProvisioningFacade {

	private static final Logger LOGGER = Logger.getLogger(SOA18ReportingGroupAndOfferProvisioningFacade.class);

	@EJB
	private ReportingGroupDelegate reportingGroupDelegate;

	@EJB
	private ReportingGroupDAO reportingGroupDAO;

	@EJB
	private OfferOptionDAO offerOptionDAO;

	@EJB
	private OfferDelegate offerDelegate;

	private static final int PAGINATION_SIZE = 100;
	private static final String PROVISIONING_PATH = "{ROOT}" + File.separatorChar + "{MOUNT}" + File.separatorChar
			+ "{PROVISIONING}" + File.separatorChar;
	private static final String COMMERCIAL_PERIMETER_FILE_NAME = "COMMERCIAL_PERIMETER{START_YYMMDD}.xml";
	private static final String TECHNICAL_PERIMETER_FILE_NAME = "TECHNICAL_PERIMETER{START_YYMMDD}.xml";

	public ProvisioningActionStatusTO updateReportingGroupsAndOffers(Date provisioningDate, SOAContext soaContext) {
		ProvisioningActionStatusTO actionStatus = new ProvisioningActionStatusTO("updateReportingGroupsAndOffers");
		long startTime = Utils.getTime();

		PatternParameter patternParam = new PatternParameter();
		patternParam.startUnit = provisioningDate;
		patternParam.properties = Configuration.mountConfiguration;
		patternParam.origin = OriginEnum.ALL.getValue();

		String fullFilePath = PatternResolutionDelegate
				.resolveBasePattern(PROVISIONING_PATH + COMMERCIAL_PERIMETER_FILE_NAME, patternParam);
		File file = new File(fullFilePath);

		actionStatus.comment = fullFilePath;

		if (!file.exists()) {

			LOGGER.error(SOATools.buildSOALogMessage(soaContext,
					"[updateReportingGroupsAndOffers] File " + fullFilePath + " not found."));
			actionStatus.comment += " / File not found";
			actionStatus.error = true;

		} else {

			String technicalRGFullFilePath = PatternResolutionDelegate
					.resolveBasePattern(PROVISIONING_PATH + TECHNICAL_PERIMETER_FILE_NAME, patternParam);
			File technicalRGFile = new File(technicalRGFullFilePath);

			UpdateReportingGroupContext updateContext = null;
			try {
				updateContext = reportingGroupDelegate.initUpdateReportingGroupContext(provisioningDate, soaContext);

				long startUnmarshallTime = Utils.getTime();

				LOGGER.info(SOATools.buildSOALogMessage(soaContext,
						"[updateReportingGroupsAndOffers] Reading file: " + fullFilePath));
				CreateReportingGroupsAndOffersParameter reportingGroupsAndOffers = (CreateReportingGroupsAndOffersParameter) JAXBRefReportFactory
						.getUnmarshaller().unmarshal(file);

				List<CreateReportingGroupParameter> groupsAndOffers = new ArrayList<>();

				Set<String> offersKnown = offerDelegate.getAllOfferAliases();
				if (reportingGroupsAndOffers.reportingGroups != null) {
					for (CreateReportingGroupParameter group : reportingGroupsAndOffers.reportingGroups) {
						boolean oneOfferKnown = false;
						for (CreateReportingGroupOfferParameter offer : group.offers) {
							if (offersKnown.contains(offer.name)) {
								oneOfferKnown = true;
								break;
							}
						}

						Set<String> reportingGroupRefsByOrigin = updateContext.reportingGroupRefsByOrigin
								.get(group.origin);
						boolean groupToAdd = oneOfferKnown
								// Case of one reporting group with no offer option
								|| (!oneOfferKnown && reportingGroupRefsByOrigin != null
										&& reportingGroupRefsByOrigin.contains(group.reportingGroupRef));
						if (groupToAdd) {
							groupsAndOffers.add(group);
						}
					}
				}
				updateContext.clearReportingGroups();

				if (technicalRGFile.exists()) {
					LOGGER.info(SOATools.buildSOALogMessage(soaContext,
							"[updateReportingGroupsAndOffers] Reading file: " + technicalRGFullFilePath));
					CreateReportingGroupsAndOffersParameter vpnReportingGroupsAndOffers = (CreateReportingGroupsAndOffersParameter) JAXBRefReportFactory
							.getUnmarshaller().unmarshal(technicalRGFile);
					if (vpnReportingGroupsAndOffers.reportingGroups != null) {
						groupsAndOffers.addAll(vpnReportingGroupsAndOffers.reportingGroups);
					}
				}

				updateContext.timeUnmarshal = Utils.getTime() - startUnmarshallTime;

				int end = 0;
				long timeInit, timeUnmarshal, timeGetFromDB, timeUpdateCriteriaAndDataLocation, timeUpdateGroupingRules,
						timeUpdateOptionAndReports, timePersist = 0L;

				for (int start = 0; start < groupsAndOffers.size(); start += PAGINATION_SIZE) {

					long stepStartTime = Utils.getTime();
					timeInit = updateContext.timeInit;
					timeUnmarshal = updateContext.timeUnmarshal;
					timeGetFromDB = updateContext.timeGetFromDB;
					timeUpdateCriteriaAndDataLocation = updateContext.timeUpdateCriteriaAndDataLocation;
					timeUpdateGroupingRules = updateContext.timeUpdateGroupingRules;
					timeUpdateOptionAndReports = updateContext.timeUpdateOptionAndReports;
					timePersist = updateContext.timePersist;

					end = (start + PAGINATION_SIZE < groupsAndOffers.size()) ? (start + PAGINATION_SIZE)
							: groupsAndOffers.size();
					reportingGroupDelegate.updateReportingGroup(groupsAndOffers.subList(start, end), updateContext);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Process " + PAGINATION_SIZE + " more ReportingGroups (Up to " + end + ") in "
								+ (Utils.getTime() - stepStartTime) + " ms.");
						LOGGER.debug("TIMES: timeInit=" + (updateContext.timeInit - timeInit) + ", timeUnmarshal="
								+ (updateContext.timeUnmarshal - timeUnmarshal) + ", timeGetFromDB="
								+ (updateContext.timeGetFromDB - timeGetFromDB) + ", timeUpdateCriteriaAndDataLocation="
								+ (updateContext.timeUpdateCriteriaAndDataLocation - timeUpdateCriteriaAndDataLocation)
								+ ", timeUpdateGroupingRules="
								+ (updateContext.timeUpdateGroupingRules - timeUpdateGroupingRules)
								+ ", timeUpdateOptionAndReports="
								+ (updateContext.timeUpdateOptionAndReports - timeUpdateOptionAndReports)
								+ ", timePersist=" + (updateContext.timePersist - timePersist));
					}
				}
				actionStatus.addInfo("nbUpdatedReportingGroup", end);
				actionStatus.addInfo("timeInit", Long.valueOf(updateContext.timeInit).intValue());
				actionStatus.addInfo("timeUnmarshal", Long.valueOf(updateContext.timeUnmarshal).intValue());
				actionStatus.addInfo("timeGetFromDB", Long.valueOf(updateContext.timeGetFromDB).intValue());
				actionStatus.addInfo("timeUpdateCriteriaAndDataLocation",
						Long.valueOf(updateContext.timeUpdateCriteriaAndDataLocation).intValue());
				actionStatus.addInfo("timeUpdateGroupingRules",
						Long.valueOf(updateContext.timeUpdateGroupingRules).intValue());
				actionStatus.addInfo("timeUpdateOptionAndReports",
						Long.valueOf(updateContext.timeUpdateOptionAndReports).intValue());
				actionStatus.addInfo("timePersist", Long.valueOf(updateContext.timePersist).intValue());
			} catch (Exception e) {
				Throwable cause = e;
				while (cause != null && (cause instanceof EJBTransactionRolledbackException
						|| cause instanceof TransactionRolledbackLocalException || cause instanceof EJBException)) {
					cause = cause.getCause();
				}
				LOGGER.error(SOATools.buildSOALogMessage(soaContext,
						"[updateReportingGroupsAndOffers] Error: " + cause.getMessage(), e));
				actionStatus.error = true;
			}
		}

		actionStatus.duration = Utils.getTime() - startTime;
		return actionStatus;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateReportingGroupsDataLocation(SOAContext soaContext) throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[updateReportingGroupsDataLocation] Start"));
		long startTime = Utils.getTime();

		int reportingGroupNb = 0;
		List<ReportingGroupKeyTO> reportingGroups = reportingGroupDAO.findAllReportingGroupsKeys();
		int end;
		for (int start = 0; start < reportingGroups.size(); start += PAGINATION_SIZE) {

			long stepStartTime = Utils.getTime();
			end = (start + PAGINATION_SIZE < reportingGroups.size()) ? (start + PAGINATION_SIZE)
					: reportingGroups.size();
			reportingGroupNb += reportingGroupDelegate.updateReportingGroupsDataLocation(soaContext,
					reportingGroups.subList(start, end));

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Process " + PAGINATION_SIZE + " more ReportingGroups (Up to " + end + ") in "
						+ (Utils.getTime() - stepStartTime) + " ms.");
			}
		}

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[updateReportingGroupsDataLocation] " + reportingGroupNb
				+ " are updated in " + (Utils.getTime() - startTime) + " ms"));

	}

}
