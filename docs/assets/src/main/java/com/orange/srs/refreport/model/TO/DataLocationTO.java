package com.orange.srs.refreport.model.TO;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "reportingGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataLocationTO implements Serializable {

	private static final long serialVersionUID = -7773093334977174135L;

	@XmlElement
	private String pattern;

	@XmlElement
	private String criteriaType;

	@XmlElement
	private String criteriaValue;

	public DataLocationTO() {
		super();
	}

	public DataLocationTO(String pattern, String criteriaType, String criteriaValue) {
		this.pattern = pattern;
		this.criteriaType = criteriaType;
		this.criteriaValue = criteriaValue;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getCriteriaType() {
		return criteriaType;
	}

	public void setCriteriaType(String criteriaType) {
		this.criteriaType = criteriaType;
	}

	public String getCriteriaValue() {
		return criteriaValue;
	}

	public void setCriteriaValue(String criteriaValue) {
		this.criteriaValue = criteriaValue;
	}

}
