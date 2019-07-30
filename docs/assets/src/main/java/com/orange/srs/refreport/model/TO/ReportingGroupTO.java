package com.orange.srs.refreport.model.TO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.enumerate.ReportingGroupTypeEnum;
import com.orange.srs.refreport.model.external.OfferOptionTO;

@XmlRootElement(name = "reportingGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportingGroupTO implements Serializable {

	private static final long serialVersionUID = 8206004145392487580L;

	@XmlTransient
	private Long reportingGroupPk;
	@XmlAttribute
	private String reportingGroupRef;
	@XmlAttribute
	private String origin;
	@XmlAttribute
	private String label;
	@XmlAttribute
	private String source;
	@XmlAttribute
	private ReportingGroupTypeEnum type;
	@XmlElement
	private String groupingCriteria;
	@XmlElement
	private String groupingValue;
	@XmlElement
	private String dataLocation;
	@XmlTransient
	private List<ReportingEntity> entities;

	@XmlElementWrapper(name = "offerOptionList")
	@XmlElement(name = "offerOption")
	private List<OfferOptionTO> offerOptions;

	public ReportingGroupTO() {
		super();
	}

	public ReportingGroupTO(String reportingGroupRef, String origin, String label, String source,
			ReportingGroupTypeEnum type, String groupingCriteria, String groupingValue, String dataLocation) {
		this.reportingGroupRef = reportingGroupRef;
		this.origin = origin;
		this.label = label;
		this.source = source;
		this.type = type;
		this.groupingCriteria = groupingCriteria;
		this.groupingValue = groupingValue;
		this.dataLocation = dataLocation;
	}

	public ReportingGroupTO(Long reportingGroupPk, String reportingGroupRef, String origin, String label, String source,
			ReportingGroupTypeEnum type, String groupingCriteria, String groupingValue, String dataLocation) {
		this.reportingGroupPk = reportingGroupPk;
		this.reportingGroupRef = reportingGroupRef;
		this.label = label;
		this.source = source;
		this.type = type;
		this.groupingCriteria = groupingCriteria;
		this.groupingValue = groupingValue;
		this.dataLocation = dataLocation;
		this.origin = origin;
	}

	public ReportingGroupTO(Long reportingGroupPk, String reportingGroupRef, String label, String groupingValue) {
		this.reportingGroupPk = reportingGroupPk;
		this.reportingGroupRef = reportingGroupRef;
		this.label = label;
		this.groupingValue = groupingValue;
	}

	public Long getReportingGroupPk() {
		return reportingGroupPk;
	}

	public void setReportingGroupPk(Long reportingGroupPk) {
		this.reportingGroupPk = reportingGroupPk;
	}

	public String getReportingGroupRef() {
		return reportingGroupRef;
	}

	public void setReportingGroupRef(String reportingGroupRef) {
		this.reportingGroupRef = reportingGroupRef;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public ReportingGroupTypeEnum getType() {
		return type;
	}

	public void setType(ReportingGroupTypeEnum type) {
		this.type = type;
	}

	public String getGroupingCriteria() {
		return groupingCriteria;
	}

	public void setGroupingCriteria(String groupingCriteria) {
		this.groupingCriteria = groupingCriteria;
	}

	public String getGroupingValue() {
		return groupingValue;
	}

	public void setGroupingValue(String groupingValue) {
		this.groupingValue = groupingValue;
	}

	public String getDataLocation() {
		return dataLocation;
	}

	public void setDataLocation(String dataLocation) {
		this.dataLocation = dataLocation;
	}

	public List<ReportingEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<ReportingEntity> entities) {
		this.entities = entities;
	}

	public List<OfferOptionTO> getOfferOptions() {
		if (offerOptions == null) {
			offerOptions = new ArrayList<>();
		}
		return offerOptions;
	}

	public void setOfferOptions(List<OfferOptionTO> offerOptions) {
		this.offerOptions = offerOptions;
	}

	@Override
	public String toString() {
		return "[reportingGroupRef=" + reportingGroupRef + ";origin=" + origin + "]";
	}

}
