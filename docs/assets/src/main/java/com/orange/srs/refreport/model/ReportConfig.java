package com.orange.srs.refreport.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.orange.srs.statcommon.model.enums.ComputeScopeEnum;

@Entity
@Table(name = "T_REPORT_CONFIG")
public class ReportConfig {

	public static final String FIELD_PK = "pk";
	public static final String FIELD_ALIAS = "alias";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_REPORT_DEFAULT_VERSION = "reportDefaultVersion";
	public static final String FIELD_COMPUTE_SCOPE = "computeScope";
	public static final String FIELD_ASSOCIATED_REPORT = "associatedReport";
	public static final String FIELD_ASSOCIATED_REPORT_OUTPUT = "associatedReportOutput";
	public static final String FIELD_CRITERIA = "criteria";
	public static final String FIELD_OFFER_OPTION = "offerOption";
	public static final String FIELD_PARAM_TYPES = "paramTypes";
	public static final String FIELD_INDICATORS = "indicators";
	public static final String FIELD_OPTIONAL = "optional";

	public static final char REPORT_CONFIG_ALIAS_SEPARATOR = '_';

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "ALIAS", nullable = false)
	private String alias;

	@Column(name = "TYPE", nullable = false)
	private String type;

	@Column(name = "REPORT_DEFAULT_VERSION", nullable = false)
	private String reportDefaultVersion;

	@Column(name = "COMPUTE_SCOPE", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private ComputeScopeEnum computeScope;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name = "REPORT_FK")
	private Report associatedReport;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "REPORT_OUTPUT_FK")
	private ReportOutput associatedReportOutput;

	@ManyToOne(cascade = CascadeType.REFRESH, optional = false)
	@JoinColumns({ @JoinColumn(name = "CRITERIA_TYPE", referencedColumnName = "CRITERIA_TYPE"),
			@JoinColumn(name = "CRITERIA_VALUE", referencedColumnName = "CRITERIA_VALUE") })
	private Criteria criteria;

	@ManyToOne(cascade = CascadeType.REFRESH, optional = false)
	@JoinColumn(name = "OFFER_OPTION_FK")
	private OfferOption offerOption;

	@ManyToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "TJ_REPORT_CONFIG_TO_PARAM_TYPE", joinColumns = @JoinColumn(name = "REPORT_CONFIG_FK"), inverseJoinColumns = @JoinColumn(name = "PARAM_TYPE_FK"), uniqueConstraints = @UniqueConstraint(columnNames = {
			"REPORT_CONFIG_FK", "PARAM_TYPE_FK" }))
	private Set<ParamType> paramTypes;

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = GroupReportConfig.FIELD_REPORT_CONFIG)
	private List<GroupReportConfig> groupReportConfigs;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
	@JoinTable(name = "TJ_INDICATOR_TO_REPORT_CONFIG", inverseJoinColumns = @JoinColumn(name = "INDICATOR_FK"), joinColumns = @JoinColumn(name = "REPORT_CONFIG_FK"), uniqueConstraints = @UniqueConstraint(columnNames = {
			"INDICATOR_FK", "REPORT_CONFIG_FK" }))
	private List<Indicator> indicators;

	@Column(name = "OPTIONAL", nullable = false)
	private boolean optional;

	@PreRemove
	public void preRemove() {
		if (offerOption != null) {
			offerOption.getReportConfigs().remove(this);
		}
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReportDefaultVersion() {
		return reportDefaultVersion;
	}

	public void setReportDefaultVersion(String reportDefaultVersion) {
		this.reportDefaultVersion = reportDefaultVersion;
	}

	public ComputeScopeEnum getComputeScope() {
		return computeScope;
	}

	public void setComputeScope(ComputeScopeEnum computeScope) {
		this.computeScope = computeScope;
	}

	public Report getAssociatedReport() {
		return associatedReport;
	}

	public void setAssociatedReport(Report associatedReport) {
		this.associatedReport = associatedReport;
	}

	public ReportOutput getAssociatedReportOutput() {
		return associatedReportOutput;
	}

	public void setAssociatedReportOutput(ReportOutput associatedReportOutput) {
		this.associatedReportOutput = associatedReportOutput;
	}

	public OfferOption getOfferOption() {
		return offerOption;
	}

	public void setOfferOption(OfferOption offerOption) {
		this.offerOption = offerOption;
	}

	public Criteria getCriteria() {
		return criteria;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public Set<ParamType> getParamTypes() {
		if (paramTypes == null) {
			paramTypes = new HashSet<>();
		}
		return paramTypes;
	}

	public void setParamTypes(Set<ParamType> paramTypes) {
		this.paramTypes = paramTypes;
	}

	public List<GroupReportConfig> getGroupReportConfigs() {
		if (groupReportConfigs == null) {
			groupReportConfigs = new ArrayList<>();
		}
		return groupReportConfigs;
	}

	public void setGroupReportConfigs(List<GroupReportConfig> groupReportConfigs) {
		this.groupReportConfigs = groupReportConfigs;
	}

	public List<Indicator> getIndicators() {
		if (indicators == null) {
			indicators = new ArrayList<>();
		}
		return indicators;
	}

	public void setIndicators(List<Indicator> indicators) {
		this.indicators = indicators;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReportConfig [pk=");
		builder.append(pk);
		builder.append(", alias=");
		builder.append(alias);
		builder.append(", type=");
		builder.append(type);
		builder.append(", reportDefaultVersion=");
		builder.append(reportDefaultVersion);
		builder.append(", computeScope=");
		builder.append(computeScope);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((reportDefaultVersion == null) ? 0 : reportDefaultVersion.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((computeScope == null) ? 0 : computeScope.hashCode());
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
		ReportConfig other = (ReportConfig) obj;
		if (alias == null) {
			if (other.alias != null) {
				return false;
			}
		} else if (!alias.equals(other.alias)) {
			return false;
		}
		if (pk == null) {
			if (other.pk != null) {
				return false;
			}
		} else if (!pk.equals(other.pk)) {
			return false;
		}
		if (reportDefaultVersion == null) {
			if (other.reportDefaultVersion != null) {
				return false;
			}
		} else if (!reportDefaultVersion.equals(other.reportDefaultVersion)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (computeScope == null) {
			if (other.computeScope != null) {
				return false;
			}
		} else if (!computeScope.equals(other.computeScope)) {
			return false;
		}
		return true;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

}
