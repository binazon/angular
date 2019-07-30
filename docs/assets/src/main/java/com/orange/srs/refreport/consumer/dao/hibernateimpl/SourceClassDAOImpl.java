package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.SourceClassDAO;
import com.orange.srs.refreport.model.SourceClass;

@Stateless
public class SourceClassDAOImpl extends AbstractJpaDao<SourceClass, String> implements SourceClassDAO {
}
