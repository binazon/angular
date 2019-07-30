package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.ReportDelegate;
import com.orange.srs.refreport.model.Report;
import com.orange.srs.refreport.model.TO.ReportKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class ReportDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<ReportProvisioningTO, Report, ReportKeyTO> {

	@EJB
	private ReportDelegate reportDelegate;

	@Override
	protected void sortProvisioningTOs(List<ReportProvisioningTO> reportProvisioningTOs) {
		ReportDelegate.sortReportProvisioningTO(reportProvisioningTOs);
	}

	@Override
	protected List<Report> getModelObjectsSorted() {
		return reportDelegate.getAllReportSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<ReportProvisioningTO> reportProvisioningTOs) throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(ReportProvisioningTO reportProvisioningTO) throws BusinessException {
		// No functional checks
	}

	@Override
	protected ReportKeyTO getProvisioningTOKey(ReportProvisioningTO reportProvisioningTO) {
		return ReportDelegate.getReportProvisioningTOKey(reportProvisioningTO);
	}

	@Override
	protected ReportKeyTO getModelObjectKey(Report report) {
		return ReportDelegate.getReportKey(report);
	}

	@Override
	protected Boolean getSuppressFlag(ReportProvisioningTO reportProvisioningTO) {
		return reportProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(Report report) {
		reportDelegate.removeReport(report);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, Report report,
			ReportProvisioningTO reportProvisioningTO) throws BusinessException {
		return reportDelegate.updateReportIfNecessary(report, reportProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext, ReportProvisioningTO reportProvisioningTO)
			throws BusinessException {
		reportDelegate.createReport(reportProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "report";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
