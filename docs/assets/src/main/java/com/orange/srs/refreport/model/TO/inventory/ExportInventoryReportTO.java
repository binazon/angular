package com.orange.srs.refreport.model.TO.inventory;

import com.orange.srs.statcommon.model.enums.ComputeScopeEnum;
import com.orange.srs.statcommon.model.enums.ReportGranularityEnum;
import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;
import com.orange.srs.statcommon.model.enums.ReportTimeUnitEnum;

public class ExportInventoryReportTO {

	public Long reportConfigPk;
	public ComputeScopeEnum computeScope;
	public String indicatorId;
	public String refId;
	public ReportTimeUnitEnum reportTimeUnit;
	public ReportGranularityEnum granularity;
	public String computeUri;
	public String outputPatternPrefix;
	public String outputPatternSuffix;
	public String outputUri;
	public ReportOutputTypeEnum type;
	public String offerAlias;
	public String optionAlias;
	public String optionLabel;
	public String reportVersion;
	public String criteriaType;
	public String criteriaValue;

	public ExportInventoryReportTO() {
	}

	public ExportInventoryReportTO(Long reportingConfigPk, ComputeScopeEnum computeScope, String indicatorId,
			String refId, ReportTimeUnitEnum reportTimeUnit, ReportGranularityEnum granularity, String computeUri,
			String outputPatternPrefix, String outputPatternSuffix, String outputUri, ReportOutputTypeEnum type,
			String offerAlias, String optionAlias, String optionLabel, String reportVersion, String criteriaType,
			String criteriaValue) {
		super();
		this.reportConfigPk = reportingConfigPk;
		this.indicatorId = indicatorId;
		this.refId = refId;
		this.reportTimeUnit = reportTimeUnit;
		this.granularity = granularity;
		this.computeScope = computeScope;
		this.computeUri = computeUri;
		this.outputPatternPrefix = outputPatternPrefix;
		this.outputPatternSuffix = outputPatternSuffix;
		this.outputUri = outputUri;
		this.type = type;
		this.offerAlias = offerAlias;
		this.optionAlias = optionAlias;
		this.optionLabel = optionLabel;
		this.reportVersion = reportVersion;
		this.criteriaType = criteriaType;
		this.criteriaValue = criteriaValue;
	}

}
