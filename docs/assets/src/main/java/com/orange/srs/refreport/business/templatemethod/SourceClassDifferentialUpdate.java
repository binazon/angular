package com.orange.srs.refreport.business.templatemethod;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.orange.srs.refreport.business.delegate.SourceClassDelegate;
import com.orange.srs.refreport.model.SourceClass;
import com.orange.srs.refreport.model.TO.SourceClassKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputKeyProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.SourceClassProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class SourceClassDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<SourceClassProvisioningTO, SourceClass, SourceClassKeyTO> {

	@EJB
	private SourceClassDelegate sourceClassDelegate;

	@Inject
	private SourceClassReportInputLinkDifferentialUpdate sourceClassReportInputLinkDifferentialUpdate;

	private List<ReportInputKeyProvisioningTO> reportInputKeyProvisioningTOsForAllSourceClass;

	@Override
	protected void sortProvisioningTOs(List<SourceClassProvisioningTO> sourceClassProvisioningTOs) {
		SourceClassDelegate.sortSourceClassProvisioningTO(sourceClassProvisioningTOs);
	}

	@Override
	protected List<SourceClass> getModelObjectsSorted() {
		return sourceClassDelegate.getAllSourceClassSorted();
	}

	@Override
	protected void initForChecks() {
		reportInputKeyProvisioningTOsForAllSourceClass = new ArrayList<ReportInputKeyProvisioningTO>();
	}

	@Override
	protected void checkProvisioningTOsData(List<SourceClassProvisioningTO> sourceClassProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(SourceClassProvisioningTO sourceClassProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected SourceClassKeyTO getProvisioningTOKey(SourceClassProvisioningTO sourceClassProvisioningTO) {
		return SourceClassDelegate.getSourceClassProvisioningTOKey(sourceClassProvisioningTO);
	}

	@Override
	protected SourceClassKeyTO getModelObjectKey(SourceClass sourceClass) {
		return SourceClassDelegate.getSourceClassKey(sourceClass);
	}

	@Override
	protected Boolean getSuppressFlag(SourceClassProvisioningTO sourceClassProvisioningTO) {
		return sourceClassProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(SourceClass sourceClass) {
		sourceClassDelegate.removeSourceClass(sourceClass);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, SourceClass sourceClass,
			SourceClassProvisioningTO sourceClassTO) throws BusinessException {
		sourceClassReportInputLinkDifferentialUpdate.setSourceClass(sourceClass);
		sourceClassReportInputLinkDifferentialUpdate
				.setReportInputKeyProvisioningTOsAlreadyUsed(reportInputKeyProvisioningTOsForAllSourceClass);
		return sourceClassReportInputLinkDifferentialUpdate.updateByDifferential(soaContext,
				sourceClassTO.reportInputKeyProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext, SourceClassProvisioningTO sourceClassTO)
			throws BusinessException {
		SourceClass createdSourceClass = sourceClassDelegate.createSourceClass(sourceClassTO);
		sourceClassReportInputLinkDifferentialUpdate.setSourceClass(createdSourceClass);
		sourceClassReportInputLinkDifferentialUpdate
				.setReportInputKeyProvisioningTOsAlreadyUsed(reportInputKeyProvisioningTOsForAllSourceClass);
		sourceClassReportInputLinkDifferentialUpdate.updateByDifferential(soaContext,
				sourceClassTO.reportInputKeyProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "sourceClass";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
