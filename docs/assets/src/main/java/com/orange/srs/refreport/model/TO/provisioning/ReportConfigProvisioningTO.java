package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.orange.srs.statcommon.model.enums.ComputeScopeEnum;
import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;

@XmlRootElement(name = "reportConfig")
public class ReportConfigProvisioningTO {

	@XmlTransient
	public Long reportConfigPk;
	@XmlAttribute(required = true)
	public String alias;
	@XmlAttribute(required = true)
	public String type;
	@XmlAttribute(required = true)
	public String reportVersion;
	@XmlAttribute(required = true)
	public String reportRefId;
	@XmlAttribute(required = true)
	public String computeScope;

	@XmlAttribute(required = false)
	public Boolean suppress;

	@XmlAttribute(required = false)
	public Boolean optional = false;

	@XmlElementWrapper(name = "listParamType")
	@XmlElement(name = "paramType")
	public List<ParamTypeAliasProvisioningTO> paramTypeAliasProvisioningTOs = new ArrayList<>();

	@XmlElementWrapper(name = "listIndicator", nillable = true)
	@XmlElement(name = "indicator")
	public List<IndicatorIdProvisioningTO> indicatorIdProvisioningTOs = new ArrayList<>();

	@XmlElement(name = "reportOutput", nillable = false)
	public ReportOutputProvisioningTO reportOutputProvisioningTO;

	@XmlElement(name = "criteria", nillable = false)
	public CriteriaProvisioningTO criteriaProvisioningTO;

	public ReportConfigProvisioningTO() {
	}

	public ReportConfigProvisioningTO(Long reportConfigPk, String alias, String type, String reportVersion,
			ComputeScopeEnum computeScope, Boolean optional, String reportRefId, ReportOutputTypeEnum reportOutputType,
			String reportOutputFormat, String reportOutputLocationPatternPrefix,
			String reportOutputLocationPatternSuffix, String reportOutputUri, String compression, String criteriaType,
			String criteriaValue) {
		this.reportConfigPk = reportConfigPk;
		this.alias = alias;
		this.type = type;
		this.reportVersion = reportVersion;
		this.computeScope = computeScope.name();
		this.reportRefId = reportRefId;
		this.optional = optional;
		this.reportOutputProvisioningTO = new ReportOutputProvisioningTO(reportOutputType.name(), reportOutputFormat,
				reportOutputLocationPatternPrefix, reportOutputLocationPatternSuffix, reportOutputUri, compression);
		this.criteriaProvisioningTO = new CriteriaProvisioningTO(criteriaType, criteriaValue);
	}
}
