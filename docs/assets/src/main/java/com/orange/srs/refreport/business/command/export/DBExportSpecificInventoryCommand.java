package com.orange.srs.refreport.business.command.export;

import com.orange.srs.refreport.model.exception.InventoryException;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.SpecificInventoryParameter;

public interface DBExportSpecificInventoryCommand {
	public String execute(final SpecificInventoryParameter parameter, SOAContext soaContext)
			throws InventoryException, BusinessException;
}
