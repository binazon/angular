package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "exportEntities")
public class ExportEntitiesTO {

	@XmlElement(name = "entity")
	public List<EntityTO> entities = new ArrayList<>();

	public static class EntityTO {

		@XmlAttribute
		public String origin;
		@XmlAttribute(required = true)
		public String type;
		@XmlAttribute(required = false)
		public String subtype;

	}

}
