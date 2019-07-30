package com.orange.srs.refreport.business.command.export;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.h2.jdbc.JdbcSQLException;

import com.orange.srs.refreport.consumer.dao.OfferOptionDAO;
import com.orange.srs.refreport.model.exception.InventoryException;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.consumer.jdbc.JDBCH2Consumer;
import com.orange.srs.statcommon.model.parameter.CreateTableParameter;
import com.orange.srs.statcommon.model.parameter.PatternParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.SpecificInventoryParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.TechnicalException;
import com.orange.srs.statcommon.technical.jdbc.StatementByColumnAdapter;

/**
 * Delegate used to created H2 Table for export inventory report interactive and template
 */
public abstract class AbstractDBExportSpecificInventoryCommand implements DBExportSpecificInventoryCommand {

	private static final Logger LOGGER = Logger.getLogger(AbstractDBExportSpecificInventoryCommand.class);

	@Inject
	private JDBCH2Consumer h2consumer;

	@EJB
	private OfferOptionDAO offerOptionDAO;

	@Override
	public String execute(final SpecificInventoryParameter parameter, SOAContext soaContext)
			throws InventoryException, BusinessException {

		checkParameters(parameter);

		/*
		 * This boolean is used to know if we need to close H2 connection in case of exception. To avoid re-close
		 * connection in case of exception during the close
		 */
		boolean needToCloseH2Connection = true;
		boolean h2ConnectionError = true;

		// Get the location to store the inventory export
		PatternParameter patternParameter = new PatternParameter();
		patternParameter.properties = Configuration.mountConfiguration;

		List<String> fileLocations = getResultFileLocations(patternParameter);
		List<String> fileLocationsWithExtension = getResultFileLocationsWithExtension(patternParameter);

		int fileLocationIndex = fileLocations.size() - 1;
		String fileLocation = fileLocations.get(fileLocationIndex);

		String TEMP_SUFFIX = (new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss")).format(new Date());

		do {
			String fileLocationFullPath = fileLocationsWithExtension.get(fileLocationIndex);
			String fileLocationTemp = fileLocation + TEMP_SUFFIX;
			String fileLocationFullPathTemp = fileLocationFullPath.replaceFirst(fileLocation, fileLocationTemp);

			try {

				LOGGER.debug("[exportH2SpecificInventory] -> Location " + fileLocation);
				h2consumer.createConnexion(fileLocationTemp + Configuration.h2FileReadwriteLockOptionsProperty, false,
						false);

				createAndFillTable(h2consumer, parameter);

				needToCloseH2Connection = false;
				h2consumer.closeConnexion();

				try {
					Files.move(Paths.get(fileLocationFullPathTemp), Paths.get(fileLocationFullPath),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception efc) {
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
							"Exception occured. Specific Inventory file not replaced : " + efc.getMessage()));
				}

				h2ConnectionError = false;
			} catch (JdbcSQLException jdbcsqle) {

				if (needToCloseH2Connection) {
					try {
						h2consumer.closeConnexion();
					} catch (Exception e2) {
						LOGGER.error(SOATools.buildSOALogMessage(soaContext,
								"[exportH2SpecificInventory] KO on close connection", e2));
						throw new InventoryException(jdbcsqle.getMessage() + "\n" + e2.getMessage(), e2);
					}
				}
				try {
					Files.delete(Paths.get(fileLocationFullPathTemp));
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
							"Exception occured. Specific temporary Inventory file deleted : "
									+ fileLocationFullPathTemp));
				} catch (Exception efd) {
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext, efd.getMessage()));
				}

				if (JDBCH2Consumer.H2_FILE_READONLY_DATABASE_ERROR_CODES.contains(jdbcsqle.getErrorCode())
						&& fileLocationIndex > -1) {
					h2ConnectionError = true;
					fileLocationIndex--;
					fileLocation = fileLocations.get(fileLocationIndex);
					// fileLocationFullPath = fileLocationsWithExtension.get(fileLocationIndex);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
								"H2 connection error (" + jdbcsqle.getErrorCode()
										+ ") accessing H2 Table reportInputPatternFile: " + jdbcsqle.getMessage()));
					}
				} else {
					LOGGER.error(SOATools.buildSOALogMessage(soaContext, "[exportH2SpecificInventory] KO", jdbcsqle));
					throw new InventoryException(jdbcsqle.getMessage(), jdbcsqle);
				}

			} catch (Exception e) {

				LOGGER.error("[exportH2SpecificInventory] KO", e);
				if (needToCloseH2Connection) {
					try {
						h2consumer.closeConnexion();
					} catch (Exception e2) {
						LOGGER.error(SOATools.buildSOALogMessage(soaContext,
								"[exportH2SpecificInventory] KO on close connection", e2));
						throw new InventoryException(e.getMessage() + "\n" + e2.getMessage(), e2);
					}
				}

				try {
					Files.delete(Paths.get(fileLocationFullPathTemp));
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
							"Exception occured. Specific temporary Inventory file deleted : " + fileLocationFullPath));
				} catch (Exception efd) {
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext, efd.getMessage()));
				}

				throw new InventoryException(e.getMessage(), e);
			}
		} while (h2ConnectionError);
		return fileLocation;
	}

	protected StatementByColumnAdapter createSpecificInventoryTable(CreateTableParameter cparameter)
			throws TechnicalException, SQLException {
		StatementByColumnAdapter statement = h2consumer.createTableAndStatement(cparameter, true, true);
		LOGGER.info("[exportH2SpecificInventory] Adding table " + cparameter.tableName);
		return statement;
	}

	public void createAndFillTable(JDBCH2Consumer h2consumer, SpecificInventoryParameter parameter)
			throws TechnicalException, SQLException {
		CreateTableParameter cparameter = getCreateTableParameter();

		// Creation of the table and the statement
		StatementByColumnAdapter statement = createSpecificInventoryTable(cparameter);

		h2consumer.commit();
		fillSpecificInventoryTable(parameter, statement);

		h2consumer.commit();
	}

	protected abstract void fillSpecificInventoryTable(SpecificInventoryParameter parameter,
			StatementByColumnAdapter statement) throws TechnicalException, SQLException;

	protected abstract CreateTableParameter getCreateTableParameter();

	protected abstract List<String> getResultFileLocations(PatternParameter patternParameter);

	protected abstract List<String> getResultFileLocationsWithExtension(PatternParameter patternParameter);

	protected abstract void checkParameters(SpecificInventoryParameter parameter) throws BusinessException;
}
