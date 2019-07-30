package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.EntityAttributeDAO;
import com.orange.srs.refreport.model.EntityAttribute;

@Stateless
public class EntityAttributeDAOImpl extends AbstractJpaDao<EntityAttribute, Long> implements EntityAttributeDAO {

}
