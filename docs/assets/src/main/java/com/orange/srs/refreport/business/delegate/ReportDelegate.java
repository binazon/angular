package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.ReportDAO;
import com.orange.srs.refreport.model.Report;
import com.orange.srs.refreport.model.TO.ReportKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.enums.ReportGranularityEnum;
import com.orange.srs.statcommon.model.enums.ReportTimeUnitEnum;

@Stateless
public class ReportDelegate {

	@EJB
	private ReportDAO reportDao;

	public static ReportKeyTO getReportKey(Report report) {
		return new ReportKeyTO(report.getRefId());
	}

	public static ReportKeyTO getReportProvisioningTOKey(ReportProvisioningTO reportProvisioningTO) {
		return new ReportKeyTO(reportProvisioningTO.refId);
	}

	public Report getReportByKey(String reportRefId) throws BusinessException {
		List<Report> listReport = reportDao.findBy(Report.FIELD_REFID, reportRefId);
		if (listReport.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": Report with key [reportRefId="
					+ reportRefId + "]");
		}
		return listReport.get(0);
	}

	public Report createReport(ReportProvisioningTO reportProvisioningTO) {
		Report report = new Report();
		report.setRefId(reportProvisioningTO.refId);
		report.setLabel(reportProvisioningTO.label);
		report.setComputeUri(reportProvisioningTO.computeUri);
		report.setReportTimeUnit(ReportTimeUnitEnum.valueOf(reportProvisioningTO.reportTimeUnit));
		report.setGranularity(ReportGranularityEnum.valueOf(reportProvisioningTO.granularity));
		reportDao.persistAndFlush(report);
		return report;
	}

	public boolean updateReportIfNecessary(Report report, ReportProvisioningTO reportProvisioningTO) {

		boolean updated = false;

		ReportTimeUnitEnum reportTimeUnitEnumTO = ReportTimeUnitEnum.valueOf(reportProvisioningTO.reportTimeUnit);
		ReportGranularityEnum reportGranularityEnumTO = ReportGranularityEnum.valueOf(reportProvisioningTO.granularity);

		if (!reportProvisioningTO.label.equals(report.getLabel())
				|| !reportProvisioningTO.computeUri.equals(report.getComputeUri())
				|| !reportTimeUnitEnumTO.equals(report.getReportTimeUnit())
				|| !reportGranularityEnumTO.equals(report.getGranularity())) {

			report.setLabel(reportProvisioningTO.label);
			report.setComputeUri(reportProvisioningTO.computeUri);
			report.setReportTimeUnit(reportTimeUnitEnumTO);
			report.setGranularity(reportGranularityEnumTO);
			reportDao.persistAndFlush(report);
			updated = true;
		}
		return updated;
	}

	public void removeReport(Report report) {
		reportDao.remove(report);
	}

	public List<Report> getAllReportSorted() {
		List<Report> reports = reportDao.findAll();
		Collections.sort(reports, new ReportComparator());
		return reports;
	}

	public ReportListProvisioningTO getReportListProvisioningTOSorted() {
		ReportListProvisioningTO reportListProvisioningTO = new ReportListProvisioningTO();
		reportListProvisioningTO.reportProvisioningTOs = reportDao.findAllReportProvisioningTO();
		sortReportProvisioningTO(reportListProvisioningTO.reportProvisioningTOs);
		return reportListProvisioningTO;
	}

	public static void sortReportProvisioningTO(List<ReportProvisioningTO> reportProvisioningTOs) {
		Collections.sort(reportProvisioningTOs, new ReportProvisioningTOComparator());
	}

	private static class ReportComparator implements Comparator<Report> {
		@Override
		public int compare(Report firstObj, Report secondObj) {
			return getReportKey(firstObj).compareTo(getReportKey(secondObj));
		}
	}

	private static class ReportProvisioningTOComparator implements Comparator<ReportProvisioningTO> {
		@Override
		public int compare(ReportProvisioningTO firstObj, ReportProvisioningTO secondObj) {
			return getReportProvisioningTOKey(firstObj).compareTo(getReportProvisioningTOKey(secondObj));
		}
	}
}
