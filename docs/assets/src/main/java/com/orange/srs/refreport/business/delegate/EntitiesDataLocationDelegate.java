package com.orange.srs.refreport.business.delegate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.orange.srs.refreport.consumer.dao.ReportingEntityDAO;
import com.orange.srs.refreport.model.DataLocation;
import com.orange.srs.refreport.model.TO.ProvisioningActionStatusTO;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.statcommon.business.commonFunctions.FDNDelegate;
import com.orange.srs.statcommon.model.constant.ProvisioningFileData;
import com.orange.srs.statcommon.model.enums.EntityTypeEnum;
import com.orange.srs.statcommon.model.enums.OriginEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

@Stateless
public class EntitiesDataLocationDelegate {

	private static final Logger LOGGER = Logger.getLogger(EntitiesDataLocationDelegate.class);

	private static final List<String> FILE_DATA_LOCATION_COL_NAMES = Lists.newArrayList(
			DataLocation.COL_NAME_CRITERIA_TYPE, DataLocation.COL_NAME_CRITERIA_VALUE,
			DataLocation.COL_NAME_LOCATION_PATTERN, DataLocation.COL_NAME_REPORTING_ENTITY_FK);

	@Resource(lookup = "dataSourceRefReport")
	private DataSource refReportDataSource;

	@EJB
	private ReportingEntityDAO reportingEntityDAO;

	private static final String DATA_LOCATION_DEFAULT_TYPE = "default";
	private static final String DATA_LOCATION_DEFAULT_VALUE = "DI";
	private static final String DATA_LOCATION_REPORTINGGROUP_TYPE = "reportingGroup";
	private static final String DATA_LOCATION_REPORTINGGROUP_VALUE = "REPORTINGGROUPREF";

	public ProvisioningActionStatusTO updateEntitiesDataLocation(SOAContext soaContext,
			ProvisioningActionStatusTO actionStatus, Map<String, String> mapReportinGroupEquantByDomain,
			Map<String, String> mapReportinGroupSceByDomain) throws IOException, SQLException {

		Map<String, Map<String, String>> mapReportinGroupByDomainByOrigin = new HashMap<>();
		mapReportinGroupByDomainByOrigin.put(OriginEnum.EQUANT.getValue(), mapReportinGroupEquantByDomain);
		mapReportinGroupByDomainByOrigin.put(OriginEnum.SCE.getValue(), mapReportinGroupSceByDomain);
		Map<String, String> mapReportinGroupAllByDomain = new HashMap<>(mapReportinGroupEquantByDomain);
		mapReportinGroupAllByDomain.putAll(mapReportinGroupSceByDomain);
		mapReportinGroupByDomainByOrigin.put(OriginEnum.ALL.getValue(), mapReportinGroupAllByDomain);

		Path dataLocationPathTmp = Paths.get(Configuration.tmpDirectory,
				ProvisioningFileData.FILENAME_ENTITY_DATA_LOCATION);
		try (BufferedWriter dataLocationWriter = Utils.newBufferedWriter(dataLocationPathTmp,
				ProvisioningFileData.DATA_CHARSET, ProvisioningFileData.WRITER_BUFFER_SIZE)) {

			// --- Equipment
			addDIAndNoEquantRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter,
					EntityTypeEnum.EQUIPMENT, mapReportinGroupByDomainByOrigin);

			// --- PhysicalConnection
			addDIAndRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter,
					EntityTypeEnum.PHYSICALCONNECTION, mapReportinGroupByDomainByOrigin);

			// --- LogicalConnection
			addDIAndRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter,
					EntityTypeEnum.LOGICALCONNECTION, mapReportinGroupByDomainByOrigin);

			// --- Hub Phone
			addDomainAndRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter, EntityTypeEnum.HUBPHONE,
					mapReportinGroupByDomainByOrigin);

			// --- Measure
			addDIAndNoEquantRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter, EntityTypeEnum.MEASURE,
					mapReportinGroupByDomainByOrigin);

			// --- COS
			addDIAndNoEquantRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter, EntityTypeEnum.COS,
					mapReportinGroupByDomainByOrigin);

			// --- GkSite
			addRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter, EntityTypeEnum.GKSITE,
					mapReportinGroupByDomainByOrigin);

			// --- GkSan
			addRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter, EntityTypeEnum.GKSAN,
					mapReportinGroupByDomainByOrigin);

			// --- GkTrunk
			addRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter, EntityTypeEnum.GKTRUNK,
					mapReportinGroupByDomainByOrigin);

			// --- GkVpn
			addRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter, EntityTypeEnum.GKVPN,
					mapReportinGroupByDomainByOrigin);

			// --- GKSBC
			addRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter, EntityTypeEnum.GKSBC,
					mapReportinGroupByDomainByOrigin);

			// --- GK Shared Call limiter
			addRGDataLocationsToFile(soaContext, actionStatus, dataLocationWriter, EntityTypeEnum.GKSHAREDCALLLIMITER,
					mapReportinGroupByDomainByOrigin);

		}

		long startLoad = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Loading data location file in database"));
		try (Connection refReportConnection = refReportDataSource.getConnection();
				Statement statement = refReportConnection.createStatement()) {

			int resultUdpate = statement.executeUpdate(buildDataLocationLoadQuery(dataLocationPathTmp));
			switch (resultUdpate) {
			case Statement.EXECUTE_FAILED:
				String errorMsg = "Error loading dataLocations file " + dataLocationPathTmp;
				LOGGER.error(SOATools.buildSOALogMessage(soaContext, errorMsg));
				break;
			default:
				String successMsg = "File " + dataLocationPathTmp + " loaded in " + (Utils.getTime() - startLoad)
						+ " ms";
				LOGGER.info(SOATools.buildSOALogMessage(soaContext, successMsg));
				break;
			}
		}

		Files.deleteIfExists(dataLocationPathTmp);

		return actionStatus;

	}

	private void addDIAndNoEquantRGDataLocationsToFile(SOAContext soaContext, ProvisioningActionStatusTO actionStatus,
			BufferedWriter dataLocationWriter, EntityTypeEnum entityType,
			Map<String, Map<String, String>> mapReportinGroupByDomainByOrigin) throws IOException {

		long startTime = Utils.getTime();
		int nbDataLocationAdded = 0;
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Process data location for " + entityType.getValue()));

		List<Object[]> dataLocationsInfo = reportingEntityDAO.findEntityDataLocationInfo(entityType.getValue());
		int dataLocationSize = dataLocationsInfo.size();
		LOGGER.info(
				SOATools.buildSOALogMessage(soaContext, "Found " + dataLocationSize + " data locations info for type "
						+ entityType.getValue() + " in " + (Utils.getTime() - startTime) + " ms"));

		int nextLog = Configuration.paginationSize;
		int lastNbDataLocation = 0;
		long startStep = Utils.getTime();

		for (Object[] dataInfo : dataLocationsInfo) {

			Long entityPk = ((BigInteger) dataInfo[0]).longValue();
			String entityId = (String) dataInfo[1];
			String origin = (String) dataInfo[2];
			String domain = (String) dataInfo[3];

			addDIDataLocation(dataLocationWriter, entityPk, entityId);
			nbDataLocationAdded++;
			if (!OriginEnum.EQUANT.getValue().equals(origin)) {
				if (addReportingGroupDataLocation(dataLocationWriter, entityPk,
						mapReportinGroupByDomainByOrigin.get(origin), domain)) {
					nbDataLocationAdded++;
				}
			}

			if (LOGGER.isTraceEnabled() && (nbDataLocationAdded > nextLog)) {
				LOGGER.trace((nbDataLocationAdded - lastNbDataLocation) + " more data locations added to file - up to "
						+ nbDataLocationAdded + " for a maximum of " + (2 * dataLocationSize) + " in "
						+ (Utils.getTime() - startStep) + " ms.");
				lastNbDataLocation = nbDataLocationAdded;
				startStep = Utils.getTime();
				nextLog += Configuration.paginationSize;
			}

		}

		actionStatus.addInfo("nbDataLocationAdded" + entityType.getValue(), nbDataLocationAdded);
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbDataLocationAdded + " data locations added to file for "
				+ entityType.getValue() + " in " + (Utils.getTime() - startTime) + " ms"));

	}

	private void addDIAndRGDataLocationsToFile(SOAContext soaContext, ProvisioningActionStatusTO actionStatus,
			BufferedWriter dataLocationWriter, EntityTypeEnum entityType,
			Map<String, Map<String, String>> mapReportinGroupByDomainByOrigin) throws IOException {

		long startTime = Utils.getTime();
		int nbDataLocationAdded = 0;
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Process data location for " + entityType.getValue()));

		List<Object[]> dataLocationsInfo = reportingEntityDAO.findEntityDataLocationInfo(entityType.getValue());
		int dataLocationSize = dataLocationsInfo.size();
		LOGGER.info(
				SOATools.buildSOALogMessage(soaContext, "Found " + dataLocationSize + " data locations info for type "
						+ entityType.getValue() + " in " + (Utils.getTime() - startTime) + " ms"));

		int nextLog = Configuration.paginationSize;
		int lastNbDataLocation = 0;
		long startStep = Utils.getTime();

		for (Object[] dataInfo : dataLocationsInfo) {

			Long entityPk = ((BigInteger) dataInfo[0]).longValue();
			String entityId = (String) dataInfo[1];
			String origin = (String) dataInfo[2];
			String domain = (String) dataInfo[3];

			addDIDataLocation(dataLocationWriter, entityPk, entityId);
			nbDataLocationAdded++;
			if (addReportingGroupDataLocation(dataLocationWriter, entityPk,
					mapReportinGroupByDomainByOrigin.get(origin), domain)) {
				nbDataLocationAdded++;
			}

			if (LOGGER.isTraceEnabled() && (nbDataLocationAdded > nextLog)) {
				LOGGER.trace((nbDataLocationAdded - lastNbDataLocation) + " more data locations added to file - up to "
						+ nbDataLocationAdded + " for a maximum of " + (2 * dataLocationSize) + " in "
						+ (Utils.getTime() - startStep) + " ms.");
				lastNbDataLocation = nbDataLocationAdded;
				startStep = Utils.getTime();
				nextLog += Configuration.paginationSize;
			}

		}

		actionStatus.addInfo("nbDataLocationAdded" + entityType.getValue(), nbDataLocationAdded);
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbDataLocationAdded + " data locations added to file for "
				+ entityType.getValue() + " in " + (Utils.getTime() - startTime) + " ms"));

	}

	private void addDomainAndRGDataLocationsToFile(SOAContext soaContext, ProvisioningActionStatusTO actionStatus,
			BufferedWriter dataLocationWriter, EntityTypeEnum entityType,
			Map<String, Map<String, String>> mapReportinGroupByDomainByOrigin) throws IOException {

		long startTime = Utils.getTime();
		int nbDataLocationAdded = 0;
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Process data location for " + entityType.getValue()));

		List<Object[]> dataLocationsInfo = reportingEntityDAO.findEntityDataLocationInfo(entityType.getValue());
		int dataLocationSize = dataLocationsInfo.size();
		LOGGER.info(
				SOATools.buildSOALogMessage(soaContext, "Found " + dataLocationSize + " data locations info for type "
						+ entityType.getValue() + " in " + (Utils.getTime() - startTime) + " ms"));

		int nextLog = Configuration.paginationSize;
		int lastNbDataLocation = 0;
		long startStep = Utils.getTime();

		for (Object[] dataInfo : dataLocationsInfo) {

			Long entityPk = ((BigInteger) dataInfo[0]).longValue();
			String origin = (String) dataInfo[2];
			String domain = (String) dataInfo[3];

			addDomainDataLocation(dataLocationWriter, entityPk, domain);
			nbDataLocationAdded++;
			if (addReportingGroupDataLocation(dataLocationWriter, entityPk,
					mapReportinGroupByDomainByOrigin.get(origin), domain)) {
				nbDataLocationAdded++;
			}

			if (LOGGER.isTraceEnabled() && (nbDataLocationAdded > nextLog)) {
				LOGGER.trace((nbDataLocationAdded - lastNbDataLocation) + " more data locations added to file - up to "
						+ nbDataLocationAdded + " for a maximum of " + (2 * dataLocationSize) + " in "
						+ (Utils.getTime() - startStep) + " ms.");
				lastNbDataLocation = nbDataLocationAdded;
				startStep = Utils.getTime();
				nextLog += Configuration.paginationSize;
			}

		}

		actionStatus.addInfo("nbDataLocationAdded" + entityType.getValue(), nbDataLocationAdded);
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbDataLocationAdded + " data locations added to file for "
				+ entityType.getValue() + " in " + (Utils.getTime() - startTime) + " ms"));

	}

	private void addRGDataLocationsToFile(SOAContext soaContext, ProvisioningActionStatusTO actionStatus,
			BufferedWriter dataLocationWriter, EntityTypeEnum entityType,
			Map<String, Map<String, String>> mapReportinGroupByDomainByOrigin) throws IOException {

		long startTime = Utils.getTime();
		int nbDataLocationAdded = 0;
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Process data location for " + entityType.getValue()));

		List<Object[]> dataLocationsInfo = reportingEntityDAO.findEntityDataLocationInfo(entityType.getValue());
		int dataLocationSize = dataLocationsInfo.size();
		LOGGER.info(
				SOATools.buildSOALogMessage(soaContext, "Found " + dataLocationSize + " data locations info for type "
						+ entityType.getValue() + " in " + (Utils.getTime() - startTime) + " ms"));

		int nextLog = Configuration.paginationSize;
		int lastNbDataLocation = 0;
		long startStep = Utils.getTime();

		for (Object[] dataInfo : dataLocationsInfo) {

			Long entityPk = ((BigInteger) dataInfo[0]).longValue();
			String origin = (String) dataInfo[2];
			String domain = (String) dataInfo[3];

			if (addReportingGroupDataLocation(dataLocationWriter, entityPk,
					mapReportinGroupByDomainByOrigin.get(origin), domain)) {
				nbDataLocationAdded++;
			}

			if (LOGGER.isTraceEnabled() && (nbDataLocationAdded > nextLog)) {
				LOGGER.trace((nbDataLocationAdded - lastNbDataLocation) + " more data locations added to file - up to "
						+ nbDataLocationAdded + " for a maximum of " + dataLocationSize + " in "
						+ (Utils.getTime() - startStep) + " ms.");
				lastNbDataLocation = nbDataLocationAdded;
				startStep = Utils.getTime();
				nextLog += Configuration.paginationSize;
			}

		}

		actionStatus.addInfo("nbDataLocationAdded" + entityType.getValue(), nbDataLocationAdded);
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, nbDataLocationAdded + " data locations added to file for "
				+ entityType.getValue() + " in " + (Utils.getTime() - startTime) + " ms"));

	}

	private void addDIDataLocation(BufferedWriter dataLocationWriter, Long entityPk, String entityId)
			throws IOException {
		dataLocationWriter.append(DATA_LOCATION_DEFAULT_TYPE).append(ProvisioningFileData.DATA_SEPARATOR);
		dataLocationWriter.append(DATA_LOCATION_DEFAULT_VALUE).append(ProvisioningFileData.DATA_SEPARATOR);
		dataLocationWriter.append(FDNDelegate.getDIFromEntityId(entityId)).append(ProvisioningFileData.DATA_SEPARATOR);
		dataLocationWriter.append(Long.toString(entityPk));
		dataLocationWriter.newLine();
	}

	private void addDomainDataLocation(BufferedWriter dataLocationWriter, Long entityPk, String domain)
			throws IOException {
		dataLocationWriter.append(DATA_LOCATION_DEFAULT_TYPE).append(ProvisioningFileData.DATA_SEPARATOR);
		dataLocationWriter.append(DATA_LOCATION_DEFAULT_VALUE).append(ProvisioningFileData.DATA_SEPARATOR);
		dataLocationWriter.append(domain).append(ProvisioningFileData.DATA_SEPARATOR);
		dataLocationWriter.append(Long.toString(entityPk));
		dataLocationWriter.newLine();
	}

	private boolean addReportingGroupDataLocation(BufferedWriter dataLocationWriter, Long entityPk,
			Map<String, String> mapReportinGroupByDomain, String domain) throws IOException {
		String locationPattern = mapReportinGroupByDomain.get(domain);
		if (locationPattern != null) {
			dataLocationWriter.append(DATA_LOCATION_REPORTINGGROUP_TYPE).append(ProvisioningFileData.DATA_SEPARATOR);
			dataLocationWriter.append(DATA_LOCATION_REPORTINGGROUP_VALUE).append(ProvisioningFileData.DATA_SEPARATOR);
			dataLocationWriter.append(locationPattern).append(ProvisioningFileData.DATA_SEPARATOR);
			dataLocationWriter.append(Long.toString(entityPk));
			dataLocationWriter.newLine();
		}
		return locationPattern != null;
	}

	private String buildDataLocationLoadQuery(Path filePath) {
		String fileName = StringUtils.replaceChars(filePath.toString(), '\\', '/');
		StringBuilder loadSqlQuery = new StringBuilder("LOAD DATA LOCAL INFILE '").append(fileName).append('\'');
		loadSqlQuery.append(" INTO TABLE ").append(DataLocation.TABLE_NAME);
		loadSqlQuery.append(" CHARACTER SET latin1");
		loadSqlQuery.append(" FIELDS TERMINATED BY '").append(ProvisioningFileData.DATA_SEPARATOR)
				.append("' ESCAPED BY '").append(ProvisioningFileData.ESCAPE_CHARACTER);
		loadSqlQuery.append("' LINES TERMINATED BY '").append(System.lineSeparator()).append("' (");
		boolean first = true;
		for (String colName : FILE_DATA_LOCATION_COL_NAMES) {
			if (!first) {
				loadSqlQuery.append(',');
			}
			loadSqlQuery.append(colName);
			first = false;
		}
		loadSqlQuery.append(")");
		return loadSqlQuery.toString();
	}
}
