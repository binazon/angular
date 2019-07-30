package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.EntityTypeAndSubtypeDelegate;
import com.orange.srs.refreport.model.EntityTypeAndSubtype;
import com.orange.srs.refreport.model.EntityTypeAndSubtypeId;
import com.orange.srs.refreport.model.TO.provisioning.SubtypeProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class EntitySubtypeDifferentialUpdate extends
		AbstractDifferentialUpdateTemplateMethod<SubtypeProvisioningTO, EntityTypeAndSubtype, EntityTypeAndSubtypeId> {

	@EJB
	private EntityTypeAndSubtypeDelegate entityTypeAndSubtypeDelegate;

	private String entityType;

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	@Override
	protected void sortProvisioningTOs(List<SubtypeProvisioningTO> subtypeProvisioningTOs) {
		EntityTypeAndSubtypeDelegate.sortSubtypeProvisioningTO(entityType, subtypeProvisioningTOs);
	}

	@Override
	protected List<EntityTypeAndSubtype> getModelObjectsSorted() {
		return entityTypeAndSubtypeDelegate.getAllTypeSubtypeSortedForEntityType(entityType);
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<SubtypeProvisioningTO> subtypeProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(SubtypeProvisioningTO subtypeProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected EntityTypeAndSubtypeId getProvisioningTOKey(SubtypeProvisioningTO subtypeProvisioningTO) {
		return EntityTypeAndSubtypeDelegate.getSubtypeProvisioningTOKey(entityType, subtypeProvisioningTO);
	}

	@Override
	protected EntityTypeAndSubtypeId getModelObjectKey(EntityTypeAndSubtype entityTypeAndSubtype) {
		return EntityTypeAndSubtypeDelegate.getEntityTypeAndSubtypeKey(entityTypeAndSubtype);
	}

	@Override
	protected Boolean getSuppressFlag(SubtypeProvisioningTO subtypeProvisioningTO) {
		return subtypeProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(EntityTypeAndSubtype entityTypeAndSubtype) throws BusinessException {
		entityTypeAndSubtypeDelegate.removeEntityTypeAndSubtype(entityTypeAndSubtype);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, EntityTypeAndSubtype entityTypeAndSubtype,
			SubtypeProvisioningTO subtypeProvisioningTO) throws BusinessException {
		return entityTypeAndSubtypeDelegate.updateEntityTypeAndSubtypeIfNecessary(entityTypeAndSubtype,
				subtypeProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			SubtypeProvisioningTO subtypeProvisioningTO) throws BusinessException {
		entityTypeAndSubtypeDelegate.createEntityTypeAndSubtype(entityType, subtypeProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "entitySubtype";
	}

	@Override
	protected String getEndLogMessage() {
		return " for entityType " + entityType;
	}

}
