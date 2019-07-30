package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.consumer.dao.ReportDAO;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.Report;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.TO.ReportRefIdAndOfferOptionTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportProvisioningTO;

@Stateless
public class ReportDAOImpl extends AbstractJpaDao<Report, Long> implements ReportDAO {

	@Override
	public List<ReportProvisioningTO> findAllReportProvisioningTO() {

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportProvisioningTOBuilder("r") + " FROM "
				+ getEntityName() + " r";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ReportRefIdAndOfferOptionTO> findAllReportsWithOfferOptionTO() {

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportRefIdAndOfferOptionTOBuilder("r", "oo", "rco")
				+ " FROM " + getEntityName() + " r" + " INNER JOIN r." + Report.FIELD_REPORT_CONFIGS + " rc"
				+ " INNER JOIN rc." + ReportConfig.FIELD_OFFER_OPTION + " oo" + " INNER JOIN rc."
				+ ReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " rco" + " WHERE oo." + OfferOption.FIELD_TYPE
				+ " <> 'INTERACTIVE'";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}
