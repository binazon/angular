package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.HyperlinkDAO;
import com.orange.srs.refreport.model.Hyperlink;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.TO.HyperlinkKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.HyperlinkListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.HyperlinkProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class HyperlinkDelegate {

	@EJB
	private HyperlinkDAO hyperlinkDao;

	@EJB
	private IndicatorDelegate indicatorDelegate;

	@EJB
	private OfferOptionDelegate offerOptionDelegate;

	@EJB
	private ParamTypeDelegate paramTypeDelegate;

	public static HyperlinkKeyTO getHyperlinkKey(Hyperlink hyperlink) {
		return new HyperlinkKeyTO(hyperlink.getLabel());
	}

	public static HyperlinkKeyTO getHyperlinkProvisioningTOKey(HyperlinkProvisioningTO hyperlinkProvisioningTO) {
		return new HyperlinkKeyTO(hyperlinkProvisioningTO.label);
	}

	public Hyperlink getHyperlinkByKey(String hyperlinkLabel) throws BusinessException {
		List<Hyperlink> listHyperlink = hyperlinkDao.findBy(Hyperlink.FIELD_LABEL, hyperlinkLabel);
		if (listHyperlink.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": Hyperlink with key [label="
					+ hyperlinkLabel + "]");
		}
		return listHyperlink.get(0);
	}

	public Hyperlink createHyperlink(HyperlinkProvisioningTO hyperlinkProvisioningTO) throws BusinessException {
		Hyperlink hyperlink = new Hyperlink();
		hyperlink.setLabel(hyperlinkProvisioningTO.label);
		Indicator indicator = indicatorDelegate.getIndicatorByKey(hyperlinkProvisioningTO.indicatorId);
		hyperlink.setIndicator(indicator);
		OfferOption offerOption = offerOptionDelegate.getOfferOptionByKey(hyperlinkProvisioningTO.offerOptionAlias);
		hyperlink.setOfferOption(offerOption);
		ParamType paramType = paramTypeDelegate.getParamTypeByKey(hyperlinkProvisioningTO.paramTypeAlias);
		hyperlink.setParamType(paramType);
		if (hyperlinkProvisioningTO.additionalParamTypeAlias != null) {
			ParamType additionalParamType = paramTypeDelegate
					.getParamTypeByKey(hyperlinkProvisioningTO.additionalParamTypeAlias);
			hyperlink.setAdditionalParamType(additionalParamType);
		}

		hyperlinkDao.persistAndFlush(hyperlink);
		return hyperlink;
	}

	public boolean updateHyperlinkIfNecessary(Hyperlink hyperlink, HyperlinkProvisioningTO hyperlinkProvisioningTO)
			throws BusinessException {

		boolean updated = false;

		String currentIndicatorId = hyperlink.getIndicator().getIndicatorId();
		if (!hyperlinkProvisioningTO.indicatorId.equals(currentIndicatorId)) {
			Indicator indicator = indicatorDelegate.getIndicatorByKey(hyperlinkProvisioningTO.indicatorId);
			hyperlink.setIndicator(indicator);
			updated = true;
		}

		String currentOfferOptionAlias = hyperlink.getOfferOption().getAlias();
		if (!hyperlinkProvisioningTO.offerOptionAlias.equals(currentOfferOptionAlias)) {
			OfferOption offerOption = offerOptionDelegate.getOfferOptionByKey(hyperlinkProvisioningTO.offerOptionAlias);
			hyperlink.setOfferOption(offerOption);
			updated = true;
		}

		String currentParamTypeAlias = hyperlink.getParamType().getAlias();
		if (!hyperlinkProvisioningTO.paramTypeAlias.equals(currentParamTypeAlias)) {
			ParamType paramType = paramTypeDelegate.getParamTypeByKey(hyperlinkProvisioningTO.paramTypeAlias);
			hyperlink.setParamType(paramType);
			updated = true;
		}

		String currentAdditionalParamTypeAlias = hyperlink.getAdditionalParamType() == null ? null
				: hyperlink.getAdditionalParamType().getAlias();
		if ((hyperlinkProvisioningTO.additionalParamTypeAlias != null)
				&& (!hyperlinkProvisioningTO.additionalParamTypeAlias.equals(currentAdditionalParamTypeAlias))) {
			ParamType additionalParamType = paramTypeDelegate
					.getParamTypeByKey(hyperlinkProvisioningTO.additionalParamTypeAlias);
			hyperlink.setAdditionalParamType(additionalParamType);
			updated = true;
		}

		if (updated) {
			hyperlinkDao.persistAndFlush(hyperlink);
		}
		return updated;
	}

	public void removeHyperlink(Hyperlink hyperlink) {
		hyperlinkDao.remove(hyperlink);
	}

	public List<Hyperlink> getAllHyperlinkSorted() {
		List<Hyperlink> hyperlinkList = hyperlinkDao.findAll();
		sortHyperlink(hyperlinkList);
		return hyperlinkList;
	}

	public HyperlinkListProvisioningTO getHyperlinkListProvisioningTOSorted() {
		HyperlinkListProvisioningTO hyperlinkListProvisioningTO = new HyperlinkListProvisioningTO();
		hyperlinkListProvisioningTO.hyperlinkProvisioningTOs = hyperlinkDao.findAllHyperlinkProvisioningTO();
		sortHyperlinkProvisioningTO(hyperlinkListProvisioningTO.hyperlinkProvisioningTOs);
		return hyperlinkListProvisioningTO;
	}

	public static void sortHyperlink(List<Hyperlink> hyperlinks) {
		Collections.sort(hyperlinks, new HyperlinkComparator());
	}

	public static void sortHyperlinkProvisioningTO(List<HyperlinkProvisioningTO> hyperlinkProvisioningTOs) {
		Collections.sort(hyperlinkProvisioningTOs, new HyperlinkProvisioningTOComparator());
	}

	private static class HyperlinkComparator implements Comparator<Hyperlink> {
		@Override
		public int compare(Hyperlink firstObj, Hyperlink secondObj) {
			return getHyperlinkKey(firstObj).compareTo(getHyperlinkKey(secondObj));
		}
	}

	private static class HyperlinkProvisioningTOComparator implements Comparator<HyperlinkProvisioningTO> {
		@Override
		public int compare(HyperlinkProvisioningTO firstObj, HyperlinkProvisioningTO secondObj) {
			return getHyperlinkProvisioningTOKey(firstObj).compareTo(getHyperlinkProvisioningTOKey(secondObj));
		}
	}
}
