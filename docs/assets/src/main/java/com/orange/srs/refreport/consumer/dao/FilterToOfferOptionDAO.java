package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.FilterToOfferOption;
import com.orange.srs.refreport.model.FilterToOfferOptionId;
import com.orange.srs.refreport.model.TO.FilterToOfferOptionTO;

public interface FilterToOfferOptionDAO extends Dao<FilterToOfferOption, FilterToOfferOptionId> {

	public List<FilterToOfferOptionTO> findAllFilterToOfferOptionTOs();

}
