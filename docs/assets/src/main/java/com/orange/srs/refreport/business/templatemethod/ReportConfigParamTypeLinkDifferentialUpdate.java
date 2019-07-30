package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.ParamTypeDelegate;
import com.orange.srs.refreport.business.delegate.ReportConfigDelegate;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.TO.ParamTypeKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeAliasProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class ReportConfigParamTypeLinkDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<ParamTypeAliasProvisioningTO, ParamType, ParamTypeKeyTO> {

	@EJB
	private ReportConfigDelegate reportConfigDelegate;

	@EJB
	private ParamTypeDelegate paramTypeDelegate;

	private ReportConfig reportConfig;

	public void setReportConfig(ReportConfig reportConfig) {
		this.reportConfig = reportConfig;
	}

	@Override
	protected void sortProvisioningTOs(List<ParamTypeAliasProvisioningTO> paramTypeAliasProvisioningTOs) {
		ParamTypeDelegate.sortParamTypeAliasProvisioningTO(paramTypeAliasProvisioningTOs);
	}

	@Override
	protected List<ParamType> getModelObjectsSorted() {
		return paramTypeDelegate.getAllParamTypeSortedForReportConfig(reportConfig);
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<ParamTypeAliasProvisioningTO> paramTypeAliasProvisioningTOs) {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(ParamTypeAliasProvisioningTO paramTypeAliasProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected ParamTypeKeyTO getProvisioningTOKey(ParamTypeAliasProvisioningTO paramTypeAliasProvisioningTO) {
		return ParamTypeDelegate.getParamTypeAliasProvisioningTOKey(paramTypeAliasProvisioningTO);
	}

	@Override
	protected ParamTypeKeyTO getModelObjectKey(ParamType paramType) {
		return ParamTypeDelegate.getParamTypeKey(paramType);
	}

	@Override
	protected Boolean getSuppressFlag(ParamTypeAliasProvisioningTO paramTypeAliasProvisioningTO) {
		return paramTypeAliasProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(ParamType paramType) {
		reportConfigDelegate.removeReportConfigParamTypeLink(reportConfig, paramType);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, ParamType paramType,
			ParamTypeAliasProvisioningTO paramTypeAliasProvisioningTO) {
		// No update possible
		return false;
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			ParamTypeAliasProvisioningTO paramTypeAliasProvisioningTO) throws BusinessException {
		ParamType paramType = paramTypeDelegate.getParamTypeByKey(paramTypeAliasProvisioningTO.alias);
		reportConfigDelegate.createReportConfigParamTypeLink(reportConfig, paramType);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "paramType link";
	}

	@Override
	protected String getEndLogMessage() {
		return " for reportConfig " + reportConfig.getAlias();
	}

}
