package com.orange.srs.refreport.model.TO.inventory;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExportInventoryStatusTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2812836749588035436L;

	public String fileName;

	public Long amountOfLink = 0L;
	public Long amountOfComplexLink = 0L;
	public Long amountOfSimpleLink = 0L;
	public Double avgAmountOfLink = 0D;
	public Long maxAmountOfLink = 0L;

	public Long amountOfEntity = 0L;
	public Long amountOfParent = 0L;

	public Long amountOfAttributes = 0L;
	public Double avgAmountOfAttributes = 0D;
	public Long maxAmountOfAttributes = 0L;

	public Long amountOfLinkAttribute = 0L;
	public Double avgAmountOfLinkAttribute = 0D;
	public Long maxAmountOfLinkAttribute = 0L;

	public Long amountOfSubtype = 0L;
	public Double avgAmountOfSubtype = 0D;
	public Long maxAmountOfSubtype = 0L;

	public Long amountOfEntityGroupAttributes = 0L;
	public Double avgAmountOfEntityGroupAttributes = 0D;
	public Long maxAmountOfEntityGroupAttributes = 0L;

	public Long overAllTime = 0L;
	public Long entityRequestTime = 0L;
	public Long attributeRequestTime = 0L;
	public Long subtypeRequestTime = 0L;
	public Long groupRequestTime = 0L;
	public Long linkRequestTime = 0L;
	public Long linkAttributeRequestTime = 0L;
	public Long requestTime = 0L;

}
