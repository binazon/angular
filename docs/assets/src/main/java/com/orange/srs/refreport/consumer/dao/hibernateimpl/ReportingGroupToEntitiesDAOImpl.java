package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.ReportingGroupToEntitiesDAO;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.ReportingGroupToEntities;
import com.orange.srs.refreport.model.ReportingGroupToEntitiesId;

@Stateless
public class ReportingGroupToEntitiesDAOImpl extends
		AbstractJpaDao<ReportingGroupToEntities, ReportingGroupToEntitiesId> implements ReportingGroupToEntitiesDAO {

	@Override
	public List<Object[]> findAllEntityPartitionByReportingGroupPk(Long reportingGroupPk) {
		String jpqlQuery = "SELECT e." + ReportingEntity.COL_NAME_ENTITY_TYPE + ", e."
				+ ReportingEntity.COL_NAME_PARTITION_NUMBER + " FROM " + ReportingGroupToEntities.TABLE_NAME + " tj, "
				+ ReportingEntity.TABLE_NAME + " e " + " WHERE e." + ReportingEntity.COL_NAME_PK + "=" + "tj."
				+ ReportingGroupToEntities.COL_NAME_REPORTING_ENTITY_FK + " AND tj."
				+ ReportingGroupToEntities.COL_NAME_REPORTING_GROUP_FK + " = :reportingGroupPk";

		Query query = getEntityManager().createNativeQuery(jpqlQuery);
		query.setParameter("reportingGroupPk", reportingGroupPk);
		return query.getResultList();
	}
}
