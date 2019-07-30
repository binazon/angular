package com.orange.srs.refreport.model.TO;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

public class InfoStatusTO implements Serializable {

	private static final long serialVersionUID = -2823269160335637041L;

	@XmlAttribute(name = "name")
	public String name;
	@XmlAttribute(name = "value")
	public Integer value;

	public InfoStatusTO() {
		super();
	}

	public InfoStatusTO(String name, Integer value) {
		super();
		this.name = name;
		this.value = value;
	}

}
