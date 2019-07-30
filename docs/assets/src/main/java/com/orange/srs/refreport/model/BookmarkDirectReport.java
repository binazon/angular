package com.orange.srs.refreport.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "T_BOOKMARK_DIRECT_REPORT")
public class BookmarkDirectReport extends Bookmark {

	public static final String FIELD_LABEL = "label";
	public static final String FIELD_ADDITIONAL_PARAM_TYPE = "additionalParamType";
	public static final String FIELD_HIERARCHY = "hierarchy";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "LABEL")
	private String label;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ADDITIONAL_PARAM_TYPE_FK", nullable = true)
	private ParamType additionalParamType;

	@Column(name = "HIERARCHY")
	private String hierarchy;

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the additionalParamType
	 */
	public ParamType getAdditionalParamType() {
		return additionalParamType;
	}

	/**
	 * @param additionalParamType
	 *            the additionalParamType to set
	 */
	public void setAdditionalParamType(ParamType additionalParamType) {
		this.additionalParamType = additionalParamType;
	}

	/**
	 * @return the hierarchy
	 */
	public String getHierarchy() {
		return hierarchy;
	}

	/**
	 * @param hierarchy
	 *            the hierarchy to set
	 */
	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

}
