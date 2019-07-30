package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.ExternalIndicatorDelegate;
import com.orange.srs.refreport.model.ExternalIndicator;
import com.orange.srs.refreport.model.TO.ExternalIndicatorKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ExternalIndicatorProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class ExternalIndicatorDifferentialUpdate extends
		AbstractDifferentialUpdateTemplateMethod<ExternalIndicatorProvisioningTO, ExternalIndicator, ExternalIndicatorKeyTO> {

	@EJB
	private ExternalIndicatorDelegate externalIndicatorDelegate;

	@Override
	protected void sortProvisioningTOs(List<ExternalIndicatorProvisioningTO> externalIndicatorProvisioningTOs) {
		ExternalIndicatorDelegate.sortExternalIndicatorProvisioningTO(externalIndicatorProvisioningTOs);
	}

	@Override
	protected List<ExternalIndicator> getModelObjectsSorted() {
		return externalIndicatorDelegate.getAllExternalIndicatorSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<ExternalIndicatorProvisioningTO> externalIndicatorProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(ExternalIndicatorProvisioningTO externalIndicatorProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected ExternalIndicatorKeyTO getProvisioningTOKey(
			ExternalIndicatorProvisioningTO externalIndicatorProvisioningTO) {
		return ExternalIndicatorDelegate.getExternalIndicatorProvisioningTOKey(externalIndicatorProvisioningTO);
	}

	@Override
	protected ExternalIndicatorKeyTO getModelObjectKey(ExternalIndicator externalIndicator) {
		return ExternalIndicatorDelegate.getExternalIndicatorKey(externalIndicator);
	}

	@Override
	protected Boolean getSuppressFlag(ExternalIndicatorProvisioningTO externalIndicatorProvisioningTO) {
		return externalIndicatorProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(ExternalIndicator externalIndicator) {
		externalIndicatorDelegate.removeExternalIndicator(externalIndicator);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, ExternalIndicator externalIndicator,
			ExternalIndicatorProvisioningTO externalIndicatorProvisioningTO) throws BusinessException {
		return externalIndicatorDelegate.updateExternalIndicatorIfNecessary(externalIndicator,
				externalIndicatorProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			ExternalIndicatorProvisioningTO externalIndicatorProvisioningTO) throws BusinessException {
		externalIndicatorDelegate.createExternalIndicator(externalIndicatorProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "externalIndicator";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
