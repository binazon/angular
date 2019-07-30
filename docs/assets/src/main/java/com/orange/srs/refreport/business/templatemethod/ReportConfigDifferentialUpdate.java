package com.orange.srs.refreport.business.templatemethod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.orange.srs.refreport.business.delegate.ReportConfigDelegate;
import com.orange.srs.refreport.business.delegate.ReportOutputDelegate;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.ReportOutput;
import com.orange.srs.refreport.model.TO.ReportConfigKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportConfigProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class ReportConfigDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<ReportConfigProvisioningTO, ReportConfig, ReportConfigKeyTO> {

	@EJB
	private ReportConfigDelegate reportConfigDelegate;

	@EJB
	private ReportOutputDelegate reportOutputDelegate;

	@Inject
	private ReportConfigParamTypeLinkDifferentialUpdate reportConfigParamTypeLinkDifferentialUpdate;

	@Inject
	private ReportConfigIndicatorLinkDifferentialUpdate reportConfigIndicatorLinkDifferentialUpdate;

	private String offerOptionAlias;

	private Set<String> reportRefIdsFromDatabase = new HashSet<>();

	public void setOfferOptionAlias(String offerOptionAlias) {
		this.offerOptionAlias = offerOptionAlias;
	}

	@Override
	protected void sortProvisioningTOs(List<ReportConfigProvisioningTO> reportConfigProvisioningTOs) {
		ReportConfigDelegate.sortReportConfigProvisioningTO(reportConfigProvisioningTOs);
	}

	@Override
	protected List<ReportConfig> getModelObjectsSorted() {
		return reportConfigDelegate.getAllReportConfigSortedForOfferOption(offerOptionAlias);
	}

	@Override
	protected void initForChecks() {
		reportRefIdsFromDatabase = reportConfigDelegate
				.findAllReportRefIdLinkedToReportConfigForOfferOption(offerOptionAlias);
	}

	@Override
	protected void checkProvisioningTOsData(List<ReportConfigProvisioningTO> reportConfigProvisioningTOs)
			throws BusinessException {
		Set<String> reportRefIdsFromProvisioningTOs = new HashSet<>();
		for (ReportConfigProvisioningTO reportConfigProvisioningTO : reportConfigProvisioningTOs) {
			if (reportConfigProvisioningTO.reportRefId != null
					&& !Boolean.TRUE.equals(reportConfigProvisioningTO.suppress)) {
				if (reportRefIdsFromProvisioningTOs.contains(reportConfigProvisioningTO.reportRefId)) {
					throw new BusinessException(
							"Unable to proccess reportConfig [alias=" + reportConfigProvisioningTO.alias
									+ "], the reportRefId [" + reportConfigProvisioningTO.reportRefId
									+ "] is already defined in the provisioning file for another reportConfig");
				}
				reportRefIdsFromProvisioningTOs.add(reportConfigProvisioningTO.reportRefId);
			}
		}
	}

	@Override
	protected void processFunctionalCreationChecks(ReportConfigProvisioningTO reportConfigProvisioningTO)
			throws BusinessException {
		if (reportConfigProvisioningTO.reportRefId != null
				&& reportRefIdsFromDatabase.contains(reportConfigProvisioningTO.reportRefId)) {
			throw new BusinessException("Unable to create reportConfig (alias " + reportConfigProvisioningTO.alias
					+ "), the reportRefId (" + reportConfigProvisioningTO.reportRefId
					+ ") is already associated to another reportConfig in database for the offerOption (alias "
					+ offerOptionAlias + ")");
		}
	}

	@Override
	protected ReportConfigKeyTO getProvisioningTOKey(ReportConfigProvisioningTO reportConfigProvisioningTO) {
		return ReportConfigDelegate.getReportConfigProvisioningTOKey(reportConfigProvisioningTO);
	}

	@Override
	protected ReportConfigKeyTO getModelObjectKey(ReportConfig reportConfig) {
		return ReportConfigDelegate.getReportConfigKey(reportConfig);
	}

	@Override
	protected Boolean getSuppressFlag(ReportConfigProvisioningTO reportConfigProvisioningTO) {
		return reportConfigProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(ReportConfig reportConfig) {
		reportRefIdsFromDatabase.remove(reportConfig.getAssociatedReport().getRefId());
		reportConfigDelegate.removeReportConfig(reportConfig);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, ReportConfig reportConfig,
			ReportConfigProvisioningTO reportConfigProvisioningTO) throws BusinessException {

		// Update reportOutput if necessary
		boolean reportOutputHasBeenUpdated = reportOutputDelegate.updateReportOutputIfNecessary(
				reportConfig.getAssociatedReportOutput(), reportConfigProvisioningTO.reportOutputProvisioningTO);

		// Update reportConfig and some links if necessary
		boolean reportConfigHasBeenUpdated = reportConfigDelegate
				.updateReportConfigAndSingleLinksIfNecessary(reportConfig, reportConfigProvisioningTO);

		// Update reportConfig paramType links if necessary
		reportConfigParamTypeLinkDifferentialUpdate.setReportConfig(reportConfig);
		boolean paramTypeLinkHasBeenUpdated = reportConfigParamTypeLinkDifferentialUpdate.updateByDifferential(
				soaContext, reportConfigProvisioningTO.paramTypeAliasProvisioningTOs,
				FORCE_UPDATE_FROM_FILE_TO_DATABASE);

		// Update reportConfig indicator links if necessary
		reportConfigIndicatorLinkDifferentialUpdate.setReportConfig(reportConfig);
		boolean indicatorLinkHasBeenUpdated = reportConfigIndicatorLinkDifferentialUpdate.updateByDifferential(
				soaContext, reportConfigProvisioningTO.indicatorIdProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);

		return reportOutputHasBeenUpdated || reportConfigHasBeenUpdated || paramTypeLinkHasBeenUpdated
				|| indicatorLinkHasBeenUpdated;
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			ReportConfigProvisioningTO reportConfigProvisioningTO) throws BusinessException {

		ReportOutput reportOutput = reportOutputDelegate
				.createReportOutput(reportConfigProvisioningTO.reportOutputProvisioningTO);
		ReportConfig reportConfig = reportConfigDelegate.createReportConfigAndSingleLinks(reportConfigProvisioningTO,
				offerOptionAlias, reportOutput);
		int numberOfGroupReportConfigcreated = reportConfigDelegate
				.createGroupReportConfigsForReportConfig(reportConfig, offerOptionAlias);
		String logMessage = "[updateByDifferential] " + numberOfGroupReportConfigcreated
				+ " groupReportConfig(s) to be created at transaction commit for reportConfig "
				+ reportConfig.getAlias() + " and offer option " + offerOptionAlias;
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, logMessage));

		reportConfigParamTypeLinkDifferentialUpdate.setReportConfig(reportConfig);
		reportConfigParamTypeLinkDifferentialUpdate.updateByDifferential(soaContext,
				reportConfigProvisioningTO.paramTypeAliasProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);

		reportConfigIndicatorLinkDifferentialUpdate.setReportConfig(reportConfig);
		reportConfigIndicatorLinkDifferentialUpdate.updateByDifferential(soaContext,
				reportConfigProvisioningTO.indicatorIdProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "reportConfig";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
