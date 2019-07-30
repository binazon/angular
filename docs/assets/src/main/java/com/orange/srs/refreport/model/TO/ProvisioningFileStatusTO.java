package com.orange.srs.refreport.model.TO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ProvisioningFileStatusTO implements Serializable {

	private static final long serialVersionUID = -185441462034479735L;

	@XmlAttribute(name = "nbEffectiveRetrievedFile")
	public int nbEffectiveRetrievedFile = 0;
	@XmlAttribute(name = "nbExpectedRetrievedFile")
	public int nbExpectedRetrievedFile = 0;

	@XmlElement(name = "file")
	public List<FileStatusTO> files = new ArrayList<FileStatusTO>();

}
