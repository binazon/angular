package com.orange.srs.refreport.business.delegate.graph;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;
import com.orange.srs.refreport.model.parameter.inventory.AddEdgeCreationCommandParameter;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class EdgeCreationCommandFactory {

	private static final Map<String, Class> classMap = new HashMap<String, Class>();

	static {
		classMap.put("*", EverythingMatchEdgeCreationRuleCommand.class);
		classMap.put("EMPTY", EmptyParameterEdgeCreationRuleCommand.class);
		classMap.put("NOT_EMPTY", NotEmptyParameterEdgeCreationRuleCommand.class);
	}

	public static EdgeCreationWithRuleCommand createEdgeCreationCommand(AddEdgeCreationCommandParameter parameter)
			throws BusinessException, NoSuchMethodException, InvocationTargetException, IllegalAccessException,
			InstantiationException {
		Class edgeCreationWithCommandRuleCommandClass = classMap.get(parameter.associatedRule);

		if (edgeCreationWithCommandRuleCommandClass == null) {
			throw new BusinessException(
					BusinessException.UNKNOWN_GRAPH_CREATION_COMMAND_EXCEPTION + " : " + parameter.associatedRule,
					BusinessException.UNKNOWN_GRAPH_CREATION_COMMAND);
		}

		return (EdgeCreationWithRuleCommand) edgeCreationWithCommandRuleCommandClass
				.getConstructor(String.class, String.class, InventoryGraphEdgeTypeEnum.class, String.class)
				.newInstance(parameter.originType, parameter.destinationType, parameter.edgeCorrespondingType,
						parameter.associatedRule);
	}
}
