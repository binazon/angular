package com.orange.srs.refreport.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = DataLocation.TABLE_NAME)
public class DataLocation {

	public static final String TABLE_NAME = "T_DATA_LOCATION";

	public final static String FIELD_PK = "pk";
	public final static String FIELD_LOCATION_PATTERN = "locationPattern";
	public final static String FIELD_REPORTING_ENTITY = "reportingEntity";

	public final static String COL_NAME_CRITERIA_VALUE = "CRITERIA_VALUE";
	public final static String COL_NAME_CRITERIA_TYPE = "CRITERIA_TYPE";
	public final static String COL_NAME_LOCATION_PATTERN = "LOCATION_PATTERN";
	public final static String COL_NAME_PK = "PK";
	public final static String COL_NAME_REPORTING_ENTITY_FK = "REPORTING_ENTITY_FK";

	@Id
	@Column(name = COL_NAME_PK)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = COL_NAME_LOCATION_PATTERN, nullable = false)
	private String locationPattern;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumns({ @JoinColumn(name = COL_NAME_CRITERIA_VALUE, referencedColumnName = "CRITERIA_VALUE"),
			@JoinColumn(name = COL_NAME_CRITERIA_TYPE, referencedColumnName = "CRITERIA_TYPE") })
	private Criteria criteria;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = COL_NAME_REPORTING_ENTITY_FK)
	private ReportingEntity reportingEntity;

	public String getLocationPattern() {
		return locationPattern;
	}

	public void setLocationPattern(String locationPattern) {
		this.locationPattern = locationPattern;
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public Criteria getCriteria() {
		return criteria;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public ReportingEntity getReportingEntity() {
		return reportingEntity;
	}

	public void setReportingEntity(ReportingEntity reportingEntity) {
		this.reportingEntity = reportingEntity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((criteria == null) ? 0 : criteria.hashCode());
		result = prime * result + ((locationPattern == null) ? 0 : locationPattern.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
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
		DataLocation other = (DataLocation) obj;
		if (criteria == null) {
			if (other.criteria != null) {
				return false;
			}
		} else if (!criteria.equals(other.criteria)) {
			return false;
		}
		if (locationPattern == null) {
			if (other.locationPattern != null) {
				return false;
			}
		} else if (!locationPattern.equals(other.locationPattern)) {
			return false;
		}
		if (pk == null) {
			if (other.pk != null) {
				return false;
			}
		} else if (!pk.equals(other.pk)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataLocation [pk=");
		builder.append(pk);
		builder.append(", locationPattern=");
		builder.append(locationPattern);
		builder.append(", criteria=");
		builder.append(criteria);
		builder.append("]");
		return builder.toString();
	}
}
