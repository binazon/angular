package com.orange.srs.refreport.business.delegate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.h2.jdbc.JdbcSQLException;

import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.model.TO.FilterToOfferOptionTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportTemplateTO;
import com.orange.srs.refreport.model.TO.inventory.OfferOptionListNewProvisioningTO;
import com.orange.srs.refreport.model.exception.InventoryException;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.business.commonFunctions.BDStatCommonFunction;
import com.orange.srs.statcommon.consumer.jdbc.JDBCH2Consumer;
import com.orange.srs.statcommon.model.constant.H2Table;
import com.orange.srs.statcommon.model.parameter.ColumnParameter;
import com.orange.srs.statcommon.model.parameter.CreateTableParameter;
import com.orange.srs.statcommon.model.parameter.ExportInventoryParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.TechnicalException;
import com.orange.srs.statcommon.technical.jdbc.StatementByColumnAdapter;

/**
 * Delegate used to created H2 Table for export inventory report interactive and template
 */
@Stateless
public class DBExportInventoryDelegate {

	private static final Logger LOGGER = Logger.getLogger(DBExportInventoryDelegate.class);

	@Inject
	private JDBCH2Consumer h2consumer;

	@EJB
	private ReportingGroupDAO reportingGroupDAO;

	@EJB
	private ReportConfigDelegate reportConfigDelegate;

	@EJB
	private ReportingGroupDelegate reportingGroupDelegate;

	public String exportInventoryReportInteractive(SOAContext soaContext, ExportInventoryParameter parameter,
			List<String> fileLocations) throws InventoryException, BusinessException {

		/*
		 * This boolean is used to know if we need to close H2 connection in case of exception. To avoid re-close
		 * connection in case of exception during the close
		 */
		boolean needToCloseH2Connection = true;
		boolean h2ConnectionError = true;

		int fileLocationIndex = fileLocations.size() - 1;

		String fileLocation = fileLocations.get(fileLocationIndex);

		// NEW PROVISIONING: get the list of offer options associated to the new provisioning process
		Path newProvisioningOfferOptionsFilePath = Paths.get(Configuration.rootProperty,
				Configuration.configProvisioningProperty, "newProvisioningOfferOptions.xml");
		OfferOptionListNewProvisioningTO offerOptionListNewProvisioningTO = null;
		try {
			offerOptionListNewProvisioningTO = (OfferOptionListNewProvisioningTO) JAXBRefReportFactory.getUnmarshaller()
					.unmarshal(newProvisioningOfferOptionsFilePath.toFile());
		} catch (JAXBException jaxbe) {
			throw new BusinessException("Unable to read file " + newProvisioningOfferOptionsFilePath.toString()
					+ ", so unable to export report's inventory.", jaxbe);
		}

		do {
			try {

				LOGGER.debug("[exportInventoryReportInteractive] -> Location " + fileLocation);
				h2consumer.createConnexion(fileLocation + Configuration.h2FileReadwriteLockOptionsProperty, false,
						false);

				/*
				 * Create tables and statements
				 */
				// Use to store the report Pk
				long pkNumber = 0L;
				// Map keeping the matching between tables and PreparedStatement
				Map<String, StatementByColumnAdapter> tableStatements = new HashMap<>();

				// Creation of the first table and the statement
				createReportInteractiveTable(tableStatements);

				// Creation of the second table and the statement
				createTypeAliasTable(tableStatements);

				// Creation of the third table and the statement
				createFilterTable(tableStatements);

				h2consumer.commit();

				/*
				 * Fill tables
				 */
				// Get all param type alias for all report config and set them into a map with
				// reportConfigFk
				Map<Long, List<String>> paramTypeAliasByReportConfigPk = reportConfigDelegate
						.getAllParamTypeAliasByReportConfigPk();

				for (ExportInventoryReportTO expInvRepTO : reportingGroupDAO
						.findExportInventoryReportTOForReportingGroupWithOption(parameter.origin,
								parameter.reportingGroupRefId, offerOptionListNewProvisioningTO)) {

					LOGGER.debug("[exportInventoryReportInteractive] Exporting the refId " + expInvRepTO.refId
							+ " and indicatorId " + expInvRepTO.indicatorId);

					List<String> paramTypeAliases = paramTypeAliasByReportConfigPk.get(expInvRepTO.reportConfigPk);
					if (paramTypeAliases != null) {
						for (String paramTypeAlias : paramTypeAliases) {
							fillTypeAliasTable(paramTypeAlias, pkNumber, tableStatements);
						}
					}

					fillReportInteractiveTable(expInvRepTO, pkNumber, tableStatements);
					pkNumber++;

				}

				for (FilterToOfferOptionTO offerOptionAliasFilterIdTO : reportingGroupDAO
						.findOptionAliasAndFilterIdForReportingGroup(parameter.origin, parameter.reportingGroupRefId)) {
					fillFilterTable(offerOptionAliasFilterIdTO, tableStatements);
				}

				needToCloseH2Connection = false;
				h2consumer.closeConnexion();
				h2ConnectionError = false;
			} catch (JdbcSQLException jdbcsqle) {
				if (JDBCH2Consumer.H2_FILE_READONLY_DATABASE_ERROR_CODES.contains(jdbcsqle.getErrorCode())
						&& fileLocationIndex > -1) {
					h2ConnectionError = true;
					fileLocationIndex--;
					fileLocation = fileLocations.get(fileLocationIndex);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
								"H2 connection error (" + jdbcsqle.getErrorCode()
										+ ") accessing H2 Table reportInputPatternFile: " + jdbcsqle.getMessage()));
					}
				} else {
					LOGGER.error(
							SOATools.buildSOALogMessage(soaContext, "[exportInventoryReportInteractive] KO", jdbcsqle));
					if (needToCloseH2Connection) {
						try {
							h2consumer.closeConnexion();
						} catch (Exception e2) {
							LOGGER.error(SOATools.buildSOALogMessage(soaContext,
									"[exportInventoryReportInteractive] KO on close connection", e2));
							throw new InventoryException(jdbcsqle.getMessage() + "\n" + e2.getMessage(), e2);
						}
					}
					throw new InventoryException(jdbcsqle.getMessage(), jdbcsqle);
				}

			} catch (Exception e) {

				LOGGER.error("[exportInventoryReportInteractive] KO", e);
				if (needToCloseH2Connection) {
					try {
						h2consumer.closeConnexion();
					} catch (Exception e2) {
						LOGGER.error(SOATools.buildSOALogMessage(soaContext,
								"[exportInventoryReportInteractive] KO on close connection", e2));
						throw new InventoryException(e.getMessage() + "\n" + e2.getMessage(), e2);
					}
				}
				throw new InventoryException(e.getMessage(), e);
			}
		} while (h2ConnectionError);
		return fileLocation;
	}

	public String exportInventoryReportTemplate(SOAContext soaContext, ExportInventoryParameter parameter,
			List<String> fileLocations) throws InventoryException {

		/*
		 * This boolean is used to know if we need to close H2 connection in case of exception. To avoid re-close
		 * connection in case of exception during the close
		 */
		boolean needToCloseH2Connection = true;
		boolean h2ConnectionError = true;

		int fileLocationIndex = fileLocations.size() - 1;

		String fileLocation = fileLocations.get(fileLocationIndex);
		do {
			try {

				LOGGER.debug("[exportInventoryReportTemplate] -> Location " + fileLocation);
				h2consumer.createConnexion(fileLocation + Configuration.h2FileReadwriteLockOptionsProperty, false,
						false);

				/*
				 * Create tables and statements
				 */
				// Use to store the report Pk
				long pkNumber = 0L;

				// Map keeping the matching between tables and PreparedStatement
				Map<String, StatementByColumnAdapter> tableStatements = new HashMap<>();

				// Creation of the first table and the statement
				createReportTemplateTable(tableStatements);

				// Creation of the third table and the statement
				createFilterTable(tableStatements);

				h2consumer.commit();

				/*
				 * Fill tables
				 */
				for (ExportInventoryReportTemplateTO expInvRepTplTO : reportingGroupDAO
						.findExportInventoryReportTemplateTOForReportingGroupWithOption(parameter.origin,
								parameter.reportingGroupRefId)) {

					LOGGER.debug("[exportInventoryReportTemplate] Exporting the refId " + expInvRepTplTO.refId);

					fillReportTemplateTable(expInvRepTplTO, pkNumber, tableStatements);
					pkNumber++;

				}

				for (FilterToOfferOptionTO offerOptionAliasFilterIdTO : reportingGroupDAO
						.findOptionAliasAndFilterIdForReportingGroup(parameter.origin, parameter.reportingGroupRefId)) {
					fillFilterTable(offerOptionAliasFilterIdTO, tableStatements);
				}

				needToCloseH2Connection = false;
				h2ConnectionError = false;
				h2consumer.closeConnexion();
			} catch (JdbcSQLException jdbcsqle) {
				if (JDBCH2Consumer.H2_FILE_READONLY_DATABASE_ERROR_CODES.contains(jdbcsqle.getErrorCode())
						&& fileLocationIndex > -1) {
					h2ConnectionError = true;
					fileLocationIndex--;
					fileLocation = fileLocations.get(fileLocationIndex);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
								"H2 connection error (" + jdbcsqle.getErrorCode()
										+ ") accessing H2 Table reportInputPatternFile: " + jdbcsqle.getMessage()));
					}
				} else {
					LOGGER.error(
							SOATools.buildSOALogMessage(soaContext, "[exportInventoryReportInteractive] KO", jdbcsqle));
					if (needToCloseH2Connection) {
						try {
							h2consumer.closeConnexion();
						} catch (Exception e2) {
							LOGGER.error(SOATools.buildSOALogMessage(soaContext,
									"[exportInventoryReportInteractive] KO on close connection", e2));
							throw new InventoryException(jdbcsqle.getMessage() + "\n" + e2.getMessage(), e2);
						}
					}
					throw new InventoryException(jdbcsqle.getMessage(), jdbcsqle);
				}

			} catch (Exception e) {

				LOGGER.error("[exportInventoryReportTemplate] KO", e);
				if (needToCloseH2Connection) {
					try {
						h2consumer.closeConnexion();
					} catch (Exception e2) {
						LOGGER.error("[exportInventoryReportTemplate] KO on close connection", e2);
						throw new InventoryException(e.getMessage() + "\n" + e2.getMessage(), e2);
					}
				}
				throw new InventoryException(e.getMessage(), e);
			}
		} while (h2ConnectionError);
		return fileLocation;
	}

	private void createReportInteractiveTable(Map<String, StatementByColumnAdapter> tableStatements)
			throws TechnicalException, SQLException {

		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_REPORT;

		ColumnParameter pkColumn = new ColumnParameter();
		pkColumn.columnName = BDStatCommonFunction.makePk(cparameter.tableName);
		pkColumn.columnType = JDBCH2Consumer.BIGINT_TYPE;
		cparameter.pkColumn = pkColumn;

		ColumnParameter indicatorId = new ColumnParameter();
		indicatorId.columnName = H2Table.Report.COLUMN_INDICATORID;
		indicatorId.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(indicatorId);

		ColumnParameter refId = new ColumnParameter();
		refId.columnName = H2Table.Report.COLUMN_REFID;
		refId.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(refId);

		ColumnParameter reportTimeUnit = new ColumnParameter();
		reportTimeUnit.columnName = H2Table.Report.COLUMN_REPORT_TIME_UNIT;
		reportTimeUnit.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		cparameter.columns.add(reportTimeUnit);

		ColumnParameter granularity = new ColumnParameter();
		granularity.columnName = H2Table.Report.COLUMN_GRANULARITY;
		granularity.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		cparameter.columns.add(granularity);

		ColumnParameter computeScope = new ColumnParameter();
		computeScope.columnName = H2Table.Report.COLUMN_COMPUTE_SCOPE;
		computeScope.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		cparameter.columns.add(computeScope);

		ColumnParameter computeUri = new ColumnParameter();
		computeUri.columnName = H2Table.Report.COLUMN_COMPUTE_URI;
		computeUri.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(computeUri);

		ColumnParameter outputPatternPrefix = new ColumnParameter();
		outputPatternPrefix.columnName = H2Table.Report.COLUMN_OUTPUT_PATTERN_PREFIX;
		outputPatternPrefix.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(outputPatternPrefix);

		ColumnParameter outputPatternSuffix = new ColumnParameter();
		outputPatternSuffix.columnName = H2Table.Report.COLUMN_OUTPUT_PATTERN_SUFFIX;
		outputPatternSuffix.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(outputPatternSuffix);

		ColumnParameter outputUri = new ColumnParameter();
		outputUri.columnName = H2Table.Report.COLUMN_OUTPUT_URI;
		outputUri.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(outputUri);

		ColumnParameter type = new ColumnParameter();
		type.columnName = H2Table.Report.COLUMN_TYPE;
		type.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		cparameter.columns.add(type);

		ColumnParameter offerAlias = new ColumnParameter();
		offerAlias.columnName = H2Table.Report.COLUMN_OFFER_ALIAS;
		offerAlias.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(offerAlias);

		ColumnParameter optionAlias = new ColumnParameter();
		optionAlias.columnName = H2Table.Report.COLUMN_OPTION_ALIAS;
		optionAlias.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(optionAlias);

		ColumnParameter optionLabel = new ColumnParameter();
		optionLabel.columnName = H2Table.Report.COLUMN_OPTION_LABEL;
		optionLabel.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(optionLabel);

		ColumnParameter reportVersion = new ColumnParameter();
		reportVersion.columnName = H2Table.Report.COLUMN_REPORT_VERSION;
		reportVersion.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		cparameter.columns.add(reportVersion);

		ColumnParameter criteriaType = new ColumnParameter();
		criteriaType.columnName = H2Table.Report.COLUMN_CRITERIA_TYPE;
		criteriaType.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(criteriaType);

		ColumnParameter criteriaValue = new ColumnParameter();
		criteriaValue.columnName = H2Table.Report.COLUMN_CRITERIA_VALUE;
		criteriaValue.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(criteriaValue);

		StatementByColumnAdapter statement = h2consumer.createTableAndStatement(cparameter, true, true);
		LOGGER.info("[exportInventoryReport] Adding table " + cparameter.tableName);
		tableStatements.put(cparameter.tableName, statement);

	}

	private void createReportTemplateTable(Map<String, StatementByColumnAdapter> tableStatements)
			throws TechnicalException, SQLException {

		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_REPORT_TEMPLATE;

		ColumnParameter pkColumn = new ColumnParameter();
		pkColumn.columnName = BDStatCommonFunction.makePk(cparameter.tableName);
		pkColumn.columnType = JDBCH2Consumer.BIGINT_TYPE;
		cparameter.pkColumn = pkColumn;

		ColumnParameter reportConfigAlias = new ColumnParameter();
		reportConfigAlias.columnName = H2Table.ReportTemplate.COLUMN_REPORT_CONFIG_ALIAS;
		reportConfigAlias.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(reportConfigAlias);

		ColumnParameter refId = new ColumnParameter();
		refId.columnName = H2Table.ReportTemplate.COLUMN_REFID;
		refId.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(refId);

		ColumnParameter outputPatternPrefix = new ColumnParameter();
		outputPatternPrefix.columnName = H2Table.ReportTemplate.COLUMN_OUTPUT_PATTERN_PREFIX;
		outputPatternPrefix.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(outputPatternPrefix);

		ColumnParameter outputPatternSuffix = new ColumnParameter();
		outputPatternSuffix.columnName = H2Table.ReportTemplate.COLUMN_OUTPUT_PATTERN_SUFFIX;
		outputPatternSuffix.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(outputPatternSuffix);

		ColumnParameter type = new ColumnParameter();
		type.columnName = H2Table.ReportTemplate.COLUMN_TYPE;
		type.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		cparameter.columns.add(type);

		ColumnParameter format = new ColumnParameter();
		format.columnName = H2Table.ReportTemplate.COLUMN_FORMAT;
		format.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		cparameter.columns.add(format);

		ColumnParameter offerAlias = new ColumnParameter();
		offerAlias.columnName = H2Table.Report.COLUMN_OFFER_ALIAS;
		offerAlias.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(offerAlias);

		ColumnParameter optionAlias = new ColumnParameter();
		optionAlias.columnName = H2Table.ReportTemplate.COLUMN_OPTION_ALIAS;
		optionAlias.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(optionAlias);

		ColumnParameter optionLabel = new ColumnParameter();
		optionLabel.columnName = H2Table.ReportTemplate.COLUMN_OPTION_LABEL;
		optionLabel.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(optionLabel);

		ColumnParameter reportVersion = new ColumnParameter();
		reportVersion.columnName = H2Table.ReportTemplate.COLUMN_REPORT_VERSION;
		reportVersion.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		cparameter.columns.add(reportVersion);

		StatementByColumnAdapter statement = h2consumer.createTableAndStatement(cparameter, true, true);
		LOGGER.info("[exportInventoryReportTemplate] Adding table " + cparameter.tableName);
		tableStatements.put(cparameter.tableName, statement);

	}

	private void createTypeAliasTable(Map<String, StatementByColumnAdapter> tableStatements)
			throws TechnicalException, SQLException {

		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_PARAM_TYPE_ALIAS;

		ColumnParameter reportFk = new ColumnParameter();
		reportFk.columnName = H2Table.ParamTypeAlias.COLUMN_REPORT_FK;
		reportFk.columnType = JDBCH2Consumer.BIGINT_TYPE;
		cparameter.columns.add(reportFk);

		ColumnParameter typeAlias = new ColumnParameter();
		typeAlias.columnName = H2Table.ParamTypeAlias.COLUMN_TYPE_ALIAS;
		typeAlias.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(typeAlias);

		StatementByColumnAdapter statement = h2consumer.createTableAndStatement(cparameter, true, true);
		LOGGER.info("[exportInventoryReport] Adding table " + cparameter.tableName);
		tableStatements.put(cparameter.tableName, statement);

	}

	private void createFilterTable(Map<String, StatementByColumnAdapter> tableStatements)
			throws TechnicalException, SQLException {

		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_FILTER;

		ColumnParameter reportFk = new ColumnParameter();
		reportFk.columnName = H2Table.Filter.COLUMN_OPTION_ALIAS;
		reportFk.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(reportFk);

		ColumnParameter filterId = new ColumnParameter();
		filterId.columnName = H2Table.Filter.COLUMN_FILTER_ID;
		filterId.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(filterId);

		StatementByColumnAdapter statement = h2consumer.createTableAndStatement(cparameter, true, true);
		LOGGER.info("[exportInventoryReport] Adding table " + cparameter.tableName);
		tableStatements.put(cparameter.tableName, statement);

	}

	private void fillReportInteractiveTable(ExportInventoryReportTO expInvRepTO, long reportPkNumber,
			Map<String, StatementByColumnAdapter> tableStatements) throws TechnicalException, SQLException {

		StatementByColumnAdapter reportStatement = tableStatements.get(H2Table.TABLE_NAME_REPORT);

		reportStatement.setLong(BDStatCommonFunction.makePk(H2Table.TABLE_NAME_REPORT), reportPkNumber);
		reportStatement.setString(H2Table.Report.COLUMN_INDICATORID, expInvRepTO.indicatorId);
		reportStatement.setString(H2Table.Report.COLUMN_REFID, expInvRepTO.refId);
		reportStatement.setString(H2Table.Report.COLUMN_REPORT_TIME_UNIT, expInvRepTO.reportTimeUnit.getValue());
		reportStatement.setString(H2Table.Report.COLUMN_GRANULARITY, expInvRepTO.granularity.getValue());
		reportStatement.setString(H2Table.Report.COLUMN_COMPUTE_SCOPE, expInvRepTO.computeScope.getValue());
		reportStatement.setString(H2Table.Report.COLUMN_COMPUTE_URI, expInvRepTO.computeUri);
		reportStatement.setString(H2Table.Report.COLUMN_OUTPUT_PATTERN_PREFIX, expInvRepTO.outputPatternPrefix);
		reportStatement.setString(H2Table.Report.COLUMN_OUTPUT_PATTERN_SUFFIX, expInvRepTO.outputPatternSuffix);
		reportStatement.setString(H2Table.Report.COLUMN_OUTPUT_URI, expInvRepTO.outputUri);
		reportStatement.setString(H2Table.Report.COLUMN_TYPE, expInvRepTO.type.getValue());
		reportStatement.setString(H2Table.Report.COLUMN_OFFER_ALIAS, expInvRepTO.offerAlias);
		reportStatement.setString(H2Table.Report.COLUMN_OPTION_ALIAS, expInvRepTO.optionAlias);
		reportStatement.setString(H2Table.Report.COLUMN_OPTION_LABEL, expInvRepTO.optionLabel);
		reportStatement.setString(H2Table.Report.COLUMN_REPORT_VERSION, expInvRepTO.reportVersion);
		reportStatement.setString(H2Table.Report.COLUMN_CRITERIA_TYPE, expInvRepTO.criteriaType);
		reportStatement.setString(H2Table.Report.COLUMN_CRITERIA_VALUE, expInvRepTO.criteriaValue);

		reportStatement.execute();

		h2consumer.commit();
	}

	private void fillTypeAliasTable(String paramTypeAlias, long reportPkNumber,
			Map<String, StatementByColumnAdapter> tableStatements) throws TechnicalException, SQLException {

		StatementByColumnAdapter paramTypeAliasStatement = tableStatements.get(H2Table.TABLE_NAME_PARAM_TYPE_ALIAS);

		paramTypeAliasStatement.setLong(H2Table.ParamTypeAlias.COLUMN_REPORT_FK, reportPkNumber);
		paramTypeAliasStatement.setString(H2Table.ParamTypeAlias.COLUMN_TYPE_ALIAS, paramTypeAlias);

		paramTypeAliasStatement.execute();

		h2consumer.commit();
	}

	private void fillFilterTable(FilterToOfferOptionTO offerOptionAliasFilterIdTO,
			Map<String, StatementByColumnAdapter> tableStatements) throws TechnicalException, SQLException {

		StatementByColumnAdapter filterStatement = tableStatements.get(H2Table.TABLE_NAME_FILTER);

		filterStatement.setString(H2Table.Filter.COLUMN_OPTION_ALIAS, offerOptionAliasFilterIdTO.offerOptionAlias);
		filterStatement.setString(H2Table.Filter.COLUMN_FILTER_ID, offerOptionAliasFilterIdTO.filterId);

		filterStatement.execute();

		h2consumer.commit();
	}

	private void fillReportTemplateTable(ExportInventoryReportTemplateTO expInvRepTplTO, long reportPkNumber,
			Map<String, StatementByColumnAdapter> tableStatements) throws TechnicalException, SQLException {

		StatementByColumnAdapter reportStatement = tableStatements.get(H2Table.TABLE_NAME_REPORT_TEMPLATE);

		reportStatement.setLong(BDStatCommonFunction.makePk(H2Table.TABLE_NAME_REPORT_TEMPLATE), reportPkNumber);
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_REPORT_CONFIG_ALIAS, expInvRepTplTO.reportConfigAlias);
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_REFID, expInvRepTplTO.refId);
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_OUTPUT_PATTERN_PREFIX,
				expInvRepTplTO.outputPatternPrefix);
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_OUTPUT_PATTERN_SUFFIX,
				expInvRepTplTO.outputPatternSuffix);
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_TYPE, expInvRepTplTO.type.getValue());
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_FORMAT, expInvRepTplTO.format);
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_OFFER_ALIAS, expInvRepTplTO.offerAlias);
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_OPTION_ALIAS, expInvRepTplTO.optionAlias);
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_OPTION_LABEL, expInvRepTplTO.optionLabel);
		reportStatement.setString(H2Table.ReportTemplate.COLUMN_REPORT_VERSION, expInvRepTplTO.reportVersion);

		reportStatement.execute();

		h2consumer.commit();
	}
}
