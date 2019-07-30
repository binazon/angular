package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.OfferDAO;
import com.orange.srs.refreport.model.Offer;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.TO.OfferKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferAndOptionListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferAndOptionProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferOptionProvisioningTO;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;

@Stateless
public class OfferDelegate {

	@EJB
	private OfferDAO offerDao;

	public static OfferKeyTO getOfferKey(Offer offer) {
		return new OfferKeyTO(offer.getAlias());
	}

	public static OfferKeyTO getOfferAndOptionProvisioningTOKey(
			OfferAndOptionProvisioningTO offerAndOptionProvisioningTO) {
		return new OfferKeyTO(offerAndOptionProvisioningTO.alias);
	}

	public Offer createOffer(OfferAndOptionProvisioningTO offerAndOptionProvisioningTO) {
		Offer offer = new Offer();
		offer.setName(offerAndOptionProvisioningTO.commercialName);
		offer.setAlias(offerAndOptionProvisioningTO.alias);
		offerDao.persistAndFlush(offer);
		return offer;
	}

	public boolean updateOfferIfNecessary(Offer offer, OfferAndOptionProvisioningTO offerAndOptionProvisioningTO) {
		boolean updated = false;
		if (!offerAndOptionProvisioningTO.commercialName.equals(offer.getName())) {
			offer.setName(offerAndOptionProvisioningTO.commercialName);
			offerDao.persistAndFlush(offer);
			updated = true;
		}
		return updated;
	}

	public void removeOffer(Offer offer) {
		offerDao.remove(offer);
	}

	public List<Offer> getAllOfferSorted() {
		List<Offer> offerList = offerDao.findAll();
		Collections.sort(offerList, new OfferComparator());
		return offerList;
	}

	public Set<String> getAllOfferAliases() {
		return new HashSet<>(offerDao.findAllOfferAliases());
	}

	public OfferAndOptionListProvisioningTO getOfferAndOptionListProvisioningTOSorted() {

		OfferAndOptionListProvisioningTO offerAndOptionListProvisioningTO = new OfferAndOptionListProvisioningTO();
		long previousOfferPk = -1;
		OfferAndOptionProvisioningTO currentOfferAndOptionProvisioningTO = null;

		for (Object[] result : offerDao.findAllOffersAndOptionInfo()) {
			Offer offer = (Offer) result[0];
			String offerOptionAlias = (String) result[1];
			String offerOptionLabel = (String) result[2];
			OfferOptionTypeEnum offerOptionType = (OfferOptionTypeEnum) result[3];
			if (previousOfferPk != offer.getPk()) {
				if (currentOfferAndOptionProvisioningTO != null) {
					OfferOptionDelegate.sortOfferOptionProvisioningTO(currentOfferAndOptionProvisioningTO.alias,
							currentOfferAndOptionProvisioningTO.offerOptionProvisioningTOs);
				}
				currentOfferAndOptionProvisioningTO = new OfferAndOptionProvisioningTO();
				currentOfferAndOptionProvisioningTO.alias = offer.getAlias();
				currentOfferAndOptionProvisioningTO.commercialName = offer.getName();
				offerAndOptionListProvisioningTO.offerAndOptionProvisioningTOs.add(currentOfferAndOptionProvisioningTO);
				previousOfferPk = offer.getPk();
			}
			if (offerOptionAlias != null) {
				OfferOptionProvisioningTO offerOptionProvisioningTO = new OfferOptionProvisioningTO();
				String startOfferOptionAlias = offer.getAlias() + OfferOption.OFFER_OPTION_ALIAS_SEPARATOR;
				offerOptionProvisioningTO.cpltAlias = offerOptionAlias.substring(startOfferOptionAlias.length());
				offerOptionProvisioningTO.label = offerOptionLabel;
				offerOptionProvisioningTO.type = offerOptionType.toString();
				currentOfferAndOptionProvisioningTO.offerOptionProvisioningTOs.add(offerOptionProvisioningTO);
			}
		}

		sortOfferAndOptionProvisioningTO(offerAndOptionListProvisioningTO.offerAndOptionProvisioningTOs);
		return offerAndOptionListProvisioningTO;
	}

	public static void sortOfferAndOptionProvisioningTO(
			List<OfferAndOptionProvisioningTO> offerAndOptionProvisioningTOs) {
		Collections.sort(offerAndOptionProvisioningTOs, new OfferAndOptionProvisioningTOComparator());
	}

	private static class OfferComparator implements Comparator<Offer> {
		@Override
		public int compare(Offer firstObj, Offer secondObj) {
			return getOfferKey(firstObj).compareTo(getOfferKey(secondObj));
		}
	}

	private static class OfferAndOptionProvisioningTOComparator implements Comparator<OfferAndOptionProvisioningTO> {
		@Override
		public int compare(OfferAndOptionProvisioningTO firstObj, OfferAndOptionProvisioningTO secondObj) {
			return getOfferAndOptionProvisioningTOKey(firstObj)
					.compareTo(getOfferAndOptionProvisioningTOKey(secondObj));
		}
	}

}
