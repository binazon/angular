package com.orange.srs.refreport.business.delegate.graph.builder;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;

import com.orange.srs.refreport.business.delegate.graph.OrderedAndOrientedRelationshipsExpander;
import com.orange.srs.refreport.business.delegate.graph.OrderedTraversalEvaluatorHelper;
import com.orange.srs.refreport.business.delegate.graph.visitor.GetTraversalDefinitionVisitor;
import com.orange.srs.refreport.model.TO.inventory.EdgeTraversalDescriptionTO;
import com.orange.srs.refreport.model.TO.inventory.TraversalByIdDescriptionTO;
import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeOrientationEnum;
import com.orange.srs.refreport.model.parameter.inventory.OrderedExpanderParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.GetHierarchyForEntityByLevelParameter;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class InventoryTraversalBuilder {

	private static Logger LOGGER = Logger.getLogger(InventoryTraversalBuilder.class);

	public List<TraversalDescription> buildTraversalForEntityBasedOnHierarchy(
			GetHierarchyForEntityByLevelParameter parameter, SOAContext soaContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Build TraversalDefintions for " + parameter));
		}

		List<TraversalDescription> traversalDescriptions = new ArrayList<TraversalDescription>();

		GetTraversalDefinitionVisitor traversalVisitor = new GetTraversalDefinitionVisitor();

		List<TraversalByIdDescriptionTO> traversalDescriptionsTO = traversalVisitor.visitAllEdgeToTravserse(parameter,
				soaContext);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
					"Build " + traversalDescriptionsTO.size() + " traversalDefinition "));
		}

		for (TraversalByIdDescriptionTO traversalByIdDescriptionTO : traversalDescriptionsTO) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Traversal : " + traversalByIdDescriptionTO));
			}

			TraversalDescription traversalByIdDescription = createOrderedTraversalDescriptionBis(
					traversalByIdDescriptionTO, soaContext);
			traversalDescriptions.add(traversalByIdDescription);
		}

		return traversalDescriptions;
	}

	public TraversalDescription createOrderedTraversalDescription(
			TraversalByIdDescriptionTO traversalOrderedDescription, SOAContext soaContext) {
		ArrayList<RelationshipType> orderedPathContext = new ArrayList<RelationshipType>();

		for (EdgeTraversalDescriptionTO description : traversalOrderedDescription.getEdgeToTraverseDescription()) {
			orderedPathContext.add(description.edgeType);
		}

		TraversalDescription traversalDescription = Traversal.description()
				.evaluator(new OrderedTraversalEvaluatorHelper(orderedPathContext));

		return traversalDescription;
	}

	public TraversalDescription createOrderedTraversalDescriptionBis(
			TraversalByIdDescriptionTO traversalOrderedDescription, SOAContext soaContext) {
		OrderedExpanderParameter[] orderedExpanderParameterByLevel = new OrderedExpanderParameter[traversalOrderedDescription
				.getEdgeToTraverseDescription().size()];

		int i = 0;
		for (EdgeTraversalDescriptionTO description : traversalOrderedDescription.getEdgeToTraverseDescription()) {
			Direction direction = null;
			if (description.edgeOrientation.equals(InventoryGraphEdgeOrientationEnum.INCOMING)) {
				direction = Direction.INCOMING;
			} else {
				direction = Direction.OUTGOING;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(
						SOATools.buildSOALogMessage(soaContext, "Adding " + description.edgeType + " " + direction));
			}

			OrderedExpanderParameter currentLevelParameter = new OrderedExpanderParameter();
			currentLevelParameter.levelDirection = direction;
			currentLevelParameter.edgeTypes.add(description.edgeType);

			orderedExpanderParameterByLevel[i] = currentLevelParameter;
			i++;
		}

		OrderedAndOrientedRelationshipsExpander arrayExpander = new OrderedAndOrientedRelationshipsExpander(
				orderedExpanderParameterByLevel);

		TraversalDescription traversalDescription = Traversal.description().expand(arrayExpander)
				.evaluator(Evaluators.toDepth(orderedExpanderParameterByLevel.length));

		return traversalDescription;
	}

}
