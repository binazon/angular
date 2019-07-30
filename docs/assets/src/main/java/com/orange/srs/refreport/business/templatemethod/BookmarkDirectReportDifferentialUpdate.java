package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.BookmarkDirectReportDelegate;
import com.orange.srs.refreport.model.BookmarkDirectReport;
import com.orange.srs.refreport.model.TO.BookmarkDirectReportKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.BookmarkDirectReportProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class BookmarkDirectReportDifferentialUpdate extends
		AbstractDifferentialUpdateTemplateMethod<BookmarkDirectReportProvisioningTO, BookmarkDirectReport, BookmarkDirectReportKeyTO> {

	@EJB
	private BookmarkDirectReportDelegate bookmarkDirectReportDelegate;

	@Override
	protected void sortProvisioningTOs(List<BookmarkDirectReportProvisioningTO> bookmarkDirectReportProvisioningTOs) {
		BookmarkDirectReportDelegate.sortBookmarkDirectReportProvisioningTO(bookmarkDirectReportProvisioningTOs);
	}

	@Override
	protected List<BookmarkDirectReport> getModelObjectsSorted() {
		return bookmarkDirectReportDelegate.getAllBookmarkDirectReportSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(
			List<BookmarkDirectReportProvisioningTO> bookmarkDirectReportProvisioningTOs) throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(
			BookmarkDirectReportProvisioningTO bookmarkDirectReportProvisioningTO) throws BusinessException {
		// No functional checks
	}

	@Override
	protected BookmarkDirectReportKeyTO getProvisioningTOKey(
			BookmarkDirectReportProvisioningTO bookmarkDirectReportProvisioningTO) {
		return BookmarkDirectReportDelegate
				.getBookmarkDirectReportProvisioningTOKey(bookmarkDirectReportProvisioningTO);
	}

	@Override
	protected BookmarkDirectReportKeyTO getModelObjectKey(BookmarkDirectReport bookmarkDirectReport) {
		return BookmarkDirectReportDelegate.getBookmarkDirectReportKey(bookmarkDirectReport);
	}

	@Override
	protected Boolean getSuppressFlag(BookmarkDirectReportProvisioningTO bookmarkDirectReportProvisioningTO) {
		return bookmarkDirectReportProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(BookmarkDirectReport bookmarkDirectReport) {
		bookmarkDirectReportDelegate.removeBookmarkDirectReport(bookmarkDirectReport);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, BookmarkDirectReport bookmarkDirectReport,
			BookmarkDirectReportProvisioningTO bookmarkDirectReportProvisioningTO) throws BusinessException {
		return bookmarkDirectReportDelegate.updateBookmarkDirectReportIfNecessary(bookmarkDirectReport,
				bookmarkDirectReportProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			BookmarkDirectReportProvisioningTO bookmarkDirectReportProvisioningTO) throws BusinessException {
		bookmarkDirectReportDelegate.createBookmarkDirectReport(bookmarkDirectReportProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "bookmarkDirectReport";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
