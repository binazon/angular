package com.orange.srs.refreport.business;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.delegate.AttributeDelegate;
import com.orange.srs.refreport.model.parameter.EntityToEntityAttributeParameterList;
import com.orange.srs.refreport.model.parameter.GroupEntityAttributeParameterList;
import com.orange.srs.statcommon.model.parameter.GroupAttributeParameterList;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.interceptor.Logged;

@Logged
@Stateless
public class SOA17AttributFacade {

	private static final Logger LOGGER = Logger.getLogger(SOA17AttributFacade.class);

	@EJB
	private AttributeDelegate attributeDelegate;

	public GroupEntityAttributeParameterList getGroupEntityAttributes(SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getGroupEntityAttributes] Start"));
		Long start = Utils.getTime();
		GroupEntityAttributeParameterList result = new GroupEntityAttributeParameterList();
		result.groupEntityAttributes.addAll(attributeDelegate.getAttributesToEntityAndGroup(soaContext));
		Long end = Utils.getTime();
		LOGGER.info(
				SOATools.buildSOALogMessage(soaContext, "[getGroupEntityAttributes] end in " + (end - start) + " ms."));
		return result;
	}

	public GroupEntityAttributeParameterList getGroupEntityAttributes(String origin, String reportingGroupRef,
			SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getGroupEntityAttributes] Start"));
		Long start = Utils.getTime();
		GroupEntityAttributeParameterList result = new GroupEntityAttributeParameterList();
		result.groupEntityAttributes
				.addAll(attributeDelegate.getAttributesToEntityAndGroup(origin, reportingGroupRef, soaContext));
		Long end = Utils.getTime();
		LOGGER.info(
				SOATools.buildSOALogMessage(soaContext, "[getGroupEntityAttributes] end in " + (end - start) + " ms."));
		return result;
	}

	public GroupEntityAttributeParameterList getGroupEntityAttributesByEntity(String entityId, SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getGroupEntityAttributesByEntity] Start"));
		Long start = Utils.getTime();
		GroupEntityAttributeParameterList result = new GroupEntityAttributeParameterList();
		result.groupEntityAttributes
				.addAll(attributeDelegate.getAttributesToEntityAndGroupByEntity(entityId, soaContext));
		Long end = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[getGroupEntityAttributesByEntity] end in " + (end - start) + " ms."));
		return result;
	}

	public GroupEntityAttributeParameterList getGroupEntityAttributesByGroupAndEntity(String origin,
			String reportingGroupRef, String entityId, SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getGroupEntityAttributesByGroupAndEntity] Start"));
		Long start = Utils.getTime();
		GroupEntityAttributeParameterList result = new GroupEntityAttributeParameterList();
		result.groupEntityAttributes.addAll(attributeDelegate.getAttributesToEntityAndGroupByGroupAndEntity(origin,
				reportingGroupRef, entityId, soaContext));
		Long end = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[getGroupEntityAttributesByGroupAndEntity] end in " + (end - start) + " ms."));
		return result;
	}

	public GroupAttributeParameterList getGroupAttributes(SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getGroupAttributes] Start"));
		Long start = Utils.getTime();
		GroupAttributeParameterList result = new GroupAttributeParameterList();
		result.groupAttributeParameters.addAll(attributeDelegate.getAttributesToGroup(soaContext));
		Long end = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getGroupAttributes] end in " + (end - start) + " ms."));
		return result;
	}

	public GroupAttributeParameterList getGroupAttributes(String origin, String reportingGroupRef,
			SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getGroupAttributes] Start"));
		Long start = Utils.getTime();
		GroupAttributeParameterList result = new GroupAttributeParameterList();
		result.groupAttributeParameters
				.addAll(attributeDelegate.getAttributesToGroup(origin, reportingGroupRef, soaContext));
		Long end = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getGroupAttributes] end in " + (end - start) + " ms."));
		return result;
	}

	public EntityToEntityAttributeParameterList getEntityToEntityAttributes(SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getEntityToEntityAttributes] Start"));
		Long start = Utils.getTime();
		EntityToEntityAttributeParameterList result = new EntityToEntityAttributeParameterList();
		result.entityToEntityAttributes.addAll(attributeDelegate.getEntityToEntityAttributes(soaContext));
		Long end = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[getEntityToEntityAttributes] end in " + (end - start) + " ms."));
		return result;
	}

	public EntityToEntityAttributeParameterList getEntityToEntityAttributesByEntitySource(String entityId,
			SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getEntityToEntityAttributesByEntitySource] Start"));
		Long start = Utils.getTime();
		EntityToEntityAttributeParameterList result = new EntityToEntityAttributeParameterList();
		result.entityToEntityAttributes
				.addAll(attributeDelegate.getEntityToEntityAttributesByEntitySource(entityId, soaContext));
		Long end = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[getEntityToEntityAttributesByEntitySource] end in " + (end - start) + " ms."));
		return result;
	}

	public EntityToEntityAttributeParameterList getEntityToEntityAttributesByEntityDest(String entityId,
			SOAContext soaContext) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[getEntityToEntityAttributesByEntityDest] Start"));
		Long start = Utils.getTime();
		EntityToEntityAttributeParameterList result = new EntityToEntityAttributeParameterList();
		result.entityToEntityAttributes
				.addAll(attributeDelegate.getEntityToEntityAttributesByEntityDest(entityId, soaContext));
		Long end = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[getEntityToEntityAttributesByEntityDest] end in " + (end - start) + " ms."));
		return result;
	}
}
