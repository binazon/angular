package com.orange.srs.refreport.model.parameter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "attributelist")
public class ListAttributeValueParameterList {

	private List<ListAttributeValueParameter> attributelist;

	public ListAttributeValueParameterList() {
		this.attributelist = new ArrayList<ListAttributeValueParameter>();
	}

	@XmlElement(name = "attribute")
	public List<ListAttributeValueParameter> getValueList() {
		return this.attributelist;
	}

	public void setValueList(List<ListAttributeValueParameter> attributelist) {
		this.attributelist = attributelist;
	}

	public void AddValue(ListAttributeValueParameter value) {
		this.attributelist.add(value);
	}

}
