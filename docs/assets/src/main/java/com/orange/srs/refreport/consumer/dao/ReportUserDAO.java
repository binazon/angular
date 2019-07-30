package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.ReportUser;
import com.orange.srs.refreport.model.TO.ReportUserTO;

public interface ReportUserDAO extends Dao<ReportUser, Long> {

	public List<ReportUserTO> findReportUserByReportUserPk(Long reportUserId);

	public List<ReportUserTO> findAllReportUserTO();

}
