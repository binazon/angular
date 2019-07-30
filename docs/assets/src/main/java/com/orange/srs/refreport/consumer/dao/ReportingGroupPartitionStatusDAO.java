package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.ReportingGroupPartitionStatus;

public interface ReportingGroupPartitionStatusDAO extends Dao<ReportingGroupPartitionStatus, Long> {
	public void truncate();

	public List<ReportingGroupPartitionStatus> findByDateAndEntityType(String date, String entityType);
}
