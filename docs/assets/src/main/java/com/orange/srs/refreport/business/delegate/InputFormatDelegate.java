package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.InputColumnDAO;
import com.orange.srs.refreport.consumer.dao.InputFormatDAO;
import com.orange.srs.refreport.model.InputColumn;
import com.orange.srs.refreport.model.InputFormat;
import com.orange.srs.refreport.model.TO.InputColumnKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.InputColumnListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.InputColumnProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class InputFormatDelegate {

	@EJB
	private InputFormatDAO inputFormatDao;

	@EJB
	private InputColumnDAO inputColumnDao;

	public static InputColumnKeyTO getInputColumnKey(InputColumn inputColumn) {
		return new InputColumnKeyTO(inputColumn.getInputFormat().getFormatType(), inputColumn.getColumnName(),
				inputColumn.getType());
	}

	public static InputColumnKeyTO getInputColumnProvisioningTOKey(String inputFormatType,
			InputColumnProvisioningTO inputColumnProvisioningTO) {
		return new InputColumnKeyTO(inputFormatType, inputColumnProvisioningTO.columnName,
				inputColumnProvisioningTO.type);
	}

	public InputFormat getInputFormatByKey(String inputFormatType) throws BusinessException {
		List<InputFormat> listInputFormat = inputFormatDao.findBy(InputFormat.FIELD_FORMAT_TYPE, inputFormatType);
		if (listInputFormat.isEmpty()) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION
					+ ": InputFormat with key [formatType=" + inputFormatType + "]");
		}
		return listInputFormat.get(0);
	}

	public InputFormat createInputFormatIfNotExist(String inputFormatType) {
		InputFormat inputFormatReturned;
		List<InputFormat> listInputFormat = inputFormatDao.findBy(InputFormat.FIELD_FORMAT_TYPE, inputFormatType);
		if (listInputFormat.isEmpty()) {
			InputFormat inputFormat = new InputFormat();
			inputFormat.setFormatType(inputFormatType);
			inputFormatDao.persistAndFlush(inputFormat);
			inputFormatReturned = inputFormat;
		} else {
			inputFormatReturned = listInputFormat.get(0);
		}
		return inputFormatReturned;
	}

	public InputColumn addInputColumnToInputFormat(InputColumnProvisioningTO inputColumnProvisioningTO,
			InputFormat inputFormat) {
		InputColumn inputColumn = new InputColumn();
		inputColumn.setColumnName(inputColumnProvisioningTO.columnName);
		inputColumn.setAlias(inputColumnProvisioningTO.alias);
		inputColumn.setDataFormat(inputColumnProvisioningTO.dataFormat);
		inputColumn.setComments(inputColumnProvisioningTO.comments);
		inputColumn.setType(inputColumnProvisioningTO.type);
		inputColumn.setDefaultValue(inputColumnProvisioningTO.defaultValue);

		inputFormat.getColumns().add(inputColumn);
		inputFormatDao.persistAndFlush(inputFormat);
		return inputColumn;
	}

	public boolean updateInputColumnIfNecessary(InputColumn inputColumn,
			InputColumnProvisioningTO inputColumnProvisioningTO) {

		boolean updated = false;

		if (areDifferent(inputColumn, inputColumnProvisioningTO)) {
			inputColumn.setAlias(inputColumnProvisioningTO.alias);
			inputColumn.setDataFormat(inputColumnProvisioningTO.dataFormat);
			inputColumn.setComments(inputColumnProvisioningTO.comments);
			inputColumn.setType(inputColumnProvisioningTO.type);
			inputColumn.setDefaultValue(inputColumnProvisioningTO.defaultValue);
			inputColumnDao.persistAndFlush(inputColumn);
			updated = true;
		}
		return updated;
	}

	private boolean areDifferent(InputColumn inputColumn, InputColumnProvisioningTO inputColumnProvisioningTO) {
		// Alias
		if (inputColumnProvisioningTO.alias == null && inputColumn.getAlias() != null) {
			return true;
		}
		if (inputColumnProvisioningTO.alias != null
				&& !inputColumnProvisioningTO.alias.equals(inputColumn.getAlias())) {
			return true;
		}
		// dataFormat
		if (inputColumnProvisioningTO.dataFormat == null && inputColumn.getDataFormat() != null) {
			return true;
		}
		if (inputColumnProvisioningTO.dataFormat != null
				&& !inputColumnProvisioningTO.dataFormat.equals(inputColumn.getDataFormat())) {
			return true;
		}
		// comments
		if (inputColumnProvisioningTO.comments == null && inputColumn.getComments() != null) {
			return true;
		}
		if (inputColumnProvisioningTO.comments != null
				&& !inputColumnProvisioningTO.comments.equals(inputColumn.getComments())) {
			return true;
		}
		// type
		if (inputColumnProvisioningTO.type == null && inputColumn.getType() != null) {
			return true;
		}
		if (inputColumnProvisioningTO.type != null && !inputColumnProvisioningTO.type.equals(inputColumn.getType())) {
			return true;
		}
		// defaultValue
		if (inputColumnProvisioningTO.defaultValue == null && inputColumn.getDefaultValue() != null) {
			return true;
		}
		if (inputColumnProvisioningTO.defaultValue != null
				&& !inputColumnProvisioningTO.defaultValue.equals(inputColumn.getDefaultValue())) {
			return true;
		}
		return false;

	}

	public void removeInputColumn(InputFormat inputFormat, InputColumn inputColumn) {
		inputFormat.getColumns().remove(inputColumn);
		inputFormatDao.persistAndFlush(inputFormat);
		inputColumnDao.remove(inputColumn);
	}

	public static List<InputColumn> getAllInputColumnSortedForInputFormat(InputFormat inputFormat) {
		List<InputColumn> inputColumns = inputFormat.getColumns();
		Collections.sort(inputColumns, new InputColumnForOneInputFormatComparator());
		return inputColumns;
	}

	public InputColumnListProvisioningTO getInputColumnListProvisioningTOSortedForInputFormat(String inputFormatType) {
		InputColumnListProvisioningTO inputColumnListProvisioningTO = new InputColumnListProvisioningTO();
		inputColumnListProvisioningTO.inputColumnProvisioningTOs = inputColumnDao
				.findAllInputColumnProvisioningTOForInputFormat(inputFormatType);
		sortInputColumnProvisioningTO(inputFormatType, inputColumnListProvisioningTO.inputColumnProvisioningTOs);
		return inputColumnListProvisioningTO;
	}

	public static void sortInputColumnProvisioningTO(String formatType,
			List<InputColumnProvisioningTO> inputColumnProvisioningTOs) {
		Collections.sort(inputColumnProvisioningTOs, new InputColumnProvisioningTOComparator(formatType));
	}

	private static class InputColumnForOneInputFormatComparator implements Comparator<InputColumn> {
		@Override
		public int compare(InputColumn firstObj, InputColumn secondObj) {
			return getInputColumnKey(firstObj).compareTo(getInputColumnKey(secondObj));
		}
	}

	private static class InputColumnProvisioningTOComparator implements Comparator<InputColumnProvisioningTO> {

		private String inputFormatType;

		public InputColumnProvisioningTOComparator(String inputFormatType) {
			this.inputFormatType = inputFormatType;
		}

		@Override
		public int compare(InputColumnProvisioningTO firstObj, InputColumnProvisioningTO secondObj) {
			return getInputColumnProvisioningTOKey(inputFormatType, firstObj)
					.compareTo(getInputColumnProvisioningTOKey(inputFormatType, secondObj));
		}
	}

}
