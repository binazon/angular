package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.InputFormatDAO;
import com.orange.srs.refreport.model.InputFormat;

@Stateless
public class InputFormatDAOImpl extends AbstractJpaDao<InputFormat, Long> implements InputFormatDAO {

}
