package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.SourceProxyDAO;
import com.orange.srs.refreport.model.SourceProxy;

@Stateless
public class SourceProxyDAOImpl extends AbstractJpaDao<SourceProxy, Long> implements SourceProxyDAO {
}
