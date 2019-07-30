package com.orange.srs.refreport.business.delegate.graph;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;
import com.orange.srs.refreport.model.parameter.inventory.CheckIfLinkFulfillRuleParameter;
import com.orange.srs.refreport.technical.exception.BusinessException;

public interface EdgeCreationWithRuleCommand {
	public boolean checkIfLinkFulfillRule(CheckIfLinkFulfillRuleParameter parameter) throws BusinessException;

	public boolean checkParameterValidity(String linkParameter) throws BusinessException;

	public InventoryGraphEdgeTypeEnum getEdgeCorrespondingType();
}