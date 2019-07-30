package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.ReportingGroupPartitionStatusDAO;
import com.orange.srs.refreport.model.ReportingGroupPartitionStatus;
import com.orange.srs.statcommon.technical.ModelUtils;

@Stateless
public class ReportingGroupPartitionStatusDAOImpl extends AbstractJpaDao<ReportingGroupPartitionStatus, Long>
		implements ReportingGroupPartitionStatusDAO {

	public void truncate() {
		getEntityManager()
				.createNativeQuery(
						"TRUNCATE TABLE " + ModelUtils.getEntityTableName(ReportingGroupPartitionStatus.class))
				.executeUpdate();
	}

	public List<ReportingGroupPartitionStatus> findByDateAndEntityType(String date, String entityType) {

		String jpqlQuery = "SELECT rgps FROM " + getEntityName() + " rgps " + "WHERE rgps."
				+ ReportingGroupPartitionStatus.FIELD_DATE + " = :date" + " AND rgps."
				+ ReportingGroupPartitionStatus.FIELD_ENTITY_TYPE + " = :entityType";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("date", date);
		query.setParameter("entityType", entityType);

		return query.getResultList();
	}
}
