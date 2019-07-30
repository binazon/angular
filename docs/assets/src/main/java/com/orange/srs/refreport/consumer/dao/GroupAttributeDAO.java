package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.GroupAttribute;
import com.orange.srs.refreport.model.GroupAttributeId;
import com.orange.srs.statcommon.model.parameter.GroupAttributeParameter;

public interface GroupAttributeDAO extends Dao<GroupAttribute, GroupAttributeId> {

	public List<GroupAttributeParameter> findAllGroupAttributeParameter();

	public List<GroupAttributeParameter> findGroupAttributeParameterByGroup(String origin, String reportingGroupRef);

}
