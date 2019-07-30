package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.GroupReportConfig;
import com.orange.srs.refreport.model.TO.GroupReportConfigTO;

public interface GroupReportConfigDAO extends Dao<GroupReportConfig, Long> {

	public List<GroupReportConfig> findGroupReportConfigForOption(String optionalias);

	public List<GroupReportConfig> findGroupReportConfigForOptionLocationASC(String optionalias);

	public List<GroupReportConfig> findGroupReportConfigForOptionAndGroup(String optionalias, String origin,
			String reportingGroupRef);

	public List<Object[]> findGroupReportConfigPksAndParamTypeAliasAndReportingGroupPkAssociated();

	public int enableOrDisableGroupReportConfig(List<Long> groupReportConfigPks, boolean toBeEnabled);

	public List<GroupReportConfigTO> findAdditionalGroupReportConfigTO();

}
