package com.orange.srs.refreport.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/*
 * EntityAttribute: Parameter associated to a ReportingEntity (example: "domain" "TPC")
 * It is unique by its name and its value (primary key).
 * It is used for:
 * - statistics calculation (OpenReport)
 * - definition of devices for a ReportingGroup 
 */

@Entity
@Table(name = EntityAttributeList.TABLE_NAME, indexes = {
		@Index(name = "INDEX_T_ENTITY_ATTRIBUTE_NAME_REPORTING_ENTITY_FK", columnList = "NAME, "
				+ EntityAttributeList.COL_NAME_REPORTING_ENTITY_FK) })

public class EntityAttributeList implements EntityAttributeI {

	public static final String TABLE_NAME = "T_ENTITY_ATTRIBUTE_LIST";

	public static final String COL_NAME_REPORTING_ENTITY_FK = "REPORTING_ENTITY_FK";

	public static final String FIELD_PK = "pk";
	public static final String FIELD_NAME = "paramName";
	public static final String FIELD_VALUE = "paramValue";
	public static final String FIELD_ENTITY = "targetEntity";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "NAME", nullable = false)
	private String paramName;

	@Column(name = "VALUE", nullable = false)
	@Lob
	private String paramValue;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COL_NAME_REPORTING_ENTITY_FK)
	private ReportingEntity targetEntity;

	public EntityAttributeList() {
	}

	public EntityAttributeList(String name, String value) {
		paramValue = value;
		paramName = name;
	}

	public Long getPk() {
		return pk;
	}

	public ReportingEntity getTargetEntity() {
		return targetEntity;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public void setTargetEntity(ReportingEntity targetEntity) {
		this.targetEntity = targetEntity;
	}

	public String getParamName() {
		return paramName;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

}
