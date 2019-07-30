package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.IndicatorDAO;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.TO.IndicatorKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorIdProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class IndicatorDelegate {

	@EJB
	private IndicatorDAO indicatorDao;

	public static IndicatorKeyTO getIndicatorKey(Indicator indicator) {
		return new IndicatorKeyTO(indicator.getIndicatorId());
	}

	public static IndicatorKeyTO getIndicatorProvisioningTOKey(IndicatorProvisioningTO indicatorProvisioningTO) {
		return new IndicatorKeyTO(indicatorProvisioningTO.id);
	}

	public static IndicatorKeyTO getIndicatorIdProvisioningTOKey(IndicatorIdProvisioningTO indicatorIdProvisioningTO) {
		return new IndicatorKeyTO(indicatorIdProvisioningTO.id);
	}

	public Indicator getIndicatorByKey(String indicatorId) throws BusinessException {
		List<Indicator> listIndicator = indicatorDao.findBy(Indicator.FIELD_INDICATORID, indicatorId);
		if (listIndicator.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION
					+ ": Indicator with key [indicatorId=" + indicatorId + "]");
		}
		return listIndicator.get(0);
	}

	public Indicator createIndicator(IndicatorProvisioningTO indicatorProvisioningTO) {
		Indicator indicator = new Indicator();
		indicator.setIndicatorId(indicatorProvisioningTO.id);
		indicator.setLabel(indicatorProvisioningTO.label);
		indicatorDao.persistAndFlush(indicator);
		return indicator;
	}

	public boolean updateIndicatorIfNecessary(Indicator indicator, IndicatorProvisioningTO indicatorProvisioningTO) {
		boolean updated = false;
		if (!indicatorProvisioningTO.label.equals(indicator.getLabel())) {
			indicator.setLabel(indicatorProvisioningTO.label);
			indicatorDao.persistAndFlush(indicator);
			updated = true;
		}
		return updated;
	}

	public void removeIndicator(Indicator indicator) {
		indicatorDao.remove(indicator);
	}

	public List<Indicator> getAllIndicatorSorted() {
		List<Indicator> indicatorList = indicatorDao.findAll();
		sortIndicator(indicatorList);
		return indicatorList;
	}

	public IndicatorListProvisioningTO getIndicatorListProvisioningTOSorted() {
		IndicatorListProvisioningTO indicatorListProvisioningTO = new IndicatorListProvisioningTO();
		indicatorListProvisioningTO.indicatorProvisioningTOs = indicatorDao.findAllIndicatorProvisioningTO();
		sortIndicatorProvisioningTO(indicatorListProvisioningTO.indicatorProvisioningTOs);
		return indicatorListProvisioningTO;
	}

	public static List<Indicator> getAllIndicatorSortedForReportConfig(ReportConfig reportConfig) {
		List<Indicator> indicatorList = reportConfig.getIndicators();
		IndicatorDelegate.sortIndicator(indicatorList);
		return indicatorList;
	}

	public static void sortIndicator(List<Indicator> indicators) {
		Collections.sort(indicators, new IndicatorComparator());
	}

	public static void sortIndicatorProvisioningTO(List<IndicatorProvisioningTO> indicatorProvisioningTOs) {
		Collections.sort(indicatorProvisioningTOs, new IndicatorProvisioningTOComparator());
	}

	public static void sortIndicatorIdProvisioningTO(List<IndicatorIdProvisioningTO> indicatorIdProvisioningTOs) {
		Collections.sort(indicatorIdProvisioningTOs, new IndicatorIdProvisioningTOComparator());
	}

	private static class IndicatorComparator implements Comparator<Indicator> {
		@Override
		public int compare(Indicator firstObj, Indicator secondObj) {
			return getIndicatorKey(firstObj).compareTo(getIndicatorKey(secondObj));
		}
	}

	private static class IndicatorProvisioningTOComparator implements Comparator<IndicatorProvisioningTO> {
		@Override
		public int compare(IndicatorProvisioningTO firstObj, IndicatorProvisioningTO secondObj) {
			return getIndicatorProvisioningTOKey(firstObj).compareTo(getIndicatorProvisioningTOKey(secondObj));
		}
	}

	private static class IndicatorIdProvisioningTOComparator implements Comparator<IndicatorIdProvisioningTO> {
		@Override
		public int compare(IndicatorIdProvisioningTO firstObj, IndicatorIdProvisioningTO secondObj) {
			return getIndicatorIdProvisioningTOKey(firstObj).compareTo(getIndicatorIdProvisioningTOKey(secondObj));
		}
	}
}
