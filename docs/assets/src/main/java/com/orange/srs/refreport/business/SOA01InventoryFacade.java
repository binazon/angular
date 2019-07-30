package com.orange.srs.refreport.business;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.orange.srs.common.annotation.BusinessRule;
import com.orange.srs.refreport.business.command.export.DBExportSpecificInventoryCommand;
import com.orange.srs.refreport.business.command.export.ExportCommandFactory;
import com.orange.srs.refreport.business.delegate.DBExportInventoryDelegate;
import com.orange.srs.refreport.business.delegate.FilePurgeWalker;
import com.orange.srs.refreport.business.delegate.FilterDelegate;
import com.orange.srs.refreport.business.delegate.InventoryDelegate;
import com.orange.srs.refreport.business.delegate.OfferOptionDelegate;
import com.orange.srs.refreport.business.delegate.ReportingGroupDelegate;
import com.orange.srs.refreport.consumer.dao.FilterDAO;
import com.orange.srs.refreport.consumer.dao.GroupingRuleDAO;
import com.orange.srs.refreport.consumer.dao.ParamTypeDAO;
import com.orange.srs.refreport.consumer.dao.ReportingEntityDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupToEntitiesDAO;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.ReportingGroupTO;
import com.orange.srs.refreport.model.TO.ReportingGroupTOList;
import com.orange.srs.refreport.model.TO.ReportingGroupWithOfferOptionTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryStatusTO;
import com.orange.srs.refreport.model.exception.InventoryException;
import com.orange.srs.refreport.model.external.inventory.GetReportGroupAmountParameter;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.business.commonFunctions.DataLocator;
import com.orange.srs.statcommon.business.commonFunctions.PatternResolutionDelegate;
import com.orange.srs.statcommon.model.TO.FilterTOList;
import com.orange.srs.statcommon.model.TO.GetReportingGroupsKeysTO;
import com.orange.srs.statcommon.model.TO.ParamTypeTO;
import com.orange.srs.statcommon.model.TO.ParamTypeTOList;
import com.orange.srs.statcommon.model.TO.ReportingGroupKeyTO;
import com.orange.srs.statcommon.model.constant.ProvisioningFileData;
import com.orange.srs.statcommon.model.enums.InventoryFileTypeEnum;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;
import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;
import com.orange.srs.statcommon.model.parameter.ExportInventoryParameter;
import com.orange.srs.statcommon.model.parameter.PatternParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.SpecificInventoryParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.interceptor.Logged;
import com.orange.srs.statcommon.technical.interceptor.Perf;
import com.orange.srs.statcommon.technical.rest.FileToStreamOutput;

/**
 * Business Facade regarding every operations on business objects involved in inventory description
 * 
 * @author A159138
 */
@Logged
@Stateless
public class SOA01InventoryFacade {

	private final static String DODALL_GROUP = "DOD_ALL";

	private static Logger logger = Logger.getLogger(SOA01InventoryFacade.class);

	public final static String TYPE_PARAM_NAME = "subtype";

	/**
	 * size of the number of entities that are handled in a commit
	 */
	public static int paginationSize = 1000;

	/**
	 * Delegates and subsequent DAO
	 */
	@EJB
	private ReportingGroupDAO reportingGroupDAO;

	@EJB
	private OfferOptionDelegate offerOptionDelegate;

	@EJB
	private ParamTypeDAO paramTypeDAO;

	@EJB
	private ReportingEntityDAO reportingEntityDao;

	@EJB
	private FilterDAO filterDAO;

	@EJB
	private ReportingGroupToEntitiesDAO reportingGroupToEntitiesDao;

	@EJB
	private GroupingRuleDAO groupingRuleDAO;

	@EJB
	private ReportingGroupDelegate reportingGroupDelegate;

	@EJB
	private DBExportInventoryDelegate dbExportInventoryReportDelegate;

	@EJB
	private InventoryDelegate inventoryDelegate;

	@EJB
	private FilterDelegate filterDelegate;

	@EJB
	private ExportCommandFactory exportCommandFactory;

	/**
	 * Remove a reporting group stored in database
	 * 
	 * @param origin:
	 *            the origin of reportingGroup to remove
	 * @param reportingGroupRef:
	 *            the reportingGroupRef of reportingGroup to remove
	 * @throws BusinessException
	 *             ENTITY NOT FOUND
	 */
	public void removeReportingGroupByOriginAndRef(String origin, String reportingGroupRef, SOAContext soaContext)
			throws BusinessException {
		logger.info(SOATools.buildSOALogMessage(soaContext, "[removeReportingGroupByOriginAndRef] Start"));

		List<ReportingGroup> reportingGroups = reportingGroupDelegate.getReportingGroupListFromOriginAndRefGroup(origin,
				reportingGroupRef);
		if (reportingGroups.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": ReportingGroup with ref "
					+ reportingGroupRef + " and origin " + origin);
		}
		reportingGroupDAO.remove(reportingGroups);

		logger.info(SOATools.buildSOALogMessage(soaContext, "[removeReportingGroupByOriginAndRef] End"));
	}

	@BusinessRule(ruleId = "SOA01InventoryFacade_exportInventory", summary = "", description = "Export the entity inventory in a H2 file database for the given reportingGroup", associatedRules = {
			"InventoryDelegate_exportH2Inventory" }, keywords = { "export", "inventory", "h2", "entity" })
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ExportInventoryStatusTO exportH2Inventory(ExportInventoryParameter parameter, SOAContext soaContext)
			throws InventoryException, BusinessException {
		logger.info(SOATools.buildSOALogMessage(soaContext, "[exportH2Inventory] Start"));
		ExportInventoryStatusTO exportInventoryStatusTO = null;
		try {
			exportInventoryStatusTO = inventoryDelegate.exportH2Inventory(parameter, soaContext);

		} catch (BusinessException e) {
			logger.warn(SOATools.buildSOALogMessage(soaContext, "[exportH2Inventory] " + e.getMessage()));
			throw e;
		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[exportH2Inventory] KO: " + e.getMessage()));
			throw new InventoryException(e.getMessage(), e);
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[exportH2Inventory] End"));
		return exportInventoryStatusTO;
	}

	@BusinessRule(ruleId = "SOA01InventoryFacade_exportH2SpecificInventory", summary = "", description = "Export a specific entity inventory in a H2 file database for customers who have the given  ", associatedRules = {
			"InventoryDelegate_exportH2SpecificInventory" }, keywords = { "export", "h2", "entity",
					"specific inventory" })
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String exportH2SpecificInventory(SpecificInventoryParameter parameter, SOAContext soaContext)
			throws InventoryException {

		String resultFileLocation;
		try {
			logger.info(SOATools.buildSOALogMessage(soaContext, "[exportH2SpecificInventory] Start"));
			StringBuilder msgParam = new StringBuilder("[exportH2SpecificInventory] Parameter (")
					.append(parameter.toString()).append(")");
			logger.info(SOATools.buildSOALogMessage(soaContext, msgParam.toString()));

			// check input data
			if (parameter.getExportType() == null) {
				throw new BusinessException(BusinessException.WRONG_PARAMETER_EXCEPTION
						+ " Missing export type required = " + parameter.toString(),
						BusinessException.WRONG_PARAMETER_EXCEPTION_CODE);
			}

			DBExportSpecificInventoryCommand exportCommand = exportCommandFactory.getCommand(parameter.getExportType());

			resultFileLocation = exportCommand.execute(parameter, soaContext);

		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[exportH2SpecificInventory] KO: " + e.getMessage()));
			throw new InventoryException(e.getMessage(), e);
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[exportH2SpecificInventory] Export OK"));
		logger.info(SOATools.buildSOALogMessage(soaContext, "[exportH2SpecificInventory] End"));

		return resultFileLocation;
	}

	/**
	 * Get all the reporting groups stored in database
	 * 
	 * @return ReportingGroupTOList : the list of reportingGroupTO
	 */
	public ReportingGroupTOList getAllReportingGroups(SOAContext soaContext) {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getAllReportingGroups] Start "));

		List<ReportingGroupTO> listTO = reportingGroupDAO.findAllReportingGroupTO(
				ReportingGroupDelegate.GROUPING_CRITERIA_SEPARATOR, ReportingGroupDelegate.GROUPING_VALUES_SEPARATOR);

		ReportingGroupTOList reportingGroups = new ReportingGroupTOList();

		List<ReportingGroupTO> reportingGroupTOs = reportingGroups.reportingGroupTOs;
		for (ReportingGroupTO reportingGroupTO : listTO) {
			reportingGroupTO.setOfferOptions(reportingGroupDelegate.findOptionTOsForReportingGroup(reportingGroupTO));
			reportingGroupTOs.add(reportingGroupTO);
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getAllReportingGroups] End "));

		return reportingGroups;
	}

	/**
	 * Exports the inventory report in a H2 file database according to the given parameter
	 * {@link ExportInventoryParameter}. The database is named using the dataLocation of the {@link ReportingGroup}.
	 * 
	 * @param parameter
	 *            {@link ExportInventoryParameter} containing information about the report group requested
	 * @param soaContext
	 *            {@link SOAContext}
	 * @param reportOutputType
	 *            {@link ReportOutputTypeEnum} defining the type of reports to export
	 * @return true for success, false in case of error
	 * @throws InventoryException
	 *             In case of inventory exception
	 */
	@Perf
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String exportInventoryReport(ExportInventoryParameter parameter, SOAContext soaContext,
			ReportOutputTypeEnum reportOutputType) throws InventoryException {

		List<String> resultFileLocations = null;
		String resultFileLocation;
		try {
			logger.info(SOATools.buildSOALogMessage(soaContext, "[exportInventoryReport] Start"));
			StringBuilder msgParam = new StringBuilder("[exportInventoryReport] Parameter ")
					.append(parameter.reportingGroupRefId).append(" (").append(parameter.origin)
					.append(") and report type ").append(reportOutputType);
			logger.info(SOATools.buildSOALogMessage(soaContext, msgParam.toString()));

			/*
			 * Get the file location to export data
			 */
			// Get the group from database (unique for couple origin/refGroupId) and throw
			// an exception if it has not
			// been found
			ReportingGroup requiredGroup = reportingGroupDelegate
					.getReportingGroupFromOriginAndRefGroup(parameter.origin, parameter.reportingGroupRefId);

			if (requiredGroup == null) {
				StringBuilder msgUnknowGroup = new StringBuilder(InventoryException.UNKWNON_GROUP_EXCEPTION)
						.append(" with groupRef '").append(parameter.reportingGroupRefId).append("' and origin '")
						.append(parameter.origin).append("'");
				logger.error("[exportInventoryReport] " + msgUnknowGroup);
				throw new InventoryException(msgUnknowGroup.toString());
			}

			if (requiredGroup.getDataLocation() == null) {
				throw new BusinessException(BusinessException.NO_DATA_FOUND_EXCEPTION_MESSAGE + " group = " + parameter
						+ " : data location NOT FOUND", BusinessException.NO_DATA_FOUND_EXCEPTION);
			}

			// inventories are stored with previous day date of export in inventory filename
			// the inventory date is a parameter of inventory task
			// if date is not set, the default date is previous day date
			Date exportDayDate;
			if (parameter.date != null) {
				exportDayDate = parameter.date.getTime();
			} else {
				exportDayDate = Utils.getYesterdayDate();
			}

			// Get the location to store the inventory export
			PatternParameter patternParameter = new PatternParameter();
			patternParameter.startUnit = exportDayDate;
			patternParameter.origin = requiredGroup.getOrigin();
			patternParameter.reportingGroupLocation = requiredGroup.getDataLocation().getLocationPattern();
			patternParameter.properties = Configuration.mountConfiguration;

			switch (reportOutputType) {
			case INTERACTIVE:
				resultFileLocations = DataLocator.getFirstInventoryReportFileLocationWithoutExtension(patternParameter);
				resultFileLocation = dbExportInventoryReportDelegate.exportInventoryReportInteractive(soaContext,
						parameter, resultFileLocations);
				break;
			case TEMPLATE:
				resultFileLocations = DataLocator
						.getFirstInventoryReportTemplateFileLocationWithoutExtension(patternParameter);
				resultFileLocation = dbExportInventoryReportDelegate.exportInventoryReportTemplate(soaContext,
						parameter, resultFileLocations);
				break;
			default:
				throw new IllegalArgumentException("Unmanaged export for report type '" + reportOutputType + "'");
			}

		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[exportInventoryReport] KO: " + e.getMessage()));
			throw new InventoryException(e.getMessage(), e);
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[exportInventoryReport] Export OK"));
		logger.info(SOATools.buildSOALogMessage(soaContext, "[exportInventoryReport] End"));

		return resultFileLocation;
	}

	@Perf
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void exportAllFilters(SOAContext soaContext) {
		filterDelegate.exportFilters(soaContext);
	}

	public GetReportingGroupsKeysTO getReportingGroupKeys(SOAContext soaContext) {
		logger.debug("[getStatGroups] Get all reportingGroups");

		GetReportingGroupsKeysTO getReportingGroupsTO = new GetReportingGroupsKeysTO();
		getReportingGroupsTO.groupKeys = reportingGroupDAO.findAllReportingGroupsKeys();

		return getReportingGroupsTO;
	}

	public GetReportingGroupsKeysTO getInteractiveReportingGroupKeys(SOAContext soaContext) {
		logger.debug("[getInteractiveStatGroups] Get reportingGroups for Opendashboard files retrieving");

		GetReportingGroupsKeysTO getReportingGroupsTO = new GetReportingGroupsKeysTO();
		getReportingGroupsTO.groupKeys = new ArrayList<ReportingGroupKeyTO>();

		for (ReportingGroupKeyTO key : reportingGroupDAO.findAllReportingGroupsKeysWithSourceColumn()) {
			if (!(key.getReportingGroupRef().startsWith(ProvisioningFileData.SPECIFIC_GROUP_VPN_SUFFIX)
					&& key.getOrigin().equals("EQUANT") && key.getSource().equals("REFOBJECT"))) {
				getReportingGroupsTO.groupKeys.add(key);
			}
		}
		return getReportingGroupsTO;
	}

	/**
	 * Get the inventory file according to the given parameter
	 * 
	 * @param origin
	 *            Origin
	 * @param reportingGroupRef
	 *            Reporting group ref
	 * @param inventoryFileType
	 *            {@link InventoryFileTypeEnum} used to distinguish which inventory file to get
	 * @param soaContext
	 *            {@link SOAContext} for logging
	 * @return a {@link StreamingOutput} object corresponding to the file
	 * @throws FileNotFoundException
	 *             If the inventory file is not found
	 * @throws BusinessException
	 *             If the reporting group is not found or if there is multiple reporting group in database for the given
	 *             key (origin/reportingGroupRef)
	 */
	public StreamingOutput getInventoryFile(String origin, String reportingGroupRef,
			InventoryFileTypeEnum inventoryFileType, SOAContext soaContext)
			throws BusinessException, FileNotFoundException {

		FileToStreamOutput outputStream = null;
		try {
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getInventoryFile] Start"));
			String msgParam = "Group " + reportingGroupRef + " (" + origin + ") and file type " + inventoryFileType;
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getInventoryFile] " + msgParam));

			// Get the group from database and throw an exception if it has not been found
			ReportingGroup requiredGroup = reportingGroupDelegate.getReportingGroupFromOriginAndRefGroup(origin,
					reportingGroupRef);
			if (requiredGroup == null) {
				throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + " " + msgParam,
						BusinessException.ENTITY_NOT_FOUND_EXCEPTION_CODE);
			}

			if (requiredGroup.getDataLocation() == null) {
				throw new BusinessException(BusinessException.NO_DATA_FOUND_EXCEPTION_MESSAGE + " " + msgParam
						+ " : data location NOT FOUND", BusinessException.NO_DATA_FOUND_EXCEPTION);
			}

			// Inventories are stored with previous day date of export in inventory filename
			Date exportDayDate = Utils.getYesterdayDate();

			// Get the location to store the inventory export
			PatternParameter patternParameter = new PatternParameter();
			patternParameter.startUnit = exportDayDate;
			patternParameter.origin = requiredGroup.getOrigin();
			patternParameter.reportingGroupLocation = requiredGroup.getDataLocation().getLocationPattern();
			patternParameter.properties = Configuration.mountConfiguration;

			List<String> fileLocations = new ArrayList<>();

			switch (inventoryFileType) {
			case ENTITY:
				fileLocations.add(DataLocator.getLastInventoryEntityFileLocationWithExtension(patternParameter));
				break;
			case REPORT_INTERACTIVE:
				fileLocations = DataLocator.getInventoryReportFileLocationsWithExtension(patternParameter);
				break;
			case REPORT_TEMPLATE:
				fileLocations = DataLocator.getInventoryReportTemplateFileLocationsWithExtension(patternParameter);
				break;
			default:
				throw new IllegalArgumentException("Unmanaged inventory file type '" + inventoryFileType + "'");
			}

			for (String file : fileLocations) {
				if (outputStream == null) {
					String lockLocation = StringUtils.replace(file, PatternResolutionDelegate.EXTENSION_H2FILE,
							PatternResolutionDelegate.EXTENSION_H2LOCKFILE);
					if (!new File(lockLocation).exists()) {
						logger.debug("[getInventoryFile] -> Location " + file);

						outputStream = new FileToStreamOutput(file);
					} else {
						logger.warn("[getInventoryFile] -> File " + file
								+ " is locked, retreive the previous one if there is any");
					}
				}

			}

		} catch (BusinessException be) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[getInventoryFile] Get KO: " + be.getMessage()));
			throw be;
		} catch (FileNotFoundException fne) {
			logger.warn(SOATools.buildSOALogMessage(soaContext, "[getInventoryFile] Get KO: " + fne.getMessage()));
			throw fne;
		}
		logger.debug(SOATools.buildSOALogMessage(soaContext, "[getInventoryFile] End"));

		return outputStream;

	}

	/**
	 * Get all the {@link ParamType} stored in database
	 * 
	 * @param soaContext
	 *            {@link SOAContext} for logging
	 * @return {@link ParamTypeTOList} : the list of {@link ParamTypeTO}
	 * @throws InventoryException
	 *             In case of exception
	 */
	public ParamTypeTOList getAllParamTypes(SOAContext soaContext) throws InventoryException {

		ParamTypeTOList paramTypeTOList = new ParamTypeTOList();

		try {
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getAllParamTypes] Start "));

			List<ParamTypeTO> listTO = paramTypeDAO.findAllParamTypeTO();

			paramTypeTOList.paramTypeTOs = listTO;

		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[getAllParamTypes] KO"));
			throw new InventoryException(e.getMessage(), e);
		}

		logger.debug(SOATools.buildSOALogMessage(soaContext, "[getAllParamTypes] End "));

		return paramTypeTOList;
	}

	public int getReportingGroupAmount(GetReportGroupAmountParameter parameter, SOAContext context) {
		String[] params = { ReportingGroup.FIELD_REPORTING_GROUP_REF, ReportingGroup.FIELD_ORIGIN };
		String[] values = { parameter.reportingGroupRef, parameter.origin };
		List<ReportingGroup> groups = reportingGroupDAO.findByMultipleCriteria(params, values);
		return groups.size();
	}

	public FilterTOList getAllFilters(SOAContext soaContext) throws InventoryException {

		FilterTOList filterTOList = new FilterTOList();

		try {

			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getAllFilters] Start "));
			filterTOList.filterTOs = filterDAO.findAllFilterTO();
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getAllFilters] OK"));

		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "[getAllFilters] KO"));
			throw new InventoryException(e.getMessage(), e);
		} finally {
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getAllFilters] End "));
		}

		return filterTOList;
	}

	@Asynchronous
	public void purgeInventoryFile() throws IOException {

		Path root = Paths.get(Configuration.rootProperty, Configuration.statgroupFolderProperty);

		Files.walkFileTree(root, new FilePurgeWalker());
	}

	public String getReportingGroupsByOfferOptionType(List<OfferOptionTypeEnum> offerOptionTypes, SOAContext soaContext)
			throws BusinessException {
		logger.info(SOATools.buildSOALogMessage(soaContext, "[getReportingGroupsByOfferOptionType] Start"));

		List<ReportingGroupWithOfferOptionTO> reportingGroupWithOfferOptionTOs = reportingGroupDAO
				.findAllReportingGroupAndOfferOptionByOfferOptionType(offerOptionTypes);
		List<Object[]> reportingGroupsWithGroupingRules = groupingRuleDAO
				.findAllReportingGroupAndDistinctGroupingRule();
		Map<Long, String> groupingRulesByReportingGroupPk = new HashMap<>();
		for (Object[] reportingGroupWithGroupingRules : reportingGroupsWithGroupingRules) {
			Long reportingGroupPk = (Long) reportingGroupWithGroupingRules[0];
			String groupingRule = (String) reportingGroupWithGroupingRules[1];
			String groupingRules = groupingRulesByReportingGroupPk.get(reportingGroupPk);
			if (groupingRules == null) {
				groupingRules = groupingRule;
			} else {
				groupingRules += ',' + groupingRule;
			}
			groupingRulesByReportingGroupPk.put(reportingGroupPk, groupingRules);
		}

		Path generatedFilePath = Paths.get(Configuration.rootProperty, Configuration.statgroupDataFolderProperty,
				Configuration.allFolderProperty, Configuration.moduleName, "ExportReportingGroups.csv");
		Path tempGeneratedFilePath = Paths.get(generatedFilePath.toString() + ".tmp");

		File directory = generatedFilePath.getParent().toFile();
		if (!directory.exists()) {
			directory.mkdirs();
		}

		// Remove the temporary file
		FileUtils.deleteQuietly(tempGeneratedFilePath.toFile());
		// Generate the temporary file
		try (BufferedWriter writer = Files.newBufferedWriter(tempGeneratedFilePath, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE)) {
			String currentReportingGroupRef = null;
			for (ReportingGroupWithOfferOptionTO reportingGroupWithOfferOptionTO : reportingGroupWithOfferOptionTOs) {

				if (currentReportingGroupRef == null
						|| !currentReportingGroupRef.equals(reportingGroupWithOfferOptionTO.reportingGroupRef)) {

					if (currentReportingGroupRef != null) {
						// Change of reportingGroup
						writer.newLine();
					}

					currentReportingGroupRef = reportingGroupWithOfferOptionTO.reportingGroupRef;
					writer.append(reportingGroupWithOfferOptionTO.reportingGroupRef).append(';');
					writer.append(reportingGroupWithOfferOptionTO.origin).append(';');
					String groupingRules = groupingRulesByReportingGroupPk
							.get(reportingGroupWithOfferOptionTO.reportingGroupPk);
					if (groupingRules != null) {
						writer.append(groupingRules);
					}
					writer.append(';');
					writer.append(reportingGroupWithOfferOptionTO.offerOptionAlias);
				} else {
					writer.append(',').append(reportingGroupWithOfferOptionTO.offerOptionAlias);
				}
			}
		} catch (IOException e) {
			logger.error(SOATools.buildSOALogMessage(soaContext,
					"[getReportingGroupsByOfferOptionType] Error while generating " + tempGeneratedFilePath.toString()),
					e);
			throw new BusinessException("Unable to generate file " + generatedFilePath.toString());
		}
		reportingGroupWithOfferOptionTOs.clear();

		// Move the temporary file to the real file
		try {
			Files.move(tempGeneratedFilePath, generatedFilePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(
					SOATools.buildSOALogMessage(soaContext, "[getReportingGroupsByOfferOptionType] Error while moving "
							+ tempGeneratedFilePath.toString() + " to " + generatedFilePath.toString()),
					e);
			throw new BusinessException("Unable to generate file " + generatedFilePath.toString());
		}

		logger.info(SOATools.buildSOALogMessage(soaContext,
				"[getReportingGroupsByOfferOptionType] End: file " + generatedFilePath.toString() + " generated"));
		return generatedFilePath.toString();
	}

}
