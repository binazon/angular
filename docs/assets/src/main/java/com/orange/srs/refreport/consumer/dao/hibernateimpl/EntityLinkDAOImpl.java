package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.EntityLinkDAO;
import com.orange.srs.refreport.model.EntityLink;
import com.orange.srs.refreport.model.EntityLinkId;

@Stateless
public class EntityLinkDAOImpl extends AbstractJpaDao<EntityLink, EntityLinkId> implements EntityLinkDAO {

}
