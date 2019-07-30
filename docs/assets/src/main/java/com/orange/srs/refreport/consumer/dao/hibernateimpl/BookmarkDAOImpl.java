package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.BookmarkDAO;
import com.orange.srs.refreport.model.ReportsBookmark;

@Stateless
public class BookmarkDAOImpl extends AbstractJpaDao<ReportsBookmark, Long> implements BookmarkDAO {

}
