package com.orange.srs.refreport.model.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entityToEntityAttributeList")
public class EntityToEntityAttributeParameterList implements Serializable {

	private static final long serialVersionUID = -1585465153378145080L;

	@XmlElement(name = "entityToEntityAttribute")
	public List<EntityToEntityAttributeParameter> entityToEntityAttributes = new ArrayList<EntityToEntityAttributeParameter>();
}
