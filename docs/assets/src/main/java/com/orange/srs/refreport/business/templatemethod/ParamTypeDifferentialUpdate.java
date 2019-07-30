package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.ParamTypeDelegate;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.TO.ParamTypeKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class ParamTypeDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<ParamTypeProvisioningTO, ParamType, ParamTypeKeyTO> {

	@EJB
	private ParamTypeDelegate paramTypeDelegate;

	@Override
	protected void sortProvisioningTOs(List<ParamTypeProvisioningTO> paramTypeProvisioningTOs) {
		ParamTypeDelegate.sortParamTypeProvisioningTO(paramTypeProvisioningTOs);
	}

	@Override
	protected List<ParamType> getModelObjectsSorted() {
		return paramTypeDelegate.getAllParamTypeSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<ParamTypeProvisioningTO> paramTypeProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(ParamTypeProvisioningTO paramTypeProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected ParamTypeKeyTO getProvisioningTOKey(ParamTypeProvisioningTO paramTypeProvisioningTO) {
		return ParamTypeDelegate.getParamTypeProvisioningTOKey(paramTypeProvisioningTO);
	}

	@Override
	protected ParamTypeKeyTO getModelObjectKey(ParamType paramType) {
		return ParamTypeDelegate.getParamTypeKey(paramType);
	}

	@Override
	protected Boolean getSuppressFlag(ParamTypeProvisioningTO paramTypeProvisioningTO) {
		return paramTypeProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(ParamType paramType) {
		paramTypeDelegate.removeParamType(paramType);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, ParamType paramType,
			ParamTypeProvisioningTO paramTypeProvisioningTO) throws BusinessException {
		return paramTypeDelegate.updateParamTypeIfNecessary(paramType, paramTypeProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			ParamTypeProvisioningTO paramTypeProvisioningTO) throws BusinessException {
		paramTypeDelegate.createParamType(paramTypeProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "paramType";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
