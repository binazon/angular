package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.consumer.dao.ReportUserDAO;
import com.orange.srs.refreport.model.ReportUser;
import com.orange.srs.refreport.model.TO.ReportUserTO;
import com.orange.srs.statcommon.technical.ModelUtils;

@Stateless
public class ReportUserDAOImpl extends AbstractJpaDao<ReportUser, Long> implements ReportUserDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<ReportUserTO> findReportUserByReportUserPk(Long reportUserId) {
		String jpqlQuery = "SELECT ru." + ReportUser.FIELD_PK + " FROM  " + getEntityName() + " ru, "
				+ ModelUtils.getEntityNameForClass(ReportUser.class) + " u" + " WHERE u." + ReportUser.FIELD_PK
				+ "=:reportUserId)";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("reportUserId", reportUserId);

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ReportUserTO> findAllReportUserTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportUserTOBuilder("ru") + " FROM "
				+ getEntityName() + " ru";

		Query query = getEntityManager().createQuery(jpqlQuery);

		return query.getResultList();
	}

}
