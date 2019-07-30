package com.orange.srs.refreport.business.templatemethod;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.IndicatorDelegate;
import com.orange.srs.refreport.business.delegate.ReportConfigDelegate;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.TO.IndicatorKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorIdProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class ReportConfigIndicatorLinkDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<IndicatorIdProvisioningTO, Indicator, IndicatorKeyTO> {

	@EJB
	private ReportConfigDelegate reportConfigDelegate;

	@EJB
	private IndicatorDelegate indicatorDelegate;

	private ReportConfig reportConfig;

	public void setReportConfig(ReportConfig reportConfig) {
		this.reportConfig = reportConfig;
	}

	@Override
	protected void sortProvisioningTOs(List<IndicatorIdProvisioningTO> indicatorIdProvisioningTOs) {
		IndicatorDelegate.sortIndicatorIdProvisioningTO(indicatorIdProvisioningTOs);
	}

	@Override
	protected List<Indicator> getModelObjectsSorted() {
		// Create a copy of the list used for the differential update: to avoid
		// automatic delete of the model object from this list deleting this model
		// object from the session
		return new ArrayList<>(IndicatorDelegate.getAllIndicatorSortedForReportConfig(reportConfig));
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<IndicatorIdProvisioningTO> indicatorIdProvisioningTOs) {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(IndicatorIdProvisioningTO indicatorIdProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected IndicatorKeyTO getProvisioningTOKey(IndicatorIdProvisioningTO indicatorIdProvisioningTO) {
		return IndicatorDelegate.getIndicatorIdProvisioningTOKey(indicatorIdProvisioningTO);
	}

	@Override
	protected IndicatorKeyTO getModelObjectKey(Indicator indicator) {
		return IndicatorDelegate.getIndicatorKey(indicator);
	}

	@Override
	protected Boolean getSuppressFlag(IndicatorIdProvisioningTO indicatorIdProvisioningTO) {
		return indicatorIdProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(Indicator indicator) {
		reportConfigDelegate.removeReportConfigIndicatorLink(reportConfig, indicator);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, Indicator indicator,
			IndicatorIdProvisioningTO indicatorIdProvisioningTO) {
		// No update possible
		return false;
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			IndicatorIdProvisioningTO indicatorIdProvisioningTO) throws BusinessException {
		Indicator indicator = indicatorDelegate.getIndicatorByKey(indicatorIdProvisioningTO.id);
		reportConfigDelegate.createReportConfigIndicatorLink(reportConfig, indicator);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "indicator link";
	}

	@Override
	protected String getEndLogMessage() {
		return " for reportConfigAlias " + reportConfig.getAlias();
	}

}
