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
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = GroupReportConfig.TABLE_NAME)
public class GroupReportConfig {

	public static final String TABLE_NAME = "T_GROUP_REPORT_CONFIG";

	public static final String COL_NAME_REPORTING_GROUP = "REPORTING_GROUP_FK";

	public static final String FIELD_PK = "pk";
	public static final String FIELD_REPORT_VERSION = "reportVersion";
	public static final String FIELD_REPORT_CONFIG = "reportConfig";
	public static final String FIELD_ASSOCIATED_REPORT_OUTPUT = "associatedReportOutput";
	public static final String FIELD_CRITERIA = "criteria";
	public static final String FIELD_REPORTING_GROUP = "reportingGroup";
	public static final String FIELD_IS_ENABLE = "isEnable";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "REPORT_VERSION", nullable = false)
	private String reportVersion;

	@Column(name = "IS_ENABLE", nullable = false)
	private boolean isEnable;

	@ManyToOne
	@JoinColumn(name = "REPORT_CONFIG_FK", nullable = false)
	private ReportConfig reportConfig;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "REPORT_OUTPUT_FK")
	private ReportOutput associatedReportOutput;

	@ManyToOne(cascade = CascadeType.REFRESH, optional = false)
	@JoinColumns({ @JoinColumn(name = "CRITERIA_TYPE", referencedColumnName = "CRITERIA_TYPE"),
			@JoinColumn(name = "CRITERIA_VALUE", referencedColumnName = "CRITERIA_VALUE") })
	private Criteria criteria;

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH })
	@JoinColumn(name = COL_NAME_REPORTING_GROUP, insertable = false, updatable = false, nullable = false)
	private ReportingGroup reportingGroup;

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getReportVersion() {
		return reportVersion;
	}

	public void setReportVersion(String reportVersion) {
		this.reportVersion = reportVersion;
	}

	public boolean isEnable() {
		return isEnable;
	}

	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	public ReportConfig getReportConfig() {
		return reportConfig;
	}

	public void setReportConfig(ReportConfig reportConfig) {
		this.reportConfig = reportConfig;
	}

	public ReportOutput getAssociatedReportOutput() {
		return associatedReportOutput;
	}

	public void setAssociatedReportOutput(ReportOutput associatedReportOutput) {
		this.associatedReportOutput = associatedReportOutput;
	}

	public Criteria getCriteria() {
		return criteria;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public ReportingGroup getReportingGroup() {
		return reportingGroup;
	}

	public void setReportingGroup(ReportingGroup reportingGroup) {
		this.reportingGroup = reportingGroup;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupReportConfig [pk=");
		builder.append(pk);
		builder.append(", reportVersion=");
		builder.append(reportVersion);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((associatedReportOutput == null) ? 0 : associatedReportOutput.hashCode());
		result = prime * result + ((criteria == null) ? 0 : criteria.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((reportConfig == null) ? 0 : reportConfig.hashCode());
		result = prime * result + ((reportVersion == null) ? 0 : reportVersion.hashCode());
		result = prime * result + ((reportingGroup == null) ? 0 : reportingGroup.hashCode());
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
		if (!(obj instanceof GroupReportConfig)) {
			return false;
		}
		GroupReportConfig other = (GroupReportConfig) obj;
		if (associatedReportOutput == null) {
			if (other.associatedReportOutput != null) {
				return false;
			}
		} else if (!associatedReportOutput.equals(other.associatedReportOutput)) {
			return false;
		}
		if (criteria == null) {
			if (other.criteria != null) {
				return false;
			}
		} else if (!criteria.equals(other.criteria)) {
			return false;
		}
		if (pk == null) {
			if (other.pk != null) {
				return false;
			}
		} else if (!pk.equals(other.pk)) {
			return false;
		}
		if (reportConfig == null) {
			if (other.reportConfig != null) {
				return false;
			}
		} else if (!reportConfig.equals(other.reportConfig)) {
			return false;
		}
		if (reportVersion == null) {
			if (other.reportVersion != null) {
				return false;
			}
		} else if (!reportVersion.equals(other.reportVersion)) {
			return false;
		}
		if (reportingGroup == null) {
			if (other.reportingGroup != null) {
				return false;
			}
		} else if (!reportingGroup.equals(other.reportingGroup)) {
			return false;
		}
		return true;
	}

}
