package com.orange.srs.refreport.business;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.delegate.EntitiesPartitionsDelegate;
import com.orange.srs.refreport.business.delegate.GroupPartitionsDelegate;
import com.orange.srs.refreport.business.delegate.H2EntitiesPartitionsDelegate;
import com.orange.srs.refreport.model.PartitionStatus;
import com.orange.srs.refreport.model.TO.ProvisioningActionStatusTO;
import com.orange.srs.refreport.model.TO.ReportingEntityPartitionTO;
import com.orange.srs.refreport.model.exception.InventoryException;
import com.orange.srs.statcommon.model.TO.ReportingGroupPartitionStatusTOList;
import com.orange.srs.statcommon.model.TO.rest.PartitionStatusParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.interceptor.Logged;

@Logged
@Stateless
public class SOA15PartitioningFacade {

	private static Logger logger = Logger.getLogger(SOA15PartitioningFacade.class);

	@EJB
	private H2EntitiesPartitionsDelegate h2EntitiesPartitionsDelegate;

	@EJB
	private EntitiesPartitionsDelegate entitesPartitionsDelegate;

	@EJB
	private GroupPartitionsDelegate groupPartitionsDelegate;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String exportEntitiesPartitions(SOAContext soaContext) throws InventoryException {
		String resultFileLocation = null;
		try {
			logger.info(SOATools.buildSOALogMessage(soaContext, "[exportPartitions] Start"));
			Long globalStart = Utils.getTime();

			resultFileLocation = h2EntitiesPartitionsDelegate.exportEntitesPartitionsTables(soaContext);

			Long globalEnd = Utils.getTime();
			logger.info(SOATools.buildSOALogMessage(soaContext,
					"[exportPartitions] Export Partitions OK in " + (globalEnd - globalStart) + " ms"));
		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[exportPartitions] KO"));
			throw new InventoryException(e.getMessage(), e);
		}
		return resultFileLocation;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean importEntitiesPartitions(SOAContext soaContext) throws InventoryException {
		try {
			logger.info(SOATools.buildSOALogMessage(soaContext, "[importPartitions] Start"));
			Long globalStart = Utils.getTime();

			List<ReportingEntityPartitionTO> entityPartitionsTOs = h2EntitiesPartitionsDelegate
					.getEntitiesPartitions(soaContext);
			entitesPartitionsDelegate.saveEntitesPartitions(entityPartitionsTOs, soaContext);
			entityPartitionsTOs.clear();
			List<PartitionStatus> partitionStatus = h2EntitiesPartitionsDelegate.getPartitionsStatus(soaContext);
			entitesPartitionsDelegate.savePartitionStatus(partitionStatus, soaContext);

			Long globalEnd = Utils.getTime();
			logger.info(SOATools.buildSOALogMessage(soaContext,
					"[importPartitions] Import Partitions OK in " + (globalEnd - globalStart) + " ms"));
		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[importPartitions] KO"));
			throw new InventoryException(e.getMessage(), e);
		}
		return true;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ProvisioningActionStatusTO updateEntitiesPartitions(SOAContext soaContext) throws InventoryException {

		ProvisioningActionStatusTO status = new ProvisioningActionStatusTO("updateEntitiesPartitions");
		try {
			logger.info(SOATools.buildSOALogMessage(soaContext, "[updatePartitions] Start"));

			Long globalStart = Utils.getTime();
			List<PartitionStatus> partitionStatus = entitesPartitionsDelegate.updatePartition(status, soaContext);
			entitesPartitionsDelegate.savePartitionStatus(partitionStatus, soaContext);
			Long globalEnd = Utils.getTime();

			status.duration = globalEnd - globalStart;

			logger.info(SOATools.buildSOALogMessage(soaContext,
					"[updatePartitions] Update Partitions OK in " + (globalEnd - globalStart) + " ms"));
		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[updatePartitions] KO"));
			throw new InventoryException(e.getMessage(), e);
		}

		return status;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updatePartitionsStatus(SOAContext soaContext) throws InventoryException {
		try {
			logger.info(SOATools.buildSOALogMessage(soaContext, "[updatePartitionsStatus] Start"));
			Long globalStart = Utils.getTime();

			entitesPartitionsDelegate.updatePartitionStatus(soaContext);

			Long globalEnd = Utils.getTime();
			logger.info(SOATools.buildSOALogMessage(soaContext,
					"[updatePartitionsStatus] Update Partitions Status OK in " + (globalEnd - globalStart) + " ms"));
		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[updatePartitionsStatus] KO"));
			throw new InventoryException(e.getMessage(), e);
		}
		return true;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ProvisioningActionStatusTO updateGroupsPartitions(SOAContext soaContext) throws InventoryException {

		ProvisioningActionStatusTO status = new ProvisioningActionStatusTO("updateGroupsPartitions");
		try {
			logger.info(SOATools.buildSOALogMessage(soaContext, "[updateGroupsPartitions] Start"));

			Long globalStart = Utils.getTime();
			groupPartitionsDelegate.removeReportingGroupsPartitionStatus(soaContext);
			groupPartitionsDelegate.saveReportingGroupPartition(soaContext);
			Long globalEnd = Utils.getTime();

			status.duration = globalEnd - globalStart;

			logger.info(SOATools.buildSOALogMessage(soaContext,
					"[updateGroupsPartitions] update Partitions Number By Groups OK in " + (globalEnd - globalStart)
							+ " ms"));
		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[updateGroupsPartitions] KO"));
			throw new InventoryException(e.getMessage(), e);
		}
		return status;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ReportingGroupPartitionStatusTOList getGroupsPartitions(PartitionStatusParameter getPartitionStatus,
			SOAContext soaContext) throws InventoryException {
		ReportingGroupPartitionStatusTOList groupPartitionStatus = new ReportingGroupPartitionStatusTOList();
		try {
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getGroupsPartitions] Start"));
			Long globalStart = Utils.getTime();

			groupPartitionStatus.reportingGroupPartitionStatusTO = groupPartitionsDelegate
					.getReportingGroupPartitions(getPartitionStatus, soaContext);

			Long globalEnd = Utils.getTime();
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[getGroupsPartitions] get Partitions Number By date and entity type OK in "
							+ (globalEnd - globalStart) + " ms"));
		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[getGroupsPartitions] KO : " + e));
			throw new InventoryException(e.getMessage(), e);
		}
		return groupPartitionStatus;
	}
}
