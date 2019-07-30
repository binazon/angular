package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "reportInput")
public class ReportInputProvisioningTO {

	@XmlElement(required = true)
	public String reportInputRef;
	@XmlElement(required = true)
	public String granularity;
	@XmlElement(required = true)
	public String sourceTimeUnit;
	@XmlElement(required = false)
	public String locationPatternPrefix;
	@XmlElement(required = false)
	public String locationPatternSuffix;
	@XmlElement(required = true)
	public String source;
	@XmlElement(required = true)
	public String typeDb;
	@XmlElement(required = true)
	public String tableDb;
	@XmlElement(required = true)
	public String formatType;

	@XmlAttribute(required = false)
	public Boolean suppress;

	public ReportInputProvisioningTO() {
	}

	public ReportInputProvisioningTO(String reportInputRef, String granularity, String sourceTimeUnit,
			String locationPatternPrefix, String locationPatternSuffix, String source, String typeDb, String tableDb,
			String formatType) {
		this.reportInputRef = reportInputRef;
		this.granularity = granularity;
		this.sourceTimeUnit = sourceTimeUnit;
		this.locationPatternPrefix = locationPatternPrefix;
		this.locationPatternSuffix = locationPatternSuffix;
		this.source = source;
		this.typeDb = typeDb;
		this.tableDb = tableDb;
		this.formatType = formatType;
	}
}
