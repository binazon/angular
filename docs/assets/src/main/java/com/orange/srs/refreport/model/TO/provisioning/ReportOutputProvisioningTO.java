package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "reportOutput")
public class ReportOutputProvisioningTO {

	@XmlElement(required = true, nillable = false)
	public String type;
	@XmlElement(required = true, nillable = false)
	public String format;
	@XmlElement(required = true, nillable = false)
	public String locationPatternPrefix;
	@XmlElement(required = true, nillable = false)
	public String locationPatternSuffix;
	@XmlElement(required = true, nillable = false)
	public String uri;
	// IP2262
	@XmlElement(required = false)
	public String compression;

	public ReportOutputProvisioningTO() {
	}

	public ReportOutputProvisioningTO(String type, String format, String locationPatternPrefix,
			String locationPatternSuffix, String uri) {
		this(type, format, locationPatternPrefix, locationPatternSuffix, uri, null);
	}

	public ReportOutputProvisioningTO(String type, String format, String locationPatternPrefix,
			String locationPatternSuffix, String uri, String compression) {
		this.type = type;
		this.format = format;
		this.compression = compression;
		this.locationPatternPrefix = locationPatternPrefix;
		this.locationPatternSuffix = locationPatternSuffix;
		this.uri = uri;
	}
}
