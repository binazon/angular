package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.ReportOutputDAO;
import com.orange.srs.refreport.model.ReportOutput;

@Stateless
public class ReportOutputDAOImpl extends AbstractJpaDao<ReportOutput, Long> implements ReportOutputDAO {

}
