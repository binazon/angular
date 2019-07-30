package com.orange.srs.refreport.model.parameter;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "groupEntityAttribute")
public class GroupEntityAttributeParameter implements Serializable, Comparable<GroupEntityAttributeParameter> {

	private static final long serialVersionUID = -3883154408989320209L;

	@XmlAttribute(name = "origin", required = true)
	public String origin;

	@XmlAttribute(name = "reportingGroupRef", required = true)
	public String reportingGroupRef;

	@XmlAttribute(name = "reportingEntity", required = true)
	public String reportingEntityId;

	@XmlAttribute(name = "name", required = true)
	public String attributeName;

	@XmlAttribute(name = "value", required = true)
	public String attributeValue;

	public GroupEntityAttributeParameter() {
	}

	public GroupEntityAttributeParameter(String origin, String reportingGroupRef, String reportingEntityId,
			String attributeName, String attributeValue) {
		super();
		this.origin = origin;
		this.reportingGroupRef = reportingGroupRef;
		this.reportingEntityId = reportingEntityId;
		this.attributeValue = attributeValue;
		this.attributeName = attributeName;
	}

	@Override
	public int compareTo(GroupEntityAttributeParameter o) {
		if (origin.equalsIgnoreCase(o.origin)) {
			if (reportingGroupRef.equalsIgnoreCase(o.reportingGroupRef)) {
				if (reportingEntityId.equalsIgnoreCase(o.reportingEntityId)) {
					return attributeName.compareTo(o.attributeName);
				} else {
					return reportingEntityId.compareTo(o.reportingEntityId);
				}
			} else {
				return reportingGroupRef.compareTo(o.reportingGroupRef);
			}
		} else {
			return origin.compareTo(o.origin);
		}
	}
}
