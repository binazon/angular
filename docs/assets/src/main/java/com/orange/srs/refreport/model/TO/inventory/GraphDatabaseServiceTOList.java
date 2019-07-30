package com.orange.srs.refreport.model.TO.inventory;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GraphDatabaseServiceTOList {
	public ArrayList<GraphDatabaseServiceTO> graphDatabaseServices = new ArrayList<GraphDatabaseServiceTO>();
}
