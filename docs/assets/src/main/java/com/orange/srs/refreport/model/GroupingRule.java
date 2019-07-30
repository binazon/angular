package com.orange.srs.refreport.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = GroupingRule.TABLE_NAME)
public class GroupingRule {

	public static final String TABLE_NAME = "T_GROUPING_RULE";

	public static final String FIELD_GROUPINGRULE = "groupingRuleId.groupingRule";
	public static final String FIELD_GROUPINGVALUE = "groupingRuleId.groupingValue";
	public static final String FIELD_REPORTING_GROUP = "groupingRuleId.targetGroup";

	@EmbeddedId
	private GroupingRuleId groupingRuleId;

	public GroupingRuleId getGroupingRuleId() {
		return groupingRuleId;
	}

	public void setGroupingRuleId(GroupingRuleId groupingRuleId) {
		this.groupingRuleId = groupingRuleId;
	}

	public String getGroupingRule() {
		if (groupingRuleId == null) {
			groupingRuleId = new GroupingRuleId();
		}
		return groupingRuleId.getGroupingRule();
	}

	public void setGroupingRule(String groupingRule) {
		if (groupingRuleId == null) {
			groupingRuleId = new GroupingRuleId();
		}
		groupingRuleId.setGroupingRule(groupingRule);
	}

	public String getGroupingValue() {
		if (groupingRuleId == null) {
			groupingRuleId = new GroupingRuleId();
		}
		return groupingRuleId.getGroupingValue();
	}

	public void setGroupingValue(String groupingValue) {
		if (groupingRuleId == null) {
			groupingRuleId = new GroupingRuleId();
		}
		groupingRuleId.setGroupingValue(groupingValue);
	}

	public ReportingGroup getTargetGroup() {
		if (groupingRuleId == null) {
			groupingRuleId = new GroupingRuleId();
		}
		return groupingRuleId.getTargetGroup();
	}

	public void setTargetGroup(ReportingGroup reportingGroup) {
		if (groupingRuleId == null) {
			groupingRuleId = new GroupingRuleId();
		}
		groupingRuleId.setTargetGroup(reportingGroup);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupingRuleId == null) ? 0 : groupingRuleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupingRule other = (GroupingRule) obj;
		if (groupingRuleId == null) {
			if (other.groupingRuleId != null)
				return false;
		} else if (!groupingRuleId.equals(other.groupingRuleId))
			return false;
		return true;
	}
}
