package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.lang.StringUtils;

import com.orange.srs.refreport.consumer.dao.BookmarkDirectReportDAO;
import com.orange.srs.refreport.model.BookmarkDirectReport;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.TO.BookmarkDirectReportKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.BookmarkDirectReportListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.BookmarkDirectReportProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class BookmarkDirectReportDelegate {

	@EJB
	private BookmarkDirectReportDAO bookmarkDirectReportDao;

	@EJB
	private IndicatorDelegate indicatorDelegate;

	@EJB
	private OfferOptionDelegate offerOptionDelegate;

	@EJB
	private ParamTypeDelegate paramTypeDelegate;

	public static BookmarkDirectReportKeyTO getBookmarkDirectReportKey(BookmarkDirectReport bookmarkDirectReport) {
		return new BookmarkDirectReportKeyTO(bookmarkDirectReport.getLabel(),
				bookmarkDirectReport.getIndicator().getIndicatorId(), bookmarkDirectReport.getParamType().getAlias());
	}

	public static BookmarkDirectReportKeyTO getBookmarkDirectReportProvisioningTOKey(
			BookmarkDirectReportProvisioningTO bookmarkDirectReportProvisioningTO) {
		return new BookmarkDirectReportKeyTO(bookmarkDirectReportProvisioningTO.label,
				bookmarkDirectReportProvisioningTO.indicatorId, bookmarkDirectReportProvisioningTO.paramTypeAlias);
	}

	public BookmarkDirectReport getBookmarkDirectReportByKey(String bookmarkDirectReportLabel)
			throws BusinessException {
		List<BookmarkDirectReport> listBookmarkDirectReport = bookmarkDirectReportDao
				.findBy(BookmarkDirectReport.FIELD_LABEL, bookmarkDirectReportLabel);
		if (listBookmarkDirectReport.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION
					+ ": BookmarkDirectReport with key [label=" + bookmarkDirectReportLabel + "]");
		}
		return listBookmarkDirectReport.get(0);
	}

	public BookmarkDirectReport createBookmarkDirectReport(
			BookmarkDirectReportProvisioningTO bookmarkDirectReportProvisioningTO) throws BusinessException {
		BookmarkDirectReport bookmarkDirectReport = new BookmarkDirectReport();
		bookmarkDirectReport.setLabel(bookmarkDirectReportProvisioningTO.label);
		Indicator indicator = indicatorDelegate.getIndicatorByKey(bookmarkDirectReportProvisioningTO.indicatorId);
		bookmarkDirectReport.setIndicator(indicator);
		OfferOption offerOption = offerOptionDelegate
				.getOfferOptionByKey(bookmarkDirectReportProvisioningTO.offerOptionAlias);
		bookmarkDirectReport.setOfferOption(offerOption);
		ParamType paramType = paramTypeDelegate.getParamTypeByKey(bookmarkDirectReportProvisioningTO.paramTypeAlias);
		bookmarkDirectReport.setParamType(paramType);
		if (!StringUtils.isEmpty(bookmarkDirectReportProvisioningTO.additionalParamTypeAlias)) {
			ParamType additionalParamType = paramTypeDelegate
					.getParamTypeByKey(bookmarkDirectReportProvisioningTO.additionalParamTypeAlias);
			bookmarkDirectReport.setAdditionalParamType(additionalParamType);
		}
		if (bookmarkDirectReportProvisioningTO.hierarchy != null) {
			bookmarkDirectReport.setHierarchy(bookmarkDirectReportProvisioningTO.hierarchy.toString());
		}

		bookmarkDirectReportDao.persistAndFlush(bookmarkDirectReport);
		return bookmarkDirectReport;
	}

	public boolean updateBookmarkDirectReportIfNecessary(BookmarkDirectReport bookmarkDirectReport,
			BookmarkDirectReportProvisioningTO bookmarkDirectReportProvisioningTO) throws BusinessException {

		boolean updated = false;

		String currentIndicatorId = bookmarkDirectReport.getIndicator().getIndicatorId();
		if (!bookmarkDirectReportProvisioningTO.indicatorId.equals(currentIndicatorId)) {
			Indicator indicator = indicatorDelegate.getIndicatorByKey(bookmarkDirectReportProvisioningTO.indicatorId);
			bookmarkDirectReport.setIndicator(indicator);
			updated = true;
		}

		String currentOfferOptionAlias = bookmarkDirectReport.getOfferOption().getAlias();
		if (!bookmarkDirectReportProvisioningTO.offerOptionAlias.equals(currentOfferOptionAlias)) {
			OfferOption offerOption = offerOptionDelegate
					.getOfferOptionByKey(bookmarkDirectReportProvisioningTO.offerOptionAlias);
			bookmarkDirectReport.setOfferOption(offerOption);
			updated = true;
		}

		String currentParamTypeAlias = bookmarkDirectReport.getParamType().getAlias();
		if (!bookmarkDirectReportProvisioningTO.paramTypeAlias.equals(currentParamTypeAlias)) {
			ParamType paramType = paramTypeDelegate
					.getParamTypeByKey(bookmarkDirectReportProvisioningTO.paramTypeAlias);
			bookmarkDirectReport.setParamType(paramType);
			updated = true;
		}

		String currentAdditionalParamTypeAlias = bookmarkDirectReport.getAdditionalParamType() == null ? null
				: bookmarkDirectReport.getAdditionalParamType().getAlias();
		if ((!StringUtils.isEmpty(bookmarkDirectReportProvisioningTO.additionalParamTypeAlias))
				&& (!bookmarkDirectReportProvisioningTO.additionalParamTypeAlias
						.equals(currentAdditionalParamTypeAlias))) {
			ParamType additionalParamType = paramTypeDelegate
					.getParamTypeByKey(bookmarkDirectReportProvisioningTO.additionalParamTypeAlias);
			bookmarkDirectReport.setAdditionalParamType(additionalParamType);
			updated = true;
		} else if ((StringUtils.isEmpty(bookmarkDirectReportProvisioningTO.additionalParamTypeAlias))
				&& (currentAdditionalParamTypeAlias != null)) {
			bookmarkDirectReport.setAdditionalParamType(null);
			updated = true;
		}

		String hierarchy = bookmarkDirectReport.getHierarchy();
		if ((bookmarkDirectReportProvisioningTO.hierarchy != null)
				&& (!bookmarkDirectReportProvisioningTO.hierarchy.toString().equals(hierarchy))) {
			bookmarkDirectReport.setHierarchy(bookmarkDirectReportProvisioningTO.hierarchy.toString());
			updated = true;
		}

		if (updated) {
			bookmarkDirectReportDao.persistAndFlush(bookmarkDirectReport);
		}
		return updated;
	}

	public void removeBookmarkDirectReport(BookmarkDirectReport bookmarkDirectReport) {
		bookmarkDirectReportDao.remove(bookmarkDirectReport);
	}

	public List<BookmarkDirectReport> getAllBookmarkDirectReportSorted() {
		List<BookmarkDirectReport> bookmarkDirectReportList = bookmarkDirectReportDao.findAll();
		sortBookmarkDirectReport(bookmarkDirectReportList);
		return bookmarkDirectReportList;
	}

	public BookmarkDirectReportListProvisioningTO getBookmarkDirectReportListProvisioningTOSorted() {
		BookmarkDirectReportListProvisioningTO bookmarkDirectReportListProvisioningTO = new BookmarkDirectReportListProvisioningTO();
		bookmarkDirectReportListProvisioningTO.bookmarkDirectReportProvisioningTOs = bookmarkDirectReportDao
				.findAllBookmarkDirectReportProvisioningTO();
		sortBookmarkDirectReportProvisioningTO(
				bookmarkDirectReportListProvisioningTO.bookmarkDirectReportProvisioningTOs);
		return bookmarkDirectReportListProvisioningTO;
	}

	public static void sortBookmarkDirectReport(List<BookmarkDirectReport> bookmarkDirectReports) {
		Collections.sort(bookmarkDirectReports, new BookmarkDirectReportComparator());
	}

	public static void sortBookmarkDirectReportProvisioningTO(
			List<BookmarkDirectReportProvisioningTO> bookmarkDirectReportProvisioningTOs) {
		Collections.sort(bookmarkDirectReportProvisioningTOs, new BookmarkDirectReportProvisioningTOComparator());
	}

	private static class BookmarkDirectReportComparator implements Comparator<BookmarkDirectReport> {
		@Override
		public int compare(BookmarkDirectReport firstObj, BookmarkDirectReport secondObj) {
			return getBookmarkDirectReportKey(firstObj).compareTo(getBookmarkDirectReportKey(secondObj));
		}
	}

	private static class BookmarkDirectReportProvisioningTOComparator
			implements Comparator<BookmarkDirectReportProvisioningTO> {
		@Override
		public int compare(BookmarkDirectReportProvisioningTO firstObj, BookmarkDirectReportProvisioningTO secondObj) {
			return getBookmarkDirectReportProvisioningTOKey(firstObj)
					.compareTo(getBookmarkDirectReportProvisioningTOKey(secondObj));
		}
	}

}
