package com.orange.srs.refreport.business.delegate.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orange.srs.refreport.model.parameter.inventory.AddEdgeCreationCommandParameter;
import com.orange.srs.refreport.technical.exception.BusinessException;

public class EdgeCreationCommandRegistry {

	private Map<String, List<EdgeCreationWithRuleCommand>> edgeCreationCommandByType = new HashMap<String, List<EdgeCreationWithRuleCommand>>();

	public void addEdgeCreationCommand(AddEdgeCreationCommandParameter parameter) throws Exception {
		EdgeCreationWithRuleCommand edgeCreationWithRuleCommand = EdgeCreationCommandFactory
				.createEdgeCreationCommand(parameter);
		String commandKey = makeTypeKey(parameter.originType, parameter.destinationType, parameter.role);
		List<EdgeCreationWithRuleCommand> commandList = edgeCreationCommandByType.get(commandKey);

		if (commandList == null) {
			commandList = new ArrayList<EdgeCreationWithRuleCommand>();
			edgeCreationCommandByType.put(commandKey, commandList);
		}

		commandList.add(edgeCreationWithRuleCommand);
	}

	public List<EdgeCreationWithRuleCommand> getEdgeCreationCommandByType(String originType, String destinationType,
			String role) throws BusinessException {
		List<EdgeCreationWithRuleCommand> edgeCreationWithRuleCommand = edgeCreationCommandByType
				.get(makeTypeKey(originType, destinationType, role));

		if (edgeCreationWithRuleCommand == null) {
			throw new BusinessException(BusinessException.UNKNOWN_GRAPH_CREATION_COMMAND_EXCEPTION + " : " + originType
					+ " - " + destinationType, BusinessException.UNKNOWN_GRAPH_CREATION_COMMAND);
		}

		return edgeCreationWithRuleCommand;
	}

	private String makeTypeKey(String originType, String destinationType, String role) {
		return originType + ":|:" + destinationType + ":|:" + role;
	}
}
