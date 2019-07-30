package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.CriteriaDAO;
import com.orange.srs.refreport.model.Criteria;
import com.orange.srs.refreport.model.TO.CriteriaKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.CriteriaListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.CriteriaProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class CriteriaDelegate {

	@EJB
	private CriteriaDAO criteriaDao;

	private static final String DATA_LOCATION_DEFAULT_TYPE = "default";
	private static final String DATA_LOCATION_DEFAULT_VALUE = "DI";
	private static final String DATA_LOCATION_REPORTINGGROUP_TYPE = "reportingGroup";
	private static final String DATA_LOCATION_REPORTINGGROUP_VALUE = "REPORTINGGROUPREF";

	public static final String DATA_LOCATION_CUSTOMERCODE_TYPE = "default";
	public static final String DATA_LOCATION_CUSTOMERCODE_VALUE = "customerCode";

	public static Criteria getDefaultCriteria() {
		Criteria criteria = new Criteria();
		criteria.setCriteriaType(DATA_LOCATION_DEFAULT_TYPE);
		criteria.setCriteriaValue(DATA_LOCATION_DEFAULT_VALUE);
		return criteria;
	}

	public static Criteria getReportingGroupCriteria() {
		Criteria criteria = new Criteria();
		criteria.setCriteriaType(DATA_LOCATION_REPORTINGGROUP_TYPE);
		criteria.setCriteriaValue(DATA_LOCATION_REPORTINGGROUP_VALUE);
		return criteria;
	}

	public static CriteriaKeyTO getCriteriaKey(Criteria criteria) {
		return new CriteriaKeyTO(criteria.getCriteriaType(), criteria.getCriteriaValue());
	}

	public static CriteriaKeyTO getCriteriaProvisioningTOKey(CriteriaProvisioningTO criteriaProvisioningTO) {
		return new CriteriaKeyTO(criteriaProvisioningTO.type, criteriaProvisioningTO.value);
	}

	public void createCriteria(CriteriaProvisioningTO criteriaProvisioningTO) {
		Criteria criteria = new Criteria();
		criteria.setCriteriaType(criteriaProvisioningTO.type);
		criteria.setCriteriaValue(criteriaProvisioningTO.value);
		criteriaDao.persistAndFlush(criteria);
	}

	public Criteria getCriteriaByKey(String criteriaType, String criteriaValue) throws BusinessException {
		String[] attributes = { Criteria.FIELD_CRITERIA_TYPE, Criteria.FIELD_CRITERIA_VALUE };
		Object[] values = { criteriaType, criteriaValue };
		List<Criteria> listCriteria = criteriaDao.findByMultipleCriteria(attributes, values);
		if (listCriteria.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION
					+ ": Criteria with key [criteriaType=" + criteriaType + ", criteriaValue=" + criteriaValue + "]");
		}
		return listCriteria.get(0);
	}

	public List<Criteria> getAllCriteriaSorted() {
		List<Criteria> criteriaList = criteriaDao.findAll();
		Collections.sort(criteriaList, new CriteriaComparator());
		return criteriaList;
	}

	public CriteriaListProvisioningTO getCriteriaListProvisioningTOSorted() {
		CriteriaListProvisioningTO criteriaListProvisioningTO = new CriteriaListProvisioningTO();
		criteriaListProvisioningTO.criteriaProvisioningTOs = criteriaDao.findAllCriteriaProvisioningTO();
		sortCriteriaProvisioningTO(criteriaListProvisioningTO.criteriaProvisioningTOs);
		return criteriaListProvisioningTO;
	}

	public static void sortCriteriaProvisioningTO(List<CriteriaProvisioningTO> criteriaProvisioningTOs) {
		Collections.sort(criteriaProvisioningTOs, new CriteriaProvisioningTOComparator());
	}

	private static class CriteriaComparator implements Comparator<Criteria> {
		@Override
		public int compare(Criteria firstObj, Criteria secondObj) {
			return getCriteriaKey(firstObj).compareTo(getCriteriaKey(secondObj));
		}
	}

	private static class CriteriaProvisioningTOComparator implements Comparator<CriteriaProvisioningTO> {
		@Override
		public int compare(CriteriaProvisioningTO firstObj, CriteriaProvisioningTO secondObj) {
			return getCriteriaProvisioningTOKey(firstObj).compareTo(getCriteriaProvisioningTOKey(secondObj));
		}
	}
}
