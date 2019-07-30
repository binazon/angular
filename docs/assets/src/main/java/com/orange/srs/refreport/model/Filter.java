package com.orange.srs.refreport.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.orange.srs.statcommon.model.enums.FilterTypeEnum;

@Entity
@Table(name = "T_FILTER")
public class Filter {

	public final static String FIELD_FILTERID = "filterId";
	public final static String FIELD_NAME = "name";
	public final static String FIELD_COMMENTS = "comments";
	public final static String FIELD_URI = "uri";
	public final static String FIELD_TYPE = "type";
	public final static String FIELD_VALUE = "value";
	public final static String FIELD_FILTER_CONFIGS = "filterConfigs";
	public final static String FIELD_OFFER_OPTIONS = "offerOptions";

	@Id
	@Column(name = "FILTER_ID", unique = true)
	private String filterId;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "COMMENTS", nullable = true)
	private String comments;

	@Column(name = "URI", nullable = false)
	private String uri;

	@Column(name = "TYPE", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private FilterTypeEnum type;

	@Column(name = "VALUE", nullable = true)
	private String value;

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH })
	@JoinTable(name = "TJ_FILTER_TO_OFFER_OPTION", joinColumns = @JoinColumn(name = "FILTER_FK"), inverseJoinColumns = @JoinColumn(name = "OFFER_OPTION_FK"), uniqueConstraints = @UniqueConstraint(columnNames = {
			"FILTER_FK", "OFFER_OPTION_FK" }))
	private List<OfferOption> offerOptions;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = FilterConfig.FIELD_FILTER)
	private List<FilterConfig> filterConfigs;

	public String getFilterId() {
		return filterId;
	}

	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public FilterTypeEnum getType() {
		return type;
	}

	public void setType(FilterTypeEnum type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
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

	public List<OfferOption> getOfferOptions() {
		if (offerOptions == null) {
			offerOptions = new ArrayList<>();
		}
		return offerOptions;
	}

	public void setOfferOptions(List<OfferOption> offerOptions) {
		this.offerOptions = offerOptions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filterId == null) ? 0 : filterId.hashCode());
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
		if (!(obj instanceof Filter)) {
			return false;
		}
		Filter other = (Filter) obj;
		if (filterId == null) {
			if (other.filterId != null) {
				return false;
			}
		} else if (!filterId.equals(other.filterId)) {
			return false;
		}
		return true;
	}
}
