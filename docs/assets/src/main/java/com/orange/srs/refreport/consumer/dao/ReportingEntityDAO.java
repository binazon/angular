package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.TO.ReportingEntityPartitionTO;
import com.orange.srs.refreport.model.TO.ReportingEntityTypeTO;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryGkTO;

public interface ReportingEntityDAO extends Dao<ReportingEntity, Long> {

	public List<Object[]> findEntityDataLocationInfo(String entityType, String... origin);

	public List<Long> findPkOrderedByGroupAndType();

	public List<ReportingEntityPartitionTO> findEntityPartitionWherePkInSelectionOrderByType(List<Long> pkSelection);

	public List<ReportingEntityPartitionTO> findEntityPartitionWherePkInSelectionOrderByGroupAndType(
			List<Long> pkSelection);

	public List<Long> findPkOrderBy(String[] orderAttributes, boolean reverseOrder);

	public List<ReportingEntityTypeTO> findReportingEntitiesTypesForAReportingGroup(String reportingGroupRef,
			String reportingGroupOrigin, String entityType, String entitySubtype);

	public List<ExportSpecificInventoryGkTO> findExportInventoryForGK(List<String> entityTypes);

	@Deprecated
	public Long findLastPk();
}
