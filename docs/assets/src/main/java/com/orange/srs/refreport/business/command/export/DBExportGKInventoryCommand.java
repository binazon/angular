package com.orange.srs.refreport.business.command.export;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.ReportingEntityDAO;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryGkTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.business.commonFunctions.DataLocator;
import com.orange.srs.statcommon.consumer.jdbc.JDBCH2Consumer;
import com.orange.srs.statcommon.model.constant.H2Table;
import com.orange.srs.statcommon.model.enums.EntityTypeEnum;
import com.orange.srs.statcommon.model.parameter.ColumnParameter;
import com.orange.srs.statcommon.model.parameter.CreateTableParameter;
import com.orange.srs.statcommon.model.parameter.PatternParameter;
import com.orange.srs.statcommon.model.parameter.inventory.SpecificInventoryParameter;
import com.orange.srs.statcommon.technical.exception.TechnicalException;
import com.orange.srs.statcommon.technical.jdbc.StatementByColumnAdapter;

@Stateless
public class DBExportGKInventoryCommand extends AbstractDBExportSpecificInventoryCommand {

	@EJB
	private ReportingEntityDAO reportingEntityDAO;

	@Override
	protected void fillSpecificInventoryTable(SpecificInventoryParameter parameter, StatementByColumnAdapter statement)
			throws TechnicalException, SQLException {

		List<String> entityTypes = new ArrayList<String>();
		entityTypes.add(EntityTypeEnum.GKSAN.getValue());
		entityTypes.add(EntityTypeEnum.GKTRUNK.getValue());
		entityTypes.add(EntityTypeEnum.GKSITE.getValue());
		for (ExportSpecificInventoryGkTO exportTO : reportingEntityDAO.findExportInventoryForGK(entityTypes)) {

			statement.setString(H2Table.InventoryGK.COLUMN_ENTITY_ID, exportTO.entityId);
			statement.setString(H2Table.InventoryGK.COLUMN_LABEL, exportTO.label);

			statement.execute();
		}

	}

	@Override
	protected CreateTableParameter getCreateTableParameter() {
		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_SPECIFIC_INVENTORY_GK;

		ColumnParameter entityID = new ColumnParameter();
		entityID.columnName = H2Table.InventoryGK.COLUMN_ENTITY_ID;
		entityID.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(entityID);

		ColumnParameter label = new ColumnParameter();
		label.columnName = H2Table.InventoryGK.COLUMN_LABEL;
		label.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(label);

		return cparameter;
	}

	@Override
	protected List<String> getResultFileLocations(PatternParameter patternParameter) {
		return DataLocator.getFirstSpecificInventoryForGKFileLocationWithoutExtension(patternParameter);
	}

	@Override
	protected List<String> getResultFileLocationsWithExtension(PatternParameter patternParameter) {
		return DataLocator.getFirstSpecificInventoryForGKFileLocationWithExtension(patternParameter);
	}

	@Override
	protected void checkParameters(SpecificInventoryParameter parameter) throws BusinessException {

	}
}
