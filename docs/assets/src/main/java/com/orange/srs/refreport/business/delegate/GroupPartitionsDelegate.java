package com.orange.srs.refreport.business.delegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupPartitionStatusDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupToEntitiesDAO;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.ReportingGroupPartitionStatus;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.concurrent.ReportingGroupPartitionsExecutorService;
import com.orange.srs.statcommon.model.TO.ReportingGroupPartitionStatusTO;
import com.orange.srs.statcommon.model.TO.rest.PartitionStatusParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class GroupPartitionsDelegate {

	private static final Logger LOGGER = Logger.getLogger(GroupPartitionsDelegate.class);

	private static final int NB_OF_MONTH_IN_YEAR = 12;
	private static final String[] MONTH_IN_2_DIGITS = new String[] { "00", "01", "02", "03", "04", "05", "06", "07",
			"08", "09", "10", "11", "12" };

	@EJB
	private ReportingGroupPartitionStatusDAO groupPartitionStatusDao;

	@EJB
	private ReportingGroupToEntitiesDAO reportingGroupToEntitiesDao;

	@EJB
	private ReportingGroupDAO reportingGroupDao;

	@EJB
	private ReportingGroupPartitionsExecutorService executorService;

	@Resource(lookup = "dataSourceRefReport")
	private DataSource defaultDataSource;

	public void removeReportingGroupsPartitionStatus(SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Entring removeReportingGroupsPartitionStatus"));
		groupPartitionStatusDao.truncate();
		groupPartitionStatusDao.flush();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[removeReportingGroupsPartitionStatus] ReportingGroupsPartitionStatus are deleted from RefReport database"));
	}

	private class UpdateGroupPartitionStatusCallable implements Callable<List<ReportingGroupPartitionStatus>> {

		private final Long reportingGroupPk;
		private final AtomicInteger completedThreadCounter;
		private final int totalNumberOfCallables;

		public UpdateGroupPartitionStatusCallable(Long reportingGroupPk, AtomicInteger completedThreadCounter,
				int totalNumberOfCallables) {
			this.reportingGroupPk = reportingGroupPk;
			this.completedThreadCounter = completedThreadCounter;
			this.totalNumberOfCallables = totalNumberOfCallables;
		}

		@Override
		public List<ReportingGroupPartitionStatus> call() throws Exception {

			ReportingGroup group = new ReportingGroup();
			group.setPk(reportingGroupPk);

			List<ReportingGroupPartitionStatus> result = new ArrayList<ReportingGroupPartitionStatus>();

			Map<String, Map<String, Set<Integer>>> partitionsByTypeAndByDate = new HashMap<String, Map<String, Set<Integer>>>();

			List<Object[]> allEntityPartitionPkForCurrentRG = reportingGroupToEntitiesDao
					.findAllEntityPartitionByReportingGroupPk(reportingGroupPk);

			for (Object[] entityPartitionPk : allEntityPartitionPkForCurrentRG) {
				String partitionNumber = (String) entityPartitionPk[1];
				if (partitionNumber != null) {
					String entityType = (String) entityPartitionPk[0];
					Map<String, Set<Integer>> partitionsByDate = partitionsByTypeAndByDate.get(entityType);
					if (partitionsByDate == null) {
						partitionsByDate = new HashMap<>();
						partitionsByTypeAndByDate.put(entityType, partitionsByDate);
					}

					char[] entityPartition = partitionNumber.toCharArray();

					splitPartitionStatusByDate(entityPartition, partitionsByDate);
				}
			}

			for (Entry<String, Map<String, Set<Integer>>> entry : partitionsByTypeAndByDate.entrySet()) {
				result.addAll(getReportingGroupPartitionStatus(group, entry.getKey(), entry.getValue()));
			}

			LOGGER.debug("Group partition status calculated for reporting group " + completedThreadCounter.addAndGet(1)
					+ " on " + totalNumberOfCallables);

			return result;

		}
	}

	public void saveReportingGroupPartition(SOAContext soaContext) {

		try {
			List<Long> reportingGroupsPk = reportingGroupDao.findAllReportingGroupPk();

			int size = reportingGroupsPk.size();

			List<Callable<List<ReportingGroupPartitionStatus>>> callables = new ArrayList<Callable<List<ReportingGroupPartitionStatus>>>(
					size);

			AtomicInteger completedThreadCounter = new AtomicInteger();
			int i = 0;
			for (Long reportingGroupPk : reportingGroupsPk) {
				callables.add(new UpdateGroupPartitionStatusCallable(reportingGroupPk, completedThreadCounter, size));
			}

			List<Future<List<ReportingGroupPartitionStatus>>> futures = executorService.invokeAll(callables);

			i = 0;
			for (Future<List<ReportingGroupPartitionStatus>> future : futures) {
				try {
					i++;
					groupPartitionStatusDao.persist(future.get());
					if (i % Configuration.groupPartitionsStatusBatchSize == 0) {
						groupPartitionStatusDao.flush();
						groupPartitionStatusDao.clear();
						LOGGER.debug(
								"Group partitions status done for Reporting Group n°" + i + " on " + futures.size());
					}

				} catch (Exception e) {
					LOGGER.error("Error when processing group partitions status", e);
				}
			}

		} catch (Exception ex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext,
					"[saveReportingGroupPartition] Cannot update entities " + ex.getMessage()), ex);
		}
	}

	public List<ReportingGroupPartitionStatusTO> getReportingGroupPartitions(
			PartitionStatusParameter getPartitionStatus, SOAContext soaContext) {
		List<ReportingGroupPartitionStatusTO> reportingGroupPartitions = new ArrayList<>();
		List<ReportingGroupPartitionStatus> groupPartitionStatus = groupPartitionStatusDao
				.findByDateAndEntityType(getPartitionStatus.date, getPartitionStatus.entityType);
		for (ReportingGroupPartitionStatus partitionStatus : groupPartitionStatus) {
			ReportingGroupPartitionStatusTO groupStatus = new ReportingGroupPartitionStatusTO(
					partitionStatus.getTargetGroup().getPk(), partitionStatus.getPartitions());
			reportingGroupPartitions.add(groupStatus);
		}
		return reportingGroupPartitions;
	}

	private void splitPartitionStatusByDate(char[] entityPartition, Map<String, Set<Integer>> partitionsByDate) {
		int year = Integer
				.parseInt("" + entityPartition[0] + entityPartition[1] + entityPartition[2] + entityPartition[3]);
		int month = Integer.parseInt("" + entityPartition[4] + entityPartition[5]);
		for (int index = 6; index < entityPartition.length;) {
			String partitionDate = "" + year + MONTH_IN_2_DIGITS[month];
			Set<Integer> partitions = partitionsByDate.get(partitionDate);
			if (partitions == null) {
				partitions = new HashSet<>();
				partitionsByDate.put(partitionDate, partitions);
			}
			int partition = Integer.parseInt("" + entityPartition[index++] + entityPartition[index++]
					+ entityPartition[index++] + entityPartition[index++]);
			partitions.add(partition);
			month++;
			if (month > NB_OF_MONTH_IN_YEAR) {
				month = 1;
				year++;
			}
		}
	}

	private List<ReportingGroupPartitionStatus> getReportingGroupPartitionStatus(ReportingGroup group,
			String entityType, Map<String, Set<Integer>> partitions) {
		List<ReportingGroupPartitionStatus> reportingGroupPartitionStatus = new ArrayList<>();
		for (Entry<String, Set<Integer>> partition : partitions.entrySet()) {
			ReportingGroupPartitionStatus partitionStatus = new ReportingGroupPartitionStatus();
			partitionStatus.date = partition.getKey();
			partitionStatus.entityType = entityType;
			partitionStatus.partitions = StringUtils.join(partition.getValue().toArray(), ',');
			partitionStatus.targetGroup = group;
			reportingGroupPartitionStatus.add(partitionStatus);
		}
		return reportingGroupPartitionStatus;
	}
}
