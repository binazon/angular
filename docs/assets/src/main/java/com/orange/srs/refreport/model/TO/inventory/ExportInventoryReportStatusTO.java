package com.orange.srs.refreport.model.TO.inventory;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExportInventoryReportStatusTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2812836749588035436L;

	public String fileName;

	public Long amountOfFilters = 0L;
	public Long amountOfParamTypeAlias = 0L;
	public Long amountOfInteractiveReport = 0L;

	public Long overAllTime = 0L;
	public Long filterRequestTime = 0L;
	public Long paramTypeAliasRequestTime = 0L;
	public Long interactiveReportTime = 0L;

}
