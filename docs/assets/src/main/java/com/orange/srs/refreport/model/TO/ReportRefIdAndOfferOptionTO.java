package com.orange.srs.refreport.model.TO;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.enums.ReportGranularityEnum;
import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;

@XmlRootElement(name = "report")
public class ReportRefIdAndOfferOptionTO {

	@XmlElement(required = true)
	public String refId;
	@XmlElement(required = true)
	public String granularity;
	@XmlElement(required = true)
	public String outputType;
	@XmlElement(required = true)
	public String offerOption;

	public ReportRefIdAndOfferOptionTO() {
	}

	public ReportRefIdAndOfferOptionTO(String refId, ReportGranularityEnum granularity, ReportOutputTypeEnum outputType,
			String offerOption) {
		this.refId = refId;
		this.granularity = granularity.name();
		this.outputType = outputType.name();
		this.offerOption = offerOption;
	}

	public String getRefId() {
		return refId;
	}

	public String getGranularity() {
		return granularity;
	}

	public String getOutputType() {
		return outputType;
	}

	public String getOfferOption() {
		return offerOption;
	}

}
