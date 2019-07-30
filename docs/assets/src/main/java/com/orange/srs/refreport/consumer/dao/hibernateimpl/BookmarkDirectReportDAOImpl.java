package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.BookmarkDirectReportDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.BookmarkDirectReport;
import com.orange.srs.refreport.model.EntityTypeAndSubtype;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.TO.provisioning.BookmarkDirectReportProvisioningTO;

@Stateless
public class BookmarkDirectReportDAOImpl extends AbstractJpaDao<BookmarkDirectReport, String>
		implements BookmarkDirectReportDAO {

	@Override
	public List<BookmarkDirectReportProvisioningTO> findAllBookmarkDirectReportProvisioningTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.bookmarkDirectReportProvisioningTOBuilder("b", "pt")
				+ " FROM " + getEntityName() + " b" + " LEFT OUTER JOIN b."
				+ BookmarkDirectReport.FIELD_ADDITIONAL_PARAM_TYPE + " pt";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<BookmarkDirectReport> findByLabelAndEntityTypeAndSubtype(String label, String entityType,
			String entitySubType) {
		String jpqlQuery = "SELECT b FROM " + getEntityName() + " b INNER JOIN b."
				+ BookmarkDirectReport.FIELD_PARAM_TYPE + " pt WHERE " + BookmarkDirectReport.FIELD_LABEL + "= :label";

		if (StringUtils.isNotEmpty(entityType)) {
			jpqlQuery += " AND pt." + ParamType.FIELD_ENTITY_TYPE + " = :entityType AND (pt."
					+ ParamType.FIELD_ENTITY_SUBTYPE + " = :entitySubType OR pt." + ParamType.FIELD_ENTITY_SUBTYPE
					+ " = '" + EntityTypeAndSubtype.ALL_TYPES_OR_SUBTYPES + "')";
		}
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("label", label);

		if (StringUtils.isNotEmpty(entityType)) {
			query.setParameter("entityType", entityType);
			query.setParameter("entitySubType",
					StringUtils.isNotEmpty(entitySubType) ? entitySubType : EntityTypeAndSubtype.ALL_TYPES_OR_SUBTYPES);
		}
		return query.getResultList();
	}
}
