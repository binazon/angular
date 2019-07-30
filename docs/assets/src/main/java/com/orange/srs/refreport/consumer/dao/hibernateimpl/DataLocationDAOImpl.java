package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.DataLocationDAO;
import com.orange.srs.refreport.model.DataLocation;

@Stateless
public class DataLocationDAOImpl extends AbstractJpaDao<DataLocation, Long> implements DataLocationDAO {

}
