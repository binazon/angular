package com.orange.srs.refreport.business.delegate.graph;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;
import com.orange.srs.refreport.technical.exception.BusinessException;

public class EmptyParameterEdgeCreationRuleCommand extends AbstractEdgeCreationWithRuleCommand {

	public EmptyParameterEdgeCreationRuleCommand(String originType, String destinationType,
			InventoryGraphEdgeTypeEnum edgeCorrespondingType, String ruleName) {
		super(originType, destinationType, edgeCorrespondingType, ruleName);
	}

	@Override
	public boolean checkParameterValidity(String linkParameter) throws BusinessException {
		if (linkParameter == null || "".equals(linkParameter)) {
			return true;
		}

		return false;
	}

}
