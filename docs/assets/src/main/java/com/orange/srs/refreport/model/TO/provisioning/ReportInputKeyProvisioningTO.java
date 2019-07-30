package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;

@XmlRootElement(name = "reportInput")
public class ReportInputKeyProvisioningTO {

	@XmlAttribute(required = true)
	public String reportInputRef;
	@XmlAttribute(required = true)
	public String granularity;
	@XmlAttribute(required = true)
	public String sourceTimeUnit;

	@XmlAttribute(required = false)
	public Boolean suppress;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ReportInputKeyProvisioningTO other = (ReportInputKeyProvisioningTO) obj;
		ReportInputKeyTO reportInputKeyTO = new ReportInputKeyTO(reportInputRef, granularity, sourceTimeUnit);
		ReportInputKeyTO otherReportInputKeyTO = new ReportInputKeyTO(other.reportInputRef, other.granularity,
				other.sourceTimeUnit);
		return (reportInputKeyTO.compareTo(otherReportInputKeyTO) == 0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reportInputRef == null) ? 0 : reportInputRef.hashCode());
		result = prime * result + ((granularity == null) ? 0 : granularity.hashCode());
		result = prime * result + ((sourceTimeUnit == null) ? 0 : sourceTimeUnit.hashCode());
		return result;
	}
}
