package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.FilterConfig;
import com.orange.srs.refreport.model.TO.FilterConfigTO;

public interface FilterConfigDAO extends Dao<FilterConfig, Long> {

	public List<FilterConfig> getFilterConfigsForOptionAndReportingGroup(String optionAlias, String origin,
			String reportingGroupRef);

	public List<FilterConfigTO> getFilterConfigTO();

}
