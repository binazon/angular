package com.orange.srs.refreport.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = ReportingGroupToEntities.COL_NAME_TYPE, discriminatorType = DiscriminatorType.STRING)
@Table(name = ReportingGroupToEntities.TABLE_NAME)
public class ReportingGroupToEntities {

	public static final String TABLE_NAME = "TJ_REPORTING_GROUP_TO_ENTITIES";

	public static final String COL_NAME_TYPE = "TYPE";
	public static final String COL_NAME_REPORTING_ENTITY_FK = "REPORTING_ENTITY_FK";
	public static final String COL_NAME_REPORTING_GROUP_FK = "REPORTING_GROUP_FK";
	public static final String COL_NAME_BELONGS_TO = "BELONGS_TO";

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "reportingGroup", column = @Column(name = COL_NAME_REPORTING_GROUP_FK, nullable = false)),
			@AttributeOverride(name = "reportingEntity", column = @Column(name = COL_NAME_REPORTING_ENTITY_FK, nullable = false)) })
	private ReportingGroupToEntitiesId reportingGroupToEntitiesId = new ReportingGroupToEntitiesId();

	@Column(name = COL_NAME_BELONGS_TO)
	private boolean belongsTo;

	public ReportingGroupToEntitiesId getReportingGroupToEntitiesId() {
		return reportingGroupToEntitiesId;
	}

	public void setReportingGroupToEntitiesId(ReportingGroupToEntitiesId reportingGroupToEntitiesId) {
		this.reportingGroupToEntitiesId = reportingGroupToEntitiesId;
	}

	public Long getReportingGroupId() {
		return reportingGroupToEntitiesId.getReportingGroupId();
	}

	public void setReportingGroupId(Long pk) {
		reportingGroupToEntitiesId.setReportingGroupId(pk);
	}

	public Long getReportingEntityId() {
		return reportingGroupToEntitiesId.getReportingEntityId();
	}

	public void setReportingEntityId(Long pk) {
		reportingGroupToEntitiesId.setReportingEntityId(pk);
	}

	public boolean isBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(boolean belongsTo) {
		this.belongsTo = belongsTo;
	}

	@Override
	public String toString() {
		return getReportingGroupId() + "," + getReportingEntityId();
	}
}
