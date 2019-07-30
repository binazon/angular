package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.ExternalIndicatorDAO;
import com.orange.srs.refreport.model.ExternalIndicator;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.TO.ExternalIndicatorKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ExternalIndicatorListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ExternalIndicatorProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.enums.ComputeScopeEnum;

@Stateless
public class ExternalIndicatorDelegate {

	@EJB
	private ExternalIndicatorDAO externalIndicatorDao;

	@EJB
	private IndicatorDelegate indicatorDelegate;

	@EJB
	private OfferOptionDelegate offerOptionDelegate;

	@EJB
	private ParamTypeDelegate paramTypeDelegate;

	public static ExternalIndicatorKeyTO getExternalIndicatorKey(ExternalIndicator externalIndicator) {
		return new ExternalIndicatorKeyTO(externalIndicator.getLabel());
	}

	public static ExternalIndicatorKeyTO getExternalIndicatorProvisioningTOKey(
			ExternalIndicatorProvisioningTO externalIndicatorProvisioningTO) {
		return new ExternalIndicatorKeyTO(externalIndicatorProvisioningTO.label);
	}

	public ExternalIndicator getExternalIndicatorByKey(String externalIndicatorLabel) throws BusinessException {
		List<ExternalIndicator> listExternalIndicator = externalIndicatorDao.findBy(ExternalIndicator.FIELD_LABEL,
				externalIndicatorLabel);
		if (listExternalIndicator.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION
					+ ": ExternalIndicator with key [label=" + externalIndicatorLabel + "]");
		}
		return listExternalIndicator.get(0);
	}

	public ExternalIndicator createExternalIndicator(ExternalIndicatorProvisioningTO externalIndicatorProvisioningTO)
			throws BusinessException {
		ExternalIndicator externalIndicator = new ExternalIndicator();
		externalIndicator.setLabel(externalIndicatorProvisioningTO.label);
		externalIndicator.setComputeScope(ComputeScopeEnum.valueOf(externalIndicatorProvisioningTO.computeScope));
		Indicator indicator = indicatorDelegate.getIndicatorByKey(externalIndicatorProvisioningTO.indicatorId);
		externalIndicator.setIndicator(indicator);
		OfferOption offerOption = offerOptionDelegate
				.getOfferOptionByKey(externalIndicatorProvisioningTO.offerOptionAlias);
		externalIndicator.setOfferOption(offerOption);
		ParamType paramType = paramTypeDelegate.getParamTypeByKey(externalIndicatorProvisioningTO.paramTypeAlias);
		externalIndicator.setParamType(paramType);
		externalIndicatorDao.persistAndFlush(externalIndicator);
		return externalIndicator;
	}

	public boolean updateExternalIndicatorIfNecessary(ExternalIndicator externalIndicator,
			ExternalIndicatorProvisioningTO externalIndicatorProvisioningTO) throws BusinessException {

		boolean updated = false;

		ComputeScopeEnum computeScopeEnumTO = ComputeScopeEnum.valueOf(externalIndicatorProvisioningTO.computeScope);
		if (computeScopeEnumTO != externalIndicator.getComputeScope()) {
			externalIndicator.setComputeScope(computeScopeEnumTO);
		}

		String currentIndicatorId = externalIndicator.getIndicator().getIndicatorId();
		if (!externalIndicatorProvisioningTO.indicatorId.equals(currentIndicatorId)) {
			Indicator indicator = indicatorDelegate.getIndicatorByKey(externalIndicatorProvisioningTO.indicatorId);
			externalIndicator.setIndicator(indicator);
			updated = true;
		}

		String currentOfferOptionAlias = externalIndicator.getOfferOption().getAlias();
		if (!externalIndicatorProvisioningTO.offerOptionAlias.equals(currentOfferOptionAlias)) {
			OfferOption offerOption = offerOptionDelegate
					.getOfferOptionByKey(externalIndicatorProvisioningTO.offerOptionAlias);
			externalIndicator.setOfferOption(offerOption);
			updated = true;
		}

		String currentParamTypeAlias = externalIndicator.getParamType().getAlias();
		if (!externalIndicatorProvisioningTO.paramTypeAlias.equals(currentParamTypeAlias)) {
			ParamType paramType = paramTypeDelegate.getParamTypeByKey(externalIndicatorProvisioningTO.paramTypeAlias);
			externalIndicator.setParamType(paramType);
			updated = true;
		}

		if (updated) {
			externalIndicatorDao.persistAndFlush(externalIndicator);
		}
		return updated;
	}

	public void removeExternalIndicator(ExternalIndicator externalIndicator) {
		externalIndicatorDao.remove(externalIndicator);
	}

	public List<ExternalIndicator> getAllExternalIndicatorSorted() {
		List<ExternalIndicator> externalIndicatorList = externalIndicatorDao.findAll();
		sortExternalIndicator(externalIndicatorList);
		return externalIndicatorList;
	}

	public ExternalIndicatorListProvisioningTO getExternalIndicatorListProvisioningTOSorted() {
		ExternalIndicatorListProvisioningTO externalIndicatorListProvisioningTO = new ExternalIndicatorListProvisioningTO();
		externalIndicatorListProvisioningTO.externalIndicatorProvisioningTOs = externalIndicatorDao
				.findAllExternalIndicatorProvisioningTO();
		sortExternalIndicatorProvisioningTO(externalIndicatorListProvisioningTO.externalIndicatorProvisioningTOs);
		return externalIndicatorListProvisioningTO;
	}

	public static void sortExternalIndicator(List<ExternalIndicator> externalIndicators) {
		Collections.sort(externalIndicators, new ExternalIndicatorComparator());
	}

	public static void sortExternalIndicatorProvisioningTO(
			List<ExternalIndicatorProvisioningTO> externalIndicatorProvisioningTOs) {
		Collections.sort(externalIndicatorProvisioningTOs, new ExternalIndicatorProvisioningTOComparator());
	}

	private static class ExternalIndicatorComparator implements Comparator<ExternalIndicator> {
		@Override
		public int compare(ExternalIndicator firstObj, ExternalIndicator secondObj) {
			return getExternalIndicatorKey(firstObj).compareTo(getExternalIndicatorKey(secondObj));
		}
	}

	private static class ExternalIndicatorProvisioningTOComparator
			implements Comparator<ExternalIndicatorProvisioningTO> {
		@Override
		public int compare(ExternalIndicatorProvisioningTO firstObj, ExternalIndicatorProvisioningTO secondObj) {
			return getExternalIndicatorProvisioningTOKey(firstObj)
					.compareTo(getExternalIndicatorProvisioningTOKey(secondObj));
		}
	}
}
