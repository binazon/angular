package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

public abstract class AbstractDifferentialUpdateTemplateMethod<ProvisioningTO, ModelObject, KeyObject extends Comparable<KeyObject>> {

	protected static final Logger LOGGER = Logger.getLogger(AbstractDifferentialUpdateTemplateMethod.class);

	protected static boolean FORCE_UPDATE_FROM_FILE_TO_DATABASE = false;

	protected abstract void sortProvisioningTOs(List<ProvisioningTO> provisioningTOs);

	protected abstract List<ModelObject> getModelObjectsSorted() throws BusinessException;

	protected abstract void initForChecks();

	protected abstract void checkProvisioningTOsData(List<ProvisioningTO> provisioningTOs) throws BusinessException;

	protected abstract void processFunctionalCreationChecks(ProvisioningTO provisioningTO) throws BusinessException;

	protected abstract KeyObject getProvisioningTOKey(ProvisioningTO provisioningTO);

	protected abstract KeyObject getModelObjectKey(ModelObject modelObject);

	protected abstract Boolean getSuppressFlag(ProvisioningTO provisioningTO);

	protected abstract void removeModelObject(ModelObject modelObject) throws BusinessException;

	protected abstract boolean updateModelObjectIfNecessary(SOAContext soaContext, ModelObject modelObject,
			ProvisioningTO provisioningTO) throws BusinessException;

	protected abstract void createModelObjectFromProvisioningTO(SOAContext soaContext, ProvisioningTO provisioningTO)
			throws BusinessException;

	protected abstract String getInfoModelObjectForLog();

	protected abstract String getEndLogMessage();

	public boolean updateByDifferential(SOAContext soaContext, List<ProvisioningTO> provisioningTOs,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {

		FORCE_UPDATE_FROM_FILE_TO_DATABASE = forceUpdateFromFileToDatabase;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "[updateByDifferential] Start" + getEndLogMessage()));
		}

		int nbCreated = 0;
		int nbUpdated = 0;
		int nbRemoved = 0;
		KeyObject provisioningTOKey = null;
		KeyObject modelObjectKey = null;
		try {

			// Sort both lists that will be compared
			sortProvisioningTOs(provisioningTOs);
			List<ModelObject> modelObjects = getModelObjectsSorted();

			initForChecks();
			checkProvisioningTOsData(provisioningTOs);

			KeyObject previousProvisioningTOKey = null;
			boolean checkDuplicates = false;
			int provisioningTOIndex = 0;
			int modelObjectIndex = 0;

			while (provisioningTOIndex < provisioningTOs.size() && modelObjectIndex < modelObjects.size()) {

				// Retrieve objects and keys
				ProvisioningTO provisioningTO = provisioningTOs.get(provisioningTOIndex);
				ModelObject modelObject = modelObjects.get(modelObjectIndex);
				provisioningTOKey = getProvisioningTOKey(provisioningTO);
				modelObjectKey = getModelObjectKey(modelObject);

				// Check for duplicate entry in provisioningTOs
				if (checkDuplicates && (provisioningTOKey.compareTo(previousProvisioningTOKey) == 0)) {
					throw new BusinessException(
							"Duplicate " + getInfoModelObjectForLog() + " entry " + provisioningTOKey);
				}
				previousProvisioningTOKey = provisioningTOKey;
				checkDuplicates = false;

				// Compare the objects
				int keyComparisonResult = provisioningTOKey.compareTo(modelObjectKey);
				if (keyComparisonResult == 0) {
					// Both in XML and base:
					if (Boolean.TRUE.equals(getSuppressFlag(provisioningTO))) {
						removeModelObject(modelObject);
						nbRemoved++;
					} else {
						boolean hasBeenUpdated = updateModelObjectIfNecessary(soaContext, modelObject, provisioningTO);
						if (hasBeenUpdated) {
							nbUpdated++;
						}
						checkDuplicates = true;
					}
					provisioningTOIndex++;
					modelObjectIndex++;
				} else if (keyComparisonResult < 0) {
					// In XML not in base:
					if (!Boolean.TRUE.equals(getSuppressFlag(provisioningTO))) {
						processFunctionalCreationChecks(provisioningTO);
						createModelObjectFromProvisioningTO(soaContext, provisioningTO);
						nbCreated++;
						checkDuplicates = true;
					}
					provisioningTOIndex++;
				} else {
					if (FORCE_UPDATE_FROM_FILE_TO_DATABASE) {
						removeModelObject(modelObject);
						nbRemoved++;
					}
					// In base not in XML: do nothing
					modelObjectIndex++;

				}
			}

			while (modelObjectIndex < modelObjects.size()) {
				if (FORCE_UPDATE_FROM_FILE_TO_DATABASE) {
					// Retrieve objects and keys
					ModelObject modelObject = modelObjects.get(modelObjectIndex);
					removeModelObject(modelObject);
					nbRemoved++;
				}

				modelObjectIndex++;
			}

			while (provisioningTOIndex < provisioningTOs.size()) {

				// Retrieve objects and keys
				ProvisioningTO provisioningTO = provisioningTOs.get(provisioningTOIndex);
				provisioningTOKey = getProvisioningTOKey(provisioningTO);

				// Check for duplicate entry in provisioningTOs
				if (checkDuplicates && provisioningTOKey.equals(previousProvisioningTOKey)) {
					throw new BusinessException(
							"Duplicate " + getInfoModelObjectForLog() + " entry with key[" + provisioningTOKey + "]");
				}
				previousProvisioningTOKey = provisioningTOKey;
				checkDuplicates = false;

				// Create if necessary
				if (!Boolean.TRUE.equals(getSuppressFlag(provisioningTO))) {
					processFunctionalCreationChecks(provisioningTO);
					createModelObjectFromProvisioningTO(soaContext, provisioningTO);
					nbCreated++;
					checkDuplicates = true;
				}
				provisioningTOIndex++;
			}
		} catch (BusinessException be) {
			throw be;
		} catch (Exception e) {
			throw new BusinessException("[updateByDifferential] Error in " + getInfoModelObjectForLog()
					+ " provisioning" + getEndLogMessage() + " for provisioning key[" + provisioningTOKey
					+ "] or DB key [" + modelObjectKey, e);
		}

		String logMessage = "[updateByDifferential] Number of " + getInfoModelObjectForLog() + " to be created:"
				+ nbCreated + ", to be updated:" + nbUpdated + ", to be deleted:" + nbRemoved + " at transaction commit"
				+ getEndLogMessage();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, logMessage));
		return nbCreated != 0 || nbUpdated != 0 || nbRemoved != 0;
	}
}
