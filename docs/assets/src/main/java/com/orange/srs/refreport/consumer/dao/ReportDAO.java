package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.Report;
import com.orange.srs.refreport.model.TO.ReportRefIdAndOfferOptionTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportProvisioningTO;

public interface ReportDAO extends Dao<Report, Long> {

	public List<ReportProvisioningTO> findAllReportProvisioningTO();

	public List<ReportRefIdAndOfferOptionTO> findAllReportsWithOfferOptionTO();

}
