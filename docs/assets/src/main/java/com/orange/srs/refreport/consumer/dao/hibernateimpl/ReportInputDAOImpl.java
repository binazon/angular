package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.consumer.dao.ReportInputDAO;
import com.orange.srs.refreport.model.Report;
import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputProvisioningTO;
import com.orange.srs.statcommon.model.TO.ReportInputTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;
import com.orange.srs.statcommon.model.parameter.report.ReportInputKeyParameter;
import com.orange.srs.statcommon.technical.ModelUtils;

@Stateless
public class ReportInputDAOImpl extends AbstractJpaDao<ReportInput, Long> implements ReportInputDAO {

	@Override
	public List<ReportInputTO> findAllReportInput() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportInputTOBuilder("ri", "if") + " FROM "
				+ getEntityName() + " ri " + " JOIN ri." + ReportInput.FIELD_FORMAT + " if";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ReportInputTO> findReportInputByReportRefId(String reportRefId) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportInputTOBuilder("ri", "if") + " FROM "
				+ getEntityName() + " ri, " + ModelUtils.getEntityNameForClass(Report.class) + " r" + " JOIN ri."
				+ ReportInput.FIELD_FORMAT + " if " + " WHERE r." + Report.FIELD_REFID
				+ "=:reportRefId AND ri MEMBER OF r." + Report.FIELD_REPORT_INPUTS;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("reportRefId", reportRefId);
		return query.getResultList();

	}

	@Override
	public List<ReportInput> findReportInputByKey(ReportInputKeyParameter keyParameter) {
		String jpqlQuery = "SELECT ri FROM  " + getEntityName() + " ri " + " WHERE ri." + ReportInput.FIELD_GRANULARITY
				+ "=:granularity " + " AND ri." + ReportInput.FIELD_REPORT_INPUT_REF + "=:reportInputRef " + " AND ri."
				+ ReportInput.FIELD_SOURCE_TIME_UNIT + "=:sourceTimeUnit";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("granularity", keyParameter.granularity);
		query.setParameter("reportInputRef", keyParameter.reportInputRef);
		query.setParameter("sourceTimeUnit", keyParameter.sourceTimeUnit);

		return query.getResultList();
	}

	@Override
	public List<ReportInputKeyTO> findAvailableReportInputKeys() {

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportInputKeyTOListBuilder("ri") + " FROM  "
				+ getEntityName() + " ri WHERE ri." + ReportInput.FIELD_REPORT_INPUT_SOURCE_CLASS + " IS NULL";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ReportInputProvisioningTO> findAllReportInputProvisioningTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportInputProvisioningTOBuilder("ri") + " FROM "
				+ getEntityName() + " ri";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ReportInput> findCassandraReportInput() {
		String jpqlQuery = "SELECT ri FROM  " + getEntityName() + " ri" + " WHERE ri." + ReportInput.FIELD_TYPEDB
				+ "=:cassandra ";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("cassandra", "CASSANDRA");
		return query.getResultList();
	}

}
