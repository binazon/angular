package com.orange.srs.refreport.business.templatemethod;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.InputFormatDelegate;
import com.orange.srs.refreport.model.InputColumn;
import com.orange.srs.refreport.model.InputFormat;
import com.orange.srs.refreport.model.TO.InputColumnKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.InputColumnProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class InputColumnDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<InputColumnProvisioningTO, InputColumn, InputColumnKeyTO> {

	@EJB
	private InputFormatDelegate inputFormatDelegate;

	private InputFormat inputFormat;

	public void setInputFormat(InputFormat inputFormat) {
		this.inputFormat = inputFormat;
	}

	@Override
	protected void sortProvisioningTOs(List<InputColumnProvisioningTO> inputColumnProvisioningTOs) {
		InputFormatDelegate.sortInputColumnProvisioningTO(inputFormat.getFormatType(), inputColumnProvisioningTOs);
	}

	@Override
	protected List<InputColumn> getModelObjectsSorted() {
		// Create a copy of the list used for the differential update: to avoid
		// automatic delete of the model object from this list deleting this model
		// object from the session
		return new ArrayList<InputColumn>(InputFormatDelegate.getAllInputColumnSortedForInputFormat(inputFormat));
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<InputColumnProvisioningTO> inputColumnProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(InputColumnProvisioningTO inputColumnProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected InputColumnKeyTO getProvisioningTOKey(InputColumnProvisioningTO inputColumnProvisioningTO) {
		return InputFormatDelegate.getInputColumnProvisioningTOKey(inputFormat.getFormatType(),
				inputColumnProvisioningTO);
	}

	@Override
	protected InputColumnKeyTO getModelObjectKey(InputColumn inputColumn) {
		return InputFormatDelegate.getInputColumnKey(inputColumn);
	}

	@Override
	protected Boolean getSuppressFlag(InputColumnProvisioningTO inputColumnProvisioningTO) {
		return inputColumnProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(InputColumn inputColumn) {
		inputFormatDelegate.removeInputColumn(inputFormat, inputColumn);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, InputColumn inputColumn,
			InputColumnProvisioningTO inputColumnProvisioningTO) throws BusinessException {
		return inputFormatDelegate.updateInputColumnIfNecessary(inputColumn, inputColumnProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			InputColumnProvisioningTO inputColumnProvisioningTO) throws BusinessException {
		inputFormatDelegate.addInputColumnToInputFormat(inputColumnProvisioningTO, inputFormat);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "inputColumn";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
