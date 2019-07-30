package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.consumer.dao.ReportConfigDAO;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.Report;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.TO.ReportConfigIndicatorIdTO;
import com.orange.srs.refreport.model.TO.ReportConfigParamTypeAliasTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportConfigProvisioningTO;
import com.orange.srs.statcommon.technical.ModelUtils;

@Stateless
public class ReportConfigDAOImpl extends AbstractJpaDao<ReportConfig, Long> implements ReportConfigDAO {

	@Override
	public List<ReportConfigParamTypeAliasTO> getAllParamTypeAliasAndReportConfigPk() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportConfigParamTypeAliasTOBuilder("rc", "pt")
				+ " FROM " + getEntityName() + " rc " + " INNER JOIN rc." + ReportConfig.FIELD_PARAM_TYPES + " pt";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ReportConfig> findReportConfigsForOptionASC(String optionAlias) {

		String jpqlQuery = "SELECT rc " + " FROM " + getEntityName() + " rc, "
				+ ModelUtils.getEntityNameForClass(OfferOption.class) + " so" + " WHERE so." + OfferOption.FIELD_ALIAS
				+ "=:optionAlias " + " AND rc MEMBER OF so." + OfferOption.FIELD_REPORTCONFIGS + " ORDER BY rc."
				+ ReportConfig.FIELD_ALIAS + " ASC";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("optionAlias", optionAlias);

		return query.getResultList();
	}

	@Override
	public List<ReportConfigProvisioningTO> findAllReportConfigProvisioningTOWithoutParamTypeForOfferOption(
			String offerOptionAlias) {

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportConfigProvisioningTOBuilder("rc") + " FROM "
				+ getEntityName() + " rc, " + ModelUtils.getEntityNameForClass(OfferOption.class) + " so" + " WHERE so."
				+ OfferOption.FIELD_ALIAS + "=:optionAlias " + " AND rc MEMBER OF so."
				+ OfferOption.FIELD_REPORTCONFIGS;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("optionAlias", offerOptionAlias);

		return query.getResultList();
	}

	@Override
	public List<String> findAllReportRefIdLinkedToReportConfigForOfferOption(String offerOptionAlias) {

		String jpqlQuery = "SELECT rc." + ReportConfig.FIELD_ASSOCIATED_REPORT + "." + Report.FIELD_REFID + " FROM "
				+ getEntityName() + " rc, " + ModelUtils.getEntityNameForClass(OfferOption.class) + " so" + " WHERE so."
				+ OfferOption.FIELD_ALIAS + "=:optionAlias " + " AND rc MEMBER OF so."
				+ OfferOption.FIELD_REPORTCONFIGS;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("optionAlias", offerOptionAlias);

		return query.getResultList();
	}

	@Override
	public List<ReportConfigIndicatorIdTO> getAllIndicatorIdAndReportConfigPk() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportConfigIndicatorIdTOBuilder("rc", "i")
				+ " FROM " + getEntityName() + " rc " + " INNER JOIN rc." + ReportConfig.FIELD_INDICATORS + " i";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}
