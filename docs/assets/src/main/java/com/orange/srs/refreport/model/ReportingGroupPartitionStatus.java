package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = ReportingGroupPartitionStatus.TABLE_NAME, indexes = {
		@Index(name = "INDEX_T_GROUP_PARTITION_STATUS", columnList = "REPORTING_GROUP_FK, DATE"),
		@Index(name = "INDEX_T_GROUP_PARTITION_ENTITY_TYPE", columnList = "ENTITY_TYPE, DATE") })

public class ReportingGroupPartitionStatus implements Serializable {

	private static final long serialVersionUID = 2483629607728185153L;

	public static final String TABLE_NAME = "T_GROUP_PARTITION_STATUS";

	public static final String COL_NAME_REPORTING_GROUP = "REPORTING_GROUP_FK";

	public static final String FIELD_REPORTING_GROUP = "targetGroup";
	public static final String FIELD_DATE = "date";
	public static final String FIELD_ENTITY_TYPE = "entityType";
	public static final String FIELD_PARTITIONS = "partitions";

	@Id
	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COL_NAME_REPORTING_GROUP)
	public ReportingGroup targetGroup;

	@Id
	@Column(name = "DATE", length = 6)
	public String date;

	@Id
	@Column(name = "ENTITY_TYPE")
	public String entityType;

	@Lob
	@Column(name = "PARTITIONS", columnDefinition = "TEXT")
	public String partitions;

	public ReportingGroup getTargetGroup() {
		return targetGroup;
	}

	public void setTargetGroup(ReportingGroup targetGroup) {
		this.targetGroup = targetGroup;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getPartitions() {
		return partitions;
	}

	public void setPartitions(String partitions) {
		this.partitions = partitions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime * targetGroup.hashCode();
		result = prime * result + date.hashCode();
		result = prime * result + entityType.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ReportingGroupPartitionStatus other = (ReportingGroupPartitionStatus) obj;

		if (!other.targetGroup.equals(targetGroup))
			return false;
		if (!other.date.equals(date))
			return false;
		if (!other.entityType.equals(entityType))
			return false;

		return true;
	}
}
