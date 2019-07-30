package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.ReportInputDelegate;
import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class ReportInputDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<ReportInputProvisioningTO, ReportInput, ReportInputKeyTO> {

	@EJB
	private ReportInputDelegate reportInputDelegate;

	@Override
	protected void sortProvisioningTOs(List<ReportInputProvisioningTO> reportInputProvisioningTOs) {
		ReportInputDelegate.sortReportInputProvisioningTO(reportInputProvisioningTOs);
	}

	@Override
	protected List<ReportInput> getModelObjectsSorted() {
		return reportInputDelegate.getAllReportInputSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<ReportInputProvisioningTO> reportInputProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(ReportInputProvisioningTO reportInputProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected ReportInputKeyTO getProvisioningTOKey(ReportInputProvisioningTO reportInputProvisioningTO) {
		return ReportInputDelegate.getReportInputProvisioningTOKey(reportInputProvisioningTO);
	}

	@Override
	protected ReportInputKeyTO getModelObjectKey(ReportInput reportInput) {
		return ReportInputDelegate.getReportInputKey(reportInput);
	}

	@Override
	protected Boolean getSuppressFlag(ReportInputProvisioningTO reportInputProvisioningTO) {
		return reportInputProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(ReportInput reportInput) {
		reportInputDelegate.removeReportInput(reportInput);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, ReportInput reportInput,
			ReportInputProvisioningTO reportInputProvisioningTO) throws BusinessException {
		return reportInputDelegate.updateReportInputIfNecessary(reportInput, reportInputProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			ReportInputProvisioningTO reportInputProvisioningTO) throws BusinessException {
		reportInputDelegate.createReportInput(reportInputProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "reportInput";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
