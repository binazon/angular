package com.orange.srs.refreport.business.delegate;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.orange.srs.common.annotation.BusinessRule;
import com.orange.srs.refreport.business.ConfigurationXMLConstant;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.DataLocationResultSet;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.EntityAttributeResultSet;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.EntityGroupAttributeResultSet;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.EntityLinkAttributeResultSet;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.EntityListAttributeResultSet;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.EntityResultSet;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.EntitySubtypeResultSet;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.LinkResultSet;
import com.orange.srs.refreport.model.Criteria;
import com.orange.srs.refreport.model.DataLocation;
import com.orange.srs.refreport.model.GroupAttribute;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.inventory.EntityTypeH2StatementAndInsertionInfo;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryStatusTO;
import com.orange.srs.refreport.model.exception.InventoryException;
import com.orange.srs.refreport.model.parameter.inventory.EntityInfoParameter;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.JMSConnectionHandler;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.business.commonFunctions.BDStatCommonFunction;
import com.orange.srs.statcommon.business.commonFunctions.DataLocator;
import com.orange.srs.statcommon.consumer.jdbc.JDBCH2Consumer;
import com.orange.srs.statcommon.model.constant.H2Table;
import com.orange.srs.statcommon.model.parameter.ColumnParameter;
import com.orange.srs.statcommon.model.parameter.CreateTableParameter;
import com.orange.srs.statcommon.model.parameter.ExportInventoryParameter;
import com.orange.srs.statcommon.model.parameter.PatternParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.ShepherdInventoryPurgeParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.NotifyShepherdPurgeJobParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.exception.TechnicalException;
import com.orange.srs.statcommon.technical.jdbc.IndexStatementBuilder;
import com.orange.srs.statcommon.technical.jdbc.StatementByColumnAdapter;
import com.orange.srs.statcommon.technical.jdbc.StatementWithColumnNameAdapter;
import com.orange.srs.statcommon.technical.jdbc.UpdateStatementAdapter;

@Stateless
public class InventoryDelegate {

	private static Logger logger = Logger.getLogger(InventoryDelegate.class);

	private static final List<ColumnParameter> baseParametersEntities = createEntityTableColumns();
	private static final List<String> defaultcolumnsToIndex = Arrays.asList(H2Table.Entity.COLUMN_ENTITY_ID);
	private static final List<String> defaultcolumnsToLinkTableIndex = Arrays
			.asList(H2Table.Link.COLUMN_DESTINATION_ENTITY_ID, H2Table.Link.COLUMN_SOURCE_ENTITY_ID);
	private static final List<String> defaultcolumnsToEntityFilterTableIndex = Arrays
			.asList(H2Table.EntityFilter.COLUMN_FILTER_JOIN);

	@EJB
	private ReportingGroupDelegate reportingGroupDelegate;

	@Inject
	private ReportingEntityJDBCConsumer reportingEntityJDBCConsumer;

	@EJB
	private JMSConnectionHandler jmsConnectionHandler;

	@BusinessRule(ruleId = "InventoryDelegate_exportH2Inventory", summary = "", description = "Export the entity inventory in a H2 file database for the given reportingGroup. The file path is defined according to the dataLocation of the reportingGroup and the given date. "
			+ "The table names are defined according the file configuration 'inventoryConfig.xml'", associatedRules = {}, keywords = {
					"export", "inventory", "h2", "entity" })

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ExportInventoryStatusTO exportH2Inventory(ExportInventoryParameter parameter, SOAContext soaContext)
			throws SQLException, JDOMException, ClassNotFoundException, BusinessException, IOException {

		String resultFileLocation = null;
		String resultFileLocationFullPath = null;
		boolean resultFileWithError = false;
		Long start = Utils.getTime();
		ExportInventoryStatusTO exportInventoryStatusTO = new ExportInventoryStatusTO();

		logger.info(SOATools.buildSOALogMessage(soaContext,
				"[exportH2Inventory] Parameter " + parameter.reportingGroupRefId + " (" + parameter.origin + ")"));

		if (parameter.date == null || parameter.origin == null || parameter.reportingGroupRefId == null) {
			throw new BusinessException(BusinessException.WRONG_PARAMETER_EXCEPTION + " : "
					+ ExportInventoryParameter.class.toString() + " = " + parameter,
					BusinessException.WRONG_PARAMETER_EXCEPTION_CODE);
		}

		ReportingGroup currentGroup = reportingGroupDelegate.getReportingGroupFromOriginAndRefGroup(parameter.origin,
				parameter.reportingGroupRefId);

		if (currentGroup == null) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": group = " + parameter,
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION_CODE);
		}

		if (currentGroup.getDataLocation() == null) {
			throw new BusinessException(BusinessException.NO_DATA_FOUND_EXCEPTION_MESSAGE + " group = " + parameter
					+ " : data location NOT FOUND", BusinessException.NO_DATA_FOUND_EXCEPTION);
		}

		JDBCH2Consumer inventoryConsumer = null;

		EntityResultSet entityResultSet = null;
		EntityAttributeResultSet entityAttributeResultSet = null;
		EntityGroupAttributeResultSet entityGroupAttributeResultSet = null;
		EntitySubtypeResultSet entitySubtypeResultSet = null;
		EntityListAttributeResultSet entityListAttributeResultSet = null;
		DataLocationResultSet dataLocationResultSet = null;
		LinkResultSet parentLinkResultSet = null;
		EntityLinkAttributeResultSet entityLinkAttributeResultSet = null;

		try {
			reportingEntityJDBCConsumer.openRefReportConnection();

			PatternParameter patternParameter = new PatternParameter();
			patternParameter.startUnit = parameter.date.getTime();
			patternParameter.origin = currentGroup.getOrigin();
			patternParameter.reportingGroupLocation = currentGroup.getDataLocation().getLocationPattern();
			patternParameter.properties = Configuration.mountConfiguration;

			resultFileLocation = DataLocator.getInventoryEntityFileLocationWithoutExtension(patternParameter);
			resultFileLocationFullPath = DataLocator.getInventoryEntityFileLocationWithExtension(patternParameter);
			logger.debug("[exportH2Inventory] -> Location " + resultFileLocation);

			inventoryConsumer = new JDBCH2Consumer();
			inventoryConsumer.createConnexion(resultFileLocation + Configuration.h2FileReadwriteLockOptionsProperty,
					false, false);

			Map<String, Set<String>> complexLinks = new HashMap<>();
			Map<String, Set<String>> complexLinksWithParams = new HashMap<>();
			Map<String, EntityTypeH2StatementAndInsertionInfo> typeStatementAndInfoRegistry = buildH2TablesWithConfiguration(
					currentGroup, inventoryConsumer, complexLinks, complexLinksWithParams, soaContext);

			Long beforeRequest = Utils.getTime();
			entityResultSet = reportingEntityJDBCConsumer.getEntityForGroupOrderedByTypeAndPk(currentGroup.getPk());
			Long afterEntityRequest = Utils.getTime();
			entityAttributeResultSet = reportingEntityJDBCConsumer
					.getEntityAttributeForEntityForGroupOrderedByTypeAndPk(currentGroup.getPk());
			Long afterEntityAttributeRequest = Utils.getTime();
			entityGroupAttributeResultSet = reportingEntityJDBCConsumer
					.getEntityGroupAttributeForEntityForGroupOrderedByEntityPk(currentGroup.getPk());
			Long afterGroupRequest = Utils.getTime();
			entitySubtypeResultSet = reportingEntityJDBCConsumer
					.getEntitySubtypeForEntityForGroupOrderedByTypeAndPk(currentGroup.getPk());
			Long afterSubtypeRequest = Utils.getTime();
			dataLocationResultSet = reportingEntityJDBCConsumer
					.getDataLocationForEntityForGroupOrderedByTypeAndPk(currentGroup.getPk());
			parentLinkResultSet = reportingEntityJDBCConsumer
					.getParentLinkEntityForGroupOrderedByTypeAndPk(currentGroup.getPk(), complexLinks);
			Long afterLinkRequest = Utils.getTime();
			entityListAttributeResultSet = reportingEntityJDBCConsumer
					.getEntityListAttributeForEntityForGroupOrderedByTypeAndPk(currentGroup.getPk());
			Long afterLinkAttribureRequest = Utils.getTime();

			exportInventoryStatusTO.entityRequestTime = afterEntityRequest - beforeRequest;
			exportInventoryStatusTO.attributeRequestTime = afterEntityAttributeRequest - afterEntityRequest;
			exportInventoryStatusTO.groupRequestTime = afterGroupRequest - afterEntityAttributeRequest;
			exportInventoryStatusTO.subtypeRequestTime = afterSubtypeRequest - afterGroupRequest;
			exportInventoryStatusTO.linkRequestTime = afterLinkRequest - afterSubtypeRequest;
			exportInventoryStatusTO.linkAttributeRequestTime = afterLinkAttribureRequest - afterLinkRequest;
			exportInventoryStatusTO.requestTime = afterLinkAttribureRequest - beforeRequest;

			if (!complexLinksWithParams.isEmpty())
				entityLinkAttributeResultSet = reportingEntityJDBCConsumer
						.getEntityLinkAttributeForEntityForGroupOrderedByTypeAndPk(currentGroup.getPk(),
								complexLinksWithParams);

			String oldEntityType = null;
			StatementWithColumnNameAdapter statementWithColumnNameAdapter = null;
			EntityTypeH2StatementAndInsertionInfo typeStatementAndInfo = null;
			int commitIndex = 0;
			Long currentTableNextPk = 0L;
			EntityInfoParameter entityParameter = new EntityInfoParameter();

			List<String> tablesToIndex = new ArrayList<>();

			while (entityResultSet.next()) {
				String currentEntityType = entityResultSet.getEntityType();

				if (!currentEntityType.equals(oldEntityType)) {
					typeStatementAndInfo = typeStatementAndInfoRegistry.get(currentEntityType);
					if (typeStatementAndInfo == null) {
						logger.warn("No configuration found for entityType = " + currentEntityType
								+ ", entity is ignored. Check file " + Configuration.statEntityConfFileName);
						continue;
					}

					statementWithColumnNameAdapter = typeStatementAndInfo.getStatement();

					currentTableNextPk = 0L;
					oldEntityType = currentEntityType;
				}

				tablesToIndex.add(currentEntityType);
				String entityId = entityResultSet.getEntityId();

				entityParameter.entityId = entityId;
				entityParameter.inventoryPk = currentTableNextPk;
				entityParameter.entityPk = entityResultSet.getPk();

				if (logger.isTraceEnabled()) {
					logger.trace("[exportH2Inventory] processing entity : pk=" + entityParameter.entityPk + ";entityId="
							+ entityParameter.entityId + ";entityType=" + currentEntityType);
				}

				statementWithColumnNameAdapter.setValue(BDStatCommonFunction.makePk(currentEntityType),
						currentTableNextPk);
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_ENTITY_ID, entityId);
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_ENTITY_TYPE,
						entityResultSet.getEntityType());
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_LABEL, entityResultSet.getLabel());
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_SHORT_LABEL,
						entityResultSet.getShortLabel());
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_ORIGIN, entityResultSet.getOrigin());
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_PARTITIONS,
						entityResultSet.getPartitionNumber());
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_CREATION_DATE,
						entityResultSet.getCreationDate());
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_UPDATE_DATE,
						entityResultSet.getUpdateDate());
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_ORIGIN_REPORTING_GROUP,
						parameter.reportingGroupRefId);
				statementWithColumnNameAdapter.setValue(H2Table.Entity.COLUMN_BELONGS_TO_GROUP,
						entityResultSet.getBelongsTo());

				entityAttributeResultSet.insertAllNameValueForEntity(statementWithColumnNameAdapter, entityParameter,
						typeStatementAndInfoRegistry);
				entityGroupAttributeResultSet.insertAllNameValueForEntity(statementWithColumnNameAdapter,
						entityParameter, typeStatementAndInfoRegistry);
				entitySubtypeResultSet.insertAllNameValueForEntity(statementWithColumnNameAdapter, entityParameter,
						typeStatementAndInfoRegistry);
				dataLocationResultSet.insertAllNameValueForEntity(statementWithColumnNameAdapter, entityParameter,
						typeStatementAndInfoRegistry);

				if (typeStatementAndInfo.hasParentColumn() && entityResultSet.getParentId() != null) {
					statementWithColumnNameAdapter.setValue(entityResultSet.getParentType(),
							entityResultSet.getParentId());
					exportInventoryStatusTO.amountOfParent++;
				}

				// Links
				parentLinkResultSet.insertAllNameValueForEntity(statementWithColumnNameAdapter, entityParameter,
						typeStatementAndInfoRegistry);
				if (entityLinkAttributeResultSet != null)
					entityLinkAttributeResultSet.insertAllNameValueForEntity(statementWithColumnNameAdapter,
							entityParameter, typeStatementAndInfoRegistry);

				statementWithColumnNameAdapter.execute();
				currentTableNextPk++;

				if (entityListAttributeResultSet != null) {
					entityListAttributeResultSet.insertAllNameValueForEntity(statementWithColumnNameAdapter,
							entityParameter, typeStatementAndInfoRegistry);
				}

				commitIndex++;
				if (commitIndex % 100 == 0) {
					commitIndex = 0;
					inventoryConsumer.commit();
				}
			}
			inventoryConsumer.commit();

			if (tablesToIndex.size() == 0) {
				throw new BusinessException(BusinessException.ERROR_PROVISIONING_FILE_MESSAGE
						+ "No entity was added to tables. Inventory file will be deleted: "
						+ resultFileLocationFullPath);
			}

			exportInventoryStatusTO.amountOfEntity = entityResultSet.getNumberOfElements();

			exportInventoryStatusTO.amountOfAttributes = entityAttributeResultSet.getNumberOfElements();
			exportInventoryStatusTO.maxAmountOfAttributes = entityAttributeResultSet.getMaxNumberOfElements();
			exportInventoryStatusTO.avgAmountOfAttributes = entityAttributeResultSet.getAvgNumberOfElements();

			exportInventoryStatusTO.amountOfLink = parentLinkResultSet.getNumberOfElements();
			exportInventoryStatusTO.amountOfComplexLink = parentLinkResultSet.getAmountOfComplexLink();
			exportInventoryStatusTO.amountOfSimpleLink = parentLinkResultSet.getAmountOfSimpleLink();
			exportInventoryStatusTO.maxAmountOfLink = parentLinkResultSet.getMaxNumberOfElements();
			exportInventoryStatusTO.avgAmountOfLink = parentLinkResultSet.getAvgNumberOfElements();

			exportInventoryStatusTO.amountOfLinkAttribute = entityLinkAttributeResultSet.getNumberOfElements();
			exportInventoryStatusTO.maxAmountOfLinkAttribute = entityLinkAttributeResultSet.getMaxNumberOfElements();
			exportInventoryStatusTO.avgAmountOfLinkAttribute = entityLinkAttributeResultSet.getAvgNumberOfElements();

			exportInventoryStatusTO.amountOfSubtype = entitySubtypeResultSet.getNumberOfElements();
			exportInventoryStatusTO.maxAmountOfSubtype = entitySubtypeResultSet.getMaxNumberOfElements();
			exportInventoryStatusTO.avgAmountOfSubtype = entitySubtypeResultSet.getAvgNumberOfElements();

			exportInventoryStatusTO.amountOfEntityGroupAttributes = entityGroupAttributeResultSet.getNumberOfElements();
			exportInventoryStatusTO.maxAmountOfEntityGroupAttributes = entityGroupAttributeResultSet
					.getMaxNumberOfElements();
			exportInventoryStatusTO.avgAmountOfEntityGroupAttributes = entityGroupAttributeResultSet
					.getAvgNumberOfElements();

			exportInventoryStatusTO.fileName = resultFileLocation;

			// Shepherd Notification

			ShepherdInventoryPurgeParameter purgeParameters = new ShepherdInventoryPurgeParameter(resultFileLocation,
					new Timestamp(parameter.date.getTimeInMillis()));

			NotifyShepherdPurgeJobParameter jobParam = new NotifyShepherdPurgeJobParameter();
			jobParam.parameter = purgeParameters;
			// TODO: Rework after delivery G08R00C05
			// jmsConnectionHandler.sendJMSMessage(jobParam,
			// JMSAttributeName.SHEPHERD_PURGE_QUEUE);

			// Add index to EntityId
			for (Entry<String, EntityTypeH2StatementAndInsertionInfo> currentTable : typeStatementAndInfoRegistry
					.entrySet()) {
				EntityTypeH2StatementAndInsertionInfo info = currentTable.getValue();
				for (String indexColumn : info.getIndexColumns()) {
					IndexStatementBuilder indexesBuilder = new IndexStatementBuilder(inventoryConsumer.getConnection(),
							currentTable.getKey(), new String[] { indexColumn });
					indexesBuilder.executeStatement();
				}
			}

		} catch (Exception e) {
			resultFileWithError = true;
			throw e;
		} finally {
			Long end = Utils.getTime();
			logger.info(SOATools.buildSOALogMessage(soaContext,
					"Export inventory for reportingGroup " + parameter + " in " + (end - start) + " ms"));

			if (parentLinkResultSet != null) {
				parentLinkResultSet.close(soaContext);
			}
			if (entityResultSet != null) {
				entityResultSet.close(soaContext);
			}
			if (entityAttributeResultSet != null) {
				entityAttributeResultSet.close(soaContext);
			}
			if (entityGroupAttributeResultSet != null) {
				entityGroupAttributeResultSet.close(soaContext);
			}
			if (dataLocationResultSet != null) {
				dataLocationResultSet.close(soaContext);
			}
			if (entitySubtypeResultSet != null) {
				entitySubtypeResultSet.close(soaContext);
			}
			if (entityListAttributeResultSet != null) {
				entityListAttributeResultSet.close(soaContext);
			}
			if (entityLinkAttributeResultSet != null) {
				entityLinkAttributeResultSet.close(soaContext);
			}

			try {
				reportingEntityJDBCConsumer.closeRefReportConnection();
			} catch (Exception e) {
				logger.warn(SOATools.buildSOALogMessage(soaContext,
						"Export inventory for reportingGroup: closing RefReport connexion failed (" + e.getMessage()
								+ ")",
						e));
			}

			if (inventoryConsumer != null) {
				try {
					inventoryConsumer.closeConnexion();
				} catch (Exception e) {
					logger.warn(SOATools.buildSOALogMessage(soaContext,
							"Export inventory for reportingGroup: closing Inventory connexion failed (" + e.getMessage()
									+ ")",
							e));
				}
			}

			if (resultFileWithError) {
				try {
					Files.delete(Paths.get(resultFileLocationFullPath));
					logger.warn(SOATools.buildSOALogMessage(soaContext,
							"Exception occured. Inventory file deleted: " + resultFileLocationFullPath));
				} catch (Exception e2) {
					logger.warn(SOATools.buildSOALogMessage(soaContext, e2.getMessage()));
				}
			}
		}

		Long end = Utils.getTime();
		exportInventoryStatusTO.overAllTime = end - start;

		return exportInventoryStatusTO;
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	private Map<String, EntityTypeH2StatementAndInsertionInfo> buildH2TablesWithConfiguration(
			ReportingGroup currentGroup, JDBCH2Consumer inventoryConsumer, Map<String, Set<String>> complexLinks,
			Map<String, Set<String>> complexLinksWithParams, SOAContext context)
			throws SQLException, IOException, JDOMException, BusinessException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(new FileReader(Configuration.statEntityConfFileName));

		setH2VersionConstantsWithConfiguration(document, inventoryConsumer, context);

		Element reportingGroupExportConfiguration = document.getRootElement()
				.getChild(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_REPORTING_GROUP_TABLE);
		if (reportingGroupExportConfiguration != null) {
			makeReportingGroupTableWithConfiguration(reportingGroupExportConfiguration, currentGroup,
					inventoryConsumer);
		}

		return createH2TablesByEntityTypeWithConfiguration(document, currentGroup, inventoryConsumer, complexLinks,
				complexLinksWithParams, context);
	}

	private void setH2VersionConstantsWithConfiguration(Document document, JDBCH2Consumer inventoryConsumer,
			SOAContext context) throws TechnicalException, SQLException {

		Element inventoryVersion = document.getRootElement()
				.getChild(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_INVENTORY_VERSION);
		if (inventoryVersion == null) {
			logger.error(SOATools.buildSOALogMessage(context,
					"[exportH2Inventory] " + InventoryException.UNKWNON_VERSION_EXCEPTION));
			throw new InventoryException(InventoryException.UNKWNON_VERSION_EXCEPTION);
		}
		String strInventoryVersion = inventoryVersion.getValue();

		try {
			Integer.parseInt(strInventoryVersion);
		} catch (NumberFormatException e) {
			logger.error(SOATools.buildSOALogMessage(context,
					"[exportH2Inventory] " + InventoryException.UNKWNON_VERSION_EXCEPTION));
			throw new InventoryException(InventoryException.UNKWNON_VERSION_EXCEPTION);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(context,
					"[exportH2Inventory]: inventory version=" + strInventoryVersion));
		}

		Element openStatVersion = document.getRootElement()
				.getChild(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_OPENSTAT_VERSION);
		if (openStatVersion == null) {
			logger.error(SOATools.buildSOALogMessage(context,
					"[exportH2Inventory] " + InventoryException.UNKWNON_VERSION_EXCEPTION));
			throw new InventoryException(InventoryException.UNKWNON_VERSION_EXCEPTION);
		}
		String strOpenStatVersion = openStatVersion.getValue();

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(context,
					"[exportH2Inventory]: OpenStat version=" + strOpenStatVersion));
		}
		inventoryConsumer.updateConstant(H2Table.VARIABLE_NAME_INVENTORY_VERSION, strInventoryVersion);
		inventoryConsumer.updateConstant(H2Table.VARIABLE_NAME_OPEN_STAT_VERSION, strOpenStatVersion);

	}

	private void makeReportingGroupTableWithConfiguration(Element reportingGroupExportConfiguration,
			ReportingGroup reportingGroup, JDBCH2Consumer inventoryConsumer) throws SQLException {
		StatementByColumnAdapter statement = createH2ReportingGroupTableWithConfiguration(
				reportingGroupExportConfiguration, reportingGroup, inventoryConsumer);
		persistReportingGroupInfos(reportingGroup, statement);
		statement.getStatement().close();

		// Add ReportingGroupRef index
		String[] columnsToIndex = new String[] { H2Table.ReportingGroup.COLUMN_REPORTING_GROUP_REF };
		IndexStatementBuilder indexesBuilder = new IndexStatementBuilder(inventoryConsumer.getConnection(),
				H2Table.TABLE_NAME_REPORTING_GROUP, columnsToIndex);
		indexesBuilder.executeStatement();
	}

	private StatementByColumnAdapter createH2ReportingGroupTableWithConfiguration(
			Element reportingGroupExportConfiguration, ReportingGroup reportingGroup, JDBCH2Consumer inventoryConsumer)
			throws TechnicalException, SQLException {

		List<ColumnParameter> baseParametersReportingGroup = createReportingGroupTableColumns();
		List<Element> listAttributes = reportingGroupExportConfiguration
				.getChildren(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_ATTRIBUTE);
		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_REPORTING_GROUP;

		cparameter.columns.addAll(baseParametersReportingGroup);

		ColumnParameter pkColumn = new ColumnParameter();
		pkColumn.columnName = BDStatCommonFunction.makePk(cparameter.tableName);
		pkColumn.columnType = JDBCH2Consumer.BIGINT_TYPE;
		cparameter.pkColumn = pkColumn;

		String defaultOriginAttribute = ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_DEFAULT + '.'
				+ reportingGroup.getOrigin().toLowerCase();
		for (Element attributeParamElement : listAttributes) {
			ColumnParameter coparameter = new ColumnParameter();
			coparameter.columnName = attributeParamElement
					.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_NAME);
			String columnType = attributeParamElement
					.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_TYPE);
			if (columnType != null) {
				coparameter.columnType = columnType;
			} else {
				coparameter.columnType = JDBCH2Consumer.VARCHAR_TYPE;
			}
			String defaultValue = attributeParamElement
					.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_DEFAULT);
			if (defaultValue != null) {
				coparameter.defaultValue = defaultValue;
			}
			String defaultValueOrigin = attributeParamElement.getAttributeValue(defaultOriginAttribute);
			if (defaultValueOrigin != null) {
				coparameter.defaultValue = defaultValueOrigin;
			}
			cparameter.columns.add(coparameter);
		}

		for (Criteria criteria : reportingGroup.getCriterias()) {
			ColumnParameter coparameter = new ColumnParameter();
			coparameter.columnName = BDStatCommonFunction.makeDataLocationColumnName(criteria.getCriteriaValue());
			coparameter.columnType = JDBCH2Consumer.VARCHAR_TYPE;
			cparameter.columns.add(coparameter);
		}

		StatementByColumnAdapter statement = inventoryConsumer.createTableAndStatement(cparameter, true, true);

		if (logger.isDebugEnabled()) {
			logger.debug("[exportH2Inventory] adding reportinggroup table " + cparameter.tableName);
		}

		return statement;
	}

	private void persistReportingGroupInfos(ReportingGroup reportingGroup, StatementByColumnAdapter groupStatement)
			throws TechnicalException, SQLException {
		groupStatement.setLong(BDStatCommonFunction.makePk(H2Table.TABLE_NAME_REPORTING_GROUP), 0L);
		groupStatement.setString(H2Table.ReportingGroup.COLUMN_REPORTING_GROUP_REF,
				reportingGroup.getReportingGroupRef());
		groupStatement.setString(H2Table.ReportingGroup.COLUMN_ORIGIN, reportingGroup.getOrigin());
		groupStatement.setString(H2Table.ReportingGroup.COLUMN_LABEL, reportingGroup.getLabel());
		groupStatement.setTimestamp(H2Table.ReportingGroup.COLUMN_CREATION_DATE, reportingGroup.getCreationDate());
		groupStatement.setTimestamp(H2Table.ReportingGroup.COLUMN_UPDATE_DATE, reportingGroup.getUpdateDate());

		for (Criteria criteria : reportingGroup.getCriterias()) {
			DataLocation dataLocation = reportingGroup.getDataLocation();
			if (criteria.getCriteriaType().equals(dataLocation.getCriteria().getCriteriaType())
					&& criteria.getCriteriaValue().equals(dataLocation.getCriteria().getCriteriaValue())) {
				try {
					groupStatement.setString(BDStatCommonFunction.makeDataLocationColumnName(
							dataLocation.getCriteria().getCriteriaValue()), dataLocation.getLocationPattern());
				} catch (TechnicalException sbcae) {
					logger.warn(
							"[exportH2Inventory] Error while exporting Reporting Group (dataLocation) :"
									+ reportingGroup.getReportingGroupRef() + "; param :"
									+ BDStatCommonFunction
											.makeDataLocationColumnName(dataLocation.getCriteria().getCriteriaValue()),
							sbcae);
				}
			}
		}

		for (GroupAttribute attribute : reportingGroup.getGroupAttributes()) {
			try {
				groupStatement.setString(attribute.getParamName(), attribute.getParamValue());
			} catch (TechnicalException sbcae) {
				logger.warn("Error while exporting Reporting Group (Attribute) :"
						+ reportingGroup.getReportingGroupRef() + "; param :" + attribute.getParamName());
			}
		}

		groupStatement.execute();
	}

	private Map<String, EntityTypeH2StatementAndInsertionInfo> createH2TablesByEntityTypeWithConfiguration(
			Document document, ReportingGroup currentGroup, JDBCH2Consumer inventoryConsumer,
			Map<String, Set<String>> complexLinks, Map<String, Set<String>> complexLinksWithParams,
			SOAContext soaContext) throws TechnicalException, SQLException, BusinessException {
		Map<String, EntityTypeH2StatementAndInsertionInfo> typeStatementAndInfoRegistry = new HashMap<>();
		List<Element> listReportingEntities = document.getRootElement()
				.getChildren(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_REPORTING_ENTITY_TABLE);
		if (logger.isDebugEnabled()) {
			logger.debug("[exportH2Inventory] checking " + listReportingEntities.size() + " nodes");
		}

		for (Element entityTableConfiguration : listReportingEntities) {
			List<String> columnsIndex = new ArrayList<>(defaultcolumnsToIndex);
			CreateTableParameter entityTableParameter = new CreateTableParameter();
			entityTableParameter.tableName = entityTableConfiguration
					.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_TYPE);
			if (logger.isDebugEnabled()) {
				logger.debug("[exportH2Inventory] adding entity table " + entityTableParameter.tableName);
			}

			entityTableParameter.columns.addAll(baseParametersEntities);

			ColumnParameter pkColumn = new ColumnParameter();
			pkColumn.columnName = BDStatCommonFunction.makePk(entityTableParameter.tableName);
			pkColumn.columnType = JDBCH2Consumer.BIGINT_TYPE;
			entityTableParameter.pkColumn = pkColumn;

			List<Element> entityTableAttributeConfigurationList = entityTableConfiguration
					.getChildren(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_ATTRIBUTE);

			for (Element entityTableAttributeConfiguration : entityTableAttributeConfigurationList) {
				String name = entityTableAttributeConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_NAME);
				ColumnParameter coparameter = new ColumnParameter();
				coparameter.columnName = name;
				coparameter.columnType = entityTableAttributeConfiguration.getAttributeValue(
						ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_TYPE, JDBCH2Consumer.VARCHAR_TYPE);
				String defaultValue = entityTableAttributeConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_DEFAULT);
				if (defaultValue != null) {
					coparameter.defaultValue = defaultValue;
				}
				entityTableParameter.columns.add(coparameter);
				String attributIndex = entityTableAttributeConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_INDEX);
				if (attributIndex != null && attributIndex.equalsIgnoreCase("true")) {
					columnsIndex.add(name);
				}
			}

			List<Element> entityTableGroupAttributeConfigurationList = entityTableConfiguration
					.getChildren(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_GROUPATTRIBUTE);

			for (Element entityTableGroupAttributeConfiguration : entityTableGroupAttributeConfigurationList) {
				String name = entityTableGroupAttributeConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_NAME);
				ColumnParameter coparameter = new ColumnParameter();
				coparameter.columnName = name;
				coparameter.columnType = entityTableGroupAttributeConfiguration.getAttributeValue(
						ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_TYPE, JDBCH2Consumer.VARCHAR_TYPE);

				String defaultValue = entityTableGroupAttributeConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_DEFAULT);
				if (defaultValue != null) {
					coparameter.defaultValue = defaultValue;
				}

				entityTableParameter.columns.add(coparameter);
				String attributIndex = entityTableGroupAttributeConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_INDEX);
				if (attributIndex != null && attributIndex.equalsIgnoreCase("true")) {
					columnsIndex.add(name);
				}
			}

			for (Criteria criteria : currentGroup.getCriterias()) {
				ColumnParameter coparameter = new ColumnParameter();
				coparameter.columnName = BDStatCommonFunction.makeDataLocationColumnName(criteria.getCriteriaValue());
				coparameter.columnType = JDBCH2Consumer.VARCHAR_TYPE;
				entityTableParameter.columns.add(coparameter);
			}

			List<Element> entityTableListAttributeConfigurationList = entityTableConfiguration
					.getChildren(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_LIST_ATTRIBUTE);
			for (Element entityTableListAttributeConfiguration : entityTableListAttributeConfigurationList) {

				String attributeListName = entityTableListAttributeConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_NAME);

				List<String> parameterNameList = new ArrayList<>();

				List<Element> listAttributeParameterList = entityTableListAttributeConfiguration
						.getChildren(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_LIST_ATTRIBUTE_PARAMETER);
				for (Element listAttributeParameter : listAttributeParameterList) {
					String parameterName = listAttributeParameter
							.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_NAME);
					parameterNameList.add(parameterName);

				}
				typeStatementAndInfoRegistry.putAll(createAttributeListTable(entityTableParameter.tableName,
						attributeListName, parameterNameList, inventoryConsumer));
			}

			List<Element> linkConfigurationList = entityTableConfiguration
					.getChildren(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_LINK);
			EntityTypeH2StatementAndInsertionInfo info = new EntityTypeH2StatementAndInsertionInfo();
			for (Element linkConfiguration : linkConfigurationList) {
				String linkName = linkConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_NAME);
				String linkType = linkConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_TYPE);
				String targetEntity = linkConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_TARGET);
				if (StringUtils.isEmpty(linkType)
						|| linkType.equals(ConfigurationXMLConstant.EntityLinkType.SINGLE.getValue())) {
					ColumnParameter coparameter = new ColumnParameter();
					coparameter.columnName = linkName;
					coparameter.columnType = JDBCH2Consumer.VARCHAR_TYPE;
					entityTableParameter.columns.add(coparameter);
					if (logger.isDebugEnabled()) {
						logger.debug("[exportH2Inventory] adding single link column " + linkName);
					}
					columnsIndex.add(linkName);
				} else if (linkType.equals(ConfigurationXMLConstant.EntityLinkType.PARENT.getValue())) {
					ColumnParameter coparameter = new ColumnParameter();
					coparameter.columnName = linkName;
					coparameter.columnType = JDBCH2Consumer.VARCHAR_TYPE;
					entityTableParameter.columns.add(coparameter);
					info.setHasParentColumn(true);
					if (logger.isDebugEnabled()) {
						logger.debug("[exportH2Inventory] adding parent link column " + linkName);
					}
					columnsIndex.add(linkName);
				} else {
					Set<String> targetValues = complexLinks.get(linkName);
					if (targetValues == null) {
						Set<String> values = new HashSet<>();
						values.add(targetEntity.isEmpty() ? entityTableParameter.tableName : targetEntity);
						complexLinks.put(linkName, values);
					} else {
						targetValues.add(targetEntity.isEmpty() ? entityTableParameter.tableName : targetEntity);
					}

					typeStatementAndInfoRegistry.putAll(createComplexLinkTableWithConfiguration(linkConfiguration,
							targetEntity, complexLinksWithParams, inventoryConsumer));
				}
			}

			List<Element> filterConfigurationList = entityTableConfiguration
					.getChildren(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_FILTER);

			for (Element filterConfiguration : filterConfigurationList) {
				String filterName = filterConfiguration
						.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_NAME);
				if (typeStatementAndInfoRegistry
						.get(BDStatCommonFunction.buildEntityFilterTableName(filterName)) == null) {
					CreateTableParameter filterTableParameter = BDStatCommonFunction
							.createEntityFilterTableParameters(filterName);
					StatementWithColumnNameAdapter filterStatement = inventoryConsumer
							.createTableAndStatementByName(filterTableParameter, false, true);
					if (logger.isDebugEnabled()) {
						logger.debug("[exportH2Inventory] adding filter table " + filterTableParameter.tableName);
					}

					EntityTypeH2StatementAndInsertionInfo filterInfo = new EntityTypeH2StatementAndInsertionInfo();
					filterInfo.setStatement(filterStatement);
					filterInfo.setTablePk(0L);
					filterInfo.setIndexColumns(defaultcolumnsToEntityFilterTableIndex);
					typeStatementAndInfoRegistry.put(filterTableParameter.tableName, filterInfo);
				} else {
					throw new BusinessException(BusinessException.WRONG_PARAMETER_EXCEPTION
							+ " : multiple filter with same name " + filterName,
							BusinessException.WRONG_PARAMETER_EXCEPTION_CODE);
				}
			}
			StatementWithColumnNameAdapter statement = inventoryConsumer
					.createTableAndStatementByName(entityTableParameter, true, true);
			info.setStatement(statement);
			info.setTablePk(0L);
			info.setIndexColumns(columnsIndex);
			typeStatementAndInfoRegistry.put(entityTableParameter.tableName, info);

		}
		return typeStatementAndInfoRegistry;
	}

	private List<ColumnParameter> createReportingGroupTableColumns() {
		List<ColumnParameter> listColumns = new ArrayList<>();

		ColumnParameter reportingGroupRef = new ColumnParameter();
		reportingGroupRef.columnName = H2Table.ReportingGroup.COLUMN_REPORTING_GROUP_REF;
		reportingGroupRef.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter origin = new ColumnParameter();
		origin.columnName = H2Table.ReportingGroup.COLUMN_ORIGIN;
		origin.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter label = new ColumnParameter();
		label.columnName = H2Table.ReportingGroup.COLUMN_LABEL;
		label.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter creationDate = new ColumnParameter();
		creationDate.columnName = H2Table.ReportingGroup.COLUMN_CREATION_DATE;
		creationDate.columnType = JDBCH2Consumer.TIMESTAMP;

		ColumnParameter updateDate = new ColumnParameter();
		updateDate.columnName = H2Table.ReportingGroup.COLUMN_UPDATE_DATE;
		updateDate.columnType = JDBCH2Consumer.TIMESTAMP;

		listColumns.add(reportingGroupRef);
		listColumns.add(origin);
		listColumns.add(label);
		listColumns.add(creationDate);
		listColumns.add(updateDate);

		return listColumns;
	}

	private static List<ColumnParameter> createEntityTableColumns() {
		List<ColumnParameter> listColumns = new ArrayList<>();

		ColumnParameter id = new ColumnParameter();
		id.columnName = H2Table.Entity.COLUMN_ENTITY_ID;
		id.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;

		ColumnParameter type = new ColumnParameter();
		type.columnName = H2Table.Entity.COLUMN_ENTITY_TYPE;
		type.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter label = new ColumnParameter();
		label.columnName = H2Table.Entity.COLUMN_LABEL;
		label.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;

		ColumnParameter shortLabel = new ColumnParameter();
		shortLabel.columnName = H2Table.Entity.COLUMN_SHORT_LABEL;
		shortLabel.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;

		ColumnParameter creationDate = new ColumnParameter();
		creationDate.columnName = H2Table.Entity.COLUMN_CREATION_DATE;
		creationDate.columnType = JDBCH2Consumer.TIMESTAMP;

		ColumnParameter updateDate = new ColumnParameter();
		updateDate.columnName = H2Table.Entity.COLUMN_UPDATE_DATE;
		updateDate.columnType = JDBCH2Consumer.TIMESTAMP;

		ColumnParameter origin = new ColumnParameter();
		origin.columnName = H2Table.Entity.COLUMN_ORIGIN;
		origin.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter originReportingGroup = new ColumnParameter();
		originReportingGroup.columnName = H2Table.Entity.COLUMN_ORIGIN_REPORTING_GROUP;
		originReportingGroup.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter partitionNumber = new ColumnParameter();
		partitionNumber.columnName = H2Table.Entity.COLUMN_PARTITIONS;
		partitionNumber.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter belongsToReportingGroup = new ColumnParameter();
		belongsToReportingGroup.columnName = H2Table.Entity.COLUMN_BELONGS_TO_GROUP;
		belongsToReportingGroup.columnType = JDBCH2Consumer.BOOLEAN_TYPE;

		listColumns.add(id);
		listColumns.add(type);
		listColumns.add(label);
		listColumns.add(shortLabel);
		listColumns.add(creationDate);
		listColumns.add(updateDate);
		listColumns.add(origin);
		listColumns.add(originReportingGroup);
		listColumns.add(partitionNumber);
		listColumns.add(belongsToReportingGroup);

		return listColumns;
	}

	private Map<String, EntityTypeH2StatementAndInsertionInfo> createComplexLinkTableWithConfiguration(
			Element linkConfiguration, String targetEntity, Map<String, Set<String>> complexLinksWithParams,
			JDBCH2Consumer inventoryConsumer) throws TechnicalException, SQLException {
		String linkName = linkConfiguration.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_NAME);
		List<Element> attributeLinkConfigurationList = linkConfiguration
				.getChildren(ConfigurationXMLConstant.INVENTORYCONFIG_NODE_LINKATTRIBUTE);

		Map<String, EntityTypeH2StatementAndInsertionInfo> typeStatementAndInfoRegistry = new HashMap<>();
		CreateTableParameter linkTableParameter = new CreateTableParameter();
		linkTableParameter.tableName = BDStatCommonFunction.buildLinkTableName(linkName, targetEntity);

		ColumnParameter pkColumn = new ColumnParameter();
		pkColumn.columnName = BDStatCommonFunction.makePk(linkTableParameter.tableName);
		pkColumn.columnType = JDBCH2Consumer.BIGINT_TYPE;
		linkTableParameter.pkColumn = pkColumn;

		ColumnParameter destinationEntityId = new ColumnParameter();
		destinationEntityId.columnName = H2Table.Link.COLUMN_DESTINATION_ENTITY_ID;
		destinationEntityId.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		linkTableParameter.columns.add(destinationEntityId);

		ColumnParameter entityOrigin = new ColumnParameter();
		entityOrigin.columnName = H2Table.Link.COLUMN_ORIGIN;
		entityOrigin.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		linkTableParameter.columns.add(entityOrigin);

		ColumnParameter sourceEntityId = new ColumnParameter();
		sourceEntityId.columnName = H2Table.Link.COLUMN_SOURCE_ENTITY_ID;
		sourceEntityId.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		linkTableParameter.columns.add(sourceEntityId);

		ColumnParameter role = new ColumnParameter();
		role.columnName = H2Table.Link.COLUMN_ROLE;
		role.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		linkTableParameter.columns.add(role);

		ColumnParameter parameter = new ColumnParameter();
		parameter.columnName = H2Table.Link.COLUMN_PARAMETER;
		parameter.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		linkTableParameter.columns.add(parameter);

		EntityTypeH2StatementAndInsertionInfo info = new EntityTypeH2StatementAndInsertionInfo();

		// Add additional attributes defined for the link
		boolean hasParams = false;
		for (Element attributeLinkConfiguration : attributeLinkConfigurationList) {
			ColumnParameter attributeLink = new ColumnParameter();
			attributeLink.columnName = attributeLinkConfiguration
					.getAttributeValue(ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_NAME);
			attributeLink.columnType = attributeLinkConfiguration.getAttributeValue(
					ConfigurationXMLConstant.INVENTORYCONFIG_ATTRIBUTE_TYPE, JDBCH2Consumer.VARCHAR_TYPE);
			linkTableParameter.columns.add(attributeLink);

			UpdateStatementAdapter updateStatement = new UpdateStatementAdapter(inventoryConsumer.getConnection(),
					linkTableParameter.tableName);
			updateStatement.addColumn(attributeLink.columnName);
			updateStatement.addWhere(H2Table.Link.COLUMN_SOURCE_ENTITY_ID, false);
			updateStatement.addWhere(H2Table.Link.COLUMN_ORIGIN, false);
			updateStatement.addWhere(H2Table.Link.COLUMN_DESTINATION_ENTITY_ID, false);
			updateStatement.addWhere(H2Table.Link.COLUMN_ROLE, false);
			info.addUpdateStatement(attributeLink.columnName, updateStatement);

			hasParams = true;
		}
		if (hasParams == true) {
			Set<String> targetValues = complexLinksWithParams.get(linkName);
			if (targetValues == null) {
				Set<String> values = new HashSet<>();
				values.add(targetEntity);
				complexLinksWithParams.put(linkName, values);
			} else {
				targetValues.add(targetEntity);
			}
		}

		StatementWithColumnNameAdapter statement = inventoryConsumer.createTableAndStatementByName(linkTableParameter,
				true, true);
		if (logger.isDebugEnabled()) {
			logger.debug("[exportH2Inventory] adding complex link table " + linkTableParameter.tableName);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("[exportH2Inventory] updating complex link table " + linkTableParameter.tableName);
		}

		String[] indexedColumns = { H2Table.Link.COLUMN_SOURCE_ENTITY_ID, H2Table.Link.COLUMN_ORIGIN,
				H2Table.Link.COLUMN_DESTINATION_ENTITY_ID, H2Table.Link.COLUMN_ROLE };
		IndexStatementBuilder indexBuilder = new IndexStatementBuilder(inventoryConsumer.getConnection(),
				linkTableParameter.tableName, indexedColumns);
		indexBuilder.executeStatement();

		info.setStatement(statement);
		info.setTablePk(0L);
		info.setIndexColumns(defaultcolumnsToLinkTableIndex);
		typeStatementAndInfoRegistry.put(linkTableParameter.tableName, info);
		return typeStatementAndInfoRegistry;
	}

	private Map<String, EntityTypeH2StatementAndInsertionInfo> createAttributeListTable(String entityType,
			String attributeListName, List<String> parameterNameList, JDBCH2Consumer inventoryConsumer)
			throws TechnicalException, SQLException {
		Map<String, EntityTypeH2StatementAndInsertionInfo> typeStatementAndInfoRegistry = new HashMap<>();
		CreateTableParameter listTableParameter = new CreateTableParameter();
		listTableParameter.tableName = BDStatCommonFunction.buildListTableName(entityType,
				attributeListName.toUpperCase());

		if (logger.isDebugEnabled()) {
			logger.debug("[exportH2Inventory] creating table list " + listTableParameter.tableName);
		}

		ColumnParameter pkColumn = new ColumnParameter();
		pkColumn.columnName = BDStatCommonFunction.makePk(listTableParameter.tableName);
		pkColumn.columnType = JDBCH2Consumer.BIGINT_TYPE;
		listTableParameter.pkColumn = pkColumn;

		ColumnParameter entityFk = new ColumnParameter();
		entityFk.columnName = H2Table.AttributeList.COLUMN_ENTITY_FK;
		entityFk.columnType = JDBCH2Consumer.BIGINT_TYPE;
		listTableParameter.columns.add(entityFk);

		ColumnParameter entityTypeCol = new ColumnParameter();
		entityTypeCol.columnName = H2Table.AttributeList.COLUMN_ENTITY_TYPE;
		entityTypeCol.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		listTableParameter.columns.add(entityTypeCol);

		for (String parameterName : parameterNameList) {
			ColumnParameter column = new ColumnParameter();
			column.columnName = parameterName.toUpperCase();
			column.columnType = JDBCH2Consumer.VARCHAR_TYPE;
			listTableParameter.columns.add(column);
		}

		StatementWithColumnNameAdapter statement = inventoryConsumer.createTableAndStatementByName(listTableParameter,
				true, true);
		if (logger.isDebugEnabled()) {
			logger.debug("[exportH2Inventory] adding attribute list table " + listTableParameter.tableName);
		}

		EntityTypeH2StatementAndInsertionInfo listAttributeinfo = new EntityTypeH2StatementAndInsertionInfo();
		listAttributeinfo.setStatement(statement);
		listAttributeinfo.setTablePk(0L);
		typeStatementAndInfoRegistry.put(listTableParameter.tableName, listAttributeinfo);
		return typeStatementAndInfoRegistry;
	}

}
