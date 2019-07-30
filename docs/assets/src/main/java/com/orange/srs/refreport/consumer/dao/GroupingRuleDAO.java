package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.GroupingRule;
import com.orange.srs.refreport.model.GroupingRuleId;

public interface GroupingRuleDAO extends Dao<GroupingRule, GroupingRuleId> {

	public List<Object[]> findAllReportingGroupAndDistinctGroupingRule();

	public List<Object[]> findAllReportingGroupsAndAssociatedDomainGroupingValues();
}
