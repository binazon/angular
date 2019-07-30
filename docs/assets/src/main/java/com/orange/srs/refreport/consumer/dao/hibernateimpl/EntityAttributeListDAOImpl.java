package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.EntityAttributeListDAO;
import com.orange.srs.refreport.model.EntityAttributeList;

@Stateless
public class EntityAttributeListDAOImpl extends AbstractJpaDao<EntityAttributeList, Long>
		implements EntityAttributeListDAO {

}
