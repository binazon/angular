package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.EntityLinkAttribute;
import com.orange.srs.refreport.model.EntityLinkAttributeId;
import com.orange.srs.refreport.model.parameter.EntityToEntityAttributeParameter;

public interface EntityLinkAttributeDAO extends Dao<EntityLinkAttribute, EntityLinkAttributeId> {

	public List<EntityToEntityAttributeParameter> findAllEntityToEntityAttributeParameter();

	public List<EntityToEntityAttributeParameter> findEntityToEntityAttributeParameterByEntityIdSource(String entityId);

	public List<EntityToEntityAttributeParameter> findEntityToEntityAttributeParameterByEntityIdDest(String entityId);

}
