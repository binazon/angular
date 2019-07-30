package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.ReportingGroupToEntities;
import com.orange.srs.refreport.model.ReportingGroupToEntitiesId;

public interface ReportingGroupToEntitiesDAO extends Dao<ReportingGroupToEntities, ReportingGroupToEntitiesId> {
	List<Object[]> findAllEntityPartitionByReportingGroupPk(Long reportingGroupPk);
}
