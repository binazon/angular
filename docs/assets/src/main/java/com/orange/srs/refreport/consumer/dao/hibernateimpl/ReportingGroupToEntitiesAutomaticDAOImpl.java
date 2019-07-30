package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.ReportingGroupToEntitiesAutomaticDAO;
import com.orange.srs.refreport.model.ReportingGroupToEntitiesAutomatic;
import com.orange.srs.refreport.model.ReportingGroupToEntitiesId;

@Stateless
public class ReportingGroupToEntitiesAutomaticDAOImpl
		extends AbstractJpaDao<ReportingGroupToEntitiesAutomatic, ReportingGroupToEntitiesId>
		implements ReportingGroupToEntitiesAutomaticDAO {

}
