package com.orange.srs.refreport.business.delegate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.consumer.dao.PartitionStatusDAO;
import com.orange.srs.refreport.consumer.dao.ReportingEntityDAO;
import com.orange.srs.refreport.model.PartitionStatus;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.TO.ProvisioningActionStatusTO;
import com.orange.srs.refreport.model.TO.ReportingEntityPartitionTO;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

/**
 * Delegate used to update entities partitions
 * 
 * @author A159138
 */
@Stateless
public class EntitiesPartitionsDelegate {

	private static final Logger logger = Logger.getLogger(EntitiesPartitionsDelegate.class);

	@EJB
	private ReportingEntityDAO reportingEntityDao;

	@EJB
	private PartitionStatusDAO partitionStatusDao;

	@Resource(lookup = "dataSourceRefReport")
	private DataSource defaultDataSource;

	private static final FastDateFormat formatYYYYMM = FastDateFormat.getInstance("yyyyMM");

	private static final int numberOfMonth = 26;

	public List<PartitionStatus> updatePartition(ProvisioningActionStatusTO actionStatus, SOAContext soaContext) {
		// Get the Entities which the partition number must be updated
		Map<String, PartitionStatus> partitionStatus = new TreeMap<>();
		List<PartitionStatus> status = partitionStatusDao.findAll();
		Date yesterdayDate = Utils.getYesterdayDate();
		Calendar startDate = GregorianCalendar.getInstance();
		startDate.setTime(yesterdayDate);
		startDate.add(Calendar.YEAR, -2);
		String startDateFormated = formatYYYYMM.format(startDate.getTime());

		for (PartitionStatus partition : status) {
			if (partition.date.compareTo(startDateFormated) >= 0) {
				partitionStatus.put(partition.date, partition);
			} else {
				partitionStatusDao.remove(partition);
			}
		}

		// Get the Entities PK Ordered by Group and Type
		List<Long> entitiesOrdered = reportingEntityDao.findPkOrderedByGroupAndType();
		long nbEntitiesUpdated = 0L;
		int startIndex = 0;
		int nbUpdatedEntitiesForPartition = 0;
		while (startIndex < entitiesOrdered.size()) {
			int endIndex = ((startIndex + Configuration.h2PaginationSize) <= entitiesOrdered.size())
					? startIndex + Configuration.h2PaginationSize
					: entitiesOrdered.size();
			List<Long> pkSelected = entitiesOrdered.subList(startIndex, endIndex);
			List<ReportingEntityPartitionTO> entities = reportingEntityDao
					.findEntityPartitionWherePkInSelectionOrderByGroupAndType(pkSelected);
			startIndex = endIndex;

			List<ReportingEntityPartitionTO> entitiesToSave = new ArrayList<>();
			for (ReportingEntityPartitionTO entityTO : entities) {
				// update the Partition Numbers
				String partitionNumber = null;
				if (StringUtils.isEmpty(entityTO.getPartitionNumber())) {
					partitionNumber = computePartitionNumber(startDateFormated, partitionStatus, yesterdayDate);
				} else {
					partitionNumber = updatePartitionNumber(entityTO.getPartitionNumber(), startDateFormated,
							partitionStatus, yesterdayDate);
				}
				// Save the partition
				if (partitionNumber != null) {
					entityTO.setPartitionNumber(partitionNumber.toString());
					entitiesToSave.add(entityTO);
				}
			}
			entities.clear();
			saveEntitesPartitionsWithPk(entitiesToSave, soaContext);

			if (logger.isDebugEnabled() && entitiesToSave.size() != 0) {
				nbEntitiesUpdated += entitiesToSave.size();
				logger.debug(SOATools.buildSOALogMessage(soaContext, "[updatePartition] " + entitiesToSave.size()
						+ " more entities (up to " + nbEntitiesUpdated + ") has been updated."));
			}

			nbUpdatedEntitiesForPartition += entitiesToSave.size();
		}

		actionStatus.addInfo("nbUpdatedEntitiesForPartition", nbUpdatedEntitiesForPartition);

		// return partition status list
		return new ArrayList<>(partitionStatus.values());
	}

	public void updatePartitionStatus(SOAContext soaContext) {
		Map<String, PartitionStatus> partitionStatus = new TreeMap<>();
		Date yesterdayDate = Utils.getYesterdayDate();
		// Get the Entities PK Ordered by Group and Type
		String[] orderAttributes = { ReportingEntity.FIELD_PK };
		List<Long> entitiesOrdered = reportingEntityDao.findPkOrderBy(orderAttributes, false);
		List<PartitionStatus> status = partitionStatusDao.findAll();
		partitionStatusDao.remove(status);

		long nbEntitiesProcessed = 0L;
		int startIndex = 0;
		while (startIndex < entitiesOrdered.size()) {
			int endIndex = ((startIndex + Configuration.h2PaginationSize) <= entitiesOrdered.size())
					? startIndex + Configuration.h2PaginationSize
					: entitiesOrdered.size();
			List<Long> pkSelected = entitiesOrdered.subList(startIndex, endIndex);
			List<ReportingEntityPartitionTO> entities = reportingEntityDao
					.findEntityPartitionWherePkInSelectionOrderByGroupAndType(pkSelected);
			startIndex = endIndex;
			for (ReportingEntityPartitionTO entityTO : entities) {
				// update the Partition Status
				if (entityTO.getPartitionNumber() != null || !entityTO.getPartitionNumber().isEmpty()) {
					updatePartitionStatusWithEntityPartition(entityTO.getPartitionNumber(), partitionStatus,
							yesterdayDate);
				}
			}
			entities.clear();
			if (logger.isDebugEnabled()) {
				nbEntitiesProcessed += Configuration.h2PaginationSize;
				logger.debug(SOATools.buildSOALogMessage(soaContext,
						"[updatePartitionStatus] " + Configuration.h2PaginationSize + " more entities (up to "
								+ nbEntitiesProcessed + ") has been processed."));
			}
		}
		savePartitionStatus(new ArrayList<>(partitionStatus.values()), soaContext);
	}

	public void savePartitionStatus(List<PartitionStatus> partitionStatusToPersit, SOAContext soaContext) {
		partitionStatusDao.persistAndFlush(partitionStatusToPersit);
	}

	private void saveEntitesPartitionsWithPk(List<ReportingEntityPartitionTO> entityPartitionsTOs,
			SOAContext soaContext) {
		// Update ReportingEntity in the database RefReport
		Connection connection = null;
		Long currentPk = 0L;
		try {
			connection = defaultDataSource.getConnection();
			connection.setAutoCommit(false);
			PreparedStatement prepareStatement = connection
					.prepareStatement("UPDATE T_REPORTING_ENTITY SET PARTITION_NUMBER=? WHERE PK=?");
			for (ReportingEntityPartitionTO entityTO : entityPartitionsTOs) {
				prepareStatement.setString(1, entityTO.getPartitionNumber());
				prepareStatement.setLong(2, entityTO.getEntityPk());
				currentPk = entityTO.getEntityPk();
				prepareStatement.executeUpdate();
			}
			prepareStatement.close();
			connection.commit();
		} catch (SQLException sqle) {
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e) {
				logger.error(SOATools.buildSOALogMessage(soaContext,
						"[saveEntitesPartitionsWithPk] Cannot rollback update " + e.getMessage()), e);
			}
			logger.error(SOATools.buildSOALogMessage(soaContext,
					"[saveEntitesPartitionsWithPk] Cannot update entities on entityPk=" + currentPk + ", "
							+ sqle.getMessage()),
					sqle);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				logger.error(SOATools.buildSOALogMessage(soaContext,
						"[saveEntitesPartitionsWithPk] Cannot close connection " + sqle.getMessage()), sqle);
			}
		}
	}

	private String computePartitionNumber(String startDate, Map<String, PartitionStatus> partitionStatus,
			Date yesterdayDate) {
		Calendar partitionDate = GregorianCalendar.getInstance();
		partitionDate.setTime(yesterdayDate);
		partitionDate.add(Calendar.YEAR, -2);
		StringBuilder result = new StringBuilder(startDate);
		for (int month = 0; month < numberOfMonth; month++) {
			String partitionDateFormated = formatYYYYMM.format(partitionDate.getTime());
			PartitionStatus partition = new PartitionStatus(partitionDateFormated, 1, 0);
			if (partitionStatus.containsKey(partitionDateFormated)) {
				partition = partitionStatus.get(partitionDateFormated);
			} else {
				partitionStatus.put(partitionDateFormated, partition);
			}
			partition.incrementNumberOfEntity();
			result.append(String.format("%04d", partition.partitionNumber));
			partitionDate.add(Calendar.MONTH, 1);
		}
		return result.toString();
	}

	private String updatePartitionNumber(String partitionNumberToUpdate, String startDate,
			Map<String, PartitionStatus> partitionStatus, Date yesterdayDate) {
		char[] partitionToUpdate = partitionNumberToUpdate.toCharArray();
		int index = 6;
		Calendar partitionDate = GregorianCalendar.getInstance();
		partitionDate.setTime(yesterdayDate);
		partitionDate.add(Calendar.YEAR, -2);
		Calendar dateToUpdate = GregorianCalendar.getInstance();
		dateToUpdate.setTime(yesterdayDate);
		String yearToUpdate = Character.toString(partitionToUpdate[0]) + Character.toString(partitionToUpdate[1])
				+ Character.toString(partitionToUpdate[2]) + Character.toString(partitionToUpdate[3]);
		String monthToUpdate = Character.toString(partitionToUpdate[4]) + Character.toString(partitionToUpdate[5]);
		dateToUpdate.set(Calendar.YEAR, Integer.valueOf(yearToUpdate));
		dateToUpdate.set(Calendar.MONTH, Integer.valueOf(monthToUpdate) - 1);
		if (!dateToUpdate.equals(partitionDate)) {
			StringBuilder partitionNumber = new StringBuilder(startDate);
			int dateOffset = 0;
			while (dateToUpdate.before(partitionDate)) {
				dateOffset++;
				dateToUpdate.add(Calendar.MONTH, 1);
			}
			index += (dateOffset * 4);
			int month = 0;
			while (month < numberOfMonth) {
				if (index < partitionToUpdate.length) {
					partitionNumber.append(Character.toString(partitionToUpdate[index++])
							+ Character.toString(partitionToUpdate[index++])
							+ Character.toString(partitionToUpdate[index++])
							+ Character.toString(partitionToUpdate[index++]));
				} else {
					String partitionDateFormated = formatYYYYMM.format(partitionDate.getTime());
					PartitionStatus partition = new PartitionStatus(partitionDateFormated, 1, 0);
					if (partitionStatus.containsKey(partitionDateFormated)) {
						partition = partitionStatus.get(partitionDateFormated);
					} else {
						partitionStatus.put(partitionDateFormated, partition);
					}
					partition.incrementNumberOfEntity();
					partitionNumber.append(String.format("%04d", partition.partitionNumber));
				}
				partitionDate.add(Calendar.MONTH, 1);
				month++;
			}
			return partitionNumber.toString();
		} else {
			return null;
		}
	}

	private void updatePartitionStatusWithEntityPartition(String partitionNumberToUpdate,
			Map<String, PartitionStatus> partitionStatus, Date yesterdayDate) {
		char[] entityPartition = partitionNumberToUpdate.toCharArray();
		int index = 6;

		Calendar partitionDate = GregorianCalendar.getInstance();
		partitionDate.setTime(yesterdayDate);
		partitionDate.add(Calendar.YEAR, -2);

		Calendar entityPartitionDate = GregorianCalendar.getInstance();
		entityPartitionDate.setTime(yesterdayDate);
		String yearToUpdate = Character.toString(entityPartition[0]) + Character.toString(entityPartition[1])
				+ Character.toString(entityPartition[2]) + Character.toString(entityPartition[3]);
		String monthToUpdate = Character.toString(entityPartition[4]) + Character.toString(entityPartition[5]);
		entityPartitionDate.set(Calendar.YEAR, Integer.valueOf(yearToUpdate));
		entityPartitionDate.set(Calendar.MONTH, Integer.valueOf(monthToUpdate) - 1);

		// StringBuilder partitionNumber = new StringBuilder(startDate);
		int dateOffset = 0;
		while (entityPartitionDate.before(partitionDate)) {
			dateOffset++;
			entityPartitionDate.add(Calendar.MONTH, 1);
		}
		index += (dateOffset * 4);
		int month = 0;
		while (month < numberOfMonth) {
			if (index < entityPartition.length) {
				String tmpPartition = Character.toString(entityPartition[index++])
						+ Character.toString(entityPartition[index++]) + Character.toString(entityPartition[index++])
						+ Character.toString(entityPartition[index++]);
				int partitionNumber = Integer.valueOf(tmpPartition);
				String partitionDateFormated = formatYYYYMM.format(partitionDate.getTime());
				partitionDate.add(Calendar.MONTH, 1);
				PartitionStatus partition = new PartitionStatus(partitionDateFormated, 1, 0);
				if (partitionStatus.containsKey(partitionDateFormated)) {
					partition = partitionStatus.get(partitionDateFormated);
					if (partition.partitionNumber < partitionNumber) {
						partition.partitionNumber = partitionNumber;
						partition.numberOfEntity = 1;
					} else if (partition.partitionNumber == partitionNumber) {
						partition.numberOfEntity++;
					}
				} else {
					partitionStatus.put(partitionDateFormated, partition);
				}
			}
			month++;
		}
	}

	public void saveEntitesPartitions(List<ReportingEntityPartitionTO> entityPartitionsTOs, SOAContext soaContext) {
		// Update ReportingEntity in the database RefReport
		Connection connection = null;
		try {
			connection = defaultDataSource.getConnection();
			connection.setAutoCommit(false);
			PreparedStatement prepareStatement = connection.prepareStatement(
					"UPDATE T_REPORTING_ENTITY SET PARTITION_NUMBER=? WHERE ENTITY_ID=? AND ORIGIN=?");
			for (ReportingEntityPartitionTO entityTO : entityPartitionsTOs) {
				prepareStatement.setString(1, entityTO.getPartitionNumber());
				prepareStatement.setString(2, entityTO.getEntityId());
				prepareStatement.setString(3, entityTO.getOrigin());
				prepareStatement.executeUpdate();
			}
			prepareStatement.close();
			connection.commit();
		} catch (SQLException sqle) {
			logger.error(SOATools.buildSOALogMessage(soaContext,
					"[SaveEntitesPartitions] Cannot update entities " + sqle.getMessage()), sqle);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				logger.error(SOATools.buildSOALogMessage(soaContext,
						"[SaveEntitesPartitions] Cannot close connection " + sqle.getMessage()), sqle);
			}
		}
	}

}
