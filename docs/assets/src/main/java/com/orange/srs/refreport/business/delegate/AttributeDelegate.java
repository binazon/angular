package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.orange.srs.refreport.consumer.dao.EntityGroupAttributeDAO;
import com.orange.srs.refreport.consumer.dao.EntityLinkAttributeDAO;
import com.orange.srs.refreport.consumer.dao.GroupAttributeDAO;
import com.orange.srs.refreport.model.parameter.EntityToEntityAttributeParameter;
import com.orange.srs.refreport.model.parameter.GroupEntityAttributeParameter;
import com.orange.srs.statcommon.model.parameter.GroupAttributeParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class AttributeDelegate {

	// private static final Logger LOGGER =
	// Logger.getLogger(AttributeDelegate.class);

	@EJB
	private EntityGroupAttributeDAO entityGroupAttributeDAO;

	@EJB
	private GroupAttributeDAO groupAttributeDAO;

	@EJB
	private EntityLinkAttributeDAO entityLinkAttributeDAO;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<GroupEntityAttributeParameter> getAttributesToEntityAndGroup(SOAContext soaContext) {
		List<GroupEntityAttributeParameter> result = entityGroupAttributeDAO.findAllGroupEntityAttributeParameter();
		Collections.sort(result);
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<GroupEntityAttributeParameter> getAttributesToEntityAndGroup(String origin, String reportingGroupRef,
			SOAContext soaContext) {
		List<GroupEntityAttributeParameter> result = entityGroupAttributeDAO
				.findGroupEntityAttributeParameterByGroup(origin, reportingGroupRef);
		Collections.sort(result);
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<GroupEntityAttributeParameter> getAttributesToEntityAndGroupByEntity(String entityId,
			SOAContext soaContext) {
		List<GroupEntityAttributeParameter> result = entityGroupAttributeDAO
				.findGroupEntityAttributeParameterByEntityId(entityId);
		Collections.sort(result);
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<GroupEntityAttributeParameter> getAttributesToEntityAndGroupByGroupAndEntity(String origin,
			String reportingGroupRef, String entityId, SOAContext soaContext) {
		List<GroupEntityAttributeParameter> result = entityGroupAttributeDAO
				.findGroupEntityAttributeParameterByGroupAndEntityId(origin, reportingGroupRef, entityId);
		Collections.sort(result);
		return result;
	}

	public List<GroupAttributeParameter> getAttributesToGroup(SOAContext soaContext) {
		List<GroupAttributeParameter> result = groupAttributeDAO.findAllGroupAttributeParameter();
		Collections.sort(result);
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<GroupAttributeParameter> getAttributesToGroup(String origin, String reportingGroupRef,
			SOAContext soaContext) {
		List<GroupAttributeParameter> result = groupAttributeDAO.findGroupAttributeParameterByGroup(origin,
				reportingGroupRef);
		Collections.sort(result);
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<EntityToEntityAttributeParameter> getEntityToEntityAttributes(SOAContext soaContext) {
		List<EntityToEntityAttributeParameter> result = entityLinkAttributeDAO
				.findAllEntityToEntityAttributeParameter();
		Collections.sort(result);
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<EntityToEntityAttributeParameter> getEntityToEntityAttributesByEntitySource(String entityId,
			SOAContext soaContext) {
		List<EntityToEntityAttributeParameter> result = entityLinkAttributeDAO
				.findEntityToEntityAttributeParameterByEntityIdSource(entityId);
		Collections.sort(result);
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<EntityToEntityAttributeParameter> getEntityToEntityAttributesByEntityDest(String entityId,
			SOAContext soaContext) {
		List<EntityToEntityAttributeParameter> result = entityLinkAttributeDAO
				.findEntityToEntityAttributeParameterByEntityIdDest(entityId);
		Collections.sort(result);
		return result;
	}

}
