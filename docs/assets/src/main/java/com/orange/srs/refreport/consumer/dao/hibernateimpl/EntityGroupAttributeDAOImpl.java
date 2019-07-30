package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.EntityGroupAttributeDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.EntityGroupAttribute;
import com.orange.srs.refreport.model.EntityGroupAttributeId;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.parameter.GroupEntityAttributeParameter;

@Stateless
public class EntityGroupAttributeDAOImpl extends AbstractJpaDao<EntityGroupAttribute, EntityGroupAttributeId>
		implements EntityGroupAttributeDAO {

	@SuppressWarnings("unchecked")
	public List<GroupEntityAttributeParameter> findAllGroupEntityAttributeParameter() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.groupEntityAttributeParameterBuilder("a", "rg", "re")
				+ " FROM " + getEntityName() + " a" + " INNER JOIN a." + EntityGroupAttribute.FIELD_REPORTING_GROUP
				+ " rg" + " INNER JOIN a." + EntityGroupAttribute.FIELD_REPORTING_ENTITY + " re";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<GroupEntityAttributeParameter> findGroupEntityAttributeParameterByGroup(String origin,
			String reportingGroupRef) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.groupEntityAttributeParameterBuilder("a", "rg", "re")
				+ " FROM " + getEntityName() + " a" + " INNER JOIN a." + EntityGroupAttribute.FIELD_REPORTING_GROUP
				+ " rg" + " INNER JOIN a." + EntityGroupAttribute.FIELD_REPORTING_ENTITY + " re" + " WHERE rg."
				+ ReportingGroup.FIELD_ORIGIN + "=:origin" + " AND rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF
				+ "=:reportingGroupRef";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("origin", origin);
		query.setParameter("reportingGroupRef", reportingGroupRef);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<GroupEntityAttributeParameter> findGroupEntityAttributeParameterByEntityId(String entityId) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.groupEntityAttributeParameterBuilder("a", "rg", "re")
				+ " FROM " + getEntityName() + " a" + " INNER JOIN a." + EntityGroupAttribute.FIELD_REPORTING_GROUP
				+ " rg" + " INNER JOIN a." + EntityGroupAttribute.FIELD_REPORTING_ENTITY + " re" + " WHERE re."
				+ ReportingEntity.FIELD_ENTITYID + "=:entityId";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("entityId", entityId);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<GroupEntityAttributeParameter> findGroupEntityAttributeParameterByGroupAndEntityId(String origin,
			String reportingGroupRef, String entityId) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.groupEntityAttributeParameterBuilder("a", "rg", "re")
				+ " FROM " + getEntityName() + " a" + " INNER JOIN a." + EntityGroupAttribute.FIELD_REPORTING_GROUP
				+ " rg" + " INNER JOIN a." + EntityGroupAttribute.FIELD_REPORTING_ENTITY + " re" + " WHERE rg."
				+ ReportingGroup.FIELD_ORIGIN + "=:origin" + " AND rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF
				+ "=:reportingGroupRef" + " AND re." + ReportingEntity.FIELD_ENTITYID + "=:entityId";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("origin", origin);
		query.setParameter("reportingGroupRef", reportingGroupRef);
		query.setParameter("entityId", entityId);
		return query.getResultList();
	}

}
