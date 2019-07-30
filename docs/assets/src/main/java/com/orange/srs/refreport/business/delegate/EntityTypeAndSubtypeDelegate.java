package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.EntityTypeAndSubTypeDAO;
import com.orange.srs.refreport.model.EntityTypeAndSubtype;
import com.orange.srs.refreport.model.EntityTypeAndSubtypeId;
import com.orange.srs.refreport.model.TO.provisioning.SubtypeProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.TypeAndSubtypesProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.TypeSubtypeListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class EntityTypeAndSubtypeDelegate {

	@EJB
	private EntityTypeAndSubTypeDAO entityTypeAndSubTypeDAO;

	public static EntityTypeAndSubtypeId getEntityTypeAndSubtypeKey(EntityTypeAndSubtype entityTypeAndSubtype) {
		return entityTypeAndSubtype.getEntityTypeAndSubtypeId();
	}

	public static String getTypeAndSubtypesProvisioningTOKey(
			TypeAndSubtypesProvisioningTO typeAndSubtypesProvisioningTO) {
		return typeAndSubtypesProvisioningTO.type;
	}

	public static EntityTypeAndSubtypeId getSubtypeProvisioningTOKey(String entityType,
			SubtypeProvisioningTO subtypeProvisioningTO) {
		EntityTypeAndSubtypeId entityTypeAndSubtypeId = new EntityTypeAndSubtypeId();
		entityTypeAndSubtypeId.setType(entityType);
		entityTypeAndSubtypeId.setSubtype(subtypeProvisioningTO.value);
		return entityTypeAndSubtypeId;
	}

	public EntityTypeAndSubtype getEntityTypeAndSubtypeByKey(String entityType, String entitySubtype)
			throws BusinessException {
		EntityTypeAndSubtypeId entityTypeAndSubtypeId = new EntityTypeAndSubtypeId();
		entityTypeAndSubtypeId.setType(entityType);
		entityTypeAndSubtypeId.setSubtype(entitySubtype);
		EntityTypeAndSubtype entityTypeAndSubtype = entityTypeAndSubTypeDAO.findById(entityTypeAndSubtypeId);
		if (entityTypeAndSubtype == null) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION
					+ ": EntityTypeAndSubtype with key [type=" + entityType + ",subtype=" + entitySubtype + "]");
		}
		return entityTypeAndSubtype;
	}

	public EntityTypeAndSubtype createEntityTypeAndSubtype(String entityType,
			SubtypeProvisioningTO subtypeProvisioningTO) {
		EntityTypeAndSubtype entityTypeAndSubtype = new EntityTypeAndSubtype();
		entityTypeAndSubtype.setTypeAndSubType(entityType, subtypeProvisioningTO.value);
		entityTypeAndSubtype.setComment(subtypeProvisioningTO.comment);
		entityTypeAndSubTypeDAO.persistAndFlush(entityTypeAndSubtype);
		return entityTypeAndSubtype;
	}

	public boolean updateEntityTypeAndSubtypeIfNecessary(EntityTypeAndSubtype entityTypeAndSubtype,
			SubtypeProvisioningTO subtypeProvisioningTO) {
		boolean updated = false;
		if (!subtypeProvisioningTO.comment.equals(entityTypeAndSubtype.getComment())) {
			entityTypeAndSubtype.setComment(subtypeProvisioningTO.comment);
			entityTypeAndSubTypeDAO.persistAndFlush(entityTypeAndSubtype);
			updated = true;
		}
		return updated;
	}

	public void removeEntityTypeAndSubtype(EntityTypeAndSubtype entityTypeAndSubtype) {
		entityTypeAndSubTypeDAO.remove(entityTypeAndSubtype);
	}

	public void removeAllEntityType(String entityType) {
		List<EntityTypeAndSubtype> entityTypeAndSubtypes = entityTypeAndSubTypeDAO
				.findBy(EntityTypeAndSubtype.FIELD_TYPE, entityType);
		entityTypeAndSubTypeDAO.remove(entityTypeAndSubtypes);
	}

	public List<String> getAllEntityTypeSorted() {
		List<String> typeSubtypeList = entityTypeAndSubTypeDAO.findDistinctType();
		Collections.sort(typeSubtypeList);
		return typeSubtypeList;
	}

	public List<EntityTypeAndSubtype> getAllTypeSubtypeSortedForEntityType(String entityType) {
		List<EntityTypeAndSubtype> entityTypeAndSubtypes = entityTypeAndSubTypeDAO
				.findBy(EntityTypeAndSubtype.FIELD_TYPE, entityType);
		sortEntityTypeAndSubtype(entityTypeAndSubtypes);
		return entityTypeAndSubtypes;
	}

	public TypeSubtypeListProvisioningTO getTypeSubtypeListProvisioningTOSorted() {
		TypeSubtypeListProvisioningTO typeSubtypeListProvisioningTO = new TypeSubtypeListProvisioningTO();
		List<EntityTypeAndSubtype> entityTypeAndSubtypes = entityTypeAndSubTypeDAO.findAll();
		sortEntityTypeAndSubtype(entityTypeAndSubtypes);
		TypeAndSubtypesProvisioningTO currentTypeAndSubtypesProvisioningTO = null;
		for (EntityTypeAndSubtype entityTypeAndSubtype : entityTypeAndSubtypes) {
			if (currentTypeAndSubtypesProvisioningTO == null
					|| !currentTypeAndSubtypesProvisioningTO.type.equals(entityTypeAndSubtype.getType())) {
				currentTypeAndSubtypesProvisioningTO = new TypeAndSubtypesProvisioningTO();
				currentTypeAndSubtypesProvisioningTO.type = entityTypeAndSubtype.getType();
				typeSubtypeListProvisioningTO.typeAndSubtypesProvisioningTOs.add(currentTypeAndSubtypesProvisioningTO);
			}
			SubtypeProvisioningTO subtypeProvisioningTO = new SubtypeProvisioningTO();
			subtypeProvisioningTO.value = entityTypeAndSubtype.getSubtype();
			subtypeProvisioningTO.comment = entityTypeAndSubtype.getComment();
			currentTypeAndSubtypesProvisioningTO.subtypeProvisioningTOs.add(subtypeProvisioningTO);
		}
		return typeSubtypeListProvisioningTO;
	}

	public static void sortEntityTypeAndSubtype(List<EntityTypeAndSubtype> entityTypeAndSubtypes) {
		Collections.sort(entityTypeAndSubtypes, new EntityTypeAndSubtypeComparator());
	}

	public static void sortTypeAndSubtypesProvisioningTO(
			List<TypeAndSubtypesProvisioningTO> typeAndSubtypesProvisioningTOs) {
		Collections.sort(typeAndSubtypesProvisioningTOs, new TypeAndSubtypesProvisioningTOComparator());
	}

	public static void sortSubtypeProvisioningTO(String entityType,
			List<SubtypeProvisioningTO> subtypeProvisioningTOs) {
		Collections.sort(subtypeProvisioningTOs, new SubtypeProvisioningTOComparator(entityType));
	}

	private static class EntityTypeAndSubtypeComparator implements Comparator<EntityTypeAndSubtype> {
		@Override
		public int compare(EntityTypeAndSubtype firstObj, EntityTypeAndSubtype secondObj) {
			return getEntityTypeAndSubtypeKey(firstObj).compareTo(getEntityTypeAndSubtypeKey(secondObj));
		}
	}

	private static class TypeAndSubtypesProvisioningTOComparator implements Comparator<TypeAndSubtypesProvisioningTO> {

		@Override
		public int compare(TypeAndSubtypesProvisioningTO firstObj, TypeAndSubtypesProvisioningTO secondObj) {
			return getTypeAndSubtypesProvisioningTOKey(firstObj)
					.compareTo(getTypeAndSubtypesProvisioningTOKey(secondObj));
		}
	}

	private static class SubtypeProvisioningTOComparator implements Comparator<SubtypeProvisioningTO> {

		private String entityType;

		public SubtypeProvisioningTOComparator(String entityType) {
			this.entityType = entityType;
		}

		@Override
		public int compare(SubtypeProvisioningTO firstObj, SubtypeProvisioningTO secondObj) {
			return getSubtypeProvisioningTOKey(entityType, firstObj)
					.compareTo(getSubtypeProvisioningTOKey(entityType, secondObj));
		}
	}
}
