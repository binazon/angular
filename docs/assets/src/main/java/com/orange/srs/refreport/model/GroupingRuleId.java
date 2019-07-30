package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class GroupingRuleId implements Serializable {

	private static final long serialVersionUID = 3230411704605151817L;

	public static final String COL_NAME_GROUPING_RULE = "GROUPING_RULE";
	public static final String COL_NAME_GROUPING_VALUE = "GROUPING_VALUE";
	public static final String COL_NAME_REPORTING_GROUP_FK = "REPORTING_GROUP_FK";

	@Column(name = COL_NAME_GROUPING_RULE, nullable = false)
	private String groupingRule;

	@Column(name = COL_NAME_GROUPING_VALUE, nullable = false)
	private String groupingValue;

	@ManyToOne
	@JoinColumn(name = COL_NAME_REPORTING_GROUP_FK, nullable = false)
	private ReportingGroup targetGroup;

	public String getGroupingRule() {
		return groupingRule;
	}

	public void setGroupingRule(String groupingRule) {
		this.groupingRule = groupingRule;
	}

	public String getGroupingValue() {
		return groupingValue;
	}

	public void setGroupingValue(String groupingValue) {
		this.groupingValue = groupingValue;
	}

	public ReportingGroup getTargetGroup() {
		return targetGroup;
	}

	public void setTargetGroup(ReportingGroup targetGroup) {
		this.targetGroup = targetGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupingRule == null) ? 0 : groupingRule.hashCode());
		result = prime * result + ((groupingValue == null) ? 0 : groupingValue.hashCode());
		result = prime * result + ((targetGroup == null) ? 0 : targetGroup.hashCode());
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
		GroupingRuleId other = (GroupingRuleId) obj;
		if (groupingRule == null) {
			if (other.groupingRule != null)
				return false;
		} else if (!groupingRule.equals(other.groupingRule))
			return false;
		if (groupingValue == null) {
			if (other.groupingValue != null)
				return false;
		} else if (!groupingValue.equals(other.groupingValue))
			return false;
		if (targetGroup == null) {
			if (other.targetGroup != null)
				return false;
		} else if (!targetGroup.equals(other.targetGroup))
			return false;
		return true;
	}
}
