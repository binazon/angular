package com.orange.srs.refreport.business.delegate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.consumer.dao.FilterConfigDAO;
import com.orange.srs.refreport.consumer.dao.FilterDAO;
import com.orange.srs.refreport.consumer.dao.FilterToOfferOptionDAO;
import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.TO.FilterKeyTO;
import com.orange.srs.refreport.model.TO.FilterToOfferOptionTO;
import com.orange.srs.refreport.model.TO.provisioning.FilterListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.FilterProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferOptionLinkProvisioningTO;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.enums.FilterTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.filter.FilterParameter;
import com.orange.srs.statcommon.model.parameter.filter.TimeSlotFilterParameter;
import com.orange.srs.statcommon.technical.ListConverter;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class FilterDelegate {

	private static final Logger LOGGER = Logger.getLogger(FilterDelegate.class);

	@EJB
	private FilterDAO filterDao;

	@EJB
	private FilterConfigDAO filterConfigDao;

	@EJB
	private FilterToOfferOptionDAO filterToOfferOptionDAO;

	public static FilterKeyTO getFilterKey(Filter filter) {
		return new FilterKeyTO(filter.getFilterId());
	}

	public static FilterKeyTO getFilterProvisioningTOKey(FilterProvisioningTO filterProvisioningTO) {
		return new FilterKeyTO(filterProvisioningTO.id);
	}

	public Filter getFilterByKey(String filterId) throws BusinessException {
		List<Filter> listFilter = filterDao.findBy(Filter.FIELD_FILTERID, filterId);
		if (listFilter.isEmpty()) {
			throw new BusinessException(
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": Filter with key [filterId=" + filterId + "]");
		}
		return listFilter.get(0);
	}

	public Filter createFilter(FilterProvisioningTO filterProvisioningTO, SOAContext soaContext) {
		Filter filter = new Filter();
		filter.setFilterId(filterProvisioningTO.id);
		filter.setUri(filterProvisioningTO.uri);
		filter.setName(filterProvisioningTO.name);
		filter.setType(FilterTypeEnum.fromValue(filterProvisioningTO.type));
		filter.setComments(filterProvisioningTO.comments);
		filter.setValue(filterProvisioningTO.value);
		filterDao.persistAndFlush(filter);
		exportFilter(filter, soaContext);
		return filter;
	}

	public boolean updateFilterIfNecessary(Filter filter, FilterProvisioningTO filterProvisioningTO,
			SOAContext soaContext) {
		boolean updated = false;

		FilterTypeEnum filterTypeEnumTO = FilterTypeEnum.fromValue(filterProvisioningTO.type);

		if (!filterProvisioningTO.uri.equals(filter.getUri()) || !filterProvisioningTO.name.equals(filter.getName())
				|| !filterTypeEnumTO.equals(filter.getType())
				|| !filterProvisioningTO.comments.equals(filter.getComments())
				|| !filterProvisioningTO.value.equals(filter.getValue())) {
			filter.setUri(filterProvisioningTO.uri);
			filter.setName(filterProvisioningTO.name);
			filter.setType(filterTypeEnumTO);
			filter.setComments(filterProvisioningTO.comments);
			filter.setValue(filterProvisioningTO.value);
			filterDao.persistAndFlush(filter);
			updated = true;
		}
		exportFilter(filter, soaContext);
		return updated;
	}

	public void removeFilter(Filter filter) {
		Path filterPath = Paths.get(Configuration.rootProperty, filter.getUri());
		try {
			Files.deleteIfExists(filterPath);
		} catch (IOException e) {
			LOGGER.warn("[removeFilter] The file " + filterPath.toString() + " can not be removed.");
		}
		filterDao.remove(filter);
	}

	public List<Filter> getAllFilterSorted() {
		List<Filter> filterList = filterDao.findAll();
		sortFilter(filterList);
		return filterList;
	}

	public Map<String, List<FilterToOfferOptionTO>> getFilterToOfferOptionTOByFilterId() {
		Map<String, List<FilterToOfferOptionTO>> toByFilterId = new HashMap<>();
		for (FilterToOfferOptionTO filterToOfferOptionTO : filterToOfferOptionDAO.findAllFilterToOfferOptionTOs()) {
			List<FilterToOfferOptionTO> toForCurrentFilterId = toByFilterId.get(filterToOfferOptionTO.filterId);
			if (toForCurrentFilterId == null) {
				toForCurrentFilterId = new ArrayList<>();
				toByFilterId.put(filterToOfferOptionTO.filterId, toForCurrentFilterId);
			}
			toForCurrentFilterId.add(filterToOfferOptionTO);
		}
		return toByFilterId;
	}

	public Map<String, Filter> getAllFilterByFilterId(SOAContext soaContext) {
		Map<String, Filter> filterByFilterId;
		try {
			ListConverter<String, Filter> converter = new ListConverter<>(filterDao.findAll());
			filterByFilterId = converter.convertToMap(Filter.FIELD_FILTERID);
		} catch (Exception except) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Error getting filter from database"));
			filterByFilterId = new HashMap<>();
		}
		return filterByFilterId;
	}

	public FilterListProvisioningTO getFilterListProvisioningTOSorted() {
		Map<String, List<FilterToOfferOptionTO>> filterToOfferOptionTOByFilterId = getFilterToOfferOptionTOByFilterId();
		List<FilterProvisioningTO> filterProvisioningTOs = filterDao.findAllFilterProvisioningTO();
		for (FilterProvisioningTO filterProvisioningTO : filterProvisioningTOs) {

			// Fill the existing offerOption for the filter
			List<FilterToOfferOptionTO> filterToOfferOptionTOForFilterId = filterToOfferOptionTOByFilterId
					.get(filterProvisioningTO.id);
			if (filterToOfferOptionTOForFilterId != null) {
				for (FilterToOfferOptionTO filterToOfferOptionTO : filterToOfferOptionTOForFilterId) {
					OfferOptionLinkProvisioningTO offerOptionAliasProvisioningTO = new OfferOptionLinkProvisioningTO();
					offerOptionAliasProvisioningTO.alias = filterToOfferOptionTO.offerOptionAlias;
					offerOptionAliasProvisioningTO.defaultForAllGroups = filterToOfferOptionTO.defaultForAllGroups;
					filterProvisioningTO.offerOptionAliasProvisioningTOs.add(offerOptionAliasProvisioningTO);
				}
				OfferOptionDelegate
						.sortOfferOptionLinkProvisioningTO(filterProvisioningTO.offerOptionAliasProvisioningTOs);
			}
		}

		FilterListProvisioningTO filterListProvisioningTO = new FilterListProvisioningTO();
		filterListProvisioningTO.filterProvisioningTOs = filterProvisioningTOs;
		sortFilterProvisioningTO(filterListProvisioningTO.filterProvisioningTOs);
		return filterListProvisioningTO;
	}

	public static void sortFilter(List<Filter> filters) {
		Collections.sort(filters, new FilterComparator());
	}

	public static void sortFilterProvisioningTO(List<FilterProvisioningTO> filterProvisioningTOs) {
		Collections.sort(filterProvisioningTOs, new FilterProvisioningTOComparator());
	}

	private static class FilterComparator implements Comparator<Filter> {
		@Override
		public int compare(Filter firstObj, Filter secondObj) {
			return getFilterKey(firstObj).compareTo(getFilterKey(secondObj));
		}
	}

	private static class FilterProvisioningTOComparator implements Comparator<FilterProvisioningTO> {
		@Override
		public int compare(FilterProvisioningTO firstObj, FilterProvisioningTO secondObj) {
			return getFilterProvisioningTOKey(firstObj).compareTo(getFilterProvisioningTOKey(secondObj));
		}
	}

	public void exportFilters(SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[exportFilters] Start"));
		List<Filter> filters = filterDao.findAll();

		if (filters == null || filters.isEmpty()) {
			LOGGER.warn("[exportFilters] no filters defiened in dataBase");
		} else {
			try {
				Marshaller marshaller = JAXBRefReportFactory.getMarshaller();

				for (Filter filter : filters) {
					Path filterPath = Paths.get(Configuration.rootProperty, filter.getUri());
					FilterParameter filterToWrite = null;
					switch (filter.getType()) {
					case TIME_SLOT:
						filterToWrite = new TimeSlotFilterParameter();
						filterToWrite.filterId = filter.getFilterId();
						filterToWrite.name = filter.getName();
						filterToWrite.comments = filter.getComments();
						String binaryValue = "";
						for (char hexCharacter : filter.getValue().toCharArray()) {
							int binaryCharacter = Integer.parseInt(Character.toString(hexCharacter), 16);
							binaryValue += String.format("%04d",
									Integer.parseInt(Integer.toBinaryString(binaryCharacter)));
						}
						((TimeSlotFilterParameter) filterToWrite).value = binaryValue;
						break;
					default:
						LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
								"[exportFilters] The file " + filterPath.toString() + " can not be removed."));
						break;
					}
					if (filterToWrite != null) {
						try {
							Files.deleteIfExists(filterPath);
							marshaller.marshal(filterToWrite, filterPath.toFile());
						} catch (IOException e) {
							LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
									"[exportFilters] The file " + filterPath.toString() + " can not be removed."));
						}
					}
				}
			} catch (JAXBException e1) {
				LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
						"[exportFilter] Can not export Filter, marshall exception occurs"), e1);
			}
		}
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[exportFilters] Export OK"));
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[exportFilters] End"));
	}

	private void exportFilter(Filter filter, SOAContext soaContext) {
		try {
			Marshaller marshaller = JAXBRefReportFactory.getMarshaller();

			Path filterPath = Paths.get(Configuration.rootProperty, filter.getUri());
			FilterParameter filterToWrite = null;
			switch (filter.getType()) {
			case TIME_SLOT:
				filterToWrite = new TimeSlotFilterParameter();
				filterToWrite.filterId = filter.getFilterId();
				filterToWrite.name = filter.getName();
				filterToWrite.comments = filter.getComments();
				String binaryValue = "";
				for (char hexCharacter : filter.getValue().toCharArray()) {
					int binaryCharacter = Integer.parseInt(Character.toString(hexCharacter), 16);
					binaryValue += String.format("%04d", Integer.parseInt(Integer.toBinaryString(binaryCharacter)));
				}
				((TimeSlotFilterParameter) filterToWrite).value = binaryValue;
				break;
			default:
				LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
						"[exportFilter] The file " + filterPath.toString() + " can not be removed."));
				break;
			}
			if (filterToWrite != null) {
				try {
					Files.deleteIfExists(filterPath);
					marshaller.marshal(filterToWrite, filterPath.toFile());
				} catch (IOException e) {
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
							"[exportFilter] The file " + filterPath.toString() + " can not be removed."));
				}
			}

		} catch (JAXBException e1) {
			LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
					"[exportFilter] Can not export Filter, marshall exception occurs"), e1);
		}
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[exportFilters] Export OK"));
	}
}
