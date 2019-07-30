package com.orange.srs.refreport.model.parameter;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entityToEntityAttribute")
public class EntityToEntityAttributeParameter implements Serializable, Comparable<EntityToEntityAttributeParameter> {
	private static final long serialVersionUID = 9072241209527083224L;

	@XmlAttribute(name = "origin", required = true)
	public String origin;

	@XmlAttribute(name = "linkType", required = true)
	public String linkType;

	@XmlAttribute(name = "reportingEntityOrigin", required = true)
	public String reportingEntityIdOrigin;

	@XmlAttribute(name = "reportingEntityEnd", required = true)
	public String reportingEntityIdEnd;

	@XmlAttribute(name = "name", required = true)
	public String attributeName;

	@XmlAttribute(name = "value", required = true)
	public String attributeValue;

	public EntityToEntityAttributeParameter() {
	}

	public EntityToEntityAttributeParameter(String origin, String reportingEntityIdOrigin, String reportingEntityIdEnd,
			String attributeName, String attributeValue, String linkType) {
		super();
		this.origin = origin;
		this.reportingEntityIdOrigin = reportingEntityIdOrigin;
		this.reportingEntityIdEnd = reportingEntityIdEnd;
		this.attributeValue = attributeValue;
		this.attributeName = attributeName;
		this.linkType = linkType;
	}

	@Override
	public int compareTo(EntityToEntityAttributeParameter o) {
		if (origin.equalsIgnoreCase(o.origin)) {
			if (reportingEntityIdOrigin.equalsIgnoreCase(o.reportingEntityIdOrigin)) {
				if (reportingEntityIdEnd.equalsIgnoreCase(o.reportingEntityIdEnd)) {
					return attributeName.compareTo(o.attributeName);
				} else {
					return reportingEntityIdEnd.compareTo(o.reportingEntityIdEnd);
				}
			} else {
				return reportingEntityIdOrigin.compareTo(o.reportingEntityIdOrigin);
			}
		} else {
			return origin.compareTo(o.origin);
		}
	}
}
