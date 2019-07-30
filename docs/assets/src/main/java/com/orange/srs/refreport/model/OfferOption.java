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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.Table;

import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;

@Entity
@Table(name = "T_OFFER_OPTION")
public class OfferOption {

	public static final String COL_NAME_PK = "PK";
	public static final String COL_NAME_ALIAS = "ALIAS";

	public static final String FIELD_PK = "pk";
	public static final String FIELD_LABEL = "label";
	public static final String FIELD_ALIAS = "alias";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_RELATED_0FFER = "relatedOffer";
	public static final String FIELD_REPORTCONFIGS = "reportConfigs";
	public static final String FIELD_FILTER_CONFIGS = "filterConfigs";
	public static final String FIELD_FILTERS = "filters";
	public static final String FIELD_REPORTING_GROUPS = "reportGroups";

	public static final char OFFER_OPTION_ALIAS_SEPARATOR = '_';

	@Id
	@Column(name = COL_NAME_PK)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "LABEL", nullable = false)
	private String label;

	@Column(name = COL_NAME_ALIAS, nullable = false, unique = true)
	private String alias;

	@Column(name = "TYPE")
	@Enumerated(value = EnumType.STRING)
	private OfferOptionTypeEnum type;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "OFFER_FK")
	private Offer relatedOffer;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = ReportConfig.FIELD_OFFER_OPTION)
	private List<ReportConfig> reportConfigs;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = FilterConfig.FIELD_OFFER_OPTION)
	private List<FilterConfig> filterConfigs;

	@ManyToMany(cascade = { CascadeType.REFRESH,
			CascadeType.MERGE }, fetch = FetchType.EAGER, mappedBy = Filter.FIELD_OFFER_OPTIONS)
	private List<Filter> filters;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS)
	private List<ReportingGroup> reportGroups;

	@PreRemove
	public void preRemove() {
		if (reportConfigs != null) {
			for (ReportConfig reportConfig : reportConfigs) {
				reportConfig.setOfferOption(null);
			}
		}
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public OfferOptionTypeEnum getType() {
		return type;
	}

	public void setType(OfferOptionTypeEnum type) {
		this.type = type;
	}

	public Offer getRelatedOffer() {
		return relatedOffer;
	}

	public void setRelatedOffer(Offer relatedOffer) {
		this.relatedOffer = relatedOffer;
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

	public List<FilterConfig> getFilterConfigs() {
		if (filterConfigs == null) {
			filterConfigs = new ArrayList<>();
		}
		return filterConfigs;
	}

	public void setFilterConfigs(List<FilterConfig> filterConfigs) {
		this.filterConfigs = filterConfigs;
	}

	public List<Filter> getFilters() {
		if (filters == null) {
			filters = new ArrayList<>();
		}
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public List<ReportingGroup> getReportGroups() {
		if (reportGroups == null) {
			reportGroups = new ArrayList<>();
		}
		return reportGroups;
	}

	public void setReportGroups(List<ReportingGroup> reportGroups) {
		this.reportGroups = reportGroups;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OfferOption [pk=");
		builder.append(pk);
		builder.append(", label=");
		builder.append(label);
		builder.append(", alias=");
		builder.append(alias);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		OfferOption other = (OfferOption) obj;
		if (alias == null) {
			if (other.alias != null) {
				return false;
			}
		} else if (!alias.equals(other.alias)) {
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
		return true;
	}

}
