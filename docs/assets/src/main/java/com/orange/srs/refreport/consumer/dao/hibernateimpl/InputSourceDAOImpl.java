package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.InputSourceDAO;
import com.orange.srs.refreport.model.InputSource;
import com.orange.srs.refreport.model.Proxy;
import com.orange.srs.refreport.model.SourceClass;
import com.orange.srs.refreport.model.SourceProxy;
import com.orange.srs.statcommon.model.TO.report.InputSourceConfigurationTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceKey;
import com.orange.srs.statcommon.technical.ModelUtils;

@Stateless
public class InputSourceDAOImpl extends AbstractJpaDao<InputSource, Long> implements InputSourceDAO {

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.orange.srs.refreport.consumer.dao.InputSourceDAO#findByInputSourceKey(com.orange.srs.statcommon.model.TO.report.InputSourceKey)
	 */
	@SuppressWarnings("unchecked")
	public List<InputSource> findByInputSourceKey(InputSourceKey key) {
		String jpqlQuery = "SELECT p FROM " + getEntityName() + " p " + "WHERE p." + InputSource.FIELD_NAME + "=:name "
				+ "AND p." + InputSource.FIELD_SOURCE_CLASS + "." + SourceClass.FIELD_SOURCE_CLASS + "=:sourceClass";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("name", key.sourceName);
		query.setParameter("sourceClass", key.sourceClass);
		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.orange.srs.refreport.consumer.dao.InputSourceDAO#findProxyIndex(com.orange.srs.statcommon.model.TO.report.InputSourceKey,
	 *      java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	public List<SourceProxy> findProxyIndex(InputSourceKey key, Long proxyId) {
		Proxy proxy = new Proxy();
		proxy.setPk(proxyId);

		String jpqlQuery = "SELECT sp FROM " + getEntityName() + " p, "
				+ ModelUtils.getEntityNameForClass(SourceProxy.class) + " sp " + "WHERE p." + InputSource.FIELD_NAME
				+ "=:name " + "AND p." + InputSource.FIELD_SOURCE_CLASS + "." + SourceClass.FIELD_SOURCE_CLASS
				+ "=:sourceClass " + "AND sp." + SourceProxy.FIELD_PROXY + "=:proxy " + "AND sp MEMBER OF p."
				+ InputSource.FIELD_PROXIES;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("name", key.sourceName);
		query.setParameter("sourceClass", key.sourceClass);
		query.setParameter("proxy", proxy);

		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.orange.srs.refreport.consumer.dao.InputSourceDAO#findAllInputSourceKeys()
	 */
	@SuppressWarnings("unchecked")
	public List<InputSourceConfigurationTO> findAllInputSourceKeys(String sourceClass) {
		String jpqlQuery = "SELECT NEW "
				+ ModelUtils.buildJPATOConstructorArguments(InputSourceConfigurationTO.class, "p",
						InputSource.FIELD_NAME, InputSource.FIELD_SOURCE_CLASS + "." + SourceClass.FIELD_SOURCE_CLASS,
						InputSource.FIELD_POLLING_STATE)
				+ " FROM " + getEntityName() + " p" + " WHERE p." + InputSource.FIELD_SOURCE_CLASS + "."
				+ SourceClass.FIELD_SOURCE_CLASS + "=:sourceClass ";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("sourceClass", sourceClass);
		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.orange.srs.refreport.consumer.dao.InputSourceDAO#findAllInputSourceKeys()
	 */
	@SuppressWarnings("unchecked")
	public List<InputSourceKey> findAllInputSourceKeys() {
		String jpqlQuery = "SELECT NEW "
				+ ModelUtils.buildJPATOConstructorArguments(InputSourceKey.class, "sp", InputSource.FIELD_NAME,
						InputSource.FIELD_SOURCE_CLASS + "." + SourceClass.FIELD_SOURCE_CLASS)
				+ " FROM " + getEntityName() + " sp";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}
