package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = FilterConfig.TABLE_NAME)
public class FilterConfig {

	public static final String TABLE_NAME = "T_FILTER_CONFIG";

	public static final String COL_NAME_REPORTING_GROUP = "REPORTING_GROUP_FK";

	public final static String FIELD_PK = "pk";
	public final static String FIELD_FILTER = "filter";
	public final static String FIELD_OFFER_OPTION = "offerOption";
	public final static String FIELD_REPORTING_GROUP = "reportingGroup";
	public final static String FIELD_GROUP_REPORT_CONFIGS = "groupReportConfigs";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "FILTER_FK", nullable = false)
	private Filter filter;

	@ManyToOne
	@JoinColumn(name = "OFFER_OPTION_FK", nullable = false)
	private OfferOption offerOption;

	@ManyToOne
	@JoinColumn(name = COL_NAME_REPORTING_GROUP, nullable = false)
	private ReportingGroup reportingGroup;

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public OfferOption getOfferOption() {
		return offerOption;
	}

	public void setOfferOption(OfferOption offerOption) {
		this.offerOption = offerOption;
	}

	public ReportingGroup getReportingGroup() {
		return reportingGroup;
	}

	public void setReportingGroup(ReportingGroup reportingGroup) {
		this.reportingGroup = reportingGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + ((offerOption == null) ? 0 : offerOption.hashCode());
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
		if (!(obj instanceof FilterConfig)) {
			return false;
		}
		FilterConfig other = (FilterConfig) obj;
		if (pk == null) {
			if (other.pk != null) {
				return false;
			}
		} else if (!pk.equals(other.pk)) {
			return false;
		}
		if (filter == null) {
			if (other.filter != null) {
				return false;
			}
		} else if (!filter.equals(other.filter)) {
			return false;
		}
		if (offerOption == null) {
			if (other.offerOption != null) {
				return false;
			}
		} else if (!offerOption.equals(other.offerOption)) {
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
