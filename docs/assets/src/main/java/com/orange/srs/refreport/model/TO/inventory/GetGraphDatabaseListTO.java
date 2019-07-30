package com.orange.srs.refreport.model.TO.inventory;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetGraphDatabaseListTO {
	public List<String> databaseAbsolutePath = new ArrayList<String>();
}
