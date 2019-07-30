package com.orange.srs.refreport.business.templatemethod;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.ReportInputDelegate;
import com.orange.srs.refreport.business.delegate.SourceClassDelegate;
import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.SourceClass;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputKeyProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class SourceClassReportInputLinkDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<ReportInputKeyProvisioningTO, ReportInput, ReportInputKeyTO> {

	@EJB
	private SourceClassDelegate sourceClassDelegate;

	@EJB
	private ReportInputDelegate reportInputDelegate;

	private SourceClass sourceClass;
	private List<ReportInputKeyProvisioningTO> reportInputKeyProvisioningTOsAlreadyUsed;

	public void setSourceClass(SourceClass sourceClass) {
		this.sourceClass = sourceClass;
	}

	public void setReportInputKeyProvisioningTOsAlreadyUsed(
			List<ReportInputKeyProvisioningTO> reportInputKeyProvisioningTOsAlreadyUsed) {
		this.reportInputKeyProvisioningTOsAlreadyUsed = reportInputKeyProvisioningTOsAlreadyUsed;
	}

	@Override
	protected void sortProvisioningTOs(List<ReportInputKeyProvisioningTO> reportInputKeyProvisioningTOs) {
		ReportInputDelegate.sortReportInputKeyProvisioningTO(reportInputKeyProvisioningTOs);
	}

	@Override
	protected List<ReportInput> getModelObjectsSorted() {
		// Create a copy of the list used for the differential update: to avoid
		// automatic delete of the model object from this list deleting this model
		// object from the session
		return new ArrayList<ReportInput>(ReportInputDelegate.getAllReportInputSortedForSourceClass(sourceClass));
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<ReportInputKeyProvisioningTO> reportInputKeyProvisioningTOs)
			throws BusinessException {
		// Check same ReportInput defined for multiple sourceClass only if the
		// reportInput is not deleted
		for (ReportInputKeyProvisioningTO reportInputKeyProvisioningTO : reportInputKeyProvisioningTOs) {
			if (!Boolean.TRUE.equals(reportInputKeyProvisioningTO.suppress)) {
				if (reportInputKeyProvisioningTOsAlreadyUsed.contains(reportInputKeyProvisioningTO)) {
					ReportInputKeyTO reportInputKeyTO = getProvisioningTOKey(reportInputKeyProvisioningTO);
					throw new BusinessException("Unable to update sourceClass (" + sourceClass.getSourceClass()
							+ "), the reportInput (" + reportInputKeyTO
							+ ") is already associated to another sourceClass in the provisoning file");
				}
				reportInputKeyProvisioningTOsAlreadyUsed.add(reportInputKeyProvisioningTO);
			}
		}
	}

	@Override
	protected void processFunctionalCreationChecks(ReportInputKeyProvisioningTO reportInputKeyProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected ReportInputKeyTO getProvisioningTOKey(ReportInputKeyProvisioningTO reportInputKeyProvisioningTO) {
		return ReportInputDelegate.getReportInputKeyProvisioningTOKey(reportInputKeyProvisioningTO);
	}

	@Override
	protected ReportInputKeyTO getModelObjectKey(ReportInput reportInput) {
		return ReportInputDelegate.getReportInputKey(reportInput);
	}

	@Override
	protected Boolean getSuppressFlag(ReportInputKeyProvisioningTO reportInputKeyProvisioningTO) {
		return reportInputKeyProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(ReportInput reportInput) {
		sourceClassDelegate.removeSourceClassReportInputLink(sourceClass, reportInput);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, ReportInput reportInput,
			ReportInputKeyProvisioningTO reportInputKeyProvisioningTO) {
		// No update possible
		return false;
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			ReportInputKeyProvisioningTO reportInputKeyProvisioningTO) throws BusinessException {
		ReportInput reportInput = reportInputDelegate.getReportInputByKey(reportInputKeyProvisioningTO.reportInputRef,
				reportInputKeyProvisioningTO.granularity, reportInputKeyProvisioningTO.sourceTimeUnit);
		sourceClassDelegate.createSourceClassReportInputLink(sourceClass, reportInput);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "reportInput link";
	}

	@Override
	protected String getEndLogMessage() {
		return " for sourceClass " + sourceClass.getSourceClass();
	}

}
