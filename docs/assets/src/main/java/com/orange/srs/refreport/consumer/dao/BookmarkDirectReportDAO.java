package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.BookmarkDirectReport;
import com.orange.srs.refreport.model.TO.provisioning.BookmarkDirectReportProvisioningTO;

public interface BookmarkDirectReportDAO extends Dao<BookmarkDirectReport, String> {

	public List<BookmarkDirectReportProvisioningTO> findAllBookmarkDirectReportProvisioningTO();

	public List<BookmarkDirectReport> findByLabelAndEntityTypeAndSubtype(String label, String entityType,
			String entitySubType);
}
