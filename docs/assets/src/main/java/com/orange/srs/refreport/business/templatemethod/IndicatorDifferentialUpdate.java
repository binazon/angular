package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.IndicatorDelegate;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.TO.IndicatorKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class IndicatorDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<IndicatorProvisioningTO, Indicator, IndicatorKeyTO> {

	@EJB
	private IndicatorDelegate indicatorDelegate;

	@Override
	protected void sortProvisioningTOs(List<IndicatorProvisioningTO> indicatorProvisioningTOs) {
		IndicatorDelegate.sortIndicatorProvisioningTO(indicatorProvisioningTOs);
	}

	@Override
	protected List<Indicator> getModelObjectsSorted() {
		return indicatorDelegate.getAllIndicatorSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<IndicatorProvisioningTO> indicatorProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(IndicatorProvisioningTO indicatorProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected IndicatorKeyTO getProvisioningTOKey(IndicatorProvisioningTO indicatorProvisioningTO) {
		return IndicatorDelegate.getIndicatorProvisioningTOKey(indicatorProvisioningTO);
	}

	@Override
	protected IndicatorKeyTO getModelObjectKey(Indicator indicator) {
		return IndicatorDelegate.getIndicatorKey(indicator);
	}

	@Override
	protected Boolean getSuppressFlag(IndicatorProvisioningTO indicatorProvisioningTO) {
		return indicatorProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(Indicator indicator) {
		indicatorDelegate.removeIndicator(indicator);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, Indicator indicator,
			IndicatorProvisioningTO indicatorProvisioningTO) throws BusinessException {
		return indicatorDelegate.updateIndicatorIfNecessary(indicator, indicatorProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			IndicatorProvisioningTO indicatorProvisioningTO) throws BusinessException {
		indicatorDelegate.createIndicator(indicatorProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "indicator";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
