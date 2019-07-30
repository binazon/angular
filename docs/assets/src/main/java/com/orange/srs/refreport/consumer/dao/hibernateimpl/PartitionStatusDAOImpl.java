package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.PartitionStatusDAO;
import com.orange.srs.refreport.model.PartitionStatus;

@Stateless
public class PartitionStatusDAOImpl extends AbstractJpaDao<PartitionStatus, Long> implements PartitionStatusDAO {

}
