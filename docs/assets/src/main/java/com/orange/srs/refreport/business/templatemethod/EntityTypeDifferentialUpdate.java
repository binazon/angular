package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.orange.srs.refreport.business.delegate.EntityTypeAndSubtypeDelegate;
import com.orange.srs.refreport.model.TO.provisioning.TypeAndSubtypesProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class EntityTypeDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<TypeAndSubtypesProvisioningTO, String, String> {

	@Inject
	private EntitySubtypeDifferentialUpdate entitySubtypeDifferentialUpdate;

	@EJB
	private EntityTypeAndSubtypeDelegate entityTypeAndSubtypeDelegate;

	@Override
	protected void sortProvisioningTOs(List<TypeAndSubtypesProvisioningTO> typeAndSubtypesProvisioningTOs) {
		EntityTypeAndSubtypeDelegate.sortTypeAndSubtypesProvisioningTO(typeAndSubtypesProvisioningTOs);
	}

	@Override
	protected List<String> getModelObjectsSorted() {
		return entityTypeAndSubtypeDelegate.getAllEntityTypeSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<TypeAndSubtypesProvisioningTO> typeAndSubtypesProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(TypeAndSubtypesProvisioningTO typeAndSubtypesProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected String getProvisioningTOKey(TypeAndSubtypesProvisioningTO typeAndSubtypesProvisioningTO) {
		return EntityTypeAndSubtypeDelegate.getTypeAndSubtypesProvisioningTOKey(typeAndSubtypesProvisioningTO);
	}

	@Override
	protected String getModelObjectKey(String entityType) {
		return entityType;
	}

	@Override
	protected Boolean getSuppressFlag(TypeAndSubtypesProvisioningTO typeAndSubtypesProvisioningTO) {
		return typeAndSubtypesProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(String entityType) throws BusinessException {
		entityTypeAndSubtypeDelegate.removeAllEntityType(entityType);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, String entityType,
			TypeAndSubtypesProvisioningTO typeAndSubtypesProvisioningTO) throws BusinessException {
		entitySubtypeDifferentialUpdate.setEntityType(entityType);
		return entitySubtypeDifferentialUpdate.updateByDifferential(soaContext,
				typeAndSubtypesProvisioningTO.subtypeProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			TypeAndSubtypesProvisioningTO typeAndSubtypesProvisioningTO) throws BusinessException {
		entitySubtypeDifferentialUpdate.setEntityType(typeAndSubtypesProvisioningTO.type);
		entitySubtypeDifferentialUpdate.updateByDifferential(soaContext,
				typeAndSubtypesProvisioningTO.subtypeProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "entityType";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}
