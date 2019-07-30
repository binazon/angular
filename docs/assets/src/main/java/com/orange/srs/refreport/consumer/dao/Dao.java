package com.orange.srs.refreport.consumer.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

/**
 * Generic implementation of Dao that will be implemented by every specific Dao
 * 
 * @author A116174
 * @param <T>
 * @param <IdType>
 */
public interface Dao<T, IdType extends Serializable> {

	/**
	 * Persist the instance and flush
	 *
	 * @param transientEntity
	 *            must not be <code>null</code>.
	 * @return the instance.
	 */
	T persistAndFlush(T transientEntity);

	/**
	 * Persist the instance. NB: After persist the primary key will not be available
	 * 
	 * @param transientEntity
	 *            must not be <code>null</code>.
	 * @return the instance.
	 */
	T persist(T transientEntity);

	/**
	 * Merge the instance.
	 *
	 * @param detachedEntity
	 *            must not be <code>null</code>.
	 * @return the instance.
	 */
	T mergeAndFlush(T detachedEntity);

	/**
	 * Merge the instance. NB: After merge the data from database may be not available
	 * 
	 * @param detachedEntity
	 *            must not be <code>null</code>.
	 * @return the instance.
	 */
	T merge(T detachedEntity);

	/**
	 * Deletes the instance.
	 *
	 * @param entity
	 *            must not be <code>null</code>.
	 * @return the instance.
	 */
	T remove(T entity);

	/**
	 * Deletes all instances.
	 *
	 * @return number of removed instance.
	 */
	int removeAll();

	/**
	 * Refresh the instance: update the entity object with values taken from the database
	 * 
	 * @param entity
	 *            must not be <code>null</code>.
	 * @return the instance.
	 */
	T refresh(T entity);

	/**
	 * Detach the instance: Remove the given entity from the persistence context, causing a managed entity to become
	 * detached. Unflushed changes made to the entity will not be synchronized to the database.
	 * 
	 * @param entity
	 *            must not be <code>null</code>.
	 */
	void detach(T entity);

	/**
	 * Persist the list of instances.
	 *
	 * @param transientEntity
	 *            must not be <code>null</code>.
	 * @return the list of instance.
	 */
	List<T> persistAndFlush(List<T> transientEntities);

	/**
	 * Persist the list of instances. NB: After persist the primary key will not be available
	 * 
	 * @param transientEntity
	 *            must not be <code>null</code>.
	 * @return the list of instance.
	 */
	List<T> persist(List<T> transientEntities);

	/**
	 * Merge the list of instances.
	 *
	 * @param detachedEntity
	 *            must not be <code>null</code>.
	 * @return the list of instance.
	 */
	List<T> mergeAndFlush(List<T> detachedEntities);

	/**
	 * Merge the list of instances. NB: After merge the data from database may be not available
	 * 
	 * @param detachedEntity
	 *            must not be <code>null</code>.
	 * @return the list of instance.
	 */
	List<T> merge(List<T> detachedEntities);

	/**
	 * Deletes the list of instances.
	 *
	 * @param entity
	 *            must not be <code>null</code>.
	 * @return the list of instance.
	 */
	List<T> remove(List<T> entities);

	/**
	 * Refresh the list of instances: update the entity object with values taken from the database
	 * 
	 * @param entities
	 *            must not be <code>null</code>.
	 * @return the list of instance.
	 */
	List<T> refresh(List<T> entities);

	/**
	 * Detach the list of instances: Remove the given entities from the persistence context. Unflushed changes made to
	 * the entity will not be synchronized to the database.
	 * 
	 * @param entity
	 *            must not be <code>null</code>.
	 */
	void detach(List<T> entities);

	/**
	 * Finds the instance with the given id.
	 *
	 * @param id
	 *            must not be <code>null</code>.
	 * @return the instance, or <code>null</code>.
	 */
	T findById(IdType id);

	/**
	 * Retrieves all entries with the condition attribute = value.
	 * 
	 * @param attribute
	 *            must not be <code>null</code>.
	 * @param value
	 *            must not be <code>null</code>.
	 * @return never <code>null</code> but rather am empty list.
	 */
	List<T> findBy(String attribute, Object value);

	/**
	 * Retrieves one entry with the condition attribute = value.
	 * 
	 * @param attribute
	 *            must not be <code>null</code>.
	 * @param value
	 *            must not be <code>null</code>.
	 * @return entity or throw exception otherwise.
	 */
	T findSingleResultBy(String attribute, Object value) throws NoResultException, NonUniqueResultException;

	/**
	 * Retrieves all entries with the condition attributes[i] = values[i].
	 * 
	 * @param attribute
	 *            must not be <code>null</code>.
	 * @param value
	 *            must not be <code>null</code>.
	 * @return never <code>null</code> but rather am empty list.
	 */
	List<T> findByMultipleCriteria(String[] attributes, Object[] values);

	/**
	 * Retrieves one entry with the condition attributes[i] = values[i].
	 * 
	 * @param attribute
	 *            must not be <code>null</code>.
	 * @param value
	 *            must not be <code>null</code>.
	 * @return entity or throw exception otherwise.
	 */
	T findSingleResultByMultipleCriteria(String[] attributes, Object[] values)
			throws NoResultException, NonUniqueResultException;

	/**
	 * Retrieves all entries between offset and offset+chunksize with the condition attribute = value.
	 * 
	 * @param attribute
	 *            must not be <code>null</code>.
	 * @param value
	 *            must not be <code>null</code>.
	 * @param offset
	 * @param chunkSize
	 * @return never <code>null</code> but rather am empty list.
	 */
	List<T> findByChunked(String attribute, Object value, int offset, int chunkSize);

	/**
	 * Retrieves all entries between offset and offset+chunksize with the condition attribute = value. Returned entries
	 * range is done using Id (SQL IN condition)
	 * 
	 * @param attribute
	 *            must not be <code>null</code>.
	 * @param value
	 *            must not be <code>null</code>.
	 * @param offset
	 * @param chunkSize
	 * @return never <code>null</code> but rather am empty list.
	 */
	List<T> findByChunkedUsingId(String attribute, Object value, int offset, int chunkSize);

	/**
	 * Retrieves all entries with the condition attribute = value and ordered by orderAttribute
	 * 
	 * @param attribute
	 *            must not be <code>null</code>.
	 * @param value
	 *            must not be <code>null</code>.
	 * @param orderAttribute
	 *            must not be <code>null</code>.
	 * @return never <code>null</code> but rather am empty list.
	 */
	List<T> findByOrderedBy(String attribute, Object value, String orderAttribute);

	/**
	 * Retrieves all entries with the condition attribute = value and ordered by orderAttribute with a sort ascendant
	 * (reverseOrder = false) or descendant (reverseOrder = true).
	 * 
	 * @param attribute
	 *            must not be <code>null</code>.
	 * @param value
	 *            must not be <code>null</code>.
	 * @param orderAttribute
	 *            must not be <code>null</code>.
	 * @param reverseOrder
	 *            True to set descendant sort order.
	 * @return never <code>null</code> but rather am empty list.
	 */
	List<T> findByOrderedBy(String attribute, Object value, String orderAttribute, boolean reverseOrder);

	/**
	 * Finds all instances.
	 *
	 * @return never <code>null</code> but rather an empty list.
	 */
	List<T> findAll();

	/**
	 * Finds all instances between offset and offset+chunksize
	 * 
	 * @param offset
	 * @param chunkSize
	 * 
	 * @return never <code>null</code> but rather an empty list.
	 */
	List<T> findAllChunked(int offset, int chunkSize);

	/**
	 * Finds all instances between offset and offset+chunksize Returned entries range is done using Id (SQL IN
	 * condition)
	 * 
	 * @param offset
	 * @param chunkSize
	 * 
	 * @return never <code>null</code> but rather an empty list.
	 */
	List<T> findAllChunkedUsingId(int offset, int chunkSize);

	/**
	 * Finds all instances ordered by orderAttribute.
	 *
	 * @param orderAttribute
	 *            must not be <code>null</code>.
	 * @return never <code>null</code> but rather an empty list.
	 */
	List<T> findAllOrderedBy(String orderAttribute);

	/**
	 * Finds all instances ordered by orderAttribute with a sort ascendant (reverseOrder = false) or descendant
	 * (reverseOrder = true).
	 *
	 * @param orderAttribute
	 *            must not be <code>null</code>.
	 * @param reverseOrder
	 *            True to set descendant sort order.
	 * @return never <code>null</code> but rather an empty list.
	 */
	List<T> findAllOrderedBy(String orderAttribute, boolean reverseOrder);

	/**
	 * Clears the persistence context.
	 */
	void clear();

	/**
	 * Flush the persistence context.
	 */
	void flush();

	/**
	 * Return number of elements defined in a table
	 * 
	 * @return
	 */
	public long count();

	/**
	 * Return number of elements defined in a table with the condition attribute = value
	 * 
	 * @return
	 */
	public long countBy(String attribute, Object value);

	/**
	 * Update entity attributes using values with specified Id Return number of elements updated
	 * 
	 * @param id
	 * @param attributes
	 * @param values
	 * @return
	 */
	public int updateMultipleValueById(IdType id, String[] attributes, Object[] values);

	/**
	 * Truncate the table
	 */
	public void truncate();
}
