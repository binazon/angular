package com.orange.srs.refreport.business.delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.ParamTypeDAO;
import com.orange.srs.refreport.model.EntityTypeAndSubtype;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.TO.ParamTypeKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeAliasProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class ParamTypeDelegate {

	@EJB
	private ParamTypeDAO paramTypeDao;

	@EJB
	private EntityTypeAndSubtypeDelegate entityTypeAndSubtypeDelegate;

	public static ParamTypeKeyTO getParamTypeKey(ParamType paramType) {
		return new ParamTypeKeyTO(paramType.getAlias());
	}

	public static ParamTypeKeyTO getParamTypeProvisioningTOKey(ParamTypeProvisioningTO paramTypeProvisioningTO) {
		return new ParamTypeKeyTO(paramTypeProvisioningTO.alias);
	}

	public static ParamTypeKeyTO getParamTypeAliasProvisioningTOKey(
			ParamTypeAliasProvisioningTO paramTypeAliasProvisioningTO) {
		return new ParamTypeKeyTO(paramTypeAliasProvisioningTO.alias);
	}

	public ParamType getParamTypeByKey(String paramTypeAlias) throws BusinessException {
		List<ParamType> listParamType = paramTypeDao.findBy(ParamType.FIELD_ALIAS, paramTypeAlias);
		if (listParamType.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": ParamType with key [alias="
					+ paramTypeAlias + "]");
		}
		return listParamType.get(0);
	}

	public ParamType createParamType(ParamTypeProvisioningTO paramTypeProvisioningTO) throws BusinessException {
		ParamType paramType = new ParamType();
		paramType.setAlias(paramTypeProvisioningTO.alias);
		EntityTypeAndSubtype entityTypeAndSubtype = entityTypeAndSubtypeDelegate.getEntityTypeAndSubtypeByKey(
				paramTypeProvisioningTO.entityType, paramTypeProvisioningTO.entitySubtype);
		paramType.setEntityTypeAndSubtype(entityTypeAndSubtype);
		EntityTypeAndSubtype parentTypeAndSubtype = entityTypeAndSubtypeDelegate.getEntityTypeAndSubtypeByKey(
				paramTypeProvisioningTO.parentType, paramTypeProvisioningTO.parentSubtype);
		paramType.setParentTypeAndSubtype(parentTypeAndSubtype);
		paramTypeDao.persistAndFlush(paramType);
		return paramType;
	}

	public boolean updateParamTypeIfNecessary(ParamType paramType, ParamTypeProvisioningTO paramTypeProvisioningTO)
			throws BusinessException {
		boolean updated = false;
		if (!paramTypeProvisioningTO.entityType.equals(paramType.getEntityType())
				|| !paramTypeProvisioningTO.entitySubtype.equals(paramType.getEntitySubtype())
				|| !paramTypeProvisioningTO.parentType.equals(paramType.getParentType())
				|| !paramTypeProvisioningTO.parentSubtype.equals(paramType.getParentSubtype())) {
			EntityTypeAndSubtype entityTypeAndSubtype = entityTypeAndSubtypeDelegate.getEntityTypeAndSubtypeByKey(
					paramTypeProvisioningTO.entityType, paramTypeProvisioningTO.entitySubtype);
			paramType.setEntityTypeAndSubtype(entityTypeAndSubtype);
			EntityTypeAndSubtype parentTypeAndSubtype = entityTypeAndSubtypeDelegate.getEntityTypeAndSubtypeByKey(
					paramTypeProvisioningTO.parentType, paramTypeProvisioningTO.parentSubtype);
			paramType.setParentTypeAndSubtype(parentTypeAndSubtype);
			paramTypeDao.persistAndFlush(paramType);
			updated = true;
		}
		return updated;
	}

	public void removeParamType(ParamType paramType) {
		paramTypeDao.remove(paramType);
	}

	public List<ParamType> getAllParamTypeSorted() {
		List<ParamType> paramTypeList = paramTypeDao.findAll();
		sortParamType(paramTypeList);
		return paramTypeList;
	}

	public ParamTypeListProvisioningTO getParamTypeListProvisioningTOSorted() {
		ParamTypeListProvisioningTO paramTypeListProvisioningTO = new ParamTypeListProvisioningTO();
		paramTypeListProvisioningTO.paramTypeProvisioningTOs = paramTypeDao.findAllParamTypeProvisioningTO();
		sortParamTypeProvisioningTO(paramTypeListProvisioningTO.paramTypeProvisioningTOs);
		return paramTypeListProvisioningTO;
	}

	public List<ParamType> getAllParamTypeSortedForReportConfig(ReportConfig reportConfig) {
		List<ParamType> paramTypeList = new ArrayList<>(reportConfig.getParamTypes());
		sortParamType(paramTypeList);
		return paramTypeList;
	}

	public static void sortParamType(List<ParamType> paramTypes) {
		Collections.sort(paramTypes, new ParamTypeComparator());
	}

	public static void sortParamTypeProvisioningTO(List<ParamTypeProvisioningTO> paramTypeProvisioningTOs) {
		Collections.sort(paramTypeProvisioningTOs, new ParamTypeProvisioningTOComparator());
	}

	public static void sortParamTypeAliasProvisioningTO(
			List<ParamTypeAliasProvisioningTO> paramTypeAliasProvisioningTOs) {
		Collections.sort(paramTypeAliasProvisioningTOs, new ParamTypeAliasProvisioningTOComparator());
	}

	private static class ParamTypeComparator implements Comparator<ParamType> {
		@Override
		public int compare(ParamType firstObj, ParamType secondObj) {
			return getParamTypeKey(firstObj).compareTo(getParamTypeKey(secondObj));
		}
	}

	private static class ParamTypeProvisioningTOComparator implements Comparator<ParamTypeProvisioningTO> {
		@Override
		public int compare(ParamTypeProvisioningTO firstObj, ParamTypeProvisioningTO secondObj) {
			return getParamTypeProvisioningTOKey(firstObj).compareTo(getParamTypeProvisioningTOKey(secondObj));
		}
	}

	private static class ParamTypeAliasProvisioningTOComparator implements Comparator<ParamTypeAliasProvisioningTO> {

		@Override
		public int compare(ParamTypeAliasProvisioningTO firstObj, ParamTypeAliasProvisioningTO secondObj) {
			return getParamTypeAliasProvisioningTOKey(firstObj)
					.compareTo(getParamTypeAliasProvisioningTOKey(secondObj));
		}
	}
}
