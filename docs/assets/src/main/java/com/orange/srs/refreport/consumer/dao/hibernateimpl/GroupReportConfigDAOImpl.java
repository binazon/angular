package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.GroupReportConfigDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.DataLocation;
import com.orange.srs.refreport.model.GroupReportConfig;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.GroupReportConfigTO;
import com.orange.srs.statcommon.technical.ModelUtils;

@Stateless
public class GroupReportConfigDAOImpl extends AbstractJpaDao<GroupReportConfig, Long> implements GroupReportConfigDAO {

	@Override
	public List<GroupReportConfig> findGroupReportConfigForOption(String optionAlias) {

		String jpqlQuery = "SELECT grc " + " FROM " + getEntityName() + " grc, "
				+ ModelUtils.getEntityNameForClass(OfferOption.class) + " so" + " WHERE so." + OfferOption.FIELD_ALIAS
				+ "=:optionAlias " + " AND grc." + GroupReportConfig.FIELD_REPORT_CONFIG + " MEMBER OF so."
				+ OfferOption.FIELD_REPORTCONFIGS + " ORDER BY grc." + GroupReportConfig.FIELD_REPORT_CONFIG + "."
				+ ReportConfig.FIELD_ALIAS + " ASC";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("optionAlias", optionAlias);

		return query.getResultList();
	}

	@Override
	public List<GroupReportConfig> findGroupReportConfigForOptionLocationASC(String optionAlias) {

		String jpqlQuery = "SELECT grc " + " FROM " + getEntityName() + " grc, "
				+ ModelUtils.getEntityNameForClass(OfferOption.class) + " so" + " WHERE so." + OfferOption.FIELD_ALIAS
				+ "=:optionAlias " + " AND grc." + GroupReportConfig.FIELD_REPORT_CONFIG + " MEMBER OF so."
				+ OfferOption.FIELD_REPORTCONFIGS + " AND grc." + GroupReportConfig.FIELD_REPORTING_GROUP + "."
				+ ReportingGroup.FIELD_DATA_LOCATION + " IS NOT NULL" + " ORDER BY grc."
				+ GroupReportConfig.FIELD_REPORT_CONFIG + "." + ReportConfig.FIELD_ALIAS + " ASC," + " 		  grc."
				+ GroupReportConfig.FIELD_REPORTING_GROUP + "." + ReportingGroup.FIELD_DATA_LOCATION + "."
				+ DataLocation.FIELD_LOCATION_PATTERN + " ASC";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("optionAlias", optionAlias);

		return query.getResultList();
	}

	@Override
	public List<GroupReportConfig> findGroupReportConfigForOptionAndGroup(String optionAlias, String origin,
			String reportingGroupRef) {

		String jpqlQuery = "SELECT grc " + " FROM " + ModelUtils.getEntityNameForClass(ReportingGroup.class) + " rg"
				+ " JOIN rg." + ReportingGroup.FIELD_GROUP_REPORT_CONFIGS + " grc" + " JOIN grc."
				+ GroupReportConfig.FIELD_REPORT_CONFIG + " rc" + " JOIN rc." + ReportConfig.FIELD_OFFER_OPTION
				+ " oo on oo." + OfferOption.FIELD_ALIAS + "=:optionAlias" + " WHERE rg."
				+ ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef " + " AND rg."
				+ ReportingGroup.FIELD_ORIGIN + "=:origin ";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("origin", origin);
		query.setParameter("reportingGroupRef", reportingGroupRef);
		query.setParameter("optionAlias", optionAlias);

		return query.getResultList();
	}

	@Override
	public List<Object[]> findGroupReportConfigPksAndParamTypeAliasAndReportingGroupPkAssociated() {

		String jpqlQuery = "SELECT grc." + GroupReportConfig.FIELD_PK + ", grc." + GroupReportConfig.FIELD_IS_ENABLE
				+ ", param." + ParamType.FIELD_ALIAS + ", grc." + GroupReportConfig.FIELD_REPORTING_GROUP + '.'
				+ ReportingGroup.FIELD_PK + " FROM " + getEntityName() + " grc" + " INNER JOIN grc."
				+ GroupReportConfig.FIELD_REPORT_CONFIG + '.' + ReportConfig.FIELD_PARAM_TYPES + " param"
				+ " ORDER BY grc." + GroupReportConfig.FIELD_PK;

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	public int enableOrDisableGroupReportConfig(List<Long> groupReportConfigPks, boolean toBeEnabled) {
		if (groupReportConfigPks.isEmpty()) {
			return 0;
		} else {
			String jpqlQuery = "UPDATE " + getEntityName() + " grc" + " SET grc." + GroupReportConfig.FIELD_IS_ENABLE
					+ "=:is_enable" + " WHERE grc." + GroupReportConfig.FIELD_PK + " IN :pks ";

			Query query = getEntityManager().createQuery(jpqlQuery);
			query.setParameter("is_enable", toBeEnabled);
			query.setParameter("pks", groupReportConfigPks);
			return query.executeUpdate();
		}
	}

	@Override
	public List<GroupReportConfigTO> findAdditionalGroupReportConfigTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.groupReportConfigTOBuilder("grc", "rg", "rc", "oo")
				+ " FROM " + getEntityName() + " grc " + " INNER JOIN grc." + GroupReportConfig.FIELD_REPORTING_GROUP
				+ " rg" + " INNER JOIN grc." + GroupReportConfig.FIELD_REPORT_CONFIG + " rc" + " INNER JOIN rc."
				+ ReportConfig.FIELD_OFFER_OPTION + " oo" + " WHERE rc." + ReportConfig.FIELD_OPTIONAL + " IS TRUE";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();

	}
}