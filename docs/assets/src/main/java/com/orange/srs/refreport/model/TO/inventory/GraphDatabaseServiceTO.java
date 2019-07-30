package com.orange.srs.refreport.model.TO.inventory;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.refreport.technical.graph.GraphDatabaseServiceWithStateProxy;

@XmlRootElement
public class GraphDatabaseServiceTO {

	public String state;
	public String folder;
	public Date inactivationDate;
	public Date activationDate;
	public Date creationDate;
	public int currentNumberOfClient;

	public static GraphDatabaseServiceTO makeFromGraphDatabaseServiceWithStateProxy(
			GraphDatabaseServiceWithStateProxy service) {
		GraphDatabaseServiceTO graphDatabaseServiceTO = new GraphDatabaseServiceTO();
		graphDatabaseServiceTO.state = service.getState().toString();
		graphDatabaseServiceTO.folder = service.getDirectory() == null ? null
				: service.getDirectory().getAbsolutePath();
		graphDatabaseServiceTO.inactivationDate = service.getInactivationDate();
		graphDatabaseServiceTO.creationDate = service.getCreationDate();
		graphDatabaseServiceTO.activationDate = service.getActivationDate();
		graphDatabaseServiceTO.currentNumberOfClient = service.getCurrentNumberOfClients();

		return graphDatabaseServiceTO;
	}
}
