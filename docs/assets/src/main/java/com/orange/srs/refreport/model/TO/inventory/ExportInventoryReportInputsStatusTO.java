package com.orange.srs.refreport.model.TO.inventory;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExportInventoryReportInputsStatusTO implements Serializable {

	private static final long serialVersionUID = 7956480189847811040L;

	public String fileName;

	public Long amountOfInputFormats = 0L;

	public Long amountOfInputColumns = 0L;

	public Long amountOfReportInputs = 0L;

	public Long overAllTime = 0L;
	public Long reportInputRequestTime = 0L;
	public Long inputFormatrRequestTime = 0L;

}
