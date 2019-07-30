package com.orange.srs.refreport.business.delegate.graph;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;
import com.orange.srs.refreport.model.parameter.inventory.CheckIfLinkFulfillRuleParameter;
import com.orange.srs.refreport.technical.exception.BusinessException;

public abstract class AbstractEdgeCreationWithRuleCommand implements EdgeCreationWithRuleCommand {
	private String originType;
	private String destinationType;
	private InventoryGraphEdgeTypeEnum edgeCorrespondingType;
	private String ruleName;

	public AbstractEdgeCreationWithRuleCommand(String originType, String destinationType,
			InventoryGraphEdgeTypeEnum edgeCorrespondingType, String ruleNameParam) {
		super();
		this.originType = originType;
		this.destinationType = destinationType;
		this.edgeCorrespondingType = edgeCorrespondingType;
		ruleName = ruleNameParam;
	}

	public boolean checkIfLinkFulfillRule(CheckIfLinkFulfillRuleParameter parameter) throws BusinessException {
		String pOriginType = parameter.originType;
		String pDestinationType = parameter.destinationType;
		String pLinkParameter = parameter.linkParameterValue;

		if (originType == null || destinationType == null) {
			throw new BusinessException(
					BusinessException.WRONG_PARAMETER_EXCEPTION + " - originType and destiationType cannot be null",
					BusinessException.WRONG_PARAMETER_EXCEPTION_CODE);
		}

		if (!originType.equals(pOriginType) || !destinationType.equals(pDestinationType)) {
			throw new BusinessException(
					BusinessException.WRONG_PARAMETER_EXCEPTION + "  originType" + originType + " and destiationType "
							+ destinationType + " don't match (" + pOriginType + ", " + pDestinationType + ")",
					BusinessException.WRONG_PARAMETER_EXCEPTION_CODE);
		}

		boolean doesParameterFulfillRule = checkParameterValidity(pLinkParameter);
		return doesParameterFulfillRule;
	}

	public abstract boolean checkParameterValidity(String linkParameter) throws BusinessException;

	public InventoryGraphEdgeTypeEnum getEdgeCorrespondingType() {
		return edgeCorrespondingType;
	}

	@Override
	public String toString() {
		return "AbstractEdgeCreationWithRuleCommand [originType=" + originType + ", destinationType=" + destinationType
				+ ", edgeCorrespondingType=" + edgeCorrespondingType + ", ruleName=" + ruleName + "]";
	}

}
