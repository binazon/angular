package com.orange.srs.refreport.business.delegate;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.h2.jdbc.JdbcSQLException;

import com.orange.srs.refreport.consumer.dao.PartitionStatusDAO;
import com.orange.srs.refreport.consumer.dao.ReportingEntityDAO;
import com.orange.srs.refreport.model.PartitionStatus;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.TO.ReportingEntityPartitionTO;
import com.orange.srs.refreport.model.exception.InventoryException;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.statcommon.business.commonFunctions.DataLocator;
import com.orange.srs.statcommon.consumer.jdbc.JDBCH2Consumer;
import com.orange.srs.statcommon.model.constant.H2Table;
import com.orange.srs.statcommon.model.parameter.ColumnParameter;
import com.orange.srs.statcommon.model.parameter.CreateTableParameter;
import com.orange.srs.statcommon.model.parameter.PatternParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.TechnicalException;
import com.orange.srs.statcommon.technical.jdbc.IndexStatementBuilder;

@Stateless
public class H2EntitiesPartitionsDelegate {

	private static final Logger LOGGER = Logger.getLogger(H2EntitiesPartitionsDelegate.class);

	@EJB
	private ReportingEntityDAO reportingEntityDAO;

	@EJB
	private PartitionStatusDAO partitionStatusDAO;

	private static final int commitSize = 2500;

	private static final String SELECT_PREFIX = "SELECT * FROM ";
	private static final String UNION_ALL = " UNION ALL ";
	private static final String TEMP_EXTENTION = ".tmp";

	public String exportEntitesPartitionsTables(SOAContext soaContext) throws InventoryException {

		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.DATE, -1);
		// Get the File localization
		PatternParameter patternParameter = new PatternParameter();
		patternParameter.properties = Configuration.mountConfiguration;
		patternParameter.startUnit = cal.getTime();

		List<String> resultFileLocations = DataLocator
				.getFirstEntitiesPartitionFileLocationWithoutExtension(patternParameter);
		int nbOfileLocations = resultFileLocations.size();
		String resultFileLocation = resultFileLocations.get(nbOfileLocations - 1);
		boolean h2ConnectionError = true;

		JDBCH2Consumer h2consumer = new JDBCH2Consumer();

		do {
			try {
				h2consumer.createConnexion(resultFileLocation + Configuration.h2FileReadwriteLockOptionsProperty, false,
						false);
				LOGGER.debug("[exportEntitiesPartitions] -> Localisation " + resultFileLocation);
				List<String> entityTypes = fillEntitiesPartitionsTable(h2consumer, patternParameter, soaContext);
				fillPartitionsStatusTable(h2consumer);
				// suppress old file

				List<String> originList = new ArrayList<String>();
				originList.add("SCE");
				originList.add("ALL");
				originList.add("EQUANT");
				h2ConnectionError = false;
			} catch (JdbcSQLException jdbce) {
				if (JDBCH2Consumer.H2_FILE_READONLY_DATABASE_ERROR_CODES.contains(jdbce.getErrorCode())
						&& nbOfileLocations > 0) {
					nbOfileLocations--;
					resultFileLocation = resultFileLocations.get(nbOfileLocations);
					h2ConnectionError = true;
				} else {
					LOGGER.error(SOATools.buildSOALogMessage(soaContext,
							"[exportEntitesPartitions] KO" + jdbce.getMessage()), jdbce);
					throw new InventoryException(jdbce.getMessage(), jdbce);
				}

			} catch (Exception e) {
				LOGGER.error(SOATools.buildSOALogMessage(soaContext, "[exportEntitesPartitions] KO" + e.getMessage()),
						e);
				throw new InventoryException(e.getMessage(), e);
			} finally {
				try {
					h2consumer.closeConnexion();
				} catch (Exception e2) {
					LOGGER.error(SOATools.buildSOALogMessage(soaContext,
							"[exportEntitesPartitions] Cannot close connection " + e2.getMessage()), e2);
					throw new InventoryException(e2.getMessage(), e2);
				}
			}
		} while (h2ConnectionError);
		return resultFileLocation;
	}

	public List<ReportingEntityPartitionTO> getEntitiesPartitions(SOAContext soaContext) {
		List<ReportingEntityPartitionTO> entityPartitionsTOs = new ArrayList<ReportingEntityPartitionTO>();
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.DATE, -1);
		// Get the File localization
		PatternParameter patternParameter = new PatternParameter();
		patternParameter.properties = Configuration.mountConfiguration;
		patternParameter.startUnit = cal.getTime();
		JDBCH2Consumer h2consumer = new JDBCH2Consumer();
		Statement stat = null;
		ResultSet res = null;
		List<String> listOfAvailableEntitiesPartitionFileLocations = DataLocator
				.getEntitiesPartitionFileLocationsWithoutExtension(patternParameter);
		int nbOfentitiesPartitionFiles = listOfAvailableEntitiesPartitionFileLocations.size();
		int currentIndex = 0;
		boolean h2ConnectionError = true;
		String entitiesPartitionFileLocation = listOfAvailableEntitiesPartitionFileLocations.get(currentIndex);
		try {
			do {
				try {

					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace(SOATools.buildSOALogMessage(soaContext,
								"[getEntitesPartitions] Reading entityPartitionFile " + entitiesPartitionFileLocation));
					}
					h2consumer.createConnexion(
							entitiesPartitionFileLocation + Configuration.h2FileReadonlyNolockOptionsProperty, false,
							true);
					Set<String> tableList = h2consumer.getTableList();
					StringBuilder query = new StringBuilder();
					boolean isFirst = true;
					for (String table : tableList) {
						if (!table.equalsIgnoreCase(H2Table.TABLE_NAME_PARTITION_STATUS)) {
							if (isFirst) {
								isFirst = false;
							} else {
								query.append(UNION_ALL);
							}
							query.append(SELECT_PREFIX).append(table);
						}
					}
					stat = h2consumer.getConnection().createStatement();
					res = stat.executeQuery(query.toString());
					while (res.next()) {
						String entityId = res.getString(H2Table.EntitiesPartitions.COLUMN_ENTITY_ID);
						String origin = res.getString(H2Table.EntitiesPartitions.COLUMN_ORIGIN);
						String partition = res.getString(H2Table.EntitiesPartitions.COLUMN_PARTITIONS);
						ReportingEntityPartitionTO entity = new ReportingEntityPartitionTO(null, entityId, origin, null,
								partition);
						entityPartitionsTOs.add(entity);
					}
					h2ConnectionError = false;
				} catch (JdbcSQLException jsqle) {
					if (JDBCH2Consumer.H2_FILE_SOCKET_DECONNECTION_ERROR_CODES.contains(jsqle.getErrorCode())
							&& currentIndex < nbOfentitiesPartitionFiles) {
						h2ConnectionError = true;
						currentIndex++;
						entitiesPartitionFileLocation = listOfAvailableEntitiesPartitionFileLocations.get(currentIndex);
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
									"[getEntitesPartitions] H2 connection error (" + jsqle.getErrorCode()
											+ ") accessing H2 Table entity PartitionFile: " + jsqle.getMessage()));
						}
					} else {
						throw new InventoryException(jsqle.getMessage(), jsqle);
					}
				}
			} while (h2ConnectionError);
		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "[getEntitesPartitions] KO " + e.getMessage()), e);
			throw new InventoryException(e.getMessage(), e);
		} finally {
			try {
				if (res != null) {
					res.close();
				}
				if (stat != null) {
					stat.close();
				}
				h2consumer.closeConnexion();
			} catch (Exception e2) {
				LOGGER.error(SOATools.buildSOALogMessage(soaContext,
						"[getEntitesPartitions] Cannot close connection " + e2.getMessage()), e2);
				throw new InventoryException(e2.getMessage(), e2);
			}
		}

		return entityPartitionsTOs;
	}

	public List<PartitionStatus> getPartitionsStatus(SOAContext soaContext) {
		List<PartitionStatus> partitions = new ArrayList<PartitionStatus>();
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.DATE, -1);
		// Get the File localization
		PatternParameter patternParameter = new PatternParameter();
		patternParameter.properties = Configuration.mountConfiguration;
		patternParameter.startUnit = cal.getTime();
		JDBCH2Consumer h2consumer = new JDBCH2Consumer();
		Statement stat = null;
		ResultSet res = null;
		List<String> listOfAvailableEntitiesPartitionFileLocations = DataLocator
				.getEntitiesPartitionFileLocationsWithExtension(patternParameter);
		int nbOfentitiesPartitionFiles = listOfAvailableEntitiesPartitionFileLocations.size();
		int currentIndex = 0;
		boolean h2ConnectionError = true;
		String entitiesPartitionFileLocation = listOfAvailableEntitiesPartitionFileLocations.get(currentIndex);
		try {
			do {
				try {
					h2consumer.createConnexion(
							entitiesPartitionFileLocation + Configuration.h2FileReadonlyNolockOptionsProperty, false,
							true);
					String query = SELECT_PREFIX + H2Table.TABLE_NAME_PARTITION_STATUS;
					stat = h2consumer.getConnection().createStatement();
					res = stat.executeQuery(query);
					while (res.next()) {
						PartitionStatus partition = new PartitionStatus();
						partition.date = res.getString(H2Table.PartitionsStatus.COLUMN_DATE);
						partition.partitionNumber = res.getInt(H2Table.PartitionsStatus.COLUMN_PARTITION_NUMBER);
						partition.numberOfEntity = res.getInt(H2Table.PartitionsStatus.COLUMN_ELEMENT_COUNT);
						partitions.add(partition);
					}
					h2ConnectionError = false;
				} catch (JdbcSQLException jsqle) {
					if (JDBCH2Consumer.H2_FILE_SOCKET_DECONNECTION_ERROR_CODES.contains(jsqle.getErrorCode())
							&& currentIndex < nbOfentitiesPartitionFiles) {
						h2ConnectionError = true;
						currentIndex++;
						entitiesPartitionFileLocation = listOfAvailableEntitiesPartitionFileLocations.get(currentIndex);
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
									"[getPartitionStatus] H2 connection error (" + jsqle.getErrorCode()
											+ ") accessing H2 Table entity PartitionFile: " + jsqle.getMessage()));
						}
					} else {
						throw new InventoryException(jsqle.getMessage(), jsqle);
					}
				}
			} while (h2ConnectionError);
		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "[getPartitionStatus] KO " + e.getMessage()), e);
			throw new InventoryException(e.getMessage(), e);
		} finally {
			try {
				if (res != null) {
					res.close();
				}
				if (stat != null) {
					stat.close();
				}
				h2consumer.closeConnexion();
			} catch (Exception e2) {
				LOGGER.error(SOATools.buildSOALogMessage(soaContext,
						"[getPartitionStatus] Cannot close connection " + e2.getMessage()), e2);
				throw new InventoryException(e2.getMessage(), e2);
			}
		}
		return partitions;
	}

	private PreparedStatement createEntitiesPartitionsTable(JDBCH2Consumer h2consumer, String entityType)
			throws TechnicalException, SQLException {
		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = StringUtils.replace(H2Table.TABLE_NAME_ENTITIES_PARTITIONS, "{ENTITYTYPE}", entityType);

		ColumnParameter entityId = new ColumnParameter();
		entityId.columnName = H2Table.EntitiesPartitions.COLUMN_ENTITY_ID;
		entityId.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(entityId);
		cparameter.keys.add(entityId);

		ColumnParameter origin = new ColumnParameter();
		origin.columnName = H2Table.EntitiesPartitions.COLUMN_ORIGIN;
		origin.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;
		cparameter.columns.add(origin);
		cparameter.keys.add(origin);

		ColumnParameter partitions = new ColumnParameter();
		partitions.columnName = H2Table.EntitiesPartitions.COLUMN_PARTITIONS;
		partitions.columnType = JDBCH2Consumer.VARCHAR_TYPE;
		cparameter.columns.add(partitions);

		PreparedStatement statement = h2consumer.createTableAndPreparedStatement(cparameter, false, true);
		LOGGER.debug("[exportEntitesPartitions] Adding table " + cparameter.tableName);
		return statement;
	}

	private void createIndexOnTable(JDBCH2Consumer h2consumer, String entityType) throws SQLException {
		String[] index = { H2Table.EntitiesPartitions.COLUMN_ENTITY_ID };
		String tableName = StringUtils.replace(H2Table.TABLE_NAME_ENTITIES_PARTITIONS, "{ENTITYTYPE}", entityType);
		IndexStatementBuilder ibuilder = new IndexStatementBuilder(h2consumer.getConnection(), tableName, index);
		ibuilder.executeStatement();
		h2consumer.commit();
	}

	private void callCSVWriteFunctionOnTable(JDBCH2Consumer h2consumer, PatternParameter patternParameter)
			throws SQLException, IOException {
		String tableName = StringUtils.replace(H2Table.TABLE_NAME_ENTITIES_PARTITIONS, "{ENTITYTYPE}",
				patternParameter.entityType);
		Statement stat = h2consumer.getConnection().createStatement();
		List<String> possibleOutputFiles = DataLocator
				.getFirstEntitiesPartitionCsvFileLocationWithExtension(patternParameter);
		int nbOfPossibleOutputFile = possibleOutputFiles.size() - 1;
		String outputFile = possibleOutputFiles.get(nbOfPossibleOutputFile);
		boolean writeError = true;
		while (writeError) {
			try {
				FileUtils.deleteQuietly(FileUtils.getFile(outputFile));
				String query = "CALL CSVWRITE ('" + outputFile + TEMP_EXTENTION
						+ "', 'SELECT ENTITY_ID, PARTITIONS FROM " + tableName + " WHERE ORIGIN=''"
						+ patternParameter.origin
						+ "'' ORDER BY ENTITY_ID','charset=UTF-8 fieldDelimiter=  writeColumnHeader=false');";
				stat.execute(query);
				stat.close();
				h2consumer.commit();
				FileUtils.moveFile(FileUtils.getFile(outputFile + TEMP_EXTENTION), FileUtils.getFile(outputFile));
				writeError = false;
			} catch (Exception e) {
				if (nbOfPossibleOutputFile > 0) {
					nbOfPossibleOutputFile--;
					outputFile = possibleOutputFiles.get(nbOfPossibleOutputFile);
				} else {
					throw e;
				}
			}

		}
	}

	private List<String> fillEntitiesPartitionsTable(JDBCH2Consumer h2consumer, PatternParameter patternParameter,
			SOAContext soaContext) throws TechnicalException, SQLException {
		List<String> entityTypes = new ArrayList<>();
		String[] attributs = { ReportingEntity.FIELD_ENTITYTYPE, ReportingEntity.FIELD_PK };
		List<Long> entitiesOrdered = reportingEntityDAO.findPkOrderBy(attributs, false);

		int startIndex = 0;
		String previewsType = null;
		PreparedStatement partitionStatement = null;
		int commit = 0;
		while (startIndex < entitiesOrdered.size()) {
			int endIndex = ((startIndex + Configuration.h2PaginationSize) <= entitiesOrdered.size())
					? startIndex + Configuration.h2PaginationSize
					: entitiesOrdered.size();
			List<Long> pkSelected = entitiesOrdered.subList(startIndex, endIndex);
			List<ReportingEntityPartitionTO> entitiesPartitions = reportingEntityDAO
					.findEntityPartitionWherePkInSelectionOrderByType(pkSelected);

			startIndex = endIndex;
			for (ReportingEntityPartitionTO entityPartition : entitiesPartitions) {
				commit++;
				if (!StringUtils.equalsIgnoreCase(previewsType, entityPartition.getType())) {
					if (partitionStatement != null) {
						partitionStatement.close();
						h2consumer.commit();
						commit = 0;
						createIndexOnTable(h2consumer, previewsType);
						try {
							patternParameter.entityType = previewsType;
							patternParameter.origin = "SCE";
							callCSVWriteFunctionOnTable(h2consumer, patternParameter);
							patternParameter.origin = "EQUANT";
							callCSVWriteFunctionOnTable(h2consumer, patternParameter);
							patternParameter.origin = "ALL";
							callCSVWriteFunctionOnTable(h2consumer, patternParameter);
						} catch (IOException e) {
							LOGGER.error(SOATools.buildSOALogMessage(soaContext,
									"[fillEntitiesPartitionsTable] An error occurded during the export of H2 partition table to csv files: "
											+ e.getMessage()),
									e);
						}

					}
					// Creation of the first table and the statement
					entityTypes.add(entityPartition.getType());
					partitionStatement = createEntitiesPartitionsTable(h2consumer, entityPartition.getType());
				}
				partitionStatement.setString(1, entityPartition.getEntityId());
				partitionStatement.setString(2, entityPartition.getOrigin());
				if (entityPartition.getPartitionNumber() == null) {
					partitionStatement.setNull(3, java.sql.Types.VARCHAR);
				} else {
					partitionStatement.setString(3, entityPartition.getPartitionNumber());
				}
				partitionStatement.execute();
				previewsType = entityPartition.getType();
				if (commit == commitSize) {
					h2consumer.commit();
					commit = 0;
				}
			}
		}
		if (partitionStatement != null) {
			partitionStatement.close();
			h2consumer.commit();
			createIndexOnTable(h2consumer, previewsType);
		}
		return entityTypes;
	}

	private void fillPartitionsStatusTable(JDBCH2Consumer h2consumer) throws TechnicalException, SQLException {
		// Creation of the Status table and the statement
		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_PARTITION_STATUS;

		ColumnParameter date = new ColumnParameter();
		date.columnName = H2Table.PartitionsStatus.COLUMN_DATE;
		date.columnType = JDBCH2Consumer.VARCHAR + "(6)";
		cparameter.columns.add(date);

		ColumnParameter partitionNumber = new ColumnParameter();
		partitionNumber.columnName = H2Table.PartitionsStatus.COLUMN_PARTITION_NUMBER;
		partitionNumber.columnType = JDBCH2Consumer.INT;
		cparameter.columns.add(partitionNumber);

		ColumnParameter elementCount = new ColumnParameter();
		elementCount.columnName = H2Table.PartitionsStatus.COLUMN_ELEMENT_COUNT;
		elementCount.columnType = JDBCH2Consumer.INT;
		cparameter.columns.add(elementCount);

		PreparedStatement statusStatement = h2consumer.createTableAndPreparedStatement(cparameter, false, true);

		List<PartitionStatus> partitionStatus = partitionStatusDAO.findAll();
		for (PartitionStatus partition : partitionStatus) {
			statusStatement.setString(1, partition.date);
			statusStatement.setInt(2, partition.partitionNumber);
			statusStatement.setInt(3, partition.numberOfEntity);
			statusStatement.execute();
		}
		statusStatement.close();
		h2consumer.commit();
	}
}
