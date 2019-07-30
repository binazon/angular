package com.orange.srs.refreport.business.delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.consumer.dao.FilterDAO;
import com.orange.srs.refreport.consumer.dao.FilterToOfferOptionDAO;
import com.orange.srs.refreport.consumer.dao.OfferDAO;
import com.orange.srs.refreport.consumer.dao.OfferOptionDAO;
import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.Offer;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.TO.OfferOptionKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferOptionLinkProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferOptionProvisioningTO;
import com.orange.srs.refreport.model.external.OfferOptionTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;
import com.orange.srs.statcommon.model.enums.OriginEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.ListConverter;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class OfferOptionDelegate {

	private static final Logger LOGGER = Logger.getLogger(OfferOptionDelegate.class);

	@EJB
	private OfferOptionDAO offerOptionDao;

	@EJB
	private FilterDAO filterDAO;

	@EJB
	private OfferDAO offerDAO;

	@EJB
	private FilterToOfferOptionDAO filterToOfferOptionDAO;

	public static OfferOptionKeyTO getOfferOptionKey(OfferOption offerOption) {
		return new OfferOptionKeyTO(offerOption.getAlias());
	}

	public static OfferOptionKeyTO getOfferOptionProvisioningTOKey(String offerAlias,
			OfferOptionProvisioningTO offerOptionProvisioningTO) {
		return new OfferOptionKeyTO(
				offerAlias + OfferOption.OFFER_OPTION_ALIAS_SEPARATOR + offerOptionProvisioningTO.cpltAlias);
	}

	public static OfferOptionKeyTO getOfferOptionLinkProvisioningTOKey(
			OfferOptionLinkProvisioningTO offerOptionLinkProvisioningTO) {
		return new OfferOptionKeyTO(offerOptionLinkProvisioningTO.alias);
	}

	public OfferOption getOfferOptionByKey(String offerOptionAlias) throws BusinessException {
		List<OfferOption> listOfferOption = offerOptionDao.findBy(OfferOption.FIELD_ALIAS, offerOptionAlias);
		if (listOfferOption.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION
					+ ": OfferOption with key [offerOptionAlias=" + offerOptionAlias + "]");
		}
		return listOfferOption.get(0);
	}

	public void createOfferOption(Offer offer, OfferOptionProvisioningTO offerOptionProvisioningTO) {
		OfferOption offerOption = new OfferOption();
		offerOption.setLabel(offerOptionProvisioningTO.label);
		offerOption.setAlias(
				offer.getAlias() + OfferOption.OFFER_OPTION_ALIAS_SEPARATOR + offerOptionProvisioningTO.cpltAlias);
		offerOption.setType(OfferOptionTypeEnum.valueOf(offerOptionProvisioningTO.type));
		offerOption.setRelatedOffer(offer);
		offerOptionDao.persistAndFlush(offerOption);
	}

	public boolean updateOfferOptionIfNecessary(OfferOption offerOption,
			OfferOptionProvisioningTO offerOptionProvisioningTO) {
		boolean updated = false;
		OfferOptionTypeEnum offerOptionTypeEnumTO = OfferOptionTypeEnum.valueOf(offerOptionProvisioningTO.type);
		if (!offerOptionProvisioningTO.label.equals(offerOption.getLabel())
				|| !offerOptionTypeEnumTO.equals(offerOption.getType())) {
			offerOption.setLabel(offerOptionProvisioningTO.label);
			offerOption.setType(offerOptionTypeEnumTO);
			offerOptionDao.persistAndFlush(offerOption);
			updated = true;
		}
		return updated;
	}

	public void removeOfferOption(OfferOption offerOption) {
		offerOptionDao.remove(offerOption);
	}

	public List<OfferOptionTO> getAllOfferOptionTO() {
		List<OfferOptionTO> offerOptionTOs = new ArrayList<>();
		for (OfferOption offerOption : offerOptionDao.findAll()) {
			OfferOptionTO offerOptionTO = new OfferOptionTO(offerOption.getAlias());
			offerOptionTO.setType(offerOption.getType());
			offerOptionTOs.add(offerOptionTO);
			for (Filter filter : offerOption.getFilters()) {
				offerOptionTO.addFilterId(filter.getFilterId());
			}
		}
		return offerOptionTOs;
	}

	public List<OfferOptionTO> getOfferOptionTOsByTypes(List<OfferOptionTypeEnum> types) {
		List<OfferOptionTO> offerOptionTOs = new ArrayList<>();
		for (OfferOption offerOption : offerOptionDao.findOfferOptionsByTypes(types)) {
			OfferOptionTO offerOptionTO = new OfferOptionTO(offerOption.getAlias());
			offerOptionTO.setType(offerOption.getType());
			offerOptionTOs.add(offerOptionTO);
			for (Filter filter : offerOption.getFilters()) {
				offerOptionTO.addFilterId(filter.getFilterId());
			}
		}
		return offerOptionTOs;
	}

	public List<OfferOptionTO> getOfferOptionTOsFiltered(List<OfferOptionTypeEnum> types, String reportingGroupRef,
			OriginEnum reportingGroupOrigin) {
		List<OfferOptionTO> offerOptionTOs = new ArrayList<>();
		for (OfferOption offerOption : offerOptionDao.findOfferOptionsFiltered(types, reportingGroupRef,
				reportingGroupOrigin)) {
			OfferOptionTO offerOptionTO = new OfferOptionTO(offerOption.getAlias());
			offerOptionTO.setType(offerOption.getType());
			offerOptionTOs.add(offerOptionTO);
			for (Filter filter : offerOption.getFilters()) {
				offerOptionTO.addFilterId(filter.getFilterId());
			}
		}
		return offerOptionTOs;
	}

	public Map<String, OfferOption> getAllOfferOptionByOptionAlias(SOAContext soaContext) {
		Map<String, OfferOption> optionByOptionAlias;
		try {
			ListConverter<String, OfferOption> converter = new ListConverter<>(offerOptionDao.findAll());
			optionByOptionAlias = converter.convertToMap(OfferOption.FIELD_ALIAS);
		} catch (Exception except) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Error getting offerOptions from database", except));
			optionByOptionAlias = new HashMap<>();
		}
		return optionByOptionAlias;
	}

	public List<OfferOption> getAllOfferOptionSortedForOffer(Offer offer) {
		List<OfferOption> offerOptionList = offerOptionDao.findBy(OfferOption.FIELD_RELATED_0FFER, offer);
		Collections.sort(offerOptionList, new OfferOptionComparator());
		return offerOptionList;
	}

	public List<OfferOption> getAllOfferOptionSortedForFilter(Filter filter) {
		List<OfferOption> offerOptionList = new ArrayList<>(filter.getOfferOptions());
		sortOfferOption(offerOptionList);
		return offerOptionList;
	}

	public static void sortOfferOption(List<OfferOption> offerOptions) {
		Collections.sort(offerOptions, new OfferOptionComparator());
	}

	public static void sortOfferOptionProvisioningTO(String offerAlias,
			List<OfferOptionProvisioningTO> offerOptionProvisioningTOs) {
		Collections.sort(offerOptionProvisioningTOs, new OfferOptionProvisioningTOComparator(offerAlias));
	}

	public static void sortOfferOptionLinkProvisioningTO(
			List<OfferOptionLinkProvisioningTO> offerOptionLinkProvisioningTOs) {
		Collections.sort(offerOptionLinkProvisioningTOs, new OfferOptionLinkProvisioningTOComparator());
	}

	private static class OfferOptionComparator implements Comparator<OfferOption> {
		@Override
		public int compare(OfferOption firstObj, OfferOption secondObj) {
			return getOfferOptionKey(firstObj).compareTo(getOfferOptionKey(secondObj));
		}
	}

	private static class OfferOptionProvisioningTOComparator implements Comparator<OfferOptionProvisioningTO> {

		private String offerAlias;

		public OfferOptionProvisioningTOComparator(String offerAlias) {
			this.offerAlias = offerAlias;
		}

		@Override
		public int compare(OfferOptionProvisioningTO firstObj, OfferOptionProvisioningTO secondObj) {
			return getOfferOptionProvisioningTOKey(offerAlias, firstObj)
					.compareTo(getOfferOptionProvisioningTOKey(offerAlias, secondObj));
		}
	}

	private static class OfferOptionLinkProvisioningTOComparator implements Comparator<OfferOptionLinkProvisioningTO> {

		@Override
		public int compare(OfferOptionLinkProvisioningTO firstObj, OfferOptionLinkProvisioningTO secondObj) {
			return getOfferOptionLinkProvisioningTOKey(firstObj)
					.compareTo(getOfferOptionLinkProvisioningTOKey(secondObj));
		}
	}
}
