package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.GroupingRuleDAO;
import com.orange.srs.refreport.model.GroupingRule;
import com.orange.srs.refreport.model.GroupingRuleId;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.external.GroupingRuleEnum;

@Stateless
public class GroupingRuleDAOImpl extends AbstractJpaDao<GroupingRule, GroupingRuleId> implements GroupingRuleDAO {

	@Override
	public List<Object[]> findAllReportingGroupAndDistinctGroupingRule() {
		String jpqlQuery = "SELECT DISTINCT gr." + GroupingRule.FIELD_REPORTING_GROUP + "." + ReportingGroup.FIELD_PK
				+ ", gr." + GroupingRule.FIELD_GROUPINGRULE + " FROM " + getEntityName() + " gr";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<Object[]> findAllReportingGroupsAndAssociatedDomainGroupingValues() {
		String jpqlQuery = "SELECT DISTINCT gr." + GroupingRule.FIELD_REPORTING_GROUP + "."
				+ ReportingGroup.FIELD_ORIGIN + ", gr." + GroupingRule.FIELD_REPORTING_GROUP + "."
				+ ReportingGroup.FIELD_REPORTING_GROUP_REF + ", gr." + GroupingRule.FIELD_GROUPINGVALUE + " FROM "
				+ getEntityName() + " gr" + " WHERE gr." + GroupingRule.FIELD_GROUPINGRULE + "='"
				+ GroupingRuleEnum.DOMAINS.getGroupingCriteria() + "'";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}
