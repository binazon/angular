package com.orange.srs.refreport.consumer.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.orange.srs.statcommon.technical.ModelUtils;

/**
 * Generic abstract JPA Dao that will be extended by every specific Dao
 * 
 * @author A116174
 * @param <T>
 * @param <IdType>
 */
public abstract class AbstractJpaDao<T, IdType extends Serializable> implements Dao<T, IdType> {

	private Class<T> entityClass;
	private String entityName;
	private Class<IdType> idTypeClass;
	private String idAttribute;

	@Inject
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public AbstractJpaDao() {

		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		this.entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
		this.idTypeClass = (Class<IdType>) genericSuperclass.getActualTypeArguments()[1];

		// Entity attribute holding @Id
		for (Field field : entityClass.getDeclaredFields()) {
			if (field.getAnnotation(Id.class) != null) {
				idAttribute = field.getName();
			}
		}

		entityName = getEntityNameForClass(entityClass);
	}

	public AbstractJpaDao(Class<T> clazz) {
		this.entityClass = clazz;
	}

	public static String getEntityNameForClass(Class<?> clazz) {
		Entity entity = (Entity) clazz.getAnnotation(Entity.class);

		// Entity name with specified name if not empty, otherwise use Class name as
		// default name
		String entityName;
		if ("".equals(entity.name())) {
			entityName = clazz.getName();
		} else {
			entityName = entity.name();
		}
		return entityName;
	}

	public EntityManager getEntityManager() {
		if (entityManager == null) {
			throw new IllegalStateException("EntityManager has not been set on DAO before usage");
		}
		return entityManager;
	}

	public void setEntityManager(EntityManager em) {
		this.entityManager = em;
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public String getEntityName() {
		return entityName;
	}

	public String getIdAttribute() {
		return idAttribute;
	}

	/**
	 * Analyze the constraint violations.
	 * 
	 * @param exception
	 *            must not be <code>null</code>.
	 */
	private void analyzeConstraintViolations(ConstraintViolationException exception) {
		if (exception.getConstraintViolations().isEmpty()) {
			StringBuilder errorMessage = new StringBuilder();
			for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
				errorMessage.append("Attribut: ").append(violation.getPropertyPath());
				errorMessage.append(" / Value: ").append(violation.getInvalidValue());
				errorMessage.append(" => ").append(violation.getMessage());
			}
			throw new IllegalStateException("Persitence constraints violation : " + errorMessage);
		}
	}

	public T persist(T transientEntity) {
		try {
			getEntityManager().persist(transientEntity);
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return transientEntity;
	}

	public T persistAndFlush(T transientEntity) {
		try {
			getEntityManager().persist(transientEntity);
			// getEntityManager().flush();
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return transientEntity;
	}

	public T merge(T detachedEntity) {
		try {
			getEntityManager().merge(detachedEntity);
			// getEntityManager().flush();
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return detachedEntity;
	}

	public T mergeAndFlush(T detachedEntity) {
		try {
			getEntityManager().merge(detachedEntity);
			// getEntityManager().flush();
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return detachedEntity;
	}

	public T remove(T entity) {
		try {
			getEntityManager().remove(entity);
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return entity;
	}

	public int removeAll() {
		String jpqlQuery = "DELETE FROM " + entityName + " p";
		Query query = entityManager.createQuery(jpqlQuery);
		return query.executeUpdate();
	}

	public T refresh(T entity) {
		try {
			getEntityManager().refresh(entity);
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return entity;
	}

	public void detach(T entity) {
		getEntityManager().detach(entity);
	}

	public List<T> persist(List<T> entities) {
		try {
			for (T entity : entities) {
				getEntityManager().persist(entity);
			}
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return entities;
	}

	public List<T> persistAndFlush(List<T> entities) {
		try {
			for (T entity : entities) {
				getEntityManager().persist(entity);
			}
			// getEntityManager().flush();
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return entities;
	}

	public List<T> merge(List<T> entities) {
		try {
			for (T entity : entities) {
				getEntityManager().merge(entity);
			}
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return entities;
	}

	public List<T> mergeAndFlush(List<T> entities) {
		try {
			for (T entity : entities) {
				getEntityManager().merge(entity);
			}
			// getEntityManager().flush();
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return entities;
	}

	public List<T> remove(List<T> entities) {
		try {
			for (T entity : entities) {
				getEntityManager().remove(entity);
			}
		} catch (ConstraintViolationException e) {
			analyzeConstraintViolations(e);
		}
		return entities;
	}

	public List<T> refresh(List<T> entities) {
		for (T entity : entities) {
			getEntityManager().refresh(entity);
		}
		return entities;
	}

	public void detach(List<T> entities) {
		for (T entity : entities) {
			getEntityManager().detach(entity);
		}
	}

	public T findById(IdType id) {
		return getEntityManager().find(entityClass, id);
	}

	public List<T> findBy(String attribute, Object value) {
		String jpqlQuery = "SELECT p FROM " + entityName + " p WHERE p." + attribute + " = ?1 ";
		TypedQuery<T> query = entityManager.createQuery(jpqlQuery, entityClass);
		query.setParameter(1, value);

		return query.getResultList();
	}

	public T findSingleResultBy(String attribute, Object value) throws NoResultException, NonUniqueResultException {
		List<T> resultList = findBy(attribute, value);
		if (resultList.size() == 1) {
			return resultList.get(0);
		} else if (resultList.size() == 0) {
			throw new NoResultException("result returns no " + entityName + " element");
		} else {
			throw new NonUniqueResultException("result returns more than one " + entityName + " elements");
		}
	}

	public List<T> findByMultipleCriteria(String[] attributes, Object[] values) {
		if (attributes.length != values.length) {
			throw new RuntimeException(
					"Exception in findByMultipleCriteria, attributes length and values length must be equals ");
		}

		String jpqlQuery = "SELECT p FROM " + entityName + " p WHERE ";
		for (int i = 0; i < attributes.length; i++) {
			jpqlQuery += "p." + attributes[i] + " = ?" + (i + 1);
			if (i < attributes.length - 1) {
				jpqlQuery += " AND ";
			}
		}
		TypedQuery<T> query = entityManager.createQuery(jpqlQuery, entityClass);
		for (int j = 0; j < values.length; j++) {
			query.setParameter((j + 1), values[j]);
		}
		return query.getResultList();
	}

	public T findSingleResultByMultipleCriteria(String[] attributes, Object[] values)
			throws NoResultException, NonUniqueResultException {
		List<T> resultList = findByMultipleCriteria(attributes, values);
		if (resultList.size() == 1) {
			return resultList.get(0);
		} else if (resultList.size() == 0) {
			throw new NoResultException("result returns no " + entityName + " element");
		} else {
			throw new NonUniqueResultException("result returns more than one " + entityName + " elements");
		}
	}

	public List<T> findByChunked(String attribute, Object value, int offset, int chunkSize) {
		String jpqlQuery = "SELECT p FROM " + entityName + " p WHERE p." + attribute + " = ?1";
		TypedQuery<T> query = entityManager.createQuery(jpqlQuery, entityClass);
		query.setParameter(1, value).setFirstResult(offset).setMaxResults(chunkSize);

		return query.getResultList();
	}

	public List<T> findByChunkedUsingId(String attribute, Object value, int offset, int chunkSize) {

		String jpqlQueryForPks = "SELECT p." + idAttribute + " FROM " + entityName + " p WHERE p." + attribute
				+ " = ?1";
		TypedQuery<IdType> queryPk = entityManager.createQuery(jpqlQueryForPks, idTypeClass);
		queryPk.setParameter(1, value);
		List<IdType> pkList = queryPk.getResultList();

		String jpqlQueryChunck = "SELECT p FROM " + entityName + " p WHERE p." + idAttribute + " IN :pkList";
		TypedQuery<T> queryChunck = entityManager.createQuery(jpqlQueryChunck, entityClass);
		queryChunck.setParameter("pkList", pkList.subList(offset, offset + chunkSize));

		return queryChunck.getResultList();
	}

	public List<T> findByOrderedBy(String attribute, Object value, String orderAttribute) {
		return findByOrderedBy(attribute, value, orderAttribute, false);
	}

	public List<T> findByOrderedBy(String attribute, Object value, String orderAttribute, boolean reverseOrder) {
		String jpqlQuery = "SELECT p FROM " + entityName + " p WHERE p." + attribute + "= ?1 ORDER BY p."
				+ orderAttribute;
		if (reverseOrder) {
			jpqlQuery += " DESC";
		}
		TypedQuery<T> query = entityManager.createQuery(jpqlQuery, entityClass);
		query.setParameter(1, value);

		return query.getResultList();
	}

	public List<T> findAll() {
		String jpqlQuery = "SELECT p FROM " + entityName + " p";
		TypedQuery<T> query = entityManager.createQuery(jpqlQuery, entityClass);

		return query.getResultList();
	}

	public List<T> findAllChunked(int offset, int chunkSize) {
		String jpqlQuery = "SELECT p FROM " + entityName + " p";
		TypedQuery<T> query = entityManager.createQuery(jpqlQuery, entityClass);
		query.setFirstResult(offset).setMaxResults(chunkSize);

		return query.getResultList();
	}

	public List<T> findAllChunkedUsingId(int offset, int chunkSize) {
		String jpqlQueryForPks = "SELECT p." + idAttribute + " FROM " + entityName + " p";
		TypedQuery<IdType> queryPk = entityManager.createQuery(jpqlQueryForPks, idTypeClass);
		List<IdType> pkList = queryPk.getResultList();

		String jpqlQueryChunck = "SELECT p FROM " + entityName + " p WHERE p." + idAttribute + " IN :pkList";
		TypedQuery<T> queryChunck = entityManager.createQuery(jpqlQueryChunck, entityClass);
		queryChunck.setParameter("pkList", pkList.subList(offset, offset + chunkSize));

		return queryChunck.getResultList();
	}

	public List<T> findAllOrderedBy(String orderAttribute) {
		return findAllOrderedBy(orderAttribute, false);
	}

	public List<T> findAllOrderedBy(String orderAttribute, boolean reverseOrder) {
		String jpqlQuery = "SELECT p FROM " + entityName + " p ORDER BY p." + orderAttribute;
		if (reverseOrder) {
			jpqlQuery += " DESC";
		}
		TypedQuery<T> query = entityManager.createQuery(jpqlQuery, entityClass);
		return query.getResultList();
	}

	public void clear() {
		getEntityManager().clear();
	}

	public void flush() {
		getEntityManager().setFlushMode(FlushModeType.AUTO);
		getEntityManager().flush();
		getEntityManager().clear();
	}

	public long count() {
		String jpqlQuery = "SELECT COUNT(*) FROM " + entityName + " p";
		TypedQuery<Long> query = entityManager.createQuery(jpqlQuery, Long.class);
		return query.getSingleResult();
	}

	public long countBy(String attribute, Object value) {
		String jpqlQuery = "SELECT COUNT(*) FROM " + entityName + " p WHERE p." + attribute + " = ?1";
		TypedQuery<Long> query = entityManager.createQuery(jpqlQuery, Long.class);
		query.setParameter(1, value);
		return query.getSingleResult();
	}

	public int updateMultipleValueById(IdType idValue, String[] attributes, Object[] values) {

		if (attributes.length != values.length) {
			throw new RuntimeException(
					"Exception in updateMultipleValueById, attributes length and values length must be equals ");
		}

		StringBuilder jpqlQuery = new StringBuilder("UPDATE ");
		jpqlQuery.append(entityName).append(" SET ");
		for (int i = 0; i < attributes.length; i++) {
			jpqlQuery.append("p.").append(attributes[i]).append(" = ?").append(i + 1);
			if (i < attributes.length - 1) {
				jpqlQuery.append(',');
			}
		}
		jpqlQuery.append(" WHERE ").append(idAttribute).append("=:idValue");

		Query query = entityManager.createQuery(jpqlQuery.toString());
		for (int j = 0; j < values.length; j++) {
			query.setParameter((j + 1), values[j]);
		}
		query.setParameter("idValue", idValue);

		return query.executeUpdate();
	}

	public void truncate() {
		getEntityManager().createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
		getEntityManager().createNativeQuery("TRUNCATE TABLE " + ModelUtils.getEntityTableName(entityClass))
				.executeUpdate();
		getEntityManager().createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
	}
}
