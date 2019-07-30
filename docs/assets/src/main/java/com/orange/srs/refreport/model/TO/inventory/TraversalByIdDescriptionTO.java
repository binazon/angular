package com.orange.srs.refreport.model.TO.inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TraversalByIdDescriptionTO {
	private Set<String> entitiesIdToTraverse = new HashSet<String>();
	private List<EdgeTraversalDescriptionTO> edgeToTraverseDescription = new ArrayList<EdgeTraversalDescriptionTO>();

	public Set<String> getEntitiesIdToTraverse() {
		return entitiesIdToTraverse;
	}

	public void setEntitiesIdToTraverse(Set<String> entitiesIdToTraverse) {
		this.entitiesIdToTraverse = entitiesIdToTraverse;
	}

	public List<EdgeTraversalDescriptionTO> getEdgeToTraverseDescription() {
		return edgeToTraverseDescription;
	}

	public void setEdgeToTraverseDescription(List<EdgeTraversalDescriptionTO> edgeToTraverseDescription) {
		this.edgeToTraverseDescription = edgeToTraverseDescription;
	}

	@Override
	public String toString() {
		String result = "TraversalByIdDescriptionTO [entitiesIdToTraverse={";
		for (String entityId : entitiesIdToTraverse) {
			result += "-" + entityId + "-";
		}
		result += "}, edgeToTraverseDescription={";
		for (EdgeTraversalDescriptionTO edgeTraversalDescriptionTO : edgeToTraverseDescription) {
			result += "- " + edgeTraversalDescriptionTO + " -";
		}

		result += "}]";

		return result;
	}
}
