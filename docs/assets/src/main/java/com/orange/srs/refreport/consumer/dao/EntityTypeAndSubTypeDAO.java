package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.EntityTypeAndSubtype;
import com.orange.srs.refreport.model.EntityTypeAndSubtypeId;

public interface EntityTypeAndSubTypeDAO extends Dao<EntityTypeAndSubtype, EntityTypeAndSubtypeId> {

	public List<String> findDistinctType();
}
