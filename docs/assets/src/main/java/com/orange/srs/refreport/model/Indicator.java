package com.orange.srs.refreport.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "T_INDICATOR")
public class Indicator {

	public static final String FIELD_INDICATORID = "indicatorId";
	public static final String FIELD_LABEL = "label";
	public static final String FIELD_REPORTS_CONFIG = "reportConfigs";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "INDICATOR_ID", nullable = false, unique = true)
	private String indicatorId;

	@Column(name = "LABEL", nullable = false)
	private String label;

	@ManyToMany
	@JoinTable(name = "TJ_INDICATOR_TO_REPORT_CONFIG", joinColumns = @JoinColumn(name = "INDICATOR_FK"), inverseJoinColumns = @JoinColumn(name = "REPORT_CONFIG_FK"), uniqueConstraints = @UniqueConstraint(columnNames = {
			"INDICATOR_FK", "REPORT_CONFIG_FK" }))
	private List<ReportConfig> reportConfigs;

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getIndicatorId() {
		return indicatorId;
	}

	public void setIndicatorId(String indicatorId) {
		this.indicatorId = indicatorId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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
		builder.append(", indicatorId=");
		builder.append(indicatorId);
		builder.append(", label=");
		builder.append(label);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result + (indicatorId == null ? 0 : indicatorId.hashCode());
		result = prime * result + (pk == null ? 0 : pk.hashCode());
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
		Indicator other = (Indicator) obj;
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		if (indicatorId == null) {
			if (other.indicatorId != null) {
				return false;
			}
		} else if (!indicatorId.equals(other.indicatorId)) {
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

}
