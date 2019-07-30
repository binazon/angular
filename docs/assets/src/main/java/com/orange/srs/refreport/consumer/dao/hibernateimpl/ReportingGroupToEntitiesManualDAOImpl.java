package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.ReportingGroupToEntitiesManualDAO;
import com.orange.srs.refreport.model.ReportingGroupToEntitiesId;
import com.orange.srs.refreport.model.ReportingGroupToEntitiesManual;

@Stateless
public class ReportingGroupToEntitiesManualDAOImpl
		extends AbstractJpaDao<ReportingGroupToEntitiesManual, ReportingGroupToEntitiesId>
		implements ReportingGroupToEntitiesManualDAO {

}
