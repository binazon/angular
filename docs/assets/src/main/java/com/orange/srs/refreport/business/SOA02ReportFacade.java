package com.orange.srs.refreport.business;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.h2.jdbc.JdbcSQLException;

import com.orange.srs.refreport.business.delegate.ReportInputDelegate;
import com.orange.srs.refreport.consumer.dao.BookmarkDirectReportDAO;
import com.orange.srs.refreport.consumer.dao.ExternalIndicatorDAO;
import com.orange.srs.refreport.consumer.dao.HyperlinkDAO;
import com.orange.srs.refreport.consumer.dao.IndicatorDAO;
import com.orange.srs.refreport.consumer.dao.InputFormatDAO;
import com.orange.srs.refreport.consumer.dao.OfferOptionDAO;
import com.orange.srs.refreport.consumer.dao.ParamTypeDAO;
import com.orange.srs.refreport.consumer.dao.ReportDAO;
import com.orange.srs.refreport.consumer.dao.ReportInputDAO;
import com.orange.srs.refreport.model.BookmarkDirectReport;
import com.orange.srs.refreport.model.Hyperlink;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.InputColumn;
import com.orange.srs.refreport.model.InputFormat;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.Report;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.TO.HyperlinkTO;
import com.orange.srs.refreport.model.TO.ReportRefIdAndOfferOptionTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportInputsStatusTO;
import com.orange.srs.refreport.model.exception.StatInputException;
import com.orange.srs.refreport.model.parameter.CreateHyperlinkParameter;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.business.commonFunctions.BDStatCommonFunction;
import com.orange.srs.statcommon.business.commonFunctions.DataLocator;
import com.orange.srs.statcommon.consumer.jdbc.JDBCH2Consumer;
import com.orange.srs.statcommon.model.TO.GetInputFormatFullTO;
import com.orange.srs.statcommon.model.TO.GetPatternTO;
import com.orange.srs.statcommon.model.TO.GetPatternTOList;
import com.orange.srs.statcommon.model.TO.GetReportInputTO;
import com.orange.srs.statcommon.model.TO.GetReportsAndOfferOptionTO;
import com.orange.srs.statcommon.model.TO.GetReportsTO;
import com.orange.srs.statcommon.model.TO.InputColumnFullTO;
import com.orange.srs.statcommon.model.TO.InputColumnTO;
import com.orange.srs.statcommon.model.TO.ReportAndOfferOptionTO;
import com.orange.srs.statcommon.model.TO.ReportInputTO;
import com.orange.srs.statcommon.model.TO.ReportTO;
import com.orange.srs.statcommon.model.TO.StatPattern;
import com.orange.srs.statcommon.model.TO.report.LocationTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputCassandraTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputCassandraTOList;
import com.orange.srs.statcommon.model.TO.report.ReportInputColumnTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTOList;
import com.orange.srs.statcommon.model.TO.report.ReportInputLocationAndFormatTO;
import com.orange.srs.statcommon.model.TO.report.TableDefinitionTO;
import com.orange.srs.statcommon.model.TO.rest.ExternalIndicatorTO;
import com.orange.srs.statcommon.model.TO.rest.GetBookmarkDirectReportTO;
import com.orange.srs.statcommon.model.TO.rest.GetHyperlinkTO;
import com.orange.srs.statcommon.model.constant.H2Table;
import com.orange.srs.statcommon.model.parameter.GetPatternParameter;
import com.orange.srs.statcommon.model.parameter.GetPatternParameterList;
import com.orange.srs.statcommon.model.parameter.GetReportInputParameter;
import com.orange.srs.statcommon.model.parameter.GetReportInputParameterList;
import com.orange.srs.statcommon.model.parameter.PatternParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.report.ReportInputKeyParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.interceptor.Logged;
import com.orange.srs.statcommon.technical.jdbc.StatementWithColumnNameAdapter;

/**
 * Business Facade regarding every operations on business objects involved in report description
 * 
 * @author A129174
 *
 */
@Logged
@Stateless
public class SOA02ReportFacade {

	private static Logger logger = Logger.getLogger(SOA02ReportFacade.class);

	@EJB
	private ReportInputDAO reportInputDAO;

	@EJB
	private InputFormatDAO inputFormatDAO;

	@EJB
	private ReportDAO reportDAO;

	@EJB
	private IndicatorDAO indicatorDAO;

	@EJB
	private OfferOptionDAO offerOptionDAO;

	@EJB
	private ParamTypeDAO paramTypeDAO;

	@EJB
	private HyperlinkDAO hyperlinkDAO;

	@EJB
	private BookmarkDirectReportDAO bookmarkDirectReportDAO;

	@EJB
	private ExternalIndicatorDAO externalIndicatorDao;

	@EJB
	private ReportInputDelegate reportInputDelegate;

	public ReportInputKeyTOList getAvailableReports() {
		ReportInputKeyTOList result = new ReportInputKeyTOList();
		List<ReportInputKeyTO> resultlist = reportInputDAO.findAvailableReportInputKeys();
		result.getInputKeys().addAll(resultlist);
		return result;
	}

	public ReportInputLocationAndFormatTO getReportInput(ReportInputKeyParameter parameter) throws BusinessException {
		ReportInput input = reportInputDelegate.getReportInputByKey(parameter);
		return buildReportInputAndColumnsTOFromReportInput(input);
	}

	private ReportInputLocationAndFormatTO buildReportInputAndColumnsTOFromReportInput(ReportInput reportInput) {

		ReportInputLocationAndFormatTO to = new ReportInputLocationAndFormatTO();
		if (reportInput.getTableDb() != null && reportInput.getTypeDb() != null) {
			TableDefinitionTO table = new TableDefinitionTO();
			table.tableDb = reportInput.getTableDb();
			table.typeDb = reportInput.getTypeDb();
			to.tableDefinition = table;
		}

		LocationTO location = new LocationTO();
		location.locationPatternPrefix = reportInput.getLocationPatternPrefix();
		location.locationPatternSuffix = reportInput.getLocationPatternSuffix();
		to.location = location;
		to.format = reportInput.getFormat().getFormatType();
		for (InputColumn column : reportInput.getFormat().getColumns()) {
			ReportInputColumnTO columnTO = new ReportInputColumnTO();
			columnTO.alias = column.getAlias();
			columnTO.columnName = column.getColumnName();
			columnTO.comments = column.getComments();
			columnTO.dataFormat = column.getDataFormat();
			columnTO.type = column.getType();
			columnTO.defaultValue = column.getDefaultValue();
			to.reportInputColumns.add(columnTO);
		}

		return to;
	}

	/**
	 * @param parameter
	 *            parameters defining the granularity, the sourceTimeUnit, the statType required
	 * @return A list of pattern associated with their tableAlias and their format (alias, columnName, dataFormat)
	 *         Resolve pattern according to the parameter, unicity on granularity/sourceTimeUnit/statType
	 */
	public GetPatternTOList getPatterns(GetPatternParameterList parameterList) {
		GetPatternTOList result = new GetPatternTOList();
		result.getPatternTOs = new ArrayList<GetPatternTO>();

		for (GetPatternParameter parameter : parameterList.patternParameterList) {
			GetPatternTO tmpTO = new GetPatternTO();
			tmpTO.key = parameter.key;

			tmpTO.columns = new ArrayList<InputColumnTO>();

			String[] attributes = { "reportInputRef", "granularity", "sourceTimeUnit" };
			String[] values = { parameter.reportInputRef, parameter.granularity, parameter.sourceTimeUnit };

			List<ReportInput> reportInputs = reportInputDAO.findByMultipleCriteria(attributes, values);
			if (reportInputs.size() != 1) {
				if (logger.isDebugEnabled()) {
					logger.debug("[getPatterns] Problem while getting ReportInput with (" + Utils.join(attributes, ",")
							+ ") = (" + Utils.join(values, ",") + ")");
				}

				throw reportInputs.size() == 0 ? new StatInputException(StatInputException.NO_SUCH_STAT_INPUT)
						: new StatInputException(
								StatInputException.STATINPUT_UNICITY_CONSTRAINT + " size=" + reportInputs.size());
			}

			ReportInput input = reportInputs.get(0);

			StatPattern pattern = new StatPattern();
			pattern.patternPrefix = input.getLocationPatternPrefix();
			pattern.patternSuffix = input.getLocationPatternSuffix();
			pattern.table = input.getTableDb();
			pattern.typeDB = input.getTypeDb();

			tmpTO.pattern = pattern;

			if (input.getFormat() == null) {
				throw new StatInputException(input.getPk(), StatInputException.STATINPUT_NULL_FORMAT_EXCEPTION);
			} else if (input.getFormat().getColumns() == null || input.getFormat().getColumns().size() == 0) {
				throw new StatInputException(input.getPk(), StatInputException.STATINPUT_NULL_OR_NO_COLUMN_EXCEPTION);
			}

			for (InputColumn col : input.getFormat().getColumns()) {
				InputColumnTO colTo = new InputColumnTO();
				colTo.alias = col.getAlias();
				colTo.dataFormat = col.getDataFormat();
				colTo.columnName = col.getColumnName();
				colTo.type = col.getType();
				colTo.defaultValue = col.getDefaultValue();
				tmpTO.columns.add(colTo);
			}
			if (logger.isTraceEnabled()) {
				logger.trace("[getPatterns] Size " + tmpTO.columns.size());
			}
			result.getPatternTOs.add(tmpTO);
		}

		return result;
	}

	public GetReportsTO getReports() {
		GetReportsTO getReportsTO = new GetReportsTO();
		List<ReportTO> listReports = new ArrayList<ReportTO>();
		List<Report> result = reportDAO.findAll();
		for (Report report : result) {

			ReportTO reportTO = new ReportTO();
			reportTO.refId = report.getRefId();
			reportTO.label = report.getLabel();
			reportTO.computeUri = report.getComputeUri();
			reportTO.reportTimeUnit = report.getReportTimeUnit();
			listReports.add(reportTO);
		}
		getReportsTO.reports = listReports;
		return getReportsTO;
	}

	public GetReportsAndOfferOptionTO getReportsAndOfferOption() {
		GetReportsAndOfferOptionTO getReportsAndOfferOptionTO = new GetReportsAndOfferOptionTO();
		List<ReportAndOfferOptionTO> listReports = new ArrayList<ReportAndOfferOptionTO>();
		List<ReportRefIdAndOfferOptionTO> result = reportDAO.findAllReportsWithOfferOptionTO();
		for (ReportRefIdAndOfferOptionTO report : result) {

			ReportAndOfferOptionTO reportAndOfferOptionTO = new ReportAndOfferOptionTO();
			reportAndOfferOptionTO.refId = report.getRefId();
			reportAndOfferOptionTO.granularity = report.getGranularity();
			reportAndOfferOptionTO.offerOption = report.getOfferOption();
			reportAndOfferOptionTO.outputType = report.getOutputType();
			listReports.add(reportAndOfferOptionTO);
		}
		getReportsAndOfferOptionTO.reports = listReports;
		return getReportsAndOfferOptionTO;
	}

	public GetReportInputTO getReportInputs() {
		GetReportInputTO getReportInputTO = new GetReportInputTO();
		getReportInputTO.reportInputs = reportInputDAO.findAllReportInput();

		return getReportInputTO;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ExportInventoryReportInputsStatusTO exportH2ReportInputs(SOAContext soaContext)
			throws SQLException, ClassNotFoundException, BusinessException, IOException {

		logger.debug("[exportH2ReportInputs] Start");
		Long start = Utils.getTime();
		List<String> reportInputPatternFileLocations = null;

		// getReportInputs();
		List<ReportInput> reportInputList = reportInputDAO.findAll();
		// GetReportInputTO reportInputs=getReportInputs();

		List<InputFormat> inputFormatList = inputFormatDAO.findAll();

		ExportInventoryReportInputsStatusTO exportInventoryReportInputsStatusTO = new ExportInventoryReportInputsStatusTO();

		JDBCH2Consumer reportInputPatternConsumer = null;
		boolean h2ConnectionError = true;
		PatternParameter patternParam = new PatternParameter();
		patternParam.properties = Configuration.mountConfiguration;
		reportInputPatternFileLocations = DataLocator
				.getFirstReportInputPatternFileLocationWithoutExtension(patternParam);
		int reportInputPatternFileLocationsIndex = reportInputPatternFileLocations.size() - 1;

		String resultFileLocation = reportInputPatternFileLocations.get(reportInputPatternFileLocationsIndex);

		do {
			try {

				// resultFileLocation=determineExportReportInputFile(baseResultFileLocation);

				logger.debug("[exportH2ReportInputs] -> Location " + resultFileLocation);

				reportInputPatternConsumer = new JDBCH2Consumer();
				reportInputPatternConsumer.createConnexion(
						resultFileLocation + Configuration.h2FileReadwriteLockOptionsProperty, false, false);

				Long startRequestReportInput = Utils.getTime();
				StatementWithColumnNameAdapter statementWithColumnNameAdapterForReportInputTable = reportInputDelegate
						.makeReportInputTableWithConfiguration(reportInputPatternConsumer);
				exportInventoryReportInputsStatusTO.reportInputRequestTime = Utils.getTime() - startRequestReportInput;

				Long startRequestInputFormat = Utils.getTime();
				StatementWithColumnNameAdapter statementWithColumnNameAdapterForInputFormatTable = reportInputDelegate
						.makeInputColumnTableWithConfiguration(reportInputPatternConsumer);
				exportInventoryReportInputsStatusTO.inputFormatrRequestTime = Utils.getTime() - startRequestInputFormat;

				int commitIndex = 0;
				Long currentTableNextPk = 0L;

				for (InputFormat inputFormat : inputFormatList) {
					if (logger.isTraceEnabled()) {
						logger.trace(
								"[exportH2reportInputPattern] processing InputFormat : statementWithColumnNameAdapteForInputFormatTable="
										+ inputFormat.getPk());
					}
					exportInventoryReportInputsStatusTO.amountOfInputFormats++;
					for (InputColumn inputcolumn : inputFormat.getColumns()) {
						exportInventoryReportInputsStatusTO.amountOfInputColumns++;
						statementWithColumnNameAdapterForInputFormatTable.setValue(
								BDStatCommonFunction.makePk(H2Table.TABLE_NAME_REPORT_INPUT), currentTableNextPk);
						statementWithColumnNameAdapterForInputFormatTable
								.setValue(H2Table.InputColumns.COLUMN_REPORT_INPUT_KEY, inputFormat.getFormatType());
						statementWithColumnNameAdapterForInputFormatTable
								.setValue(H2Table.InputColumns.COLUMN_COLUMN_ALIAS, inputcolumn.getAlias());
						statementWithColumnNameAdapterForInputFormatTable
								.setValue(H2Table.InputColumns.COLUMN_COLUMN_DATA_FORMAT, inputcolumn.getDataFormat());
						statementWithColumnNameAdapterForInputFormatTable
								.setValue(H2Table.InputColumns.COLUMN_COLUMN_NAME, inputcolumn.getColumnName());
						statementWithColumnNameAdapterForInputFormatTable
								.setValue(H2Table.InputColumns.COLUMN_COLUMN_TYPE, inputcolumn.getType());
						statementWithColumnNameAdapterForInputFormatTable.setValue(
								H2Table.InputColumns.COLUMN_COLUMN_DEFAULT_VALUE, inputcolumn.getDefaultValue());

						statementWithColumnNameAdapterForInputFormatTable.execute();
						commitIndex++;
						if (commitIndex % 100 == 0) {
							commitIndex = 0;
							reportInputPatternConsumer.commit();
						}

					}

				}

				for (ReportInput reportInput : reportInputList) {
					if (logger.isTraceEnabled()) {
						logger.trace("[exportH2reportInputPattern] processing ReportInputPattern : ReportInputRef="
								+ reportInput.getReportInputRef());
					}
					exportInventoryReportInputsStatusTO.amountOfReportInputs++;
					statementWithColumnNameAdapterForReportInputTable.setValue(H2Table.ReportInput.COLUMN_KEY,
							reportInput.getReportInputRef() + "_" + reportInput.getGranularity() + "_"
									+ reportInput.getSourceTimeUnit());
					statementWithColumnNameAdapterForReportInputTable
							.setValue(BDStatCommonFunction.makePk(H2Table.TABLE_NAME_REPORT_INPUT), currentTableNextPk);
					statementWithColumnNameAdapterForReportInputTable.setValue(H2Table.ReportInput.COLUMN_FORMAT_TYPE,
							reportInput.getFormat().getFormatType());
					statementWithColumnNameAdapterForReportInputTable.setValue(H2Table.ReportInput.COLUMN_GRANULARITY,
							reportInput.getGranularity());
					statementWithColumnNameAdapterForReportInputTable.setValue(
							H2Table.ReportInput.COLUMN_PATTERN_PREFIX, reportInput.getLocationPatternPrefix());
					statementWithColumnNameAdapterForReportInputTable.setValue(
							H2Table.ReportInput.COLUMN_PATTERN_SUFFIX, reportInput.getLocationPatternSuffix());
					statementWithColumnNameAdapterForReportInputTable.setValue(H2Table.ReportInput.COLUMN_TABLE,
							reportInput.getTableDb());
					statementWithColumnNameAdapterForReportInputTable.setValue(H2Table.ReportInput.COLUMN_TYPE_DB,
							reportInput.getTypeDb());
					statementWithColumnNameAdapterForReportInputTable
							.setValue(H2Table.ReportInput.COLUMN_SOURCE_TIME_UNIT, reportInput.getSourceTimeUnit());

					statementWithColumnNameAdapterForReportInputTable.execute();
					commitIndex++;
					if (commitIndex % 100 == 0) {
						commitIndex = 0;
						reportInputPatternConsumer.commit();
					}
				}
				reportInputPatternConsumer.commit();

				h2ConnectionError = false;
			} catch (JdbcSQLException jdbcsqle) {
				if (JDBCH2Consumer.H2_FILE_READONLY_DATABASE_ERROR_CODES.contains(jdbcsqle.getErrorCode())
						&& reportInputPatternFileLocationsIndex > 0) {
					h2ConnectionError = true;
					reportInputPatternFileLocationsIndex--;
					resultFileLocation = reportInputPatternFileLocations.get(reportInputPatternFileLocationsIndex);
					if (logger.isDebugEnabled()) {
						logger.debug(SOATools.buildSOALogMessage(soaContext,
								"H2 connection error (" + jdbcsqle.getErrorCode()
										+ ") accessing H2 Table reportInputPatternFile: " + jdbcsqle.getMessage()));
					}
				} else {
					logger.error(SOATools.buildSOALogMessage(soaContext,
							"Export reportInputPatterns: error (" + jdbcsqle.getMessage() + ")", jdbcsqle));
					throw jdbcsqle;
				}

			} catch (Exception e) {
				logger.error(SOATools.buildSOALogMessage(soaContext,
						"Export reportInputPatterns: error (" + e.getMessage() + ")", e));
				h2ConnectionError = false;
			} finally {
				Long end = Utils.getTime();
				logger.info(SOATools.buildSOALogMessage(soaContext,
						"Export reportInputPatterns in " + (end - start) + " ms"));

				if (reportInputPatternConsumer != null) {
					try {
						reportInputPatternConsumer.closeConnexion();
					} catch (Exception e) {
						logger.warn(SOATools.buildSOALogMessage(soaContext,
								"Export ReportInputPatterns: closing ReportInputPattern connexion failed ("
										+ e.getMessage() + ")",
								e));
					}
				}
			}
		} while (h2ConnectionError);

		Long end = Utils.getTime();
		exportInventoryReportInputsStatusTO.overAllTime = end - start;

		return exportInventoryReportInputsStatusTO;
	}

	public GetInputFormatFullTO getInputFormat(String formatType) {
		GetInputFormatFullTO getInputFormatFullTO = new GetInputFormatFullTO();
		List<InputColumnFullTO> listColumns = new ArrayList<InputColumnFullTO>();
		List<InputFormat> result = inputFormatDAO.findBy(InputFormat.FIELD_FORMAT_TYPE, formatType);
		if (result.size() == 1) {
			for (InputColumn inputColumn : result.get(0).getColumns()) {
				InputColumnFullTO inputColumnFull = new InputColumnFullTO();
				inputColumnFull.alias = inputColumn.getAlias();
				inputColumnFull.columnName = inputColumn.getColumnName();
				inputColumnFull.dataFormat = inputColumn.getDataFormat();
				inputColumnFull.type = inputColumn.getType();
				inputColumnFull.defaultValue = inputColumn.getDefaultValue();
				inputColumnFull.comments = inputColumn.getComments();
				listColumns.add(inputColumnFull);
			}
		}
		getInputFormatFullTO.inputColumns = listColumns;
		return getInputFormatFullTO;
	}

	/**
	 * @param parameterList
	 *            : list of reportInput key attributes ("reportInputRef","granularity","sourceTimeUnit")
	 * @return the list of matching reportInputs
	 */
	public GetReportInputTO getReportInputByKeys(GetReportInputParameterList parameterList) {
		GetReportInputTO getReportInputTO = new GetReportInputTO();
		getReportInputTO.reportInputs = new ArrayList<>();

		for (GetReportInputParameter parameter : parameterList.reportInputParameterList) {

			String[] attributes = { ReportInput.FIELD_REPORT_INPUT_REF, ReportInput.FIELD_GRANULARITY,
					ReportInput.FIELD_SOURCE_TIME_UNIT };
			String[] values = { parameter.reportInputRef, parameter.granularity, parameter.sourceTimeUnit };

			List<ReportInput> reportInputs = new ArrayList<ReportInput>();
			reportInputs = reportInputDAO.findByMultipleCriteria(attributes, values);

			// check that result size is 1 : the parameters are the key of row in database
			if (reportInputs.size() != 1) {
				if (logger.isDebugEnabled()) {
					logger.debug("[getStatInput] Problem while getting ReportInput with (" + Utils.join(attributes, ",")
							+ ") = (" + Utils.join(values, ",") + ")");
				}

				throw reportInputs.size() == 0 ? new StatInputException(StatInputException.NO_SUCH_STAT_INPUT)
						: new StatInputException(
								StatInputException.STATINPUT_UNICITY_CONSTRAINT + " size=" + reportInputs.size());
			}

			ReportInput reportInput = reportInputs.get(0);

			// build TO from entity
			ReportInputTO reportInputTO = new ReportInputTO();
			reportInputTO.format = reportInput.getFormat().getFormatType();
			reportInputTO.granularity = reportInput.getGranularity();
			reportInputTO.locationPatternPrefix = reportInput.getLocationPatternPrefix();
			reportInputTO.locationPatternSuffix = reportInput.getLocationPatternSuffix();
			reportInputTO.source = reportInput.getSource();
			reportInputTO.sourceTimeUnit = reportInput.getSourceTimeUnit();
			reportInputTO.reportInputRef = reportInput.getReportInputRef();

			// add to TO result List
			getReportInputTO.reportInputs.add(reportInputTO);
		}
		return getReportInputTO;
	}

	/**
	 * Create a hyperlink object in the database
	 * 
	 * @param hyperlinkParam
	 * @param soaContext
	 * @return
	 * @throws BusinessException
	 */
	public HyperlinkTO createHyperlink(CreateHyperlinkParameter hyperlinkParam, SOAContext soaContext)
			throws BusinessException, IllegalArgumentException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[createHyperlink]: Start"));

		if (hyperlinkParam == null) {
			throw new IllegalArgumentException("HyperlinkParameter must be specified");
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[createHyperlink]: Getting the Matching SRSId"));

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[createHyperlink]: creating label: " + hyperlinkParam.getLabel() + " for offer option "
							+ hyperlinkParam.getOfferOption() + " and indicator " + hyperlinkParam.getIndicator()));
		}

		List<Indicator> indicators = indicatorDAO.findBy(Indicator.FIELD_INDICATORID, hyperlinkParam.getIndicator());
		if (indicators.size() != 1)
			throw new BusinessException(" Invalid hyperlink: indicator " + hyperlinkParam.getIndicator()
					+ " does not exist or is multiple in database");

		List<OfferOption> offerOptions = offerOptionDAO.findBy(OfferOption.FIELD_ALIAS,
				hyperlinkParam.getOfferOption());
		if (offerOptions.size() != 1)
			throw new BusinessException(" Invalid hyperlink: offerOption " + hyperlinkParam.getOfferOption()
					+ " does not exist or is multiple in database");

		List<Indicator> indicatorTemp = new ArrayList<>();
		for (ReportConfig reportConfig : offerOptions.get(0).getReportConfigs()) {
			indicatorTemp.addAll(reportConfig.getIndicators());
		}
		if (!indicatorTemp.contains(indicators.get(0))) {
			throw new BusinessException(" Invalid hyperlink: Indicator " + hyperlinkParam.getIndicator()
					+ " is not configured for offerOption " + hyperlinkParam.getOfferOption());
		}

		List<ParamType> paramTypeList = paramTypeDAO.findBy(ParamType.FIELD_ALIAS, hyperlinkParam.getTypeAlias());
		if (paramTypeList.size() != 1)
			throw new BusinessException(" Invalid hyperlink: paramType " + hyperlinkParam.getTypeAlias()
					+ " does not exist or is multiple in database");

		Hyperlink hyperlink = new Hyperlink();

		if (hyperlinkParam.getAdditionalTypeAlias() != null) {
			List<ParamType> additionalParamTypeList = paramTypeDAO.findBy(ParamType.FIELD_ALIAS,
					hyperlinkParam.getAdditionalTypeAlias());
			if (additionalParamTypeList.size() != 1)
				throw new BusinessException(" Invalid hyperlink: additionalParamType "
						+ hyperlinkParam.getAdditionalTypeAlias() + " does not exist or is multiple in database");
			hyperlink.setAdditionalParamType(additionalParamTypeList.get(0));
		}

		hyperlink.setIndicator(indicators.get(0));
		hyperlink.setOfferOption(offerOptions.get(0));
		hyperlink.setParamType(paramTypeList.get(0));
		hyperlinkDAO.persistAndFlush(hyperlink);

		HyperlinkTO result = new HyperlinkTO();
		result.labelKey = hyperlink.getLabel();

		logger.info(SOATools.buildSOALogMessage(soaContext, "[createHyperlink]: End"));
		return result;
	}

	/**
	 * Update a hyperlink objet in the database. note: you can only update label, granularity, report time unit and
	 * indicator
	 * 
	 * @param hyperlinkParam
	 * @param soaContext
	 * @return
	 * @throws BusinessException
	 */
	public HyperlinkTO updateHyperlink(CreateHyperlinkParameter hyperlinkParam, SOAContext soaContext)
			throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[updateBookmark]: Start"));

		String label;
		if (hyperlinkParam == null) {
			throw new IllegalArgumentException("Hyperlink label must be specified");
		} else {
			label = hyperlinkParam.getLabel();
			if (label == null) {
				throw new IllegalArgumentException("Hyperlink Label must be valid");
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[updateHyperlink]: updating Hyperlink: " + hyperlinkParam.getLabel()));
		}

		List<Hyperlink> hyperlinkList = hyperlinkDAO.findBy(Hyperlink.FIELD_LABEL, label);
		if (hyperlinkList.size() != 1) {
			throw new BusinessException(
					" Invalid hyperlink: indicator " + label + " does not exist or is multiple in database");
		}
		Hyperlink hyperlink = hyperlinkList.get(0);

		if (hyperlink == null) {
			throw new BusinessException("Hyperlink " + label + " does not exist or is multiple in database");
		}

		List<Indicator> indicators = indicatorDAO.findBy(Indicator.FIELD_INDICATORID, hyperlinkParam.getIndicator());
		if (indicators.size() != 1)
			throw new BusinessException(" Invalid hyperlink: indicator " + hyperlinkParam.getIndicator()
					+ " does not exist or is multiple in database");

		List<ParamType> paramTypeList = paramTypeDAO.findBy(ParamType.FIELD_ALIAS, hyperlinkParam.getTypeAlias());
		if (paramTypeList.size() != 1)
			throw new BusinessException(" Invalid hyperlink: paramType " + hyperlinkParam.getTypeAlias()
					+ " does not exist or is multiple in database");

		if (hyperlinkParam.getAdditionalTypeAlias() != null) {
			List<ParamType> additionalParamTypeList = paramTypeDAO.findBy(ParamType.FIELD_ALIAS,
					hyperlinkParam.getAdditionalTypeAlias());
			if (additionalParamTypeList.size() != 1)
				throw new BusinessException(" Invalid hyperlink: AdditionalParamType "
						+ hyperlinkParam.getAdditionalTypeAlias() + " does not exist or is multiple in database");
			hyperlink.setAdditionalParamType(additionalParamTypeList.get(0));

		}

		hyperlink.setIndicator(indicators.get(0));
		hyperlink.setParamType(paramTypeList.get(0));

		hyperlinkDAO.persistAndFlush(hyperlink);

		HyperlinkTO result = new HyperlinkTO();
		result.labelKey = hyperlink.getLabel();

		logger.info(SOATools.buildSOALogMessage(soaContext, "[updateBookmark]: End"));
		return result;
	}

	/**
	 * Delete the hyperlink identified by the label passed in parameter
	 * 
	 * @param label
	 * @param soaContext
	 * @throws BusinessException
	 */
	public void deleteHyperlink(String label, SOAContext soaContext) throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[deleteHyperlink]: Start"));
		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[deleteHyperlink]: deleting hyperlink: " + label));
		}

		if (label == null) {
			throw new IllegalArgumentException("Hyperlink label " + label + " must be valid");
		}

		List<Hyperlink> hyperlinkList = hyperlinkDAO.findBy(Hyperlink.FIELD_LABEL, label);
		if (hyperlinkList.size() != 1) {
			throw new BusinessException(
					" Invalid hyperlink: indicator " + label + " does not exist or is multiple in database");
		}
		Hyperlink hyperlinkToRemove = hyperlinkList.get(0);

		if (hyperlinkToRemove == null) {
			throw new BusinessException("Failed to delete hyperlink :" + label + ", does not exist in database");
		}

		hyperlinkDAO.remove(hyperlinkToRemove);

		logger.info(SOATools.buildSOALogMessage(soaContext, "[deleteHyperlink]: End"));
	}

	/**
	 * Retrieve the bookmark information for the bookmarkId passed in parameter
	 * 
	 * @param bookmarkId
	 * @param soaContext
	 * @return GetBookmarkTO the bookmark information
	 * @throws BusinessException
	 */
	public GetHyperlinkTO getHyperlink(String label, SOAContext soaContext) throws BusinessException {

		logger.debug(SOATools.buildSOALogMessage(soaContext, "[getHyperlink]: Start getting Hyperlink: " + label));

		if (label == null) {
			throw new IllegalArgumentException("Hyperlink label " + label + " must be valid");
		}
		logger.debug(SOATools.buildSOALogMessage(soaContext, "[getHyperlink]: Start Find by label"));

		List<Hyperlink> hyperlinkList = hyperlinkDAO.findBy(Hyperlink.FIELD_LABEL, label);
		logger.debug(SOATools.buildSOALogMessage(soaContext, "[getHyperlink]: End Find by label"));
		if (hyperlinkList.size() != 1) {
			throw new BusinessException(
					" Invalid hyperlink: indicator " + label + " does not exist or is multiple in database");
		}
		Hyperlink hyperlinkToReturn = hyperlinkList.get(0);

		if (hyperlinkToReturn == null) {
			throw new BusinessException(" Failed to get hyperlink :" + label + ", does not exist in database");
		}

		GetHyperlinkTO result;
		result = new GetHyperlinkTO(hyperlinkToReturn.getIndicator().getIndicatorId(),
				hyperlinkToReturn.getOfferOption().getAlias(), hyperlinkToReturn.getParamType().getAlias(), label,
				hyperlinkToReturn.getAdditionalParamType() != null
						? hyperlinkToReturn.getAdditionalParamType().getAlias()
						: null);

		logger.debug(SOATools.buildSOALogMessage(soaContext, "[getHyperlink]: End"));
		return result;
	}

	/**
	 * Retrieve the bookmark information for the label passed in parameter
	 * 
	 * @param entitySubType
	 * @param entityType
	 * @param bookmarkId
	 * @param soaContext
	 * @return GetBookmarkTO the bookmark information
	 * @throws BusinessException
	 */
	public GetBookmarkDirectReportTO getBookmarkDirectReport(String label, String entityType, String entitySubType,
			SOAContext soaContext) throws BusinessException {

		logger.debug(SOATools.buildSOALogMessage(soaContext,
				"[getBookmarkDirectReport]: Start getting BookmarkDirectReport: " + label));

		if (label == null) {
			throw new IllegalArgumentException("BookmarkDirectReport label " + label + " must be valid");
		}
		logger.debug(SOATools.buildSOALogMessage(soaContext, "[getBookmarkDirectReport]: Start Find by label"));
		List<BookmarkDirectReport> bookmarkDirectReportList = bookmarkDirectReportDAO
				.findByLabelAndEntityTypeAndSubtype(label, entityType, entitySubType);
		logger.debug(SOATools.buildSOALogMessage(soaContext, "[getBookmarkDirectReport]: End Find by label"));

		if (bookmarkDirectReportList.size() != 1) {
			throw new BusinessException(" Invalid bookmarkDirectReport: label=" + label + ", entityType=" + entityType
					+ ", entitySubType=" + entitySubType + " does not exist or is multiple in database");
		}
		BookmarkDirectReport bookmarkDirectReportToReturn = bookmarkDirectReportList.get(0);

		if (bookmarkDirectReportToReturn == null) {
			throw new BusinessException(
					" Failed to get bookmarkDirectReport :" + label + ", does not exist in database");
		}

		GetBookmarkDirectReportTO result;
		String min = null;
		String max = null;
		if (bookmarkDirectReportToReturn.getHierarchy() != null) {
			Iterator<String> hierarchy = Arrays
					.asList(StringUtils.splitPreserveAllTokens(bookmarkDirectReportToReturn.getHierarchy(), ";"))
					.iterator();
			String nextElement = null;
			if (hierarchy.hasNext()) {
				nextElement = hierarchy.next();
				min = nextElement.trim().isEmpty() ? null : nextElement;
				if (hierarchy.hasNext()) {
					nextElement = hierarchy.next();
					max = nextElement.trim().isEmpty() ? null : nextElement;
				}
			}
		}

		result = new GetBookmarkDirectReportTO(bookmarkDirectReportToReturn.getIndicator().getIndicatorId(),
				bookmarkDirectReportToReturn.getOfferOption().getAlias(),
				bookmarkDirectReportToReturn.getParamType().getAlias(), bookmarkDirectReportToReturn.getLabel(),
				bookmarkDirectReportToReturn.getAdditionalParamType() != null
						? bookmarkDirectReportToReturn.getAdditionalParamType().getAlias()
						: null,
				min, max);

		logger.debug(SOATools.buildSOALogMessage(soaContext, "[getBookmarkDirectReport]: End"));
		return result;
	}

	public ExternalIndicatorTO getExternalIndicator(SOAContext soaContext, String label) throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getExternalIndicator]: Start"));
		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[getExternalIndicator]: getting ExternalIndicator: " + label));
		}

		if (label == null) {
			throw new BusinessException(BusinessException.WRONG_PARAMETER_EXCEPTION + ": label can not be null",
					BusinessException.WRONG_PARAMETER_EXCEPTION_CODE);
		}

		List<ExternalIndicatorTO> externalIndicatorTOs = externalIndicatorDao.getExternalIndicatorTOForLabel(label);
		if (externalIndicatorTOs.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + " with label " + label,
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION_CODE);
		} else if (externalIndicatorTOs.size() > 1) {
			throw new BusinessException(BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION + " for label " + label,
					BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION_CODE);
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getExternalIndicator]: End"));
		return externalIndicatorTOs.get(0);
	}

	public ReportInputCassandraTOList getCassandraReportInput(SOAContext soaContext) throws BusinessException {
		logger.info(SOATools.buildSOALogMessage(soaContext, "[getCassandraReportInput]: Start"));
		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[getCassandraReportInput]: getting CassandraReportInput: "));
		}
		ReportInputCassandraTOList results = new ReportInputCassandraTOList();

		List<ReportInput> cassandraReportInputs = null;
		cassandraReportInputs = reportInputDAO.findCassandraReportInput();

		for (ReportInput ri : cassandraReportInputs) {

			ReportInputCassandraTO ricto = new ReportInputCassandraTO();

			ricto.granularity = ri.getGranularity();
			ricto.locationPatternPrefix = ri.getLocationPatternPrefix();
			ricto.locationPatternSuffix = ri.getLocationPatternSuffix();
			ricto.reportInputRef = ri.getReportInputRef();
			ricto.source = ri.getSource();
			ricto.sourceTimeUnit = ri.getSourceTimeUnit();
			ricto.tableDb = ri.getTableDb();
			ricto.typeDb = ri.getTypeDb();
			results.reportInputCassandraTOs.add(ricto);
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getCassandraReportInput]: End"));
		return results;
	}
}
