package com.orange.srs.refreport.model.TO.inventory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "GraphCreationStatus")
public class GraphCreationStatusTO {

	@XmlAttribute(name = "nbNodeCreated")
	public long nbNodeCreated = 0;
	@XmlAttribute(name = "nbNodeCreationError")
	public long nbNodeCreationError = 0;
	@XmlAttribute(name = "nbParentLinkCreated")
	public long nbParentLinkCreated = 0;
	@XmlAttribute(name = "nbParentLinkCreationError")
	public long nbParentLinkCreationError = 0;
	@XmlAttribute(name = "nbComplexLinkCreated")
	public long nbComplexLinkCreated = 0;
	@XmlAttribute(name = "nbComplexLinkCreationError")
	public long nbComplexLinkCreationError = 0;

	public long totalDuration = 0;
	public long entityNodeCreationDuration = 0;
	public long parentEdgeCreationDuration = 0;
	public long complexLinxEdgeCreationDuration = 0;
	public long entityAndParentRequestDuration = 0;
	public long linkRequestDuration = 0;

	@Override
	public String toString() {
		return "GraphCreationTO [nbNodeCreated=" + nbNodeCreated + ", nbNodeCreationError=" + nbNodeCreationError
				+ ", nbParentLinkCreated=" + nbParentLinkCreated + ", nbParentLinkCreationError="
				+ nbParentLinkCreationError + ", nbComplexLinkCreated=" + nbComplexLinkCreated
				+ ", nbComplexLinkCreationError=" + nbComplexLinkCreationError + ", entityNodeCreationDuration="
				+ entityNodeCreationDuration + " ms, parentEdgeCreationDuration=" + parentEdgeCreationDuration
				+ " ms , complexLinxEdgeCreationDuration=" + complexLinxEdgeCreationDuration
				+ " ms, entityAndParentRequestDuration=" + entityAndParentRequestDuration + " ms, linkRequestDuration="
				+ linkRequestDuration + " ms, totalDuration=" + totalDuration + " ms]";
	}
}
