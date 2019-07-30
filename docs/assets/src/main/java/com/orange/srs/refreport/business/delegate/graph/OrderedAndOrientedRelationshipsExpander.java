package com.orange.srs.refreport.business.delegate.graph;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;
import com.orange.srs.refreport.model.parameter.inventory.OrderedExpanderParameter;

public class OrderedAndOrientedRelationshipsExpander implements PathExpander {

	private static Logger LOGGER = Logger.getLogger(OrderedAndOrientedRelationshipsExpander.class);

	private OrderedExpanderParameter[] relationshipsOrderParameter;

	public OrderedAndOrientedRelationshipsExpander(OrderedExpanderParameter[] orderedExpanderParameter) {
		relationshipsOrderParameter = orderedExpanderParameter;
	}

	public Iterable<Relationship> expand(Path path, BranchState state) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("In expander, current level is : \n origin " + path.startNode().getProperty("entityid")
					+ ", end=" + path.endNode().getProperty("entityid") + " ");
			for (Relationship relationship : path.endNode().getRelationships()) {
				LOGGER.debug("Corresponding relationship " + relationship.getType() + " "
						+ relationship.getStartNode().getProperty("entityid") + " "
						+ relationship.getEndNode().getProperty("entityid"));
			}
		}

		OrderedExpanderParameter currentLevelParameter = relationshipsOrderParameter[path.length()];

		InventoryGraphEdgeTypeEnum[] types = new InventoryGraphEdgeTypeEnum[currentLevelParameter.edgeTypes.size()];
		return path.endNode().getRelationships(currentLevelParameter.levelDirection,
				currentLevelParameter.edgeTypes.toArray(types));
	}

	public OrderedAndOrientedRelationshipsExpander reverse() {
		return new OrderedAndOrientedRelationshipsExpander(relationshipsOrderParameter);
	}
}
