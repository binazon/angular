package com.orange.srs.refreport.business.delegate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.orange.srs.refreport.consumer.dao.ReportingEntityDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.model.DataLocation;
import com.orange.srs.refreport.model.EntityAttribute;
import com.orange.srs.refreport.model.EntityAttributeList;
import com.orange.srs.refreport.model.EntityGroupAttribute;
import com.orange.srs.refreport.model.EntityGroupAttributeId;
import com.orange.srs.refreport.model.EntityLink;
import com.orange.srs.refreport.model.EntityLinkAttribute;
import com.orange.srs.refreport.model.EntityLinkAttributeId;
import com.orange.srs.refreport.model.EntityLinkId;
import com.orange.srs.refreport.model.FilterConfig;
import com.orange.srs.refreport.model.GroupAttribute;
import com.orange.srs.refreport.model.GroupAttributeId;
import com.orange.srs.refreport.model.GroupReportConfig;
import com.orange.srs.refreport.model.GroupingRule;
import com.orange.srs.refreport.model.GroupingRuleId;
import com.orange.srs.refreport.model.PartitionStatus;
import com.orange.srs.refreport.model.ReportUser;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.ReportingGroupPartitionStatus;
import com.orange.srs.refreport.model.ReportingGroupToEntities;
import com.orange.srs.refreport.model.ReportingGroupToEntitiesAutomatic;
import com.orange.srs.refreport.model.ReportsBookmark;
import com.orange.srs.refreport.model.TO.ProvisioningActionStatusTO;
import com.orange.srs.refreport.model.TO.provisioning.ExportEntitiesTO;
import com.orange.srs.refreport.model.TO.provisioning.ExportEntitiesTO.EntityTO;
import com.orange.srs.refreport.model.enumerate.ReportingGroupTypeEnum;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.TO.JobTO.JobSummaryTO;
import com.orange.srs.statcommon.model.constant.ProvisioningFileData;
import com.orange.srs.statcommon.model.enums.JobEventCriticityEnum;
import com.orange.srs.statcommon.model.enums.JobEventTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.DateHelper;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.concurrent.DefaultExecutorService;

@Stateless
public class ProvisioningDelegate {

	private static final Logger LOGGER = Logger.getLogger(ProvisioningDelegate.class);

	private static final char MYSQL_VAR_CHAR = '@';
	private static final int NB_BULK_DELETE_ENTITY_OBJECTS_LINK = 50;
	private static final int NB_BULK_UPDATE_ENTITY_DATE = 100;

	private static final int NB_OBJECTS_EXPORTED_FETCH_SIZE = 5000;

	private static final String DB_TMP_TABLE_ENTITIES = "T_TMP_ENTITIES_FOR_EXPORT";
	private static final String DB_TMP_TABLE_COL_NAME_REPK = "REPK";

	private static final List<String> FILE_GROUP_COL_NAMES = Lists.newArrayList(ReportingGroup.COL_NAME_PK,
			ReportingGroup.COL_NAME_CREATION_DATE, ReportingGroup.COL_NAME_LABEL, ReportingGroup.COL_NAME_LANGUAGE,
			ReportingGroup.COL_NAME_ORIGIN, ReportingGroup.COL_NAME_REPORTING_GROUP_REF, ReportingGroup.COL_NAME_SOURCE,
			ReportingGroup.COL_NAME_TIME_ZONE, ReportingGroup.COL_NAME_UPDATE_DATE,
			ReportingGroup.COL_NAME_DELETION_DATE);

	private static final List<String> FILE_GROUP_WITH_DATA_LOCATION_COL_NAMES = new ArrayList<>();
	static {
		FILE_GROUP_WITH_DATA_LOCATION_COL_NAMES.addAll(FILE_GROUP_COL_NAMES);
		FILE_GROUP_WITH_DATA_LOCATION_COL_NAMES.add(ReportingGroup.COL_NAME_DATA_LOCATION_FK);
	}

	private static final List<String> FILE_GROUP_ATTRIBUTE_COL_NAMES = Lists.newArrayList(
			GroupAttributeId.COL_NAME_REPORTING_GROUP_FK, GroupAttributeId.COL_NAME_NAME,
			GroupAttribute.COL_NAME_VALUE);

	private static final List<String> FILE_GROUP_RULE_COL_NAMES = Lists.newArrayList(
			GroupingRuleId.COL_NAME_REPORTING_GROUP_FK, GroupingRuleId.COL_NAME_GROUPING_RULE,
			GroupingRuleId.COL_NAME_GROUPING_VALUE);

	private static final List<String> FILE_ENTITY_COL_NAMES = Lists.newArrayList(ReportingEntity.COL_NAME_PK,
			ReportingEntity.COL_NAME_CREATION_DATE, ReportingEntity.COL_NAME_ENTITY_ID,
			ReportingEntity.COL_NAME_ENTITY_TYPE, ReportingEntity.COL_NAME_LABEL, ReportingEntity.COL_NAME_ORIGIN,
			ReportingEntity.COL_NAME_SHORT_LABEL, ReportingEntity.COL_NAME_SOURCE, ReportingEntity.COL_NAME_UPDATE_DATE,
			ReportingEntity.COL_NAME_PARENT);

	private static final List<String> FILE_ENTITY_WITH_PARTITION_COL_NAMES = new ArrayList<>();
	static {
		FILE_ENTITY_WITH_PARTITION_COL_NAMES.addAll(FILE_ENTITY_COL_NAMES);
		FILE_ENTITY_WITH_PARTITION_COL_NAMES.add(ReportingEntity.COL_NAME_PARTITION_NUMBER);
	}

	private static final List<String> FILE_ENTITY_ATTRIBUTE_COL_NAMES = Lists.newArrayList(
			EntityAttribute.COL_NAME_REPORTING_ENTITY_FK, EntityAttribute.COL_NAME_NAME,
			EntityAttribute.COL_NAME_VALUE);

	private static final List<String> FILE_ENTITY_TO_TYPE_COL_NAMES = Lists.newArrayList(
			ReportingEntity.TJ_COL_NAME_REPORTING_ENTITY_FK, ReportingEntity.TJ_COL_NAME_TYPE,
			ReportingEntity.TJ_COL_NAME_SUBTYPE);

	private static final List<String> FILE_ENTITY_LINK_COL_NAMES = Lists.newArrayList(
			EntityLinkId.COL_NAME_REPORTING_ENTITY_SRC_FK, EntityLinkId.COL_NAME_REPORTING_ENTITY_DEST_FK,
			EntityLink.COL_NAME_TYPE, EntityLink.COL_NAME_PARAMETER, EntityLinkId.COL_NAME_ROLE);

	private static final List<String> FILE_ENTITY_LINK_ATTRIBUTE_COL_NAMES = Lists.newArrayList(
			EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_SRC_FK,
			EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_DEST_FK, EntityLinkAttributeId.COL_NAME_ROLE,
			EntityLinkAttributeId.COL_NAME_NAME, EntityLinkAttribute.COL_NAME_VALUE);

	private static final List<String> FILE_GROUP_TO_ENTITY_COL_NAMES = Lists.newArrayList(
			ReportingGroupToEntities.COL_NAME_REPORTING_ENTITY_FK, ReportingGroupToEntities.COL_NAME_REPORTING_GROUP_FK,
			ReportingGroupToEntities.COL_NAME_BELONGS_TO);

	private static final Map<String, LoadInfileInfo> LOAD_INFILE_INFO = new HashMap<>();
	static {

		// group.txt file
		List<String> groupTimestampColNames = Lists.newArrayList(ReportingGroup.COL_NAME_CREATION_DATE,
				ReportingGroup.COL_NAME_UPDATE_DATE, ReportingGroup.COL_NAME_DELETION_DATE);
		Map<String, Object> groupConstantValueByColName = new HashMap<>();
		groupConstantValueByColName.put(ReportingGroup.COL_NAME_REPORTING_GROUP_TYPE,
				ReportingGroup.DEFAULT_VALUE_REPORTING_GROUP_TYPE);
		groupConstantValueByColName.put(ReportingGroup.COL_NAME_TYPE, ReportingGroupTypeEnum.AUTOMATIC);
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_GROUP,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_GROUP, ReportingGroup.TABLE_NAME, FILE_GROUP_COL_NAMES,
						groupTimestampColNames, Collections.EMPTY_LIST, groupConstantValueByColName, true));

		// groupWithDataLocation.txt file
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_GROUP_WITH_DATA_LOCATION,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_GROUP_WITH_DATA_LOCATION, ReportingGroup.TABLE_NAME,
						FILE_GROUP_WITH_DATA_LOCATION_COL_NAMES, groupTimestampColNames, Collections.EMPTY_LIST,
						groupConstantValueByColName, true));

		// groupAttribute.txt file
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_GROUP_ATTRIBUTE,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_GROUP_ATTRIBUTE, GroupAttribute.TABLE_NAME,
						FILE_GROUP_ATTRIBUTE_COL_NAMES, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
						Collections.EMPTY_MAP, true));

		// groupRule.txt file
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_GROUP_RULE,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_GROUP_RULE, GroupingRule.TABLE_NAME,
						FILE_GROUP_RULE_COL_NAMES, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
						Collections.EMPTY_MAP, true));

		// entity.txt file
		List<String> entityTimestampColNames = Lists.newArrayList(ReportingEntity.COL_NAME_CREATION_DATE,
				ReportingEntity.COL_NAME_UPDATE_DATE);
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_ENTITY,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_ENTITY, ReportingEntity.TABLE_NAME,
						FILE_ENTITY_COL_NAMES, entityTimestampColNames, Collections.EMPTY_LIST, Collections.EMPTY_MAP,
						true));

		// entityWithPartition.txt file
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_ENTITY_WITH_PARTITION,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_ENTITY_WITH_PARTITION, ReportingEntity.TABLE_NAME,
						FILE_ENTITY_WITH_PARTITION_COL_NAMES, entityTimestampColNames, Collections.EMPTY_LIST,
						Collections.EMPTY_MAP, true));

		// entityGroupingAttribute.txt file
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_ENTITY_GROUPING_ATTRIBUTE,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_ENTITY_GROUPING_ATTRIBUTE, EntityAttribute.TABLE_NAME,
						FILE_ENTITY_ATTRIBUTE_COL_NAMES, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
						Collections.EMPTY_MAP, false));

		// entityAttribute.txt file
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_ENTITY_ATTRIBUTE,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_ENTITY_ATTRIBUTE, EntityAttribute.TABLE_NAME,
						FILE_ENTITY_ATTRIBUTE_COL_NAMES, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
						Collections.EMPTY_MAP, false));

		// entityToType.txt file
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_ENTITY_TO_TYPE,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_ENTITY_TO_TYPE, ReportingEntity.TJ_NAME_SUBTYPES,
						FILE_ENTITY_TO_TYPE_COL_NAMES, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
						Collections.EMPTY_MAP, false));

		// entityLink.txt file
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_ENTITY_LINK,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_ENTITY_LINK, EntityLink.TABLE_NAME,
						FILE_ENTITY_LINK_COL_NAMES, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
						Collections.EMPTY_MAP, false));

		// entityLinkAttribute.txt file
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_ENTITY_LINK_ATTRIBUTE,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_ENTITY_LINK_ATTRIBUTE, EntityLinkAttribute.TABLE_NAME,
						FILE_ENTITY_LINK_ATTRIBUTE_COL_NAMES, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
						Collections.EMPTY_MAP, false));

		// groupToEntity.txt file
		List<String> groupToEntityBooleanColNames = Lists.newArrayList(ReportingGroupToEntities.COL_NAME_BELONGS_TO);
		Map<String, Object> groupToEntitiesConstantValueByColName = new HashMap<>();
		groupToEntitiesConstantValueByColName.put(ReportingGroupToEntities.COL_NAME_TYPE,
				ReportingGroupToEntitiesAutomatic.VALUE_REPORTING_GROUP_TO_ENTITIES_TYPE);
		LOAD_INFILE_INFO.put(ProvisioningFileData.FILENAME_GROUP_TO_ENTITY,
				new LoadInfileInfo(ProvisioningFileData.FILENAME_GROUP_TO_ENTITY, ReportingGroupToEntities.TABLE_NAME,
						FILE_GROUP_TO_ENTITY_COL_NAMES, Collections.EMPTY_LIST, groupToEntityBooleanColNames,
						groupToEntitiesConstantValueByColName, false));

	}

	private static class LoadInfileInfo {

		private final String fileName;
		private final String tableName;
		private final List<String> colNames;
		private final List<String> timestampColNames;
		private final List<String> booleanColNames;
		private final Map<String, Object> constantValueByColName;
		private final boolean replace;

		public LoadInfileInfo(String fileName, String tableName, List<String> colNames, List<String> timestampColNames,
				List<String> booleanColNames, Map<String, Object> constantValueByColName, boolean replace) {
			this.fileName = fileName;
			this.tableName = tableName;
			this.colNames = colNames;
			this.timestampColNames = timestampColNames;
			this.booleanColNames = booleanColNames;
			this.constantValueByColName = constantValueByColName;
			this.replace = replace;
		}
	}

	private class ProvisioningContext {

		private final SOAContext soaContext;
		private final ProvisioningActionStatusTO actionStatusTO;
		private final JobSummaryTO jobSummaryTO;
		private final Connection refReportConnection;
		private final boolean provisioningDiff;

		public ProvisioningContext(SOAContext soaContext, JobSummaryTO jobSummaryTO,
				ProvisioningActionStatusTO actionStatusTO, Connection refReportConnection, boolean provisioningDiff) {
			this.soaContext = soaContext;
			this.jobSummaryTO = jobSummaryTO;
			this.actionStatusTO = actionStatusTO;
			this.refReportConnection = refReportConnection;
			this.provisioningDiff = provisioningDiff;
		}
	}

	@Resource(lookup = "dataSourceRefReport")
	private DataSource refReportDataSource;

	@EJB
	private ReportingEntityDAO reportingEntityDao;

	@EJB
	private ReportingGroupDAO reportingGroupDao;

	@EJB
	private DefaultExecutorService executorService;

	@Deprecated
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void migrateRefReportReportingGroups(SOAContext soaContext, boolean simulateMigration) throws SQLException {

		// Get the last reporting entity pk
		Long reportingEntityPk = reportingEntityDao.findLastPk();

		// Get matching between 'new' and 'old' pk for all reporting groups
		List<ReportingGroup> reportingGroups = reportingGroupDao.findAll();
		long currentNewReportingGroupPk = reportingEntityPk++;
		Map<Long, Long> newPkByOldPk = new HashMap<>();
		for (ReportingGroup reportingGroup : reportingGroups) {
			newPkByOldPk.put(reportingGroup.getPk(), currentNewReportingGroupPk);
			currentNewReportingGroupPk++;
		}

		// Doing migration
		try (Connection refReportConnection = refReportDataSource.getConnection()) {

			try (Statement statement = refReportConnection.createStatement()) {

				statement.addBatch("SET FOREIGN_KEY_CHECKS=0");

				for (Entry<Long, Long> entry : newPkByOldPk.entrySet()) {

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Updating reporting group pk from " + entry.getKey() + " to " + entry.getValue());
					}

					if (!simulateMigration) {

						statement.addBatch("UPDATE " + FilterConfig.TABLE_NAME + " SET "
								+ FilterConfig.COL_NAME_REPORTING_GROUP + '=' + entry.getValue() + " WHERE "
								+ FilterConfig.COL_NAME_REPORTING_GROUP + '=' + entry.getKey());

						statement.addBatch("UPDATE " + GroupAttribute.TABLE_NAME + " SET "
								+ GroupAttributeId.COL_NAME_REPORTING_GROUP_FK + '=' + entry.getValue() + " WHERE "
								+ GroupAttributeId.COL_NAME_REPORTING_GROUP_FK + '=' + entry.getKey());

						statement.addBatch("UPDATE " + ReportingGroupPartitionStatus.TABLE_NAME + " SET "
								+ ReportingGroupPartitionStatus.COL_NAME_REPORTING_GROUP + '=' + entry.getValue()
								+ " WHERE " + ReportingGroupPartitionStatus.COL_NAME_REPORTING_GROUP + '='
								+ entry.getKey());

						statement.addBatch("UPDATE " + GroupReportConfig.TABLE_NAME + " SET "
								+ GroupReportConfig.COL_NAME_REPORTING_GROUP + '=' + entry.getValue() + " WHERE "
								+ GroupReportConfig.COL_NAME_REPORTING_GROUP + '=' + entry.getKey());

						statement.addBatch("UPDATE " + ReportUser.TJ_NAME_REPORTING_GROUP + " SET "
								+ ReportUser.TJ_COL_NAME_REPORTING_GROUP + '=' + entry.getValue() + " WHERE "
								+ ReportUser.TJ_COL_NAME_REPORTING_GROUP + '=' + entry.getKey());

						statement.addBatch("UPDATE " + ReportingGroup.TJ_NAME_CRITERIA + " SET "
								+ ReportingGroup.TJ_COL_NAME_REPORTING_GROUP + '=' + entry.getValue() + " WHERE "
								+ ReportingGroup.TJ_COL_NAME_REPORTING_GROUP + '=' + entry.getKey());

						statement.addBatch("UPDATE " + ReportingGroupToEntities.TABLE_NAME + " SET "
								+ ReportingGroupToEntities.COL_NAME_REPORTING_GROUP_FK + '=' + entry.getValue()
								+ " WHERE " + ReportingGroupToEntities.COL_NAME_REPORTING_GROUP_FK + '='
								+ entry.getKey());

						statement.addBatch("UPDATE " + ReportingGroup.TJ_NAME_OFFER_OPTION + " SET "
								+ ReportingGroup.TJ_COL_NAME_REPORTING_GROUP + '=' + entry.getValue() + " WHERE "
								+ ReportingGroup.TJ_COL_NAME_REPORTING_GROUP + '=' + entry.getKey());

						statement.addBatch("UPDATE " + ReportingGroup.TABLE_NAME + " SET " + ReportingGroup.COL_NAME_PK
								+ '=' + entry.getValue() + " WHERE " + ReportingGroup.COL_NAME_PK + '='
								+ entry.getKey());

						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("Executing batchs");
						}
						statement.executeBatch();
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("Batchs executed");
						}
					}
				}

				statement.addBatch("SET FOREIGN_KEY_CHECKS=1");
				statement.executeBatch();

				LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Migration done successfully"));

			} catch (SQLException ex) {
				LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Error during reporting group migration", ex));
				if (refReportConnection != null) {
					try {
						refReportConnection.rollback();
					} catch (SQLException sqlE) {
						LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Error during rollback"), sqlE);
					}
				}
				throw ex;
			}

		}

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void exportObjects(SOAContext soaContext, ExportEntitiesTO entitiesTO) throws BusinessException {
		exportGroupsAndRelatedObjects(soaContext);
		exportEntitiesAndLinkedObjects(soaContext, entitiesTO);
	}

	private void createTempEntitiesTable(SOAContext soaContext, Statement statement, ExportEntitiesTO entitiesTO) {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Creating temp entities table"));
		try {

			long startCreateTable = Utils.getTime();
			StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ").append(DB_TMP_TABLE_ENTITIES);
			boolean first = true;
			for (EntityTO entityTO : entitiesTO.entities) {
				if (first) {
					createTableQuery.append(" AS SELECT DISTINCT t.")
							.append(ReportingEntity.TJ_COL_NAME_REPORTING_ENTITY_FK).append(" AS ")
							.append(DB_TMP_TABLE_COL_NAME_REPK);
					first = false;
				} else {
					createTableQuery.append(" UNION ALL SELECT DISTINCT t.")
							.append(ReportingEntity.TJ_COL_NAME_REPORTING_ENTITY_FK);
				}
				createTableQuery.append(" FROM ").append(ReportingEntity.TJ_NAME_SUBTYPES).append(" AS t");
				if (entityTO.origin != null) {
					createTableQuery.append(" JOIN ").append(ReportingEntity.TABLE_NAME).append(" AS re");
					createTableQuery.append(" ON re.").append(ReportingEntity.FIELD_PK).append("=t.")
							.append(ReportingEntity.TJ_COL_NAME_REPORTING_ENTITY_FK);
					createTableQuery.append(" AND re.").append(ReportingEntity.COL_NAME_ORIGIN).append("='")
							.append(entityTO.origin).append("'");
				}
				createTableQuery.append(" WHERE t.").append(ReportingEntity.TJ_COL_NAME_TYPE).append("='")
						.append(entityTO.type).append("'");
				if (entityTO.subtype != null) {
					createTableQuery.append(" AND t.").append(ReportingEntity.TJ_COL_NAME_SUBTYPE).append("='")
							.append(entityTO.subtype).append("'");
				}
			}

			int resultCreateTable = statement.executeUpdate(createTableQuery.toString());
			switch (resultCreateTable) {
			case Statement.EXECUTE_FAILED:
				String errorMsg = "Error creating temp table " + DB_TMP_TABLE_ENTITIES;
				LOGGER.error(SOATools.buildSOALogMessage(soaContext, errorMsg));
				break;
			default:
				String successMsg = "Temp table " + DB_TMP_TABLE_ENTITIES + " created in "
						+ (Utils.getTime() - startCreateTable) + " ms";
				LOGGER.info(SOATools.buildSOALogMessage(soaContext, successMsg));
				break;
			}

			long startAddPrimaryKey = Utils.getTime();
			String createPrimaryKeyQuery = "ALTER TABLE " + DB_TMP_TABLE_ENTITIES + " ADD PRIMARY KEY("
					+ DB_TMP_TABLE_COL_NAME_REPK + ")";
			int resultPrimaryKey = statement.executeUpdate(createPrimaryKeyQuery);
			switch (resultPrimaryKey) {
			case Statement.EXECUTE_FAILED:
				String errorMsg = "Error adding primary key on column " + DB_TMP_TABLE_COL_NAME_REPK;
				LOGGER.error(SOATools.buildSOALogMessage(soaContext, errorMsg));
				break;
			default:
				String successMsg = "Primary key " + DB_TMP_TABLE_COL_NAME_REPK + " added in "
						+ (Utils.getTime() - startAddPrimaryKey) + " ms";
				LOGGER.info(SOATools.buildSOALogMessage(soaContext, successMsg));
				break;
			}

		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext,
					"Error creating temporary table " + DB_TMP_TABLE_ENTITIES, e));
		}

	}

	private void dropTempEntitiesTable(SOAContext soaContext, Statement statement) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Deleting temp entities table"));
		try {

			long startDeleteTable = Utils.getTime();
			StringBuilder deleteQuery = new StringBuilder("DROP TABLE IF EXISTS ").append(DB_TMP_TABLE_ENTITIES);

			int resultDeleteUpdate = statement.executeUpdate(deleteQuery.toString());
			switch (resultDeleteUpdate) {
			case Statement.EXECUTE_FAILED:
				String errorMsg = "Error deleting table " + DB_TMP_TABLE_ENTITIES;
				LOGGER.error(SOATools.buildSOALogMessage(soaContext, errorMsg));
				break;
			default:
				String successMsg = "Table deleted " + DB_TMP_TABLE_ENTITIES + " in "
						+ (Utils.getTime() - startDeleteTable) + " ms";
				LOGGER.info(SOATools.buildSOALogMessage(soaContext, successMsg));
				break;
			}
		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext,
					"Error deleting temporary table " + DB_TMP_TABLE_ENTITIES, e));
		}
	}

	private void exportGroupsAndRelatedObjects(SOAContext soaContext) throws BusinessException {

		long start = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting reporting groups and related objects"));

		try (Connection refReportConnection = refReportDataSource.getConnection();
				Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {

			statement.setFetchSize(NB_OBJECTS_EXPORTED_FETCH_SIZE);

			exportGroups(soaContext, statement);
			exportGroupAttributes(soaContext, statement);
			exportGroupRules(soaContext, statement);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"Reporting group and related objects exported in " + (Utils.getTime() - start) + " ms"));

		} catch (Exception e) {
			throw new BusinessException("Error exporting reporting group and related objects", e);
		}

	}

	private void exportGroups(SOAContext soaContext, Statement statement) throws IOException, SQLException {

		long rgStartTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting reporting groups"));

		long nbRgExported = 0;
		Path groupPathTmp = Paths.get(Configuration.tmpDirectory, ProvisioningFileData.FILENAME_GROUP_OLD);

		try (BufferedWriter groupWriter = Utils.newBufferedWriter(groupPathTmp, ProvisioningFileData.DATA_CHARSET,
				ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			long stepStartTime = Utils.getTime();
			String exportGroupQuery = "SELECT " + StringUtils.join(FILE_GROUP_COL_NAMES, ", ") + //
					" FROM " + ReportingGroup.TABLE_NAME;
			try (ResultSet resultSet = statement.executeQuery(exportGroupQuery)) {

				while (resultSet.next()) {
					groupWriter.append(getStringLong(resultSet, ReportingGroup.COL_NAME_PK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupWriter.append(getStringDateTimeInMillisecond(resultSet, ReportingGroup.COL_NAME_CREATION_DATE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupWriter.append(getString(resultSet, ReportingGroup.COL_NAME_LABEL))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupWriter.append(getString(resultSet, ReportingGroup.COL_NAME_LANGUAGE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupWriter.append(getString(resultSet, ReportingGroup.COL_NAME_ORIGIN))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupWriter.append(getString(resultSet, ReportingGroup.COL_NAME_REPORTING_GROUP_REF))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupWriter.append(getString(resultSet, ReportingGroup.COL_NAME_SOURCE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupWriter.append(getString(resultSet, ReportingGroup.COL_NAME_TIME_ZONE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupWriter.append(getStringDateTimeInMillisecond(resultSet, ReportingGroup.COL_NAME_UPDATE_DATE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupWriter
							.append(getStringDateTimeInMillisecond(resultSet, ReportingGroup.COL_NAME_DELETION_DATE));
					groupWriter.newLine();
					nbRgExported++;
					if (LOGGER.isDebugEnabled() && (nbRgExported % NB_OBJECTS_EXPORTED_FETCH_SIZE == 0)) {
						LOGGER.debug("Process " + NB_OBJECTS_EXPORTED_FETCH_SIZE + " more reporting groups (Up to "
								+ nbRgExported + ") in " + (Utils.getTime() - stepStartTime) + " ms.");
						stepStartTime = Utils.getTime();
					}
				}
			}
		}

		Files.move(groupPathTmp, Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_GROUP_OLD),
				StandardCopyOption.REPLACE_EXISTING);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				nbRgExported + " reporting groups exported in " + (Utils.getTime() - rgStartTime) + " ms."));

	}

	private void exportGroupAttributes(SOAContext soaContext, Statement statement) throws IOException, SQLException {

		long rgAttributeStartTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting reporting group attributes"));

		long nbRgAttributeExported = 0;
		Path groupAttributePathTmp = Paths.get(Configuration.tmpDirectory,
				ProvisioningFileData.FILENAME_GROUP_ATTRIBUTE_OLD);

		try (BufferedWriter groupAtributeWriter = Utils.newBufferedWriter(groupAttributePathTmp,
				ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			long stepStartTime = Utils.getTime();
			String exportGroupAttributeQuery = "SELECT " + StringUtils.join(FILE_GROUP_ATTRIBUTE_COL_NAMES, ", ") + //
					" FROM " + GroupAttribute.TABLE_NAME;
			try (ResultSet resultSet = statement.executeQuery(exportGroupAttributeQuery)) {

				while (resultSet.next()) {
					groupAtributeWriter.append(getStringLong(resultSet, GroupAttributeId.COL_NAME_REPORTING_GROUP_FK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupAtributeWriter.append(getString(resultSet, GroupAttributeId.COL_NAME_NAME))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupAtributeWriter.append(getString(resultSet, GroupAttribute.COL_NAME_VALUE));
					groupAtributeWriter.newLine();
					nbRgAttributeExported++;
					if (LOGGER.isDebugEnabled() && (nbRgAttributeExported % NB_OBJECTS_EXPORTED_FETCH_SIZE == 0)) {
						LOGGER.debug(
								"Process " + NB_OBJECTS_EXPORTED_FETCH_SIZE + " more reporting group attributes (Up to "
										+ nbRgAttributeExported + ") in " + (Utils.getTime() - stepStartTime) + " ms.");
						stepStartTime = Utils.getTime();
					}
				}
			}
		}

		Files.move(groupAttributePathTmp,
				Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_GROUP_ATTRIBUTE_OLD),
				StandardCopyOption.REPLACE_EXISTING);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbRgAttributeExported
				+ " reporting groups attributes exported in " + (Utils.getTime() - rgAttributeStartTime) + " ms."));

	}

	private void exportGroupRules(SOAContext soaContext, Statement statement) throws IOException, SQLException {

		long rgRuleStartTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting reporting group rules"));

		long nbRgRuleExported = 0;
		Path groupRulePathTmp = Paths.get(Configuration.tmpDirectory, ProvisioningFileData.FILENAME_GROUP_RULE_OLD);

		try (BufferedWriter groupRuleWriter = Utils.newBufferedWriter(groupRulePathTmp,
				ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			long stepStartTime = Utils.getTime();
			String exportGroupingRuleQuery = "SELECT " + StringUtils.join(FILE_GROUP_RULE_COL_NAMES, ", ") + //
					" FROM " + GroupingRule.TABLE_NAME;
			try (ResultSet resultSet = statement.executeQuery(exportGroupingRuleQuery)) {

				while (resultSet.next()) {
					groupRuleWriter.append(getStringLong(resultSet, GroupAttributeId.COL_NAME_REPORTING_GROUP_FK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupRuleWriter.append(getString(resultSet, GroupingRuleId.COL_NAME_GROUPING_RULE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupRuleWriter.append(getString(resultSet, GroupingRuleId.COL_NAME_GROUPING_VALUE));
					groupRuleWriter.newLine();
					nbRgRuleExported++;
					if (LOGGER.isDebugEnabled() && (nbRgRuleExported % NB_OBJECTS_EXPORTED_FETCH_SIZE == 0)) {
						LOGGER.debug("Process " + NB_OBJECTS_EXPORTED_FETCH_SIZE + " more reporting group rules (Up to "
								+ nbRgRuleExported + ") in " + (Utils.getTime() - stepStartTime) + " ms.");
						stepStartTime = Utils.getTime();
					}
				}
			}
		}

		Files.move(groupRulePathTmp,
				Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_GROUP_RULE_OLD),
				StandardCopyOption.REPLACE_EXISTING);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbRgRuleExported + " reporting groups rules exported in "
				+ (Utils.getTime() - rgRuleStartTime) + " ms."));

	}

	private void exportEntitiesAndLinkedObjects(SOAContext soaContext, ExportEntitiesTO entitiesTO)
			throws BusinessException {

		long start = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting entities and linked objects"));

		try (Connection refReportConnection = refReportDataSource.getConnection();
				Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {

			statement.setFetchSize(NB_OBJECTS_EXPORTED_FETCH_SIZE);

			dropTempEntitiesTable(soaContext, statement);
			createTempEntitiesTable(soaContext, statement, entitiesTO); // Temp table used in next methods
			exportEntities(soaContext, statement);
			exportEntityAttributes(soaContext, statement);
			exportEntityToType(soaContext, statement);
			exportEntityLink(soaContext, statement);
			exportEntityLinkAttributes(soaContext, statement);
			exportGroupToEntityLinks(soaContext, statement);
			dropTempEntitiesTable(soaContext, statement);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"Entities and linked exported in " + (Utils.getTime() - start) + " ms"));

		} catch (Exception e) {
			throw new BusinessException("Error exporting entities and linked objects", e);
		}

	}

	private void exportEntities(SOAContext soaContext, Statement statement) throws IOException, SQLException {

		long entityStartTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting entities"));

		long nbEntityExported = 0;
		Path entityPathTmp = Paths.get(Configuration.tmpDirectory, ProvisioningFileData.FILENAME_ENTITY_OLD);

		try (BufferedWriter entityWriter = Utils.newBufferedWriter(entityPathTmp, ProvisioningFileData.DATA_CHARSET,
				ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			long stepStartTime = Utils.getTime();
			String exportEntityQuery = "SELECT re." + StringUtils.join(FILE_ENTITY_COL_NAMES, ", re.") + //
					" FROM " + DB_TMP_TABLE_ENTITIES + " tmp" + //
					" JOIN " + ReportingEntity.TABLE_NAME + " re on re." + ReportingEntity.COL_NAME_PK + "=tmp."
					+ DB_TMP_TABLE_COL_NAME_REPK;
			try (ResultSet resultSet = statement.executeQuery(exportEntityQuery)) {

				while (resultSet.next()) {
					entityWriter.append(getStringLong(resultSet, ReportingEntity.COL_NAME_PK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityWriter
							.append(getStringDateTimeInMillisecond(resultSet, ReportingEntity.COL_NAME_CREATION_DATE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityWriter.append(getString(resultSet, ReportingEntity.COL_NAME_ENTITY_ID))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityWriter.append(getString(resultSet, ReportingEntity.COL_NAME_ENTITY_TYPE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityWriter.append(getString(resultSet, ReportingEntity.COL_NAME_LABEL))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityWriter.append(getString(resultSet, ReportingEntity.COL_NAME_ORIGIN))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityWriter.append(getString(resultSet, ReportingEntity.COL_NAME_SHORT_LABEL))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityWriter.append(getString(resultSet, ReportingEntity.COL_NAME_SOURCE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityWriter.append(getStringDateTimeInMillisecond(resultSet, ReportingEntity.COL_NAME_UPDATE_DATE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityWriter.append(getStringLong(resultSet, ReportingEntity.COL_NAME_PARENT));
					entityWriter.newLine();
					nbEntityExported++;
					if (LOGGER.isDebugEnabled() && (nbEntityExported % NB_OBJECTS_EXPORTED_FETCH_SIZE == 0)) {
						LOGGER.debug("Process " + NB_OBJECTS_EXPORTED_FETCH_SIZE + " more entities (Up to "
								+ nbEntityExported + ") in " + (Utils.getTime() - stepStartTime) + " ms.");
						stepStartTime = Utils.getTime();
					}
				}
			}
		}

		Files.move(entityPathTmp, Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_ENTITY_OLD),
				StandardCopyOption.REPLACE_EXISTING);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				nbEntityExported + " entities exported in " + (Utils.getTime() - entityStartTime) + " ms."));

	}

	private void exportEntityAttributes(SOAContext soaContext, Statement statement) throws IOException, SQLException {

		long entityAttributeStartTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting entity attributes"));

		long nbEntityAttributeExported = 0;
		Path entityAttributePathTmp = Paths.get(Configuration.tmpDirectory,
				ProvisioningFileData.FILENAME_ENTITY_ATTRIBUTE_OLD);

		try (BufferedWriter entityAttributeWriter = Utils.newBufferedWriter(entityAttributePathTmp,
				ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			long stepStartTime = Utils.getTime();
			String exportEntityAttributeQuery = "SELECT a." + StringUtils.join(FILE_ENTITY_ATTRIBUTE_COL_NAMES, ", a.")
					+ //
					" FROM " + DB_TMP_TABLE_ENTITIES + " tmp" + //
					" JOIN " + EntityAttribute.TABLE_NAME + " a on a." + EntityAttribute.COL_NAME_REPORTING_ENTITY_FK
					+ "=tmp." + DB_TMP_TABLE_COL_NAME_REPK;
			try (ResultSet resultSet = statement.executeQuery(exportEntityAttributeQuery)) {

				while (resultSet.next()) {
					entityAttributeWriter.append(getStringLong(resultSet, EntityAttribute.COL_NAME_REPORTING_ENTITY_FK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityAttributeWriter.append(getString(resultSet, EntityAttribute.COL_NAME_NAME))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityAttributeWriter.append(getString(resultSet, EntityAttribute.COL_NAME_VALUE));
					entityAttributeWriter.newLine();
					nbEntityAttributeExported++;
					if (LOGGER.isDebugEnabled() && (nbEntityAttributeExported % NB_OBJECTS_EXPORTED_FETCH_SIZE == 0)) {
						LOGGER.debug("Process " + NB_OBJECTS_EXPORTED_FETCH_SIZE + " more entity attributes (Up to "
								+ nbEntityAttributeExported + ") in " + (Utils.getTime() - stepStartTime) + " ms.");
						stepStartTime = Utils.getTime();
					}
				}
			}
		}

		Files.move(entityAttributePathTmp,
				Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_ENTITY_ATTRIBUTE_OLD),
				StandardCopyOption.REPLACE_EXISTING);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbEntityAttributeExported
				+ " entity attributes exported in " + (Utils.getTime() - entityAttributeStartTime) + " ms."));

	}

	private void exportEntityToType(SOAContext soaContext, Statement statement) throws IOException, SQLException {

		long entityToTypeStartTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting entity to type"));

		long nbEntityToTypeExported = 0;
		Path entityToTypePathTmp = Paths.get(Configuration.tmpDirectory,
				ProvisioningFileData.FILENAME_ENTITY_TO_TYPE_OLD);

		try (BufferedWriter entityToTypeWriter = Utils.newBufferedWriter(entityToTypePathTmp,
				ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			long stepStartTime = Utils.getTime();
			String exportEntityToTypeQuery = "SELECT et." + StringUtils.join(FILE_ENTITY_TO_TYPE_COL_NAMES, ", et.") + //
					" FROM " + DB_TMP_TABLE_ENTITIES + " tmp" + //
					" JOIN " + ReportingEntity.TJ_NAME_SUBTYPES + " et on et."
					+ ReportingEntity.TJ_COL_NAME_REPORTING_ENTITY_FK + "=tmp." + DB_TMP_TABLE_COL_NAME_REPK;
			try (ResultSet resultSet = statement.executeQuery(exportEntityToTypeQuery)) {

				while (resultSet.next()) {
					entityToTypeWriter.append(getStringLong(resultSet, ReportingEntity.TJ_COL_NAME_REPORTING_ENTITY_FK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityToTypeWriter.append(getString(resultSet, ReportingEntity.TJ_COL_NAME_TYPE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityToTypeWriter.append(getString(resultSet, ReportingEntity.TJ_COL_NAME_SUBTYPE));
					entityToTypeWriter.newLine();
					nbEntityToTypeExported++;
					if (LOGGER.isDebugEnabled() && (nbEntityToTypeExported % NB_OBJECTS_EXPORTED_FETCH_SIZE == 0)) {
						LOGGER.debug("Process " + NB_OBJECTS_EXPORTED_FETCH_SIZE + " more entity to type (Up to "
								+ nbEntityToTypeExported + ") in " + (Utils.getTime() - stepStartTime) + " ms.");
						stepStartTime = Utils.getTime();
					}
				}
			}
		}
		Files.move(entityToTypePathTmp,
				Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_ENTITY_TO_TYPE_OLD),
				StandardCopyOption.REPLACE_EXISTING);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbEntityToTypeExported + " entity to type exported in "
				+ (Utils.getTime() - entityToTypeStartTime) + " ms."));

	}

	private void exportEntityLink(SOAContext soaContext, Statement statement) throws IOException, SQLException {

		long entityLinkStartTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting entity links"));

		long nbEntityLinkExported = 0;
		Path entityLinkPathTmp = Paths.get(Configuration.tmpDirectory, ProvisioningFileData.FILENAME_ENTITY_LINK_OLD);

		try (BufferedWriter entityLinkWriter = Utils.newBufferedWriter(entityLinkPathTmp,
				ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			long stepStartTime = Utils.getTime();
			String exportLinkQuery = "SELECT el." + StringUtils.join(FILE_ENTITY_LINK_COL_NAMES, ", el.") + //
					" FROM " + DB_TMP_TABLE_ENTITIES + " tmp" + //
					" JOIN " + EntityLink.TABLE_NAME + " el on el." + EntityLinkId.COL_NAME_REPORTING_ENTITY_DEST_FK
					+ "=tmp." + DB_TMP_TABLE_COL_NAME_REPK + //
					" JOIN " + DB_TMP_TABLE_ENTITIES + " tmp2 on tmp2." + DB_TMP_TABLE_COL_NAME_REPK + "=el."
					+ EntityLinkId.COL_NAME_REPORTING_ENTITY_SRC_FK;
			try (ResultSet resultSet = statement.executeQuery(exportLinkQuery)) {

				while (resultSet.next()) {
					entityLinkWriter.append(getStringLong(resultSet, EntityLinkId.COL_NAME_REPORTING_ENTITY_SRC_FK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityLinkWriter.append(getStringLong(resultSet, EntityLinkId.COL_NAME_REPORTING_ENTITY_DEST_FK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityLinkWriter.append(getString(resultSet, EntityLink.COL_NAME_TYPE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityLinkWriter.append(getString(resultSet, EntityLink.COL_NAME_PARAMETER))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityLinkWriter.append(getString(resultSet, EntityLinkId.COL_NAME_ROLE));
					entityLinkWriter.newLine();
					nbEntityLinkExported++;
					if (LOGGER.isDebugEnabled() && (nbEntityLinkExported % NB_OBJECTS_EXPORTED_FETCH_SIZE == 0)) {
						LOGGER.debug("Process " + NB_OBJECTS_EXPORTED_FETCH_SIZE + " more entity links (Up to "
								+ nbEntityLinkExported + ") in " + (Utils.getTime() - stepStartTime) + " ms.");
						stepStartTime = Utils.getTime();
					}
				}
			}
		}

		Files.move(entityLinkPathTmp,
				Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_ENTITY_LINK_OLD),
				StandardCopyOption.REPLACE_EXISTING);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbEntityLinkExported + " entity links exported in "
				+ (Utils.getTime() - entityLinkStartTime) + " ms."));

	}

	private void exportEntityLinkAttributes(SOAContext soaContext, Statement statement)
			throws IOException, SQLException {

		long entityLinkAttributeStartTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting entity link attributes"));

		long nbEntityLinkAttributeExported = 0;
		Path entityLinkAttributePathTmp = Paths.get(Configuration.tmpDirectory,
				ProvisioningFileData.FILENAME_ENTITY_LINK_ATTRIBUTE_OLD);

		try (BufferedWriter entityLinkAttributeWriter = Utils.newBufferedWriter(entityLinkAttributePathTmp,
				ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			long stepStartTime = Utils.getTime();
			String exportLinkAttributeQuery = "SELECT ela."
					+ StringUtils.join(FILE_ENTITY_LINK_ATTRIBUTE_COL_NAMES, ", ela.") + //
					" FROM " + DB_TMP_TABLE_ENTITIES + " tmp" + //
					" JOIN " + EntityLinkAttribute.TABLE_NAME + " ela on ela."
					+ EntityLinkId.COL_NAME_REPORTING_ENTITY_DEST_FK + "=tmp." + DB_TMP_TABLE_COL_NAME_REPK + //
					" JOIN " + DB_TMP_TABLE_ENTITIES + " tmp2 on tmp2." + DB_TMP_TABLE_COL_NAME_REPK + "=ela."
					+ EntityLinkId.COL_NAME_REPORTING_ENTITY_SRC_FK;
			try (ResultSet resultSet = statement.executeQuery(exportLinkAttributeQuery)) {

				while (resultSet.next()) {
					entityLinkAttributeWriter
							.append(getStringLong(resultSet, EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_SRC_FK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityLinkAttributeWriter
							.append(getStringLong(resultSet, EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_DEST_FK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityLinkAttributeWriter.append(getString(resultSet, EntityLinkAttributeId.COL_NAME_ROLE))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityLinkAttributeWriter.append(getString(resultSet, EntityLinkAttributeId.COL_NAME_NAME))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					entityLinkAttributeWriter.append(getString(resultSet, EntityLinkAttribute.COL_NAME_VALUE));
					entityLinkAttributeWriter.newLine();
					nbEntityLinkAttributeExported++;
					if (LOGGER.isDebugEnabled()
							&& (nbEntityLinkAttributeExported % NB_OBJECTS_EXPORTED_FETCH_SIZE == 0)) {
						LOGGER.debug("Process " + NB_OBJECTS_EXPORTED_FETCH_SIZE
								+ " more entity link attributes (Up to " + nbEntityLinkAttributeExported + ") in "
								+ (Utils.getTime() - stepStartTime) + " ms.");
						stepStartTime = Utils.getTime();
					}
				}
			}
		}

		Files.move(entityLinkAttributePathTmp,
				Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_ENTITY_LINK_ATTRIBUTE_OLD),
				StandardCopyOption.REPLACE_EXISTING);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbEntityLinkAttributeExported
				+ " entity links attributes exported in " + (Utils.getTime() - entityLinkAttributeStartTime) + " ms."));

	}

	private void exportGroupToEntityLinks(SOAContext soaContext, Statement statement) throws IOException, SQLException {

		long groupToEntityStartTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Exporting group to entity links"));

		long nbGroupToEntityExported = 0;
		Path groupToEntityPathTmp = Paths.get(Configuration.tmpDirectory,
				ProvisioningFileData.FILENAME_GROUP_TO_ENTITY_OLD);

		try (BufferedWriter groupToEntityWriter = Utils.newBufferedWriter(groupToEntityPathTmp,
				ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			long stepStartTime = Utils.getTime();
			String exportGroupToEntityQuery = "SELECT rge." + StringUtils.join(FILE_GROUP_TO_ENTITY_COL_NAMES, ", rge.")
					+ //
					" FROM " + DB_TMP_TABLE_ENTITIES + " tmp" + //
					" JOIN " + ReportingGroupToEntities.TABLE_NAME + " rge on rge."
					+ ReportingGroupToEntities.COL_NAME_REPORTING_ENTITY_FK + "=tmp." + DB_TMP_TABLE_COL_NAME_REPK + //
					" WHERE rge." + ReportingGroupToEntities.COL_NAME_BELONGS_TO + " IS TRUE";
			try (ResultSet resultSet = statement.executeQuery(exportGroupToEntityQuery)) {

				while (resultSet.next()) {
					groupToEntityWriter
							.append(getStringLong(resultSet, ReportingGroupToEntities.COL_NAME_REPORTING_ENTITY_FK))
							.append(ProvisioningFileData.DATA_SEPARATOR);
					groupToEntityWriter
							.append(getStringLong(resultSet, ReportingGroupToEntities.COL_NAME_REPORTING_GROUP_FK));
					groupToEntityWriter.newLine();
					nbGroupToEntityExported++;
					if (LOGGER.isDebugEnabled() && (nbGroupToEntityExported % NB_OBJECTS_EXPORTED_FETCH_SIZE == 0)) {
						LOGGER.debug("Process " + NB_OBJECTS_EXPORTED_FETCH_SIZE + " more group to entity (Up to "
								+ nbGroupToEntityExported + ") in " + (Utils.getTime() - stepStartTime) + " ms.");
						stepStartTime = Utils.getTime();
					}
				}
			}
		}

		Files.move(groupToEntityPathTmp,
				Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_GROUP_TO_ENTITY_OLD),
				StandardCopyOption.REPLACE_EXISTING);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbGroupToEntityExported
				+ " group to entity links exported in " + (Utils.getTime() - groupToEntityStartTime) + " ms."));
	}

	private static String getStringDateTimeInMillisecond(ResultSet resultSet, String columnName) throws SQLException {
		Timestamp timestamp = resultSet.getTimestamp(columnName);
		return timestamp == null ? StringUtils.EMPTY : Long.toString(timestamp.getTime());
	}

	private static String getStringLong(ResultSet resultSet, String columnName) throws SQLException {
		Object value = resultSet.getObject(columnName);
		return value == null ? StringUtils.EMPTY : Long.toString((Long) value);
	}

	private static String getString(ResultSet resultSet, String columnName) throws SQLException {
		String value = resultSet.getString(columnName);
		return value == null ? StringUtils.EMPTY : value;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ProvisioningActionStatusTO importProvisioningFiles(SOAContext soaContext, JobSummaryTO jobSummaryTO,
			Calendar provisioningDate, boolean provisioningDiff) {

		ProvisioningActionStatusTO actionStatusTO = new ProvisioningActionStatusTO("importProvisioningFiles");

		long startTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[importProvisioningFiles] Start import"));

		Statement statement = null;
		try (Connection refReportConnection = refReportDataSource.getConnection()) {

			ProvisioningContext provisioningContext = new ProvisioningContext(soaContext, jobSummaryTO, actionStatusTO,
					refReportConnection, provisioningDiff);

			statement = refReportConnection.createStatement();
			statement.executeUpdate("SET FOREIGN_KEY_CHECKS=0");

			importGroupsProvisioningFiles(provisioningContext);
			importEntitiesProvisioningFiles(provisioningContext, provisioningDate);
			importLinksProvisioningFiles(provisioningContext);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"[importProvisioningFiles] Import done successfully in " + (Utils.getTime() - startTime) + " ms"));

		} catch (Exception ex) {
			LOGGER.error(
					SOATools.buildSOALogMessage(soaContext, "[importProvisioningFiles] Error: " + ex.getMessage(), ex));
			actionStatusTO.error = true;
		} finally {
			if (statement != null) {
				try {
					statement.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
					statement.close();
				} catch (SQLException sqlEx) {
					LOGGER.error(SOATools.buildSOALogMessage(soaContext,
							"[importProvisioningFiles] Error closing statement or setting foreign_key_checks to 1: "
									+ sqlEx.getMessage(),
							sqlEx));
				}
			}
			actionStatusTO.duration = Utils.getTime() - startTime;
		}
		if (LOGGER.isDebugEnabled()) {
			try {
				StringWriter stringWriter = new StringWriter();
				JAXBRefReportFactory.getMarshaller().marshal(actionStatusTO, stringWriter);
				LOGGER.debug("Status: " + stringWriter.toString());
			} catch (Exception e) {
				LOGGER.debug("Unable to log status", e);
			}
		}
		return actionStatusTO;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private void importGroupsProvisioningFiles(ProvisioningContext provisioningContext)
			throws SQLException, IOException {

		long startTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, "Processing import of group files"));

		try (Statement statement = provisioningContext.refReportConnection.createStatement()) {

			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
					"Start finding reporting group data location"));

			Map<Long, Long> dataLocationPkByReportingGroupPk = new HashMap<>();
			try (ResultSet dataLocationResultSet = statement.executeQuery("SELECT " + ReportingGroup.COL_NAME_PK + ','
					+ ReportingGroup.COL_NAME_DATA_LOCATION_FK + " FROM " + ReportingGroup.TABLE_NAME)) {
				while (dataLocationResultSet.next()) {
					Long reportingGroupPk = dataLocationResultSet.getLong(1);
					Long dataLocationPk = dataLocationResultSet.getLong(2); // Return 0 if null !!!
					dataLocationPkByReportingGroupPk.put(reportingGroupPk, dataLocationPk);
				}
			}

			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
					"Data locations found - Start creating new group file with data location"));

			List<Long> groupUpdatedPks = new ArrayList<>();
			Path groupFile = Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_GROUP);
			Path groupWithDataLocationFile = Paths.get(Configuration.workExportPathName,
					ProvisioningFileData.FILENAME_GROUP_WITH_DATA_LOCATION);
			try (BufferedReader readerGroup = Utils.newBufferedReader(groupFile, ProvisioningFileData.DATA_CHARSET,
					ProvisioningFileData.READER_BUFFER_SIZE);
					BufferedWriter writerGroupDataLocation = Utils.newBufferedWriter(groupWithDataLocationFile,
							ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {
				String line;
				while ((line = readerGroup.readLine()) != null) {
					String[] entityData = StringUtils.split(line, ProvisioningFileData.DATA_SEPARATOR);
					Long reportingGroupPk = Long.valueOf(entityData[0]);
					groupUpdatedPks.add(reportingGroupPk);
					Long dataLocationPk = dataLocationPkByReportingGroupPk.get(reportingGroupPk);
					// Query returned 0 if null !!!
					writerGroupDataLocation.append(line).append(ProvisioningFileData.DATA_SEPARATOR)
							.append(dataLocationPk == null || dataLocationPk == 0L ? ProvisioningFileData.DATA_NULL
									: Long.toString(dataLocationPk));
					writerGroupDataLocation.newLine();
				}
			}

			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
					"File " + groupWithDataLocationFile + " created"));

			if (provisioningContext.provisioningDiff) {

				LOGGER.info(
						SOATools.buildSOALogMessage(provisioningContext.soaContext, "Start deleting linked objects"));

				String[] objectDeletes = new String[] { GroupAttribute.TABLE_NAME, GroupingRule.TABLE_NAME };
				int cptGroupPk = 0;
				while (cptGroupPk < groupUpdatedPks.size()) {

					int endCpt = Math.min(groupUpdatedPks.size(), cptGroupPk + NB_BULK_DELETE_ENTITY_OBJECTS_LINK);
					List<Long> groupPksSubList = groupUpdatedPks.subList(cptGroupPk, endCpt);
					String groupPksInSelection = StringUtils.join(groupPksSubList, ',');

					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("Process groupPks " + groupPksInSelection);
					}

					statement.addBatch("DELETE FROM " + GroupAttribute.TABLE_NAME + " WHERE "
							+ GroupAttributeId.COL_NAME_REPORTING_GROUP_FK + " IN (" + groupPksInSelection + ')');
					statement.addBatch("DELETE FROM " + GroupingRule.TABLE_NAME + " WHERE "
							+ GroupingRuleId.COL_NAME_REPORTING_GROUP_FK + " IN (" + groupPksInSelection + ')');
					int[] results = statement.executeBatch();
					for (int i = 0; i < results.length; i++) {
						int resultDelete = results[i];
						switch (resultDelete) {
						case Statement.EXECUTE_FAILED:
							String errorMsg = "Provisioning Group Diff - Error deleting " + objectDeletes[i]
									+ " link to groupPks " + groupPksInSelection;
							provisioningContext.actionStatusTO.addInfo(errorMsg, resultDelete);
							LOGGER.error(SOATools.buildSOALogMessage(provisioningContext.soaContext, errorMsg));
							provisioningContext.actionStatusTO.error = true;
							break;
						default:
							if (LOGGER.isTraceEnabled()) {
								LOGGER.trace("Provisioning Group Diff - " + objectDeletes[i] + " link to groupPks "
										+ groupPksInSelection + " deleted");
							}
							break;
						}
					}
					statement.clearBatch();

					cptGroupPk += NB_BULK_DELETE_ENTITY_OBJECTS_LINK;
				}

				LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, "Delete linked objects done"));

				// Table Group
				executeLoadInfile(provisioningContext, statement,
						ProvisioningFileData.FILENAME_GROUP_WITH_DATA_LOCATION);

				// Table Group Attribute
				executeLoadInfile(provisioningContext, statement, ProvisioningFileData.FILENAME_GROUP_ATTRIBUTE);

				// Table Group Rule
				executeLoadInfile(provisioningContext, statement, ProvisioningFileData.FILENAME_GROUP_RULE);

			} else {

				// Table Group
				LoadInfileInfo loadGroupInfileInfo = LOAD_INFILE_INFO
						.get(ProvisioningFileData.FILENAME_GROUP_WITH_DATA_LOCATION);
				statement.addBatch("TRUNCATE TABLE " + loadGroupInfileInfo.tableName);

				// Table Group Attribute
				LoadInfileInfo loadGroupAttributeInfileInfo = LOAD_INFILE_INFO
						.get(ProvisioningFileData.FILENAME_GROUP_ATTRIBUTE);
				statement.addBatch("TRUNCATE TABLE " + loadGroupAttributeInfileInfo.tableName);

				// Table Group Rule
				LoadInfileInfo loadGroupRuleInfileInfo = LOAD_INFILE_INFO.get(ProvisioningFileData.FILENAME_GROUP_RULE);
				statement.addBatch("TRUNCATE TABLE " + loadGroupRuleInfileInfo.tableName);

				// Table Data Location
				statement.addBatch("DELETE dl FROM " + DataLocation.TABLE_NAME + " dl WHERE NOT EXISTS (SELECT 1 FROM "
						+ ReportingGroup.TABLE_NAME + " WHERE " + ReportingGroup.COL_NAME_DATA_LOCATION_FK + " = dl."
						+ DataLocation.COL_NAME_PK + ")");

				int[] results = statement.executeBatch();
				int i = 0;
				addTruncateStatusInfo(provisioningContext, loadGroupInfileInfo.tableName, results[i++]);
				addTruncateStatusInfo(provisioningContext, loadGroupAttributeInfileInfo.tableName, results[i++]);
				addTruncateStatusInfo(provisioningContext, loadGroupRuleInfileInfo.tableName, results[i++]);
				addDeleteStatusInfo(provisioningContext, DataLocation.TABLE_NAME, results[i++]);

				statement.addBatch(buildLoadQuery(loadGroupInfileInfo));
				statement.addBatch(buildLoadQuery(loadGroupAttributeInfileInfo));
				statement.addBatch(buildLoadQuery(loadGroupRuleInfileInfo));

				results = statement.executeBatch();
				i = 0;
				addLoadInfileStatusInfo(provisioningContext, loadGroupInfileInfo.tableName, results[i++]);
				addLoadInfileStatusInfo(provisioningContext, loadGroupAttributeInfileInfo.tableName, results[i++]);
				addLoadInfileStatusInfo(provisioningContext, loadGroupRuleInfileInfo.tableName, results[i++]);

			}

			// Purge dans RefReport apres un ou plusieurs appels manuels du service REST de purge de reporting groups
			// depuis RefObject
			deleteObsoleteReportingGroups(provisioningContext, statement);
		}

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"Import for reporting group done successfully in " + (Utils.getTime() - startTime) + " ms"));

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private void importEntitiesProvisioningFiles(ProvisioningContext provisioningContext, Calendar provisioningDate)
			throws SQLException, IOException {

		long startTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, "Processing import of entities files"));

		try (Statement statement = provisioningContext.refReportConnection.createStatement();
				Statement selectStatement = provisioningContext.refReportConnection.createStatement()) {

			createEntityWithPartitionsFileAndDeletingLinkedObjects(provisioningContext, statement, selectStatement);

			if (!provisioningContext.provisioningDiff) {

				// Table Entity Attribute
				LoadInfileInfo loadEntityAttributeInfileInfo = LOAD_INFILE_INFO
						.get(ProvisioningFileData.FILENAME_ENTITY_ATTRIBUTE);
				statement.addBatch("TRUNCATE TABLE " + loadEntityAttributeInfileInfo.tableName);

				// Table Entity type to subtype
				LoadInfileInfo loadEntityTypeSubtypeInfileInfo = LOAD_INFILE_INFO
						.get(ProvisioningFileData.FILENAME_ENTITY_TO_TYPE);
				statement.addBatch("TRUNCATE TABLE " + loadEntityTypeSubtypeInfileInfo.tableName);

				// Table Data Location
				// useless data location (ie: not directly linked to a reporting group) has
				// already been deleted when importing groups in FULL mode

				// Table Partition status
				statement.addBatch("TRUNCATE TABLE " + PartitionStatus.TABLE_NAME);

				// Table Entity
				LoadInfileInfo loadEntityInfileInfo = LOAD_INFILE_INFO.get(ProvisioningFileData.FILENAME_ENTITY);
				statement.addBatch("TRUNCATE TABLE " + loadEntityInfileInfo.tableName);

				int[] results = statement.executeBatch();
				int i = 0;
				addTruncateStatusInfo(provisioningContext, loadEntityAttributeInfileInfo.tableName, results[i++]);
				addTruncateStatusInfo(provisioningContext, loadEntityTypeSubtypeInfileInfo.tableName, results[i++]);
				addTruncateStatusInfo(provisioningContext, PartitionStatus.TABLE_NAME, results[i++]);
				addTruncateStatusInfo(provisioningContext, loadEntityInfileInfo.tableName, results[i++]);
				statement.clearBatch();

			}

			// Table Entity
			executeLoadInfile(provisioningContext, statement, ProvisioningFileData.FILENAME_ENTITY_WITH_PARTITION);

			// Table Entity Attribute from file entityGroupingAttribute
			executeLoadInfile(provisioningContext, statement, ProvisioningFileData.FILENAME_ENTITY_GROUPING_ATTRIBUTE);

			// Table Entity Attribute from file entityAttribute
			executeLoadInfile(provisioningContext, statement, ProvisioningFileData.FILENAME_ENTITY_ATTRIBUTE);

			// Table Entity type to subtype
			executeLoadInfile(provisioningContext, statement, ProvisioningFileData.FILENAME_ENTITY_TO_TYPE);

			updateEntitiesDate(provisioningContext, provisioningDate);

			// Purge dans RefReport suite a un provisioning (cas particulier de suppression de COS) depuis RefObject
			deleteEntities(provisioningContext, statement);

			// Purge dans RefReport apres un ou plusieurs appels manuels du service REST de purge d'entites
			// depuis RefObject
			deleteObsoleteEntities(provisioningContext, statement);
		}

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"Import for reporting entity done successfully in " + (Utils.getTime() - startTime) + " ms"));
	}

	private void createEntityWithPartitionsFileAndDeletingLinkedObjects(ProvisioningContext provisioningContext,
			Statement statement, Statement selectStatement) throws IOException, SQLException {

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"Provisioning Entity - Reading entities pk"));

		List<Long> entityPks = new ArrayList<>();
		Path entityFile = Paths.get(Configuration.workExportPathName, ProvisioningFileData.FILENAME_ENTITY);
		try (BufferedReader readerEntity = Utils.newBufferedReader(entityFile, ProvisioningFileData.DATA_CHARSET,
				ProvisioningFileData.READER_BUFFER_SIZE)) {
			String line;
			while ((line = readerEntity.readLine()) != null) {
				String[] entityData = StringUtils.split(line, ProvisioningFileData.DATA_SEPARATOR);
				entityPks.add(Long.valueOf(entityData[0]));
			}
		}

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"Provisioning Entity - All entities pk found"));

		String msg = "Find existing reporting entity partition number";
		if (provisioningContext.provisioningDiff) {
			msg += " and start deleting linked objects";
		}
		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, msg));

		String[] objectDeletes = new String[] { EntityAttribute.TABLE_NAME, ReportingEntity.TJ_NAME_SUBTYPES,
				DataLocation.TABLE_NAME };
		Map<Long, String> partitionNumberByReportingEntityPk = new HashMap<>();
		int cptEntityPk = 0;
		while (cptEntityPk < entityPks.size()) {

			int endCpt = Math.min(entityPks.size(), cptEntityPk + NB_BULK_DELETE_ENTITY_OBJECTS_LINK);
			List<Long> entityPksSubList = entityPks.subList(cptEntityPk, endCpt);
			String entityPksInSelection = StringUtils.join(entityPksSubList, ',');

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Process entityPks " + entityPksInSelection);
			}

			String partitionNumberQuery = "SELECT " + ReportingEntity.COL_NAME_PK + ','
					+ ReportingEntity.COL_NAME_PARTITION_NUMBER + " FROM " + ReportingEntity.TABLE_NAME + " WHERE "
					+ ReportingEntity.COL_NAME_PK + " IN (" + entityPksInSelection + ')';
			try (ResultSet partitionNumberResultSet = selectStatement.executeQuery(partitionNumberQuery)) {
				while (partitionNumberResultSet.next()) {
					Long reportingEntityPk = partitionNumberResultSet.getLong(1);
					String partitionNumber = partitionNumberResultSet.getString(2);
					partitionNumberByReportingEntityPk.put(reportingEntityPk, partitionNumber);
				}
			}

			if (provisioningContext.provisioningDiff) {

				statement.addBatch("DELETE FROM " + EntityAttribute.TABLE_NAME + " WHERE "
						+ EntityAttribute.COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
				statement.addBatch("DELETE FROM " + ReportingEntity.TJ_NAME_SUBTYPES + " WHERE "
						+ ReportingEntity.TJ_COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
				statement.addBatch("DELETE FROM " + DataLocation.TABLE_NAME + " WHERE "
						+ DataLocation.COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
				int[] results = statement.executeBatch();
				for (int i = 0; i < results.length; i++) {
					int resultDelete = results[i];
					switch (resultDelete) {
					case Statement.EXECUTE_FAILED:
						String errorMsg = "Provisioning Entity Diff - Error deleting " + objectDeletes[i]
								+ " link to entityPks " + entityPksInSelection;
						provisioningContext.actionStatusTO.addInfo(errorMsg, resultDelete);
						LOGGER.error(SOATools.buildSOALogMessage(provisioningContext.soaContext, errorMsg));
						provisioningContext.actionStatusTO.error = true;
						break;
					default:
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("Provisioning Entity Diff - " + objectDeletes[i] + " link to entityPks "
									+ entityPksInSelection + " deleted");
						}
						break;
					}
				}
				statement.clearBatch();
			}

			cptEntityPk += NB_BULK_DELETE_ENTITY_OBJECTS_LINK;
		}

		if (provisioningContext.provisioningDiff) {
			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, "Delete linked objects done"));
		}
		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"Partition number found - Start creating new entity file with partition number"));

		Path entityWithPartition = Paths.get(Configuration.workExportPathName,
				ProvisioningFileData.FILENAME_ENTITY_WITH_PARTITION);
		try (BufferedReader readerEntity = Utils.newBufferedReader(entityFile, ProvisioningFileData.DATA_CHARSET,
				ProvisioningFileData.READER_BUFFER_SIZE);
				BufferedWriter writerEntity = Utils.newBufferedWriter(entityWithPartition,
						ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {
			String line;
			while ((line = readerEntity.readLine()) != null) {
				String[] entityData = StringUtils.split(line, ProvisioningFileData.DATA_SEPARATOR);
				Long reportingEntityPk = Long.valueOf(entityData[0]);
				String partitionNumber = partitionNumberByReportingEntityPk.get(reportingEntityPk);
				writerEntity.append(line).append(ProvisioningFileData.DATA_SEPARATOR)
						.append(partitionNumber == null ? ProvisioningFileData.DATA_NULL : partitionNumber);
				writerEntity.newLine();
			}
		}

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"File " + entityWithPartition + " created"));
	}

	private void updateEntitiesDate(ProvisioningContext provisioningContext, Calendar provisioningDate)
			throws SQLException, IOException {

		long start = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, "Updating Entity date"));

		StringBuilder updateDateQuery = new StringBuilder("UPDATE ").append(ReportingEntity.TABLE_NAME).append(" SET ")
				.append(ReportingEntity.COL_NAME_UPDATE_DATE).append("=").append('?').append(" WHERE ")
				.append(ReportingEntity.COL_NAME_PK).append(" IN (");
		for (int i = 1; i < NB_BULK_UPDATE_ENTITY_DATE; i++) {
			updateDateQuery.append("?,");
		}
		updateDateQuery.append("?)");

		try (PreparedStatement updateDateStatement = provisioningContext.refReportConnection
				.prepareStatement(updateDateQuery.toString())) {

			List<Long> entityUpdateDatePks = new ArrayList<>();
			Path entityUpdateDateFilePath = Paths.get(Configuration.workExportPathName,
					ProvisioningFileData.FILENAME_ENTITY_UPDATE_DATE);
			try (BufferedReader readerEntity = Utils.newBufferedReader(entityUpdateDateFilePath,
					ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.READER_BUFFER_SIZE)) {
				String line;
				while ((line = readerEntity.readLine()) != null) {
					entityUpdateDatePks.add(Long.valueOf(line));
				}
			}

			int cptUpdate = 0;
			updateDateStatement.setDate(1, new java.sql.Date(provisioningDate.getTimeInMillis()));
			while (cptUpdate < entityUpdateDatePks.size()) {
				int endCptUpdate = Math.min(entityUpdateDatePks.size(), cptUpdate + NB_BULK_UPDATE_ENTITY_DATE);
				List<Long> entityPksSubList = entityUpdateDatePks.subList(cptUpdate, endCptUpdate);
				int paramIndex = 2;
				for (Long entityPk : entityPksSubList) {
					updateDateStatement.setLong(paramIndex, entityPk);
					paramIndex++;
				}
				int resultUpdate = updateDateStatement.executeUpdate();
				cptUpdate += NB_BULK_UPDATE_ENTITY_DATE;
				switch (resultUpdate) {
				case Statement.EXECUTE_FAILED:
					String errorMsg = "Error updating date for entityPks " + StringUtils.join(entityPksSubList, ',');
					provisioningContext.actionStatusTO.addInfo(errorMsg, resultUpdate);
					LOGGER.error(SOATools.buildSOALogMessage(provisioningContext.soaContext, errorMsg));
					provisioningContext.actionStatusTO.error = true;
					break;
				default:
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("EntityPks " + StringUtils.join(entityPksSubList, ',') + " date updated");
					}
					break;
				}
			}
		}

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"Entity date updated in " + (Utils.getTime() - start) + " ms"));
	}

	/**
	 * Delete entities in the RefReport MySQL database from the RefObject provisioning cleaning export file
	 * {@link ProvisioningFileData#FILENAME_ENTITY_TO_DELETE} (particular case of the COS entity deletion of the
	 * provisioning).<br/>
	 * Note: the delete operations are done by groups of 50 entity pk ids (maximum).<br/>
	 * Then, delete the input file.
	 * 
	 * @param provisioningContext
	 * @param statement
	 * @throws SQLException
	 * @throws IOException
	 */
	private void deleteEntities(ProvisioningContext provisioningContext, Statement statement)
			throws SQLException, IOException {
		String filename = ProvisioningFileData.FILENAME_ENTITY_TO_DELETE;

		long start = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, "Deleting Entities: " + filename));

		List<Long> entityToDeletePks = new ArrayList<>();
		Path entityToDeleteFilePath = Paths.get(Configuration.workExportPathName, filename);
		if (Files.exists(entityToDeleteFilePath)) {
			try (BufferedReader readerEntity = Utils.newBufferedReader(entityToDeleteFilePath,
					ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.READER_BUFFER_SIZE)) {
				String line;
				while ((line = readerEntity.readLine()) != null) {
					entityToDeletePks.add(Long.valueOf(line));
				}
			}
		}

		int cptDelete = 0;
		while (cptDelete < entityToDeletePks.size()) {
			int endCptDelete = Math.min(entityToDeletePks.size(), cptDelete + NB_BULK_DELETE_ENTITY_OBJECTS_LINK);
			List<Long> entityPksSubList = entityToDeletePks.subList(cptDelete, endCptDelete);

			String entityPksInSelection = StringUtils.join(entityPksSubList, ',');

			statement.addBatch("DELETE FROM " + ReportsBookmark.TABLE_NAME + " WHERE "
					+ ReportsBookmark.COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + DataLocation.TABLE_NAME + " WHERE "
					+ DataLocation.COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + EntityAttribute.TABLE_NAME + " WHERE "
					+ EntityAttribute.COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + EntityAttributeList.TABLE_NAME + " WHERE "
					+ EntityAttributeList.COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + EntityGroupAttribute.TABLE_NAME + " WHERE "
					+ EntityGroupAttributeId.COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + EntityLinkAttribute.TABLE_NAME + " WHERE "
					+ EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_SRC_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + EntityLinkAttribute.TABLE_NAME + " WHERE "
					+ EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_DEST_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + EntityLink.TABLE_NAME + " WHERE "
					+ EntityLinkId.COL_NAME_REPORTING_ENTITY_SRC_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + EntityLink.TABLE_NAME + " WHERE "
					+ EntityLinkId.COL_NAME_REPORTING_ENTITY_DEST_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + ReportingGroupToEntities.TABLE_NAME + " WHERE "
					+ ReportingGroupToEntities.COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + ReportingEntity.TJ_NAME_SUBTYPES + " WHERE "
					+ ReportingEntity.TJ_COL_NAME_REPORTING_ENTITY_FK + " IN (" + entityPksInSelection + ')');
			statement.addBatch("UPDATE " + ReportingEntity.TABLE_NAME + " SET " + ReportingEntity.COL_NAME_PARENT
					+ "=NULL WHERE " + ReportingEntity.COL_NAME_PARENT + " IN (" + entityPksInSelection + ')');
			statement.addBatch("DELETE FROM " + ReportingEntity.TABLE_NAME + " WHERE " + ReportingEntity.COL_NAME_PK
					+ " IN (" + entityPksInSelection + ')');

			int[] results = statement.executeBatch();

			for (int i = 0; i < results.length; i++) {
				int resultDelete = results[i];
				switch (resultDelete) {
				case Statement.EXECUTE_FAILED:
					String errorMsg = "Error updating data for entityPks " + StringUtils.join(entityPksSubList, ',');
					provisioningContext.actionStatusTO.addInfo(errorMsg, resultDelete);
					LOGGER.error(SOATools.buildSOALogMessage(provisioningContext.soaContext, errorMsg));
					provisioningContext.actionStatusTO.error = true;
					break;
				default:
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("Data for entityPks " + StringUtils.join(entityPksSubList, ',') + " are deleted");
					}
					break;
				}
			}
			cptDelete += NB_BULK_DELETE_ENTITY_OBJECTS_LINK;
		}

		// Delete the input file
		Files.deleteIfExists(entityToDeleteFilePath);

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"Entities deleted in " + (Utils.getTime() - start) + " ms"));

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private void importLinksProvisioningFiles(ProvisioningContext provisioningContext) throws SQLException {

		long startTime = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, "Processing import of link files"));

		try (Statement statement = provisioningContext.refReportConnection.createStatement()) {

			// Table Entity Link
			LoadInfileInfo loadEntityLinkInfileInfo = LOAD_INFILE_INFO.get(ProvisioningFileData.FILENAME_ENTITY_LINK);
			statement.addBatch("TRUNCATE TABLE " + loadEntityLinkInfileInfo.tableName);

			// Table Entity Attribute
			LoadInfileInfo loadEntityLinkAttributeInfileInfo = LOAD_INFILE_INFO
					.get(ProvisioningFileData.FILENAME_ENTITY_LINK_ATTRIBUTE);
			statement.addBatch("TRUNCATE TABLE " + loadEntityLinkAttributeInfileInfo.tableName);

			// Table Entity to Group
			LoadInfileInfo loadEntityToGroupInfileInfo = LOAD_INFILE_INFO
					.get(ProvisioningFileData.FILENAME_GROUP_TO_ENTITY);
			statement.addBatch("TRUNCATE TABLE " + loadEntityToGroupInfileInfo.tableName);

			int[] results = statement.executeBatch();
			int i = 0;

			addTruncateStatusInfo(provisioningContext, loadEntityLinkInfileInfo.tableName, results[i++]);
			addTruncateStatusInfo(provisioningContext, loadEntityLinkAttributeInfileInfo.tableName, results[i++]);
			addTruncateStatusInfo(provisioningContext, loadEntityToGroupInfileInfo.tableName, results[i++]);

			statement.addBatch(buildLoadQuery(loadEntityLinkInfileInfo));
			statement.addBatch(buildLoadQuery(loadEntityLinkAttributeInfileInfo));
			statement.addBatch(buildLoadQuery(loadEntityToGroupInfileInfo));

			results = statement.executeBatch();
			i = 0;
			addLoadInfileStatusInfo(provisioningContext, loadEntityLinkInfileInfo.tableName, results[i++]);
			addLoadInfileStatusInfo(provisioningContext, loadEntityLinkAttributeInfileInfo.tableName, results[i++]);
			addLoadInfileStatusInfo(provisioningContext, loadEntityToGroupInfileInfo.tableName, results[i++]);
			statement.clearBatch();
		}

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"Import for link done successfully in " + (Utils.getTime() - startTime) + " ms"));
	}

	private void executeLoadInfile(ProvisioningContext provisioningContext, Statement statement, String fileName)
			throws SQLException {
		long startStep = Utils.getTime();
		LoadInfileInfo loadInfileInfo = LOAD_INFILE_INFO.get(fileName);
		int resultUpdate = statement.executeUpdate(buildLoadQuery(loadInfileInfo));
		addLoadInfileStatusInfo(provisioningContext, loadInfileInfo.tableName, resultUpdate);
		long timeToLoad = Utils.getTime() - startStep;
		provisioningContext.actionStatusTO.addInfo("Time to load table " + loadInfileInfo.tableName,
				Long.valueOf(timeToLoad).intValue());
		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				"Table " + loadInfileInfo.tableName + " load in " + timeToLoad + " ms"));
	}

	private void addTruncateStatusInfo(ProvisioningContext provisioningContext, String truncatedTableName,
			int resultUpdate) {
		switch (resultUpdate) {
		case Statement.SUCCESS_NO_INFO:
			String successMsg = "Truncate done " + truncatedTableName;
			provisioningContext.actionStatusTO.addInfo(successMsg, 1);
			provisioningContext.jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.INFO,
					successMsg);
			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, successMsg));
			break;
		case Statement.EXECUTE_FAILED:
			String errorMsg = "Error truncating " + truncatedTableName;
			provisioningContext.actionStatusTO.addInfo(errorMsg, resultUpdate);
			provisioningContext.actionStatusTO.error = true;
			provisioningContext.jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.ERROR,
					errorMsg);
			LOGGER.error(SOATools.buildSOALogMessage(provisioningContext.soaContext, errorMsg));
			break;
		default:
			String msg = "Truncate done " + truncatedTableName;
			provisioningContext.actionStatusTO.addInfo(msg, resultUpdate);
			provisioningContext.jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.INFO, msg);
			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, msg));
			break;
		}
	}

	private void addDeleteStatusInfo(ProvisioningContext provisioningContext, String tableName, int resultUpdate) {
		switch (resultUpdate) {
		case Statement.SUCCESS_NO_INFO:
			String successMsg = "Delete done from table " + tableName;
			provisioningContext.actionStatusTO.addInfo(successMsg, 1);
			provisioningContext.jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.INFO,
					successMsg);
			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, successMsg));
			break;
		case Statement.EXECUTE_FAILED:
			String errorMsg = "Error deleting from table " + tableName;
			provisioningContext.actionStatusTO.addInfo(errorMsg, resultUpdate);
			provisioningContext.actionStatusTO.error = true;
			provisioningContext.jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.ERROR,
					errorMsg);
			LOGGER.error(SOATools.buildSOALogMessage(provisioningContext.soaContext, errorMsg));
			break;
		default:
			String msg = "Delete done from table " + tableName;
			provisioningContext.actionStatusTO.addInfo(msg, resultUpdate);
			provisioningContext.jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.INFO, msg);
			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, msg));
			break;
		}
	}

	private void addLoadInfileStatusInfo(ProvisioningContext provisioningContext, String loadTableName,
			int resultUpdate) {
		switch (resultUpdate) {
		case Statement.SUCCESS_NO_INFO:
			String successMsg = "Table " + loadTableName + " loaded - no more info";
			provisioningContext.actionStatusTO.addInfo(successMsg, 0);
			provisioningContext.jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.INFO,
					successMsg);
			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, successMsg));
			break;
		case Statement.EXECUTE_FAILED:
			String errorMsg = "Error loading in " + loadTableName;
			provisioningContext.actionStatusTO.addInfo(errorMsg, resultUpdate);
			provisioningContext.actionStatusTO.error = true;
			provisioningContext.jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.ERROR,
					errorMsg);
			LOGGER.error(SOATools.buildSOALogMessage(provisioningContext.soaContext, errorMsg));
			break;
		default:
			provisioningContext.actionStatusTO.addInfo("Nb object load in " + loadTableName, resultUpdate);
			String msg = resultUpdate + " objects load in " + loadTableName;
			provisioningContext.jobSummaryTO.addEvent(JobEventTypeEnum.PROVISIONING, JobEventCriticityEnum.INFO, msg);
			LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext, msg));
			break;
		}
	}

	private String buildLoadQuery(LoadInfileInfo loadInfileInfo) {
		Path file = Paths.get(Configuration.workExportPathName, loadInfileInfo.fileName);
		String fileName = StringUtils.replaceChars(file.toString(), '\\', '/');
		StringBuilder loadSqlQuery = new StringBuilder("LOAD DATA LOCAL INFILE '").append(fileName).append('\'');
		if (loadInfileInfo.replace) {
			loadSqlQuery.append(" REPLACE");
		}
		loadSqlQuery.append(" INTO TABLE ").append(loadInfileInfo.tableName);
		loadSqlQuery.append(" CHARACTER SET latin1");
		// loadSqlQuery.append(" CHARACTER SET binary");
		loadSqlQuery.append(" FIELDS TERMINATED BY '").append(ProvisioningFileData.DATA_SEPARATOR)
				.append("' ESCAPED BY '").append(ProvisioningFileData.ESCAPE_CHARACTER);
		loadSqlQuery.append("' LINES TERMINATED BY '").append(System.lineSeparator()).append("' (");
		boolean first = true;
		for (String colName : loadInfileInfo.colNames) {
			if (!first) {
				loadSqlQuery.append(',');
			}
			if (loadInfileInfo.timestampColNames.contains(colName)
					|| loadInfileInfo.booleanColNames.contains(colName)) {
				loadSqlQuery.append(MYSQL_VAR_CHAR).append(colName);
			} else {
				loadSqlQuery.append(colName);
			}
			first = false;
		}
		loadSqlQuery.append(")");
		first = true;
		for (String timestampColName : loadInfileInfo.timestampColNames) {
			if (first) {
				loadSqlQuery.append(" SET ");
				first = false;
			} else {
				loadSqlQuery.append(", ");
			}
			loadSqlQuery.append(timestampColName).append('=').append("FROM_UNIXTIME(").append(MYSQL_VAR_CHAR)
					.append(timestampColName).append(')');
		}
		for (String booleanColName : loadInfileInfo.booleanColNames) {
			if (first) {
				loadSqlQuery.append(" SET ");
				first = false;
			} else {
				loadSqlQuery.append(", ");
			}
			loadSqlQuery.append(booleanColName).append('=').append("(").append(MYSQL_VAR_CHAR).append(booleanColName)
					.append(" = 'true')");
		}
		for (Entry<String, Object> entryConstant : loadInfileInfo.constantValueByColName.entrySet()) {
			if (first) {
				loadSqlQuery.append(" SET ");
				first = false;
			} else {
				loadSqlQuery.append(", ");
			}
			loadSqlQuery.append(entryConstant.getKey()).append('=').append('"')
					.append(entryConstant.getValue().toString()).append('"');
		}
		// for (Entry<String, Integer> entryTruncateInfo :
		// loadInfileInfo.truncateSizeByColName.entrySet()) {
		// if (first) {
		// loadSqlQuery.append(" SET ");
		// first = false;
		// } else {
		// loadSqlQuery.append(", ");
		// }
		// loadSqlQuery.append(entryTruncateInfo.getKey()).append(" =
		// IF(LENGTH(").append(MYSQL_VAR_CHAR).append(entryTruncateInfo.getKey()).append(")
		// > ").append(entryTruncateInfo.getValue()) // IF
		// .append(",
		// SUBSTRING(").append(MYSQL_VAR_CHAR).append(entryTruncateInfo.getKey()).append(",0,").append(entryTruncateInfo.getValue()).append("),
		// ") // TRUE
		// .append(MYSQL_VAR_CHAR).append(entryTruncateInfo.getKey()).append(")"); //
		// FALSE
		// }
		return loadSqlQuery.toString();
	}

	/**
	 * Delete obsolete reporting groups in the RefReport MySQL database from the RefObject REST purge export files
	 * "{@link ProvisioningFileData#FILENAME_PURGE_REPORTING_GROUP_TO_DELETE}.yyyyMMddHHmmss" of the folder
	 * {@link Configuration#workExportPathName}.<br/>
	 * For each pk defined in the input file, RefReport proceeds to deletions in the following tables:
	 * <ul>
	 * <li>TJ_REPORTING_GROUP_TO_CRITERIA
	 * <li>TJ_REPORTING_GROUP_TO_ENTITIES
	 * <li>TJ_REPORTING_GROUP_TO_OFFER_OPTION
	 * <li>TJ_REPORT_USER_TO_REPORTING_GROUP
	 * <li>T_GROUP_ATTRIBUTE
	 * <li>T_GROUP_PARTITION_STATUS
	 * <li>T_GROUP_REPORT_CONFIG
	 * <li>T_REPORTING_GROUP
	 * </ul>
	 * Note: the delete operations are done by groups of 50 reporting group pk ids (maximum).<br/>
	 * After the execution of each group of 50 delete operations : an ERROR log is logged if the group execution has
	 * failed, a TRACE log is logged if the group execution has completed successfully.<br/>
	 * Note: RefObject input filename format:
	 * "{@link ProvisioningFileData#FILENAME_PURGE_REPORTING_GROUP_TO_DELETE}.yyyyMMddHHmmss" (timestamp of the
	 * beginning of the cleaning in RefObject).<br/>
	 * Then, RefReport renames the input file with a ".yyyyMMdd.done" suffix (timestamp of the end of the cleaning in
	 * RefReport) ; the input file is not deleted.<br/>
	 * 
	 * @param provisioningContext
	 * @param statement
	 * @throws SQLException
	 * @throws IOException
	 * @author Pascal Morvan (Atos)
	 * @see [IP02241-2018-000807-Openstat-ProvisionningHistorique-Purge, PR_02241_P228_002]
	 */
	private void deleteObsoleteReportingGroups(ProvisioningContext provisioningContext, Statement statement)
			throws SQLException, IOException {
		String filenamePrefix = ProvisioningFileData.FILENAME_PURGE_REPORTING_GROUP_TO_DELETE;
		Path exportDirPath = Paths.get(Configuration.workExportPathName);

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				String.format("Deleting obsolete reporting groups: %s.<yyyyMMddHHmmss>", filenamePrefix)));
		long startTime = Utils.getTime();
		final char SEP = ',';

		// List all matching files in the export directory for the given filename pattern.
		// Files suffixed by ".inprogress" should not be processed yet.
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(exportDirPath, path -> !Files.isDirectory(path)
				&& path.getFileName().toString().matches("\\Q" + filenamePrefix + "\\E" + "\\.[0-9]{14}"))) {
			for (Path exportFilePath : stream) {
				if (Files.exists(exportFilePath) && !Files.isDirectory(exportFilePath)) {
					LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
							String.format("Deleting obsolete reporting groups: %s", exportFilePath)));

					// Read the export file: reporting group to delete pks
					List<Long> reportingGroupToDeletePks = new ArrayList<>();
					try (BufferedReader readerEntity = Utils.newBufferedReader(exportFilePath,
							ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.READER_BUFFER_SIZE)) {
						String line;
						while ((line = readerEntity.readLine()) != null) {
							reportingGroupToDeletePks.add(Long.valueOf(line));
						}
					}

					// Delete the obsolete reporting groups in the RefReport MySQL database
					int cptDelete = 0;
					while (cptDelete < reportingGroupToDeletePks.size()) {
						int endCptDelete = Math.min(reportingGroupToDeletePks.size(),
								cptDelete + NB_BULK_DELETE_ENTITY_OBJECTS_LINK);
						List<Long> reportingGroupPksSubList = reportingGroupToDeletePks.subList(cptDelete,
								endCptDelete);

						String reportingGroupPksInSelection = StringUtils.join(reportingGroupPksSubList, SEP);

						addDeleteBatch(ReportingGroup.TJ_NAME_CRITERIA, ReportingGroup.TJ_COL_NAME_REPORTING_GROUP,
								statement, reportingGroupPksInSelection);
						addDeleteBatch(ReportingGroupToEntities.TABLE_NAME,
								ReportingGroupToEntities.COL_NAME_REPORTING_ENTITY_FK, statement,
								reportingGroupPksInSelection);
						addDeleteBatch(ReportingGroup.TJ_NAME_OFFER_OPTION, ReportingGroup.TJ_COL_NAME_REPORTING_GROUP,
								statement, reportingGroupPksInSelection);
						addDeleteBatch(ReportUser.TJ_NAME_REPORTING_GROUP, ReportUser.TJ_COL_NAME_REPORTING_GROUP,
								statement, reportingGroupPksInSelection);
						addDeleteBatch(GroupAttribute.TABLE_NAME, GroupAttributeId.COL_NAME_REPORTING_GROUP_FK,
								statement, reportingGroupPksInSelection);
						addDeleteBatch(ReportingGroupPartitionStatus.TABLE_NAME,
								ReportingGroupPartitionStatus.COL_NAME_REPORTING_GROUP, statement,
								reportingGroupPksInSelection);
						addDeleteBatch(GroupReportConfig.TABLE_NAME, GroupReportConfig.COL_NAME_REPORTING_GROUP,
								statement, reportingGroupPksInSelection);
						addDeleteBatch(ReportingGroup.TABLE_NAME, ReportingGroup.COL_NAME_PK, statement,
								reportingGroupPksInSelection);

						int[] results = statement.executeBatch();

						for (int resultDelete : results) {
							switch (resultDelete) {
							case Statement.EXECUTE_FAILED:
								String errorMsg = String.format("Error deleting data for reportingGroupPks %s",
										StringUtils.join(reportingGroupPksSubList, SEP));
								provisioningContext.actionStatusTO.addInfo(errorMsg, resultDelete);
								provisioningContext.actionStatusTO.error = true;
								LOGGER.error(SOATools.buildSOALogMessage(provisioningContext.soaContext, errorMsg));
								break;
							default:
								if (LOGGER.isTraceEnabled()) {
									LOGGER.trace(SOATools.buildSOALogMessage(provisioningContext.soaContext,
											String.format("Data for reportinGroupPks %s are deleted",
													StringUtils.join(reportingGroupPksSubList, SEP))));
								}
								break;
							}
						}
						cptDelete += NB_BULK_DELETE_ENTITY_OBJECTS_LINK;
					}

					// Rename the export file (do not delete the export file)
					String refObjectFilename = exportFilePath.getFileName().toString();
					String refReportTimestamp = DateHelper.format(DateHelper.YYYYMMDD, DateHelper.getCurrentDate());
					String suffix = "done";
					String targetFilename = String.format("%s.%s.%s", refObjectFilename, refReportTimestamp, suffix);
					Files.move(exportFilePath, exportFilePath.resolveSibling(targetFilename),
							StandardCopyOption.REPLACE_EXISTING);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(SOATools.buildSOALogMessage(provisioningContext.soaContext,
								"Processed export file renamed to: " + targetFilename));
					}
				}
			}
		}

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				String.format("Obsolete reporting groups deleted in %d ms", Utils.getTime() - startTime)));
	}

	/**
	 * Delete obsolete entities in the RefReport MySQL database from the RefObject REST purge export files
	 * "{@link ProvisioningFileData#FILENAME_PURGE_ENTITY_TO_DELETE}.yyyyMMddHHmmss" of the folder
	 * {@link Configuration#workExportPathName}.<br/>
	 * For each pk defined in the input file, RefReport proceeds to deletions in the following tables:
	 * <ul>
	 * <li>T_REPORTS_BOOKMARK
	 * <li>T_DATA_LOCATION
	 * <li>T_ENTITY_ATTRIBUTE
	 * <li>T_ENTITY_ATTRIBUTE_LIST
	 * <li>T_ENTITY_GROUP_ATTRIBUTE
	 * <li>T_ENTITY_LINK_ATTRIBUTE (source and destination)
	 * <li>T_ENTITY_LINK (source and destination)
	 * <li>TJ_REPORTING_GROUP_TO_ENTITIES
	 * <li>TJ_REPORTING_ENTITY_TO_TYPE_AND_SUBTYPE
	 * <li>T_REPORTING_ENTITY (only UPDATE parent to empty)
	 * <li>T_REPORTING_ENTITY
	 * </ul>
	 * Note: the delete operations are done by groups of 50 reporting group pk ids (maximum).<br/>
	 * After the execution of each group of 50 delete operations : an ERROR log is logged if the group execution has
	 * failed, a TRACE log is logged if the group execution has completed successfully.<br/>
	 * Note: RefObject input filename format:
	 * "{@link ProvisioningFileData#FILENAME_PURGE_ENTITY_TO_DELETE}.yyyyMMddHHmmss" (timestamp of the beginning of the
	 * cleaning in RefObject).<br/>
	 * Then, RefReport renames the input file with a ".yyyyMMdd.done" suffix (timestamp of the end of the cleaning in
	 * RefReport) ; the input file is not deleted.<br/>
	 * 
	 * @param provisioningContext
	 * @param statement
	 * @throws SQLException
	 * @throws IOException
	 * @author Pascal Morvan (Atos)
	 * @see [IP02241-2018-000807-Openstat-ProvisionningHistorique-Purge, PR_02241_P228_004]
	 */
	private void deleteObsoleteEntities(ProvisioningContext provisioningContext, Statement statement)
			throws SQLException, IOException {
		String filenamePrefix = ProvisioningFileData.FILENAME_PURGE_ENTITY_TO_DELETE;
		Path exportDirPath = Paths.get(Configuration.workExportPathName);

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				String.format("Deleting obsolete entities: %s.<yyyyMMddHHmmss>", filenamePrefix)));
		long startTime = Utils.getTime();
		final char SEP = ',';

		// List all matching files in the export directory for the given filename pattern.
		// Files suffixed by ".inprogress" should not be processed yet.
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(exportDirPath, path -> !Files.isDirectory(path)
				&& path.getFileName().toString().matches("\\Q" + filenamePrefix + "\\E" + "\\.[0-9]{14}"))) {
			for (Path exportFilePath : stream) {
				if (Files.exists(exportFilePath) && !Files.isDirectory(exportFilePath)) {
					LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
							String.format("Deleting obsolete entities: %s", exportFilePath)));

					// Read the export file: entity to delete pks
					List<Long> entityToDeletePks = new ArrayList<>();
					try (BufferedReader readerEntity = Utils.newBufferedReader(exportFilePath,
							ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.READER_BUFFER_SIZE)) {
						String line;
						while ((line = readerEntity.readLine()) != null) {
							entityToDeletePks.add(Long.valueOf(line));
						}
					}

					// Delete the obsolete entities in the RefReport MySQL database
					int cptDelete = 0;
					while (cptDelete < entityToDeletePks.size()) {
						int endCptDelete = Math.min(entityToDeletePks.size(),
								cptDelete + NB_BULK_DELETE_ENTITY_OBJECTS_LINK);
						List<Long> entityPksSubList = entityToDeletePks.subList(cptDelete, endCptDelete);

						String entityPksInSelection = StringUtils.join(entityPksSubList, SEP);

						addDeleteBatch(ReportsBookmark.TABLE_NAME, ReportsBookmark.COL_NAME_REPORTING_ENTITY_FK,
								statement, entityPksInSelection);
						addDeleteBatch(DataLocation.TABLE_NAME, DataLocation.COL_NAME_REPORTING_ENTITY_FK, statement,
								entityPksInSelection);
						addDeleteBatch(EntityAttribute.TABLE_NAME, EntityAttribute.COL_NAME_REPORTING_ENTITY_FK,
								statement, entityPksInSelection);
						addDeleteBatch(EntityAttributeList.TABLE_NAME, EntityAttributeList.COL_NAME_REPORTING_ENTITY_FK,
								statement, entityPksInSelection);
						addDeleteBatch(EntityGroupAttribute.TABLE_NAME,
								EntityGroupAttributeId.COL_NAME_REPORTING_ENTITY_FK, statement, entityPksInSelection);
						addDeleteBatch(EntityLinkAttribute.TABLE_NAME,
								EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_SRC_FK, statement,
								entityPksInSelection);
						addDeleteBatch(EntityLinkAttribute.TABLE_NAME,
								EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_DEST_FK, statement,
								entityPksInSelection);
						addDeleteBatch(EntityLink.TABLE_NAME, EntityLinkId.COL_NAME_REPORTING_ENTITY_SRC_FK, statement,
								entityPksInSelection);
						addDeleteBatch(EntityLink.TABLE_NAME, EntityLinkId.COL_NAME_REPORTING_ENTITY_DEST_FK, statement,
								entityPksInSelection);
						addDeleteBatch(ReportingGroupToEntities.TABLE_NAME,
								ReportingGroupToEntities.COL_NAME_REPORTING_ENTITY_FK, statement, entityPksInSelection);
						addDeleteBatch(ReportingEntity.TJ_NAME_SUBTYPES,
								ReportingEntity.TJ_COL_NAME_REPORTING_ENTITY_FK, statement, entityPksInSelection);
						statement.addBatch(String.format("UPDATE %s SET %s=NULL WHERE %s IN (%s)",
								ReportingEntity.TABLE_NAME, ReportingEntity.COL_NAME_PARENT,
								ReportingEntity.COL_NAME_PARENT, entityPksInSelection));
						addDeleteBatch(ReportingEntity.TABLE_NAME, ReportingEntity.COL_NAME_PK, statement,
								entityPksInSelection);

						int[] results = statement.executeBatch();

						for (int resultDelete : results) {
							switch (resultDelete) {
							case Statement.EXECUTE_FAILED:
								String errorMsg = String.format("Error deleting data for entityPks %s",
										StringUtils.join(entityPksSubList, SEP));
								provisioningContext.actionStatusTO.addInfo(errorMsg, resultDelete);
								provisioningContext.actionStatusTO.error = true;
								LOGGER.error(SOATools.buildSOALogMessage(provisioningContext.soaContext, errorMsg));
								break;
							default:
								if (LOGGER.isTraceEnabled()) {
									LOGGER.trace(SOATools.buildSOALogMessage(provisioningContext.soaContext,
											String.format("Data for entityPks %s are deleted",
													StringUtils.join(entityPksSubList, SEP))));
								}
								break;
							}
						}
						cptDelete += NB_BULK_DELETE_ENTITY_OBJECTS_LINK;
					}

					// Rename the export file (do not delete the export file)
					String refObjectFilename = exportFilePath.getFileName().toString();
					String refReportTimestamp = DateHelper.format(DateHelper.YYYYMMDD, DateHelper.getCurrentDate());
					String suffix = "done";
					String targetFilename = String.format("%s.%s.%s", refObjectFilename, refReportTimestamp, suffix);
					Files.move(exportFilePath, exportFilePath.resolveSibling(targetFilename),
							StandardCopyOption.REPLACE_EXISTING);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(SOATools.buildSOALogMessage(provisioningContext.soaContext,
								"Processed export file renamed to: " + targetFilename));
					}
				}
			}
		}

		LOGGER.info(SOATools.buildSOALogMessage(provisioningContext.soaContext,
				String.format("Obsolete entities deleted in %d ms", Utils.getTime() - startTime)));
	}

	/**
	 * Internal method: add a delete batch "<tt>DELETE FROM tableName WHERE fkFieldName IN (pksInSelection)</tt>" to the
	 * given SQL statement.
	 * 
	 * @param tableName
	 * @param fkFieldName
	 * @param statement
	 * @param entityPksInSelection
	 * @throws SQLException
	 */
	private void addDeleteBatch(String tableName, String fkFieldName, Statement statement, String pksInSelection)
			throws SQLException {
		statement.addBatch(String.format("DELETE FROM %s WHERE %s IN (%s)", tableName, fkFieldName, pksInSelection));
	}

}
