package com.orange.srs.refreport.business.command.export;

import java.sql.SQLException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.collections.CollectionUtils;

import com.orange.srs.refreport.consumer.dao.OfferOptionDAO;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryOpenFlowTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.business.commonFunctions.DataLocator;
import com.orange.srs.statcommon.consumer.jdbc.JDBCH2Consumer;
import com.orange.srs.statcommon.model.constant.H2Table;
import com.orange.srs.statcommon.model.parameter.ColumnParameter;
import com.orange.srs.statcommon.model.parameter.CreateTableParameter;
import com.orange.srs.statcommon.model.parameter.PatternParameter;
import com.orange.srs.statcommon.model.parameter.inventory.SpecificInventoryParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.SpecificInventoryJobParameter;
import com.orange.srs.statcommon.technical.exception.TechnicalException;
import com.orange.srs.statcommon.technical.jdbc.StatementByColumnAdapter;

@Stateless
public class DBExportOpenFlowInventoryCommand extends AbstractDBExportSpecificInventoryCommand {

	@EJB
	private OfferOptionDAO offerOptionDAO;

	@Override
	protected void fillSpecificInventoryTable(SpecificInventoryParameter parameter, StatementByColumnAdapter statement)
			throws TechnicalException, SQLException {

		List<String> offerOptionAliases = parameter.getOfferOptionAliasesAsStringList();
		for (ExportSpecificInventoryOpenFlowTO exportTO : offerOptionDAO
				.findExportInventoryForOpenFlowWithOfferOption(offerOptionAliases)) {

			statement.setString(H2Table.InventoryOpenFlow.COLUMN_ENTITY_ID, exportTO.entityId);
			statement.setString(H2Table.InventoryOpenFlow.COLUMN_ROUTER_IP, exportTO.routerIp);

			statement.execute();
		}

	}

	@Override
	protected CreateTableParameter getCreateTableParameter() {
		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_SPECIFIC_INVENTORY_OPENFLOW;

		ColumnParameter entityID = new ColumnParameter();
		entityID.columnName = H2Table.InventoryOpenFlow.COLUMN_ENTITY_ID;
		entityID.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(entityID);

		ColumnParameter ipRouter = new ColumnParameter();
		ipRouter.columnName = H2Table.InventoryOpenFlow.COLUMN_ROUTER_IP;
		ipRouter.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(ipRouter);

		return cparameter;
	}

	@Override
	protected List<String> getResultFileLocations(PatternParameter patternParameter) {
		return DataLocator.getFirstSpecificInventoryForOpenFlowFileLocationWithoutExtension(patternParameter);
	}

	@Override
	protected List<String> getResultFileLocationsWithExtension(PatternParameter patternParameter) {
		return DataLocator.getFirstSpecificInventoryForOpenFlowFileLocationWithExtension(patternParameter);
	}

	@Override
	protected void checkParameters(SpecificInventoryParameter parameter) throws BusinessException {
		if (CollectionUtils.isEmpty(parameter.getSieveList())) {
			throw new BusinessException(BusinessException.WRONG_PARAMETER_EXCEPTION + " : "
					+ SpecificInventoryJobParameter.class.toString() + " = " + parameter,
					BusinessException.WRONG_PARAMETER_EXCEPTION_CODE);
		}

	}
}
