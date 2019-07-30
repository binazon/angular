package com.orange.srs.refreport.model.TO;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

public class FileStatusTO implements Serializable {

	private static final long serialVersionUID = 5014080663287459805L;

	@XmlAttribute(name = "name")
	public String name;
	@XmlAttribute(name = "readDuration")
	public Long readDuration;
	@XmlAttribute(name = "treatmentDuration")
	public Long treatmentDuration;
	@XmlAttribute(name = "emptyOrError")
	public Boolean emptyOrError = false;

	public FileStatusTO() {
		super();
	}

	public FileStatusTO(String name) {
		super();
		this.name = name;
	}

	public FileStatusTO(String name, Long duration) {
		super();
		this.name = name;
		this.readDuration = duration;
	}

}
