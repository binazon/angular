package com.orange.srs.refreport.business.delegate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.ReportInputDAO;
import com.orange.srs.refreport.model.InputFormat;
import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.SourceClass;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputKeyProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.business.commonFunctions.BDStatCommonFunction;
import com.orange.srs.statcommon.consumer.jdbc.JDBCH2Consumer;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;
import com.orange.srs.statcommon.model.constant.H2Table;
import com.orange.srs.statcommon.model.parameter.ColumnParameter;
import com.orange.srs.statcommon.model.parameter.CreateTableParameter;
import com.orange.srs.statcommon.model.parameter.report.ReportInputKeyParameter;
import com.orange.srs.statcommon.technical.exception.TechnicalException;
import com.orange.srs.statcommon.technical.jdbc.IndexStatementBuilder;
import com.orange.srs.statcommon.technical.jdbc.StatementWithColumnNameAdapter;

@Stateless
public class ReportInputDelegate {

	@EJB
	private ReportInputDAO reportInputDao;

	@EJB
	private InputFormatDelegate inputFormatDelegate;

	public static ReportInputKeyTO getReportInputKey(ReportInput reportInput) {
		return new ReportInputKeyTO(reportInput.getReportInputRef(), reportInput.getGranularity(),
				reportInput.getSourceTimeUnit());
	}

	public static ReportInputKeyTO getReportInputProvisioningTOKey(
			ReportInputProvisioningTO reportInputProvisioningTO) {
		return new ReportInputKeyTO(reportInputProvisioningTO.reportInputRef, reportInputProvisioningTO.granularity,
				reportInputProvisioningTO.sourceTimeUnit);
	}

	public static ReportInputKeyTO getReportInputKeyProvisioningTOKey(
			ReportInputKeyProvisioningTO reportInputKeyProvisioningTO) {
		return new ReportInputKeyTO(reportInputKeyProvisioningTO.reportInputRef,
				reportInputKeyProvisioningTO.granularity, reportInputKeyProvisioningTO.sourceTimeUnit);
	}

	public ReportInput getReportInputByKey(String reportInputRef, String granularity, String sourceTimeUnit)
			throws BusinessException {
		ReportInputKeyParameter keyParameter = new ReportInputKeyParameter();
		keyParameter.reportInputRef = reportInputRef;
		keyParameter.granularity = granularity;
		keyParameter.sourceTimeUnit = sourceTimeUnit;
		return getReportInputByKey(keyParameter);
	}

	public ReportInput getReportInputByKey(ReportInputKeyParameter keyParameter) throws BusinessException {
		List<ReportInput> reportInputs = reportInputDao.findReportInputByKey(keyParameter);
		if (reportInputs.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": " + keyParameter);
		} else if (reportInputs.size() > 1) {
			throw new BusinessException(BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION + ": " + keyParameter);
		}
		return reportInputs.get(0);
	}

	public ReportInput createReportInput(ReportInputProvisioningTO reportInputProvisioningTO) throws BusinessException {
		ReportInput reportInput = new ReportInput();
		reportInput.setReportInputRef(reportInputProvisioningTO.reportInputRef);
		reportInput.setGranularity(reportInputProvisioningTO.granularity);
		reportInput.setSourceTimeUnit(reportInputProvisioningTO.sourceTimeUnit);
		reportInput.setLocationPatternPrefix(reportInputProvisioningTO.locationPatternPrefix);
		reportInput.setLocationPatternSuffix(reportInputProvisioningTO.locationPatternSuffix);
		reportInput.setSource(reportInputProvisioningTO.source);
		reportInput.setTypeDb(reportInputProvisioningTO.typeDb);
		reportInput.setTableDb(reportInputProvisioningTO.tableDb);
		InputFormat inputFormat = inputFormatDelegate.getInputFormatByKey(reportInputProvisioningTO.formatType);
		reportInput.setFormat(inputFormat);
		reportInputDao.persistAndFlush(reportInput);
		return reportInput;
	}

	public boolean updateReportInputIfNecessary(ReportInput reportInput,
			ReportInputProvisioningTO reportInputProvisioningTO) throws BusinessException {
		boolean updated = false;

		String inputFormatTypeCurrent = reportInput.getFormat().getFormatType();
		boolean locationPatternPrefixHasChanged = reportInputProvisioningTO.locationPatternPrefix != null
				&& !reportInputProvisioningTO.locationPatternPrefix.equals(reportInput.getLocationPatternPrefix())
				|| reportInput.getLocationPatternPrefix() != null && !reportInput.getLocationPatternPrefix()
						.equals(reportInputProvisioningTO.locationPatternPrefix);
		boolean locationPatternSuffisHasChanged = reportInputProvisioningTO.locationPatternSuffix != null
				&& !reportInputProvisioningTO.locationPatternSuffix.equals(reportInput.getLocationPatternSuffix())
				|| reportInput.getLocationPatternSuffix() != null && !reportInput.getLocationPatternSuffix()
						.equals(reportInputProvisioningTO.locationPatternSuffix);

		if (locationPatternPrefixHasChanged || locationPatternSuffisHasChanged
				|| !reportInputProvisioningTO.source.equals(reportInput.getSource())
				|| !reportInputProvisioningTO.typeDb.equals(reportInput.getTypeDb())
				|| !reportInputProvisioningTO.tableDb.equals(reportInput.getTableDb())
				|| !reportInputProvisioningTO.formatType.equals(inputFormatTypeCurrent)) {

			reportInput.setLocationPatternPrefix(reportInputProvisioningTO.locationPatternPrefix);
			reportInput.setLocationPatternSuffix(reportInputProvisioningTO.locationPatternSuffix);
			reportInput.setSource(reportInputProvisioningTO.source);
			reportInput.setTypeDb(reportInputProvisioningTO.typeDb);
			reportInput.setTableDb(reportInputProvisioningTO.tableDb);
			InputFormat inputFormat = inputFormatDelegate.getInputFormatByKey(reportInputProvisioningTO.formatType);
			reportInput.setFormat(inputFormat);
			reportInputDao.persistAndFlush(reportInput);
			updated = true;
		}
		return updated;
	}

	public void removeReportInput(ReportInput reportInput) {
		reportInputDao.remove(reportInput);
	}

	public List<ReportInput> getAllReportInputSorted() {
		List<ReportInput> reportInputList = reportInputDao.findAll();
		sortReportInput(reportInputList);
		return reportInputList;
	}

	public ReportInputListProvisioningTO getReportInputListProvisioningTOSorted() {
		ReportInputListProvisioningTO reportInputListProvisioningTO = new ReportInputListProvisioningTO();
		reportInputListProvisioningTO.reportInputProvisioningTOs = reportInputDao.findAllReportInputProvisioningTO();
		sortReportInputProvisioningTO(reportInputListProvisioningTO.reportInputProvisioningTOs);
		return reportInputListProvisioningTO;
	}

	public static List<ReportInput> getAllReportInputSortedForSourceClass(SourceClass sourceClass) {
		List<ReportInput> reportInputList = sourceClass.getProducedInputs();
		ReportInputDelegate.sortReportInput(reportInputList);
		return reportInputList;
	}

	public static void sortReportInput(List<ReportInput> reportInputs) {
		Collections.sort(reportInputs, new ReportInputComparator());
	}

	public static void sortReportInputProvisioningTO(List<ReportInputProvisioningTO> reportInputProvisioningTOs) {
		Collections.sort(reportInputProvisioningTOs, new ReportInputProvisioningTOComparator());
	}

	public static void sortReportInputKeyProvisioningTO(
			List<ReportInputKeyProvisioningTO> reportInputKeyProvisioningTOs) {
		Collections.sort(reportInputKeyProvisioningTOs, new ReportInputKeyProvisioningTOComparator());
	}

	private static class ReportInputComparator implements Comparator<ReportInput> {
		@Override
		public int compare(ReportInput firstObj, ReportInput secondObj) {
			return getReportInputKey(firstObj).compareTo(getReportInputKey(secondObj));
		}
	}

	private static class ReportInputProvisioningTOComparator implements Comparator<ReportInputProvisioningTO> {
		@Override
		public int compare(ReportInputProvisioningTO firstObj, ReportInputProvisioningTO secondObj) {
			return getReportInputProvisioningTOKey(firstObj).compareTo(getReportInputProvisioningTOKey(secondObj));
		}
	}

	private static class ReportInputKeyProvisioningTOComparator implements Comparator<ReportInputKeyProvisioningTO> {

		@Override
		public int compare(ReportInputKeyProvisioningTO firstObj, ReportInputKeyProvisioningTO secondObj) {
			return getReportInputKeyProvisioningTOKey(firstObj)
					.compareTo(getReportInputKeyProvisioningTOKey(secondObj));
		}
	}

	public StatementWithColumnNameAdapter makeReportInputTableWithConfiguration(
			JDBCH2Consumer reportInputPatternConsumer) throws SQLException {
		StatementWithColumnNameAdapter statement = createH2ReportInputTable(reportInputPatternConsumer);

		// Add reportInputRef index
		String[] columnsToIndex = new String[] { H2Table.ReportInput.COLUMN_FORMAT_TYPE,
				H2Table.ReportInput.COLUMN_GRANULARITY };
		IndexStatementBuilder indexesBuilder = new IndexStatementBuilder(reportInputPatternConsumer.getConnection(),
				H2Table.TABLE_NAME_REPORT_INPUT, columnsToIndex);
		indexesBuilder.executeStatement();

		return statement;
	}

	public StatementWithColumnNameAdapter makeInputColumnTableWithConfiguration(
			JDBCH2Consumer reportInputPatternConsumer) throws SQLException {
		StatementWithColumnNameAdapter statement = createH2InputPatternTable(reportInputPatternConsumer);

		// Add reportInputRef index
		String[] columnsToIndex = new String[] { H2Table.InputColumns.COLUMN_REPORT_INPUT_KEY };
		IndexStatementBuilder indexesBuilder = new IndexStatementBuilder(reportInputPatternConsumer.getConnection(),
				H2Table.TABLE_NAME_INPUT_COLUMN, columnsToIndex);
		indexesBuilder.executeStatement();

		return statement;
	}

	private StatementWithColumnNameAdapter createH2ReportInputTable(JDBCH2Consumer reportInputPatternConsumer)
			throws TechnicalException, SQLException {
		List<ColumnParameter> reportInputColumnParameters = createReportInputTableColumns();
		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_REPORT_INPUT;

		cparameter.columns.addAll(reportInputColumnParameters);

		ColumnParameter pkColumn = new ColumnParameter();
		pkColumn.columnName = BDStatCommonFunction.makePk(cparameter.tableName);
		pkColumn.columnType = JDBCH2Consumer.BIGINT_TYPE;
		cparameter.pkColumn = pkColumn;

		StatementWithColumnNameAdapter statement = reportInputPatternConsumer.createTableAndStatementByName(cparameter,
				false, true);

		return statement;
	}

	private StatementWithColumnNameAdapter createH2InputPatternTable(JDBCH2Consumer reportInputPatternConsumer)
			throws TechnicalException, SQLException {
		List<ColumnParameter> inputPatternColumnParameters = createInputPatternTableColumns();
		CreateTableParameter cparameter = new CreateTableParameter();
		cparameter.tableName = H2Table.TABLE_NAME_INPUT_COLUMN;

		cparameter.columns.addAll(inputPatternColumnParameters);

		ColumnParameter pkColumn = new ColumnParameter();
		pkColumn.columnName = BDStatCommonFunction.makePk(cparameter.tableName);
		pkColumn.columnType = JDBCH2Consumer.BIGINT_TYPE;
		cparameter.pkColumn = pkColumn;

		StatementWithColumnNameAdapter statement = reportInputPatternConsumer.createTableAndStatementByName(cparameter,
				false, true);

		return statement;
	}

	private List<ColumnParameter> createReportInputTableColumns() {
		List<ColumnParameter> listColumns = new ArrayList<ColumnParameter>();

		ColumnParameter key = new ColumnParameter();
		key.columnName = H2Table.ReportInput.COLUMN_KEY;
		key.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter sourceTimeUnit = new ColumnParameter();
		sourceTimeUnit.columnName = H2Table.ReportInput.COLUMN_SOURCE_TIME_UNIT;
		sourceTimeUnit.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter formatType = new ColumnParameter();
		formatType.columnName = H2Table.ReportInput.COLUMN_FORMAT_TYPE;
		formatType.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter granularity = new ColumnParameter();
		granularity.columnName = H2Table.ReportInput.COLUMN_GRANULARITY;
		granularity.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter patternPrefix = new ColumnParameter();
		patternPrefix.columnName = H2Table.ReportInput.COLUMN_PATTERN_PREFIX;
		patternPrefix.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;

		ColumnParameter patternSuffix = new ColumnParameter();
		patternSuffix.columnName = H2Table.ReportInput.COLUMN_PATTERN_SUFFIX;
		patternSuffix.columnType = JDBCH2Consumer.VARCHAR_255_TYPE;

		ColumnParameter table = new ColumnParameter();
		table.columnName = H2Table.ReportInput.COLUMN_TABLE;
		table.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter typeDB = new ColumnParameter();
		typeDB.columnName = H2Table.ReportInput.COLUMN_TYPE_DB;
		typeDB.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		listColumns.add(key);
		listColumns.add(granularity);
		listColumns.add(formatType);
		listColumns.add(patternPrefix);
		listColumns.add(patternSuffix);
		listColumns.add(table);
		listColumns.add(typeDB);
		listColumns.add(sourceTimeUnit);

		return listColumns;
	}

	private List<ColumnParameter> createInputPatternTableColumns() {
		List<ColumnParameter> listColumns = new ArrayList<ColumnParameter>();

		ColumnParameter alias = new ColumnParameter();
		alias.columnName = H2Table.InputColumns.COLUMN_COLUMN_ALIAS;
		alias.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter dataFormat = new ColumnParameter();
		dataFormat.columnName = H2Table.InputColumns.COLUMN_COLUMN_DATA_FORMAT;
		dataFormat.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter name = new ColumnParameter();
		name.columnName = H2Table.InputColumns.COLUMN_COLUMN_NAME;
		name.columnType = JDBCH2Consumer.VARCHAR_1000_TYPE;

		ColumnParameter type = new ColumnParameter();
		type.columnName = H2Table.InputColumns.COLUMN_COLUMN_TYPE;
		type.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter defaultValue = new ColumnParameter();
		defaultValue.columnName = H2Table.InputColumns.COLUMN_COLUMN_DEFAULT_VALUE;
		defaultValue.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		ColumnParameter reportInputKey = new ColumnParameter();
		reportInputKey.columnName = H2Table.InputColumns.COLUMN_REPORT_INPUT_KEY;
		reportInputKey.columnType = JDBCH2Consumer.VARCHAR_TYPE;

		listColumns.add(reportInputKey);
		listColumns.add(name);
		listColumns.add(type);
		listColumns.add(dataFormat);
		listColumns.add(alias);
		listColumns.add(defaultValue);

		return listColumns;
	}
}
