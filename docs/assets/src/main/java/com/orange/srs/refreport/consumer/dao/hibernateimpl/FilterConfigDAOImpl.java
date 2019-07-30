package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.FilterConfigDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.FilterConfig;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.FilterConfigTO;
import com.orange.srs.statcommon.technical.ModelUtils;

@Stateless
public class FilterConfigDAOImpl extends AbstractJpaDao<FilterConfig, Long> implements FilterConfigDAO {

	@Override
	public List<FilterConfig> getFilterConfigsForOptionAndReportingGroup(String optionAlias, String origin,
			String reportingGroupRef) {

		String jpqlQuery = "SELECT fc " + " FROM " + ModelUtils.getEntityNameForClass(ReportingGroup.class) + " rg, "
				+ getEntityName() + " fc" + " JOIN fc." + FilterConfig.FIELD_OFFER_OPTION + " oo" + " WHERE rg."
				+ ReportingGroup.FIELD_ORIGIN + "=:origin" + " AND rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF
				+ "=:reportingGroupRef" + " AND fc MEMBER OF rg." + ReportingGroup.FIELD_FILTER_CONFIGS + " AND oo."
				+ OfferOption.FIELD_ALIAS + "=:optionAlias";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("origin", origin);
		query.setParameter("reportingGroupRef", reportingGroupRef);
		query.setParameter("optionAlias", optionAlias);

		return query.getResultList();
	}

	@Override
	public List<FilterConfigTO> getFilterConfigTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.filterConfigTOBuilder("fc") + " FROM "
				+ getEntityName() + " fc";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

}
