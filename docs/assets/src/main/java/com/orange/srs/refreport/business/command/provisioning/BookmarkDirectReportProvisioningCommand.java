package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.BookmarkDirectReportDelegate;
import com.orange.srs.refreport.model.TO.provisioning.BookmarkDirectReportListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class BookmarkDirectReportProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(BookmarkDirectReportProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private BookmarkDirectReportDelegate bookmarkDirectReportDelegate;

	@Override
	public void execute(SOAContext soaContext, File bookmarkDirectReportProvisioningFile,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {

		LOGGER.info(
				SOATools.buildSOALogMessage(soaContext, "[BookmarkDirectReportProvisioningCommand] provisioning file "
						+ bookmarkDirectReportProvisioningFile.getAbsolutePath()));

		BookmarkDirectReportListProvisioningTO bookmarkDirectReportListProvisioningTOUnmarshalled = (BookmarkDirectReportListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				bookmarkDirectReportProvisioningFile);
		provisioningFacade.updateBookmarkDirectReportByDifferential(soaContext,
				bookmarkDirectReportListProvisioningTOUnmarshalled.bookmarkDirectReportProvisioningTOs,
				forceUpdateFromFileToDatabase);
		if (!forceUpdateFromFileToDatabase) {
			BookmarkDirectReportListProvisioningTO bookmarkDirectReportListProvisioningTOToMarshall = bookmarkDirectReportDelegate
					.getBookmarkDirectReportListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(bookmarkDirectReportListProvisioningTOToMarshall,
					bookmarkDirectReportProvisioningFile, false);
		}
	}

}
