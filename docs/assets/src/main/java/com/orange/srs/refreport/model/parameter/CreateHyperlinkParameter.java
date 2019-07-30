package com.orange.srs.refreport.model.parameter;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CreateHyperlinkParameter {

	private String label;

	private String indicator;

	private String offerOption;

	private String typeAlias;

	private String additionalTypeAlias;

	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getOfferOption() {
		return offerOption;
	}

	public void setOfferOption(String offerOption) {
		this.offerOption = offerOption;
	}

	public String getTypeAlias() {
		return typeAlias;
	}

	public void setTypeAlias(String typeAlias) {
		this.typeAlias = typeAlias;
	}

	public String getAdditionalTypeAlias() {
		return additionalTypeAlias;
	}

	public void setAdditionalTypeAlias(String additionalTypeAlias) {
		this.additionalTypeAlias = additionalTypeAlias;
	}
}
