package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.EntityLinkAttributeDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.EntityLink;
import com.orange.srs.refreport.model.EntityLinkAttribute;
import com.orange.srs.refreport.model.EntityLinkAttributeId;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.parameter.EntityToEntityAttributeParameter;

@Stateless
public class EntityLinkAttributeDAOImpl extends AbstractJpaDao<EntityLinkAttribute, EntityLinkAttributeId>
		implements EntityLinkAttributeDAO {

	public List<EntityToEntityAttributeParameter> findAllEntityToEntityAttributeParameter() {
		String jpqlQuery = "SELECT NEW "
				+ JPATOConstructorBuilder.entityToEntityAttributeParameterBuilder("a", "reS", "reD", "l") + " FROM "
				+ getEntityName() + " a" + " INNER JOIN a." + EntityLinkAttribute.FIELD_ENTITY_LINK + " l"
				+ " INNER JOIN l." + EntityLink.FIELD_REPORTING_ENTITY_SRC + " reS" + " INNER JOIN l."
				+ EntityLink.FIELD_REPORTING_ENTITY_DEST + " reD";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	public List<EntityToEntityAttributeParameter> findEntityToEntityAttributeParameterByEntityIdSource(
			String entityId) {
		String jpqlQuery = "SELECT NEW "
				+ JPATOConstructorBuilder.entityToEntityAttributeParameterBuilder("a", "reS", "reD", "l") + " FROM "
				+ getEntityName() + " a" + " INNER JOIN a." + EntityLinkAttribute.FIELD_ENTITY_LINK + " l"
				+ " INNER JOIN l." + EntityLink.FIELD_REPORTING_ENTITY_SRC + " reS" + " INNER JOIN l."
				+ EntityLink.FIELD_REPORTING_ENTITY_DEST + " reD" + " WHERE reS." + ReportingEntity.FIELD_ENTITYID
				+ "=:entityId";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("entityId", entityId);
		return query.getResultList();
	}

	public List<EntityToEntityAttributeParameter> findEntityToEntityAttributeParameterByEntityIdDest(String entityId) {
		String jpqlQuery = "SELECT NEW "
				+ JPATOConstructorBuilder.entityToEntityAttributeParameterBuilder("a", "reS", "reD", "l") + " FROM "
				+ getEntityName() + " a" + " INNER JOIN a." + EntityLinkAttribute.FIELD_ENTITY_LINK + " l"
				+ " INNER JOIN l." + EntityLink.FIELD_REPORTING_ENTITY_SRC + " reS" + " INNER JOIN l."
				+ EntityLink.FIELD_REPORTING_ENTITY_DEST + " reD" + " WHERE reD." + ReportingEntity.FIELD_ENTITYID
				+ "=:entityId";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("entityId", entityId);
		return query.getResultList();
	}

}
