package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.ComposedReportingGroupDAO;
import com.orange.srs.refreport.model.ComposedReportingGroup;

@Stateless
public class ComposedReportingGroupDAOImpl extends AbstractJpaDao<ComposedReportingGroup, Long>
		implements ComposedReportingGroupDAO {

}
