package com.orange.srs.refreport.business.templatemethod;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;

import com.orange.srs.refreport.business.delegate.InputSourceDelegate;
import com.orange.srs.refreport.model.InputSource;
import com.orange.srs.refreport.model.SourceClass;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.report.InputSourceKey;
import com.orange.srs.statcommon.model.TO.report.InputSourceTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class InputSourceDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<InputSourceTO, InputSource, InputSourceKey> {

	@EJB
	private InputSourceDelegate inputSourceDelegate;

	private SourceClass sourceClass;

	public void setSourceClass(SourceClass sourceClass) {
		this.sourceClass = sourceClass;
	}

	@Override
	protected void sortProvisioningTOs(List<InputSourceTO> inputSourceTOs) {
		InputSourceDelegate.sortInputSourceTO(inputSourceTOs);
	}

	@Override
	protected List<InputSource> getModelObjectsSorted() {
		// Create a copy of the list used for the differential update: to avoid
		// automatic delete of the model object from this list deleting this model
		// object from the session
		return new ArrayList<InputSource>(InputSourceDelegate.getAllInputSourceSortedForSourceClass(sourceClass));
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<InputSourceTO> inputSourceTOs) throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(InputSourceTO inputSourceTO) throws BusinessException {
		inputSourceDelegate.checkInputSourceUnicity(inputSourceTO);
	}

	@Override
	protected InputSourceKey getProvisioningTOKey(InputSourceTO inputSourceTO) {
		return InputSourceDelegate.getInputSourceTOKey(inputSourceTO);
	}

	@Override
	protected InputSourceKey getModelObjectKey(InputSource inputSource) {
		return InputSourceDelegate.getInputSourceKey(inputSource);
	}

	@Override
	protected Boolean getSuppressFlag(InputSourceTO inputSourceProvisioningTO) {
		return inputSourceProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(InputSource inputSource) throws BusinessException {
		try {
			inputSourceDelegate.removeInputSource(inputSource);
		} catch (JAXBException jaxbe) {
			throw new BusinessException(jaxbe);
		}
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, InputSource inputSource,
			InputSourceTO inputSourceTO) throws BusinessException {
		try {
			return inputSourceDelegate.updateInputSourceIfNecessary(inputSource, inputSourceTO);
		} catch (JAXBException jaxbe) {
			throw new BusinessException(jaxbe);
		}
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext, InputSourceTO inputSourceTO)
			throws BusinessException {
		try {
			inputSourceDelegate.createInputSource(inputSourceTO);
		} catch (JAXBException jaxbe) {
			throw new BusinessException(jaxbe);
		}
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "inputSource";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
