package com.orange.srs.refreport.business.delegate.graph.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.model.TO.inventory.EdgeTraversalDescriptionTO;
import com.orange.srs.refreport.model.TO.inventory.TraversalByIdDescriptionTO;
import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeOrientationEnum;
import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.EdgeToTraverseDefinitionParameter;
import com.orange.srs.statcommon.model.parameter.inventory.GetHierarchyForEntityByLevelParameter;
import com.orange.srs.statcommon.technical.SOATools;

public class GetTraversalDefinitionVisitor {

	private static Logger LOGGER = Logger.getLogger(GetTraversalDefinitionVisitor.class);

	public List<TraversalByIdDescriptionTO> visitAllEdgeToTravserse(GetHierarchyForEntityByLevelParameter parameter,
			SOAContext soaContext) {
		List<TraversalByIdDescriptionTO> traversalByIdDescriptionTOs = new ArrayList<TraversalByIdDescriptionTO>();

		Set<String> entityIds = new HashSet<String>();
		entityIds.add(parameter.entityId);

		for (EdgeToTraverseDefinitionParameter edgeToTraverseDefinitionParameter : parameter.edgeToTraverseDefinitions) {
			TraversalByIdDescriptionTO traversalByIdDescriptionTO = new TraversalByIdDescriptionTO();
			traversalByIdDescriptionTOs.add(traversalByIdDescriptionTO);
			traversalByIdDescriptionTO.setEntitiesIdToTraverse(entityIds);
			traversalByIdDescriptionTOs.addAll(visitLevelToFillCurrentAndCreateAlternatePath(traversalByIdDescriptionTO,
					edgeToTraverseDefinitionParameter, soaContext));
		}

		return traversalByIdDescriptionTOs;
	}

	public List<TraversalByIdDescriptionTO> visitLevelToFillCurrentAndCreateAlternatePath(
			TraversalByIdDescriptionTO currentPathDesciptionToFulfill,
			EdgeToTraverseDefinitionParameter edgeToTraverseDefinitionParameter, SOAContext soaContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Visiting node " + edgeToTraverseDefinitionParameter));
		}

		List<TraversalByIdDescriptionTO> newPathTraversalDescriptions = new ArrayList<TraversalByIdDescriptionTO>();

		EdgeTraversalDescriptionTO currentEdgeDesciption = new EdgeTraversalDescriptionTO();

		currentEdgeDesciption.edgeOrientation = InventoryGraphEdgeOrientationEnum
				.valueOf(edgeToTraverseDefinitionParameter.edgeOrientation);
		currentEdgeDesciption.edgeType = InventoryGraphEdgeTypeEnum.valueOf(edgeToTraverseDefinitionParameter.edgeType);

		currentPathDesciptionToFulfill.getEdgeToTraverseDescription().add(currentEdgeDesciption);

		TraversalByIdDescriptionTO currentPathDesciptionToFulfillClone = null;

		if (edgeToTraverseDefinitionParameter.getNextEdgeToTraverseDefinition().size() > 1) {
			currentPathDesciptionToFulfillClone = cloneTraversalByIdDescriptionTO(currentPathDesciptionToFulfill);
		}

		int i = 0;
		for (EdgeToTraverseDefinitionParameter nextEdgeToVisitForDefinition : edgeToTraverseDefinitionParameter
				.getNextEdgeToTraverseDefinition()) {
			List<TraversalByIdDescriptionTO> newPathDescriptionFromNextEdge = null;
			if (i == 0) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
							"Visiting next node " + nextEdgeToVisitForDefinition));
				}

				newPathDescriptionFromNextEdge = visitLevelToFillCurrentAndCreateAlternatePath(
						currentPathDesciptionToFulfill, nextEdgeToVisitForDefinition, soaContext);
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
							"Visiting next node " + nextEdgeToVisitForDefinition));
				}

				TraversalByIdDescriptionTO newTraversalByIdDescriptionTO = cloneTraversalByIdDescriptionTO(
						currentPathDesciptionToFulfillClone);
				newPathTraversalDescriptions.add(newTraversalByIdDescriptionTO);
				newPathDescriptionFromNextEdge = visitLevelToFillCurrentAndCreateAlternatePath(
						newTraversalByIdDescriptionTO, nextEdgeToVisitForDefinition, soaContext);
			}

			newPathTraversalDescriptions.addAll(newPathDescriptionFromNextEdge);
			i++;
		}

		return newPathTraversalDescriptions;
	}

	private TraversalByIdDescriptionTO cloneTraversalByIdDescriptionTO(TraversalByIdDescriptionTO desciptionToToClone) {
		TraversalByIdDescriptionTO traversalByIdDescriptionTOClone = new TraversalByIdDescriptionTO();
		traversalByIdDescriptionTOClone.getEdgeToTraverseDescription()
				.addAll(desciptionToToClone.getEdgeToTraverseDescription());
		traversalByIdDescriptionTOClone.getEntitiesIdToTraverse().addAll(desciptionToToClone.getEntitiesIdToTraverse());

		return traversalByIdDescriptionTOClone;
	}
}
