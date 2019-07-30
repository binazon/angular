package com.orange.srs.refreport.model.TO.inventory;

import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;

public class ExportInventoryReportTemplateTO {

	public String reportConfigAlias;
	public String refId;
	public String outputPatternPrefix;
	public String outputPatternSuffix;
	public ReportOutputTypeEnum type;
	public String format;
	public String offerAlias;
	public String optionAlias;
	public String optionLabel;
	public String reportVersion;

	public ExportInventoryReportTemplateTO() {
	}

	public ExportInventoryReportTemplateTO(String reportConfigAlias, String refId, String outputPatternPrefix,
			String outputPatternSuffix, ReportOutputTypeEnum type, String format, String offerAlias, String optionAlias,
			String optionLabel, String reportVersion) {
		super();
		this.reportConfigAlias = reportConfigAlias;
		this.refId = refId;
		this.outputPatternPrefix = outputPatternPrefix;
		this.outputPatternSuffix = outputPatternSuffix;
		this.type = type;
		this.format = format;
		this.offerAlias = offerAlias;
		this.optionAlias = optionAlias;
		this.optionLabel = optionLabel;
		this.reportVersion = reportVersion;
	}

}
