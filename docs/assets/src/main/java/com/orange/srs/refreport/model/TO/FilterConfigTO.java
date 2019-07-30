package com.orange.srs.refreport.model.TO;

import java.io.Serializable;

public class FilterConfigTO implements Serializable {

	private static final long serialVersionUID = 3150973543119754501L;

	public Long filterConfigPk;
	public String filterId;
	public Long offerOptionPk;
	public Long reportingGroupPk;

	public FilterConfigTO(Long filterConfigPk, String filterId, Long offerOptionPk, Long reportingGroupPk) {
		this.filterConfigPk = filterConfigPk;
		this.filterId = filterId;
		this.offerOptionPk = offerOptionPk;
		this.reportingGroupPk = reportingGroupPk;
	}

}
