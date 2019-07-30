package com.orange.srs.refreport.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.orange.srs.statcommon.model.enums.ReportGranularityEnum;
import com.orange.srs.statcommon.model.enums.ReportTimeUnitEnum;

@Entity
@Table(name = "T_REPORT")
public class Report {

	public static final String FIELD_PK = "pk";
	public static final String FIELD_REFID = "refId";
	public static final String FIELD_LABEL = "label";
	public static final String FIELD_COMPUTE_URI = "computeUri";
	public static final String FIELD_REPORT_TIME_UNIT = "reportTimeUnit";
	public static final String FIELD_GRANULARITY = "granularity";
	public static final String FIELD_REPORT_INPUTS = "reportInputs";
	public static final String FIELD_REPORT_CONFIGS = "reportConfigs";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "REF_ID", nullable = false)
	private String refId;

	@Column(name = "LABEL", nullable = false)
	private String label;

	@Column(name = "COMPUTE_URI", nullable = false)
	private String computeUri;

	@Column(name = "REPORT_TIME_UNIT", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private ReportTimeUnitEnum reportTimeUnit;

	@Column(name = "GRANULARITY", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private ReportGranularityEnum granularity;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
	@JoinTable(name = "TJ_REPORT_TO_INPUT", joinColumns = @JoinColumn(name = "REPORT_FK"), inverseJoinColumns = @JoinColumn(name = "REPORT_INPUT_FK"))
	private List<ReportInput> reportInputs;

	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.REFRESH,
			CascadeType.REMOVE }, mappedBy = ReportConfig.FIELD_ASSOCIATED_REPORT)
	private List<ReportConfig> reportConfigs;

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getComputeUri() {
		return computeUri;
	}

	public void setComputeUri(String computeUri) {
		this.computeUri = computeUri;
	}

	public ReportTimeUnitEnum getReportTimeUnit() {
		return reportTimeUnit;
	}

	public void setReportTimeUnit(ReportTimeUnitEnum reportTimeUnit) {
		this.reportTimeUnit = reportTimeUnit;
	}

	public ReportGranularityEnum getGranularity() {
		return granularity;
	}

	public void setGranularity(ReportGranularityEnum granularity) {
		this.granularity = granularity;
	}

	public List<ReportInput> getReportInputs() {
		if (reportInputs == null) {
			reportInputs = new ArrayList<>();
		}
		return reportInputs;
	}

	public void setReportInputs(List<ReportInput> reportInputs) {
		this.reportInputs = reportInputs;
	}

	public List<ReportConfig> getReportConfigs() {
		if (reportConfigs == null) {
			reportConfigs = new ArrayList<>();
		}
		return reportConfigs;
	}

	public void setReportConfigs(List<ReportConfig> reportConfigs) {
		this.reportConfigs = reportConfigs;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Report [pk=");
		builder.append(pk);
		builder.append(", refId=");
		builder.append(refId);
		builder.append(", label=");
		builder.append(label);
		builder.append(", computeUri=");
		builder.append(computeUri);
		builder.append(", reportTimeUnit=");
		builder.append(reportTimeUnit);
		builder.append(", granularity=");
		builder.append(granularity);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((computeUri == null) ? 0 : computeUri.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((refId == null) ? 0 : refId.hashCode());
		result = prime * result + ((reportTimeUnit == null) ? 0 : reportTimeUnit.hashCode());
		result = prime * result + ((granularity == null) ? 0 : granularity.hashCode());
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
		Report other = (Report) obj;
		if (computeUri == null) {
			if (other.computeUri != null) {
				return false;
			}
		} else if (!computeUri.equals(other.computeUri)) {
			return false;
		}
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		if (pk == null) {
			if (other.pk != null) {
				return false;
			}
		} else if (!pk.equals(other.pk)) {
			return false;
		}
		if (refId == null) {
			if (other.refId != null) {
				return false;
			}
		} else if (!refId.equals(other.refId)) {
			return false;
		}
		if (reportTimeUnit == null) {
			if (other.reportTimeUnit != null) {
				return false;
			}
		} else if (!reportTimeUnit.equals(other.reportTimeUnit)) {
			return false;
		}
		if (granularity == null) {
			if (other.granularity != null) {
				return false;
			}
		} else if (!granularity.equals(other.granularity)) {
			return false;
		}
		return true;
	}

}
