package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputProvisioningTO;
import com.orange.srs.statcommon.model.TO.ReportInputTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;
import com.orange.srs.statcommon.model.parameter.report.ReportInputKeyParameter;

public interface ReportInputDAO extends Dao<ReportInput, Long> {

	public List<ReportInputTO> findAllReportInput();

	public List<ReportInputTO> findReportInputByReportRefId(String reportRefId);

	public List<ReportInput> findReportInputByKey(ReportInputKeyParameter keyParameter);

	public List<ReportInputKeyTO> findAvailableReportInputKeys();

	public List<ReportInputProvisioningTO> findAllReportInputProvisioningTO();

	public List<ReportInput> findCassandraReportInput();
}
