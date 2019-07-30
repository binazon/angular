package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.EntityGroupAttribute;
import com.orange.srs.refreport.model.EntityGroupAttributeId;
import com.orange.srs.refreport.model.parameter.GroupEntityAttributeParameter;

public interface EntityGroupAttributeDAO extends Dao<EntityGroupAttribute, EntityGroupAttributeId> {

	public List<GroupEntityAttributeParameter> findAllGroupEntityAttributeParameter();

	public List<GroupEntityAttributeParameter> findGroupEntityAttributeParameterByGroup(String origin,
			String reportingGroupRef);

	public List<GroupEntityAttributeParameter> findGroupEntityAttributeParameterByEntityId(String entityId);

	public List<GroupEntityAttributeParameter> findGroupEntityAttributeParameterByGroupAndEntityId(String origin,
			String reportingGroupRef, String entityId);

}
