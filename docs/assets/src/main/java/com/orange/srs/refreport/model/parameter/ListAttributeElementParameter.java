package com.orange.srs.refreport.model.parameter;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ListAttributeElementParameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1049405835358491960L;

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String value;

	public ListAttributeElementParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public ListAttributeElementParameter() {
		// TODO Auto-generated constructor stub
	}
}
