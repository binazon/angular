package com.orange.srs.refreport.business;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.delegate.IndicatorDelegate;
import com.orange.srs.refreport.business.delegate.ReportUserDelegate;
import com.orange.srs.refreport.business.delegate.ReportingGroupDelegate;
import com.orange.srs.refreport.consumer.dao.BookmarkDAO;
import com.orange.srs.refreport.consumer.dao.IndicatorDAO;
import com.orange.srs.refreport.consumer.dao.OfferOptionDAO;
import com.orange.srs.refreport.consumer.dao.ParamTypeDAO;
import com.orange.srs.refreport.consumer.dao.ReportUserDAO;
import com.orange.srs.refreport.consumer.dao.ReportingEntityDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.model.EntityTypeAndSubtype;
import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.ReportUser;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.ReportsBookmark;
import com.orange.srs.refreport.model.TO.FavoriteTO;
import com.orange.srs.refreport.model.TO.ReportingEntityTypeTO;
import com.orange.srs.refreport.model.external.user.CreateReportUserParameter;
import com.orange.srs.refreport.model.parameter.CreateBookmarkParameter;
import com.orange.srs.refreport.model.parameter.UpdateBookmarkParameter;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refura.model.external.ReportGroupKeyTO;
import com.orange.srs.refura.model.external.UpdateUserReportingGroupParameter;
import com.orange.srs.statcommon.model.TO.rest.GetBookmarkTO;
import com.orange.srs.statcommon.model.TO.rest.GetEntitiesForTypeAndForReportingGroupTO;
import com.orange.srs.statcommon.model.TO.rest.GetOfferOptionByIndicatorTO;
import com.orange.srs.statcommon.model.TO.rest.GetOfferOptionTO;
import com.orange.srs.statcommon.model.TO.rest.GetParamTypeTO;
import com.orange.srs.statcommon.model.TO.rest.GetReportingEntityTO;
import com.orange.srs.statcommon.model.TO.rest.GetTypesForReportTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.interceptor.Logged;

@Logged
@Stateless
public class SOA04UserFacade {

	private static Logger logger = Logger.getLogger(SOA04UserFacade.class);

	/**
	 * Delegates and subsequent DAO
	 */

	@EJB
	private ReportingGroupDAO reportingGroupDAO;

	@EJB
	private ReportUserDAO reportUserDAO;

	@EJB
	private BookmarkDAO bookmarkDAO;

	@EJB
	private ReportingEntityDAO reportingEntityDAO;

	@EJB
	private IndicatorDAO indicatorDAO;

	@EJB
	private OfferOptionDAO offerOptionDAO;

	@EJB
	private ParamTypeDAO paramTypeDAO;

	@EJB
	private IndicatorDelegate indicatorDelegate;

	@EJB
	private ReportUserDelegate reportUserDelegate;

	@EJB
	private ReportingGroupDelegate reportingGroupDelegate;

	/**
	 * Create a reportUser object in the database
	 * 
	 * @param reportUserParam
	 * @throws BusinessException
	 */
	public void createReportUser(CreateReportUserParameter reportUserParam, SOAContext soaContext)
			throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[createReportUser]: Start"));

		if (reportUserParam == null) {
			throw new IllegalArgumentException("ReportUserId must be specified");
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[createReportUser] creating following ReportUser: "
					+ Long.toString(reportUserParam.getReportUserId())));
		}
		ReportUser reportUser = new ReportUser();
		reportUser.setPk(reportUserParam.getReportUserId());
		reportUserDAO.merge(reportUser);
		reportUserDAO.flush();

		logger.info(SOATools.buildSOALogMessage(soaContext, "[createReportUser]: End"));
	}

	/**
	 * Create a bookmark object in the database
	 * 
	 * @param bookmarkParam
	 * @param soaContext
	 * @return
	 * @throws BusinessException
	 */
	public FavoriteTO createBookmark(CreateBookmarkParameter bookmarkParam, SOAContext soaContext)
			throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[createBookmark]: Start"));

		if (bookmarkParam == null) {
			throw new IllegalArgumentException("BookmarkParameter must be specified");
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[createBookmark]: Getting the Matching SRSId"));
		long reportUserId = bookmarkParam.getSrsId();

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[createBookmark]: creating Bookmark: " + bookmarkParam.getBookmarkId() + " for reportUser "
							+ Long.toString(reportUserId) + ", reportingGroup " + bookmarkParam.getReportingGroupName()
							+ " with origin " + bookmarkParam.getOrigin() + ", reportingEntity "
							+ bookmarkParam.getReportingEntityId() + ", indicator " + bookmarkParam.getIndicator()
							+ " and filterId " + bookmarkParam.getFilterId()));
		}

		ReportUser reportUser = reportUserDAO.findById(reportUserId);

		String attributes[] = { ReportingGroup.FIELD_REPORTING_GROUP_REF, ReportingGroup.FIELD_ORIGIN };
		Object values[] = { bookmarkParam.getReportingGroupName(), bookmarkParam.getOrigin() };
		List<ReportingGroup> reportingGroups = reportingGroupDAO.findByMultipleCriteria(attributes, values);

		if (reportUser == null)
			throw new BusinessException(
					" Invalid bookmark: reportUser " + Long.toString(reportUserId) + " does not exist in database");
		if (reportingGroups.size() != 1)
			throw new BusinessException(" Invalid bookmark: reportingGroup " + bookmarkParam.getReportingGroupName()
					+ " with origin " + bookmarkParam.getOrigin() + " does not exist or is multiple in database");

		if (!reportUser.getWorkingGroups().contains(reportingGroups.get(0)))
			throw new BusinessException(" Invalid bookmark, reportingGroup " + bookmarkParam.getReportingGroupName()
					+ " with origin " + bookmarkParam.getOrigin() + " does not match for reportUser "
					+ Long.toString(reportUserId));

		List<Indicator> indicators = indicatorDAO.findBy(Indicator.FIELD_INDICATORID, bookmarkParam.getIndicator());
		if (indicators.size() != 1)
			throw new BusinessException(" Invalid bookmark: indicator " + bookmarkParam.getIndicator()
					+ " does not exist or is multiple in database");

		List<OfferOption> offerOptions = offerOptionDAO.findBy(OfferOption.FIELD_ALIAS, bookmarkParam.getOfferOption());
		if (offerOptions.size() != 1)
			throw new BusinessException(" Invalid bookmark: offerOption " + bookmarkParam.getOfferOption()
					+ " does not exist or is multiple in database");

		List<Indicator> indicatorTemp = new ArrayList<>();
		for (ReportConfig reportConfig : offerOptions.get(0).getReportConfigs()) {
			indicatorTemp.addAll(reportConfig.getIndicators());
		}
		if (!indicatorTemp.contains(indicators.get(0))) {
			throw new BusinessException(" Invalid bookmark: Indicator " + bookmarkParam.getIndicator()
					+ " is not configured for offerOption " + bookmarkParam.getOfferOption());
		}

		ReportingEntity reportingEntity = null;
		ParamType entityParamType = null;

		if (bookmarkParam.getReportingEntityId() != null) {
			List<ReportingEntity> reportingEntities = reportingEntityDAO.findBy(ReportingEntity.FIELD_ENTITYID,
					bookmarkParam.getReportingEntityId());
			if (reportingEntities.size() != 1) {
				throw new BusinessException(" Invalid bookmark: reportingEntity " + bookmarkParam.getReportingEntityId()
						+ " does not exist or is multiple in database");
			}

			if (reportingEntities.get(0) != null) {
				reportingEntity = reportingEntities.get(0);
			}

			if (!reportingEntities.get(0).getReportingGroups().contains(reportingGroups.get(0))) {
				throw new BusinessException(" Invalid bookmark: reportingEntity " + bookmarkParam.getReportingEntityId()
						+ " does not belong to the reporting group " + bookmarkParam.getReportingGroupName());
			}

			// Check the current entity has one type/subtype defined in the list of possible
			// paramtype
			String entityType = reportingEntities.get(0).getEntityType();

			Set<EntityTypeAndSubtype> listSubtypesReportingEntity = reportingEntities.get(0).getSubtype();

			List<ParamType> paramTypeList = paramTypeDAO.findAll();
			for (ParamType paramType : paramTypeList) {
				for (EntityTypeAndSubtype typeAndSubtypeReportingEntity : listSubtypesReportingEntity) {
					if (entityType.equals(paramType.getEntityType())
							&& typeAndSubtypeReportingEntity.getSubtype().equals(paramType.getEntitySubtype())
							&& EntityTypeAndSubtype.ALL_TYPES_OR_SUBTYPES.equals(paramType.getParentType())
							&& EntityTypeAndSubtype.ALL_TYPES_OR_SUBTYPES.equals(paramType.getParentSubtype())) {
						entityParamType = paramType;
						break;
					}
				}
			}

			if (entityParamType == null) {
				throw new BusinessException(" Invalid bookmark: no ParamType found for reportingEntity "
						+ bookmarkParam.getReportingEntityId());
			}

		} else {
			entityParamType = paramTypeDAO.findSingleResultBy(ParamType.FIELD_ALIAS, "NETWORK");
		}

		// FILTERS
		Filter filterBookmarked = null;
		OfferOption offerOption = offerOptions.get(0);
		if (bookmarkParam.getFilterId() != null) {
			for (Filter filter : offerOption.getFilters()) {
				if (filter.getFilterId().equals(bookmarkParam.getFilterId())) {
					filterBookmarked = filter;
				}
			}

			if (filterBookmarked == null) {
				throw new BusinessException(
						" Invalid bookmark: no Filter found for filterId " + bookmarkParam.getFilterId());
			}
		} else {
			if (offerOption.getFilters().size() > 0) {
				filterBookmarked = offerOption.getFilters().get(0);
			}
		}

		ReportsBookmark bookmark = new ReportsBookmark();
		bookmark.setIndicator(indicators.get(0));
		bookmark.setReportingEntity(reportingEntity);
		bookmark.setReportingGroup(reportingGroups.get(0));
		bookmark.setReportUser(reportUser);
		bookmark.setGranularity(bookmarkParam.getGranularity());
		bookmark.setReportTimeUnit(bookmarkParam.getReportTimeUnit());
		bookmark.setOfferOption(offerOptions.get(0));
		bookmark.setParamType(entityParamType);
		bookmark.setFilter(filterBookmarked);
		bookmarkDAO.persistAndFlush(bookmark);

		FavoriteTO result = new FavoriteTO();
		result.favoriteKey = bookmark.getBookmarkId();

		logger.info(SOATools.buildSOALogMessage(soaContext, "[createBookmark]: End"));
		return result;
	}

	/**
	 * Get the offer options list which use the indicator for reporting group passed in parameter
	 * 
	 * @param indicatorId
	 * @param reportingGroupRef
	 * @param reportingGroupOrigin
	 * @param soaContext
	 * @return GetOfferByIndicatorTO
	 * @throws BusinessException
	 */
	public GetOfferOptionByIndicatorTO getOfferByIndicatorForGroup(String indicatorId, String reportingGroupRef,
			String reportingGroupOrigin, SOAContext soaContext) throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getOfferByIndicator]: Start"));

		GetOfferOptionByIndicatorTO getOfferByIndicatorTO = new GetOfferOptionByIndicatorTO();
		getOfferByIndicatorTO.indicatorId = indicatorId;
		getOfferByIndicatorTO.reportingGroupRef = reportingGroupRef;
		getOfferByIndicatorTO.origin = reportingGroupOrigin;

		String attributes[] = { ReportingGroup.FIELD_REPORTING_GROUP_REF, ReportingGroup.FIELD_ORIGIN };
		Object values[] = { reportingGroupRef, reportingGroupOrigin };
		List<ReportingGroup> reportingGroupList = reportingGroupDAO.findByMultipleCriteria(attributes, values);
		if (reportingGroupList == null || reportingGroupList.size() != 1) {
			throw new BusinessException("ReportingGroup " + reportingGroupRef + "/" + reportingGroupOrigin
					+ " does not exist or is multiple in database");
		}
		List<OfferOption> groupOptions = reportingGroupList.get(0).getReportSourceOptions();

		Set<Long> offerOptionForReportConfigs = new HashSet<>();
		List<ReportConfig> reportConfigs = indicatorDelegate.getIndicatorByKey(indicatorId).getReportConfigs();
		for (ReportConfig reportConfig : reportConfigs) {
			offerOptionForReportConfigs.add(reportConfig.getOfferOption().getPk());
		}

		for (OfferOption offerOption : groupOptions) {
			if (offerOptionForReportConfigs.contains(offerOption.getPk())) {

				GetOfferOptionTO offerOptionTO = new GetOfferOptionTO();
				offerOptionTO.setAlias(offerOption.getAlias());
				offerOptionTO.setLabel(offerOption.getLabel());
				offerOptionTO.setOfferOptionPk(offerOption.getPk());

				getOfferByIndicatorTO.offerOptions.add(offerOptionTO);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getOfferByIndicator]: Found "
					+ getOfferByIndicatorTO.offerOptions.size() + " offer option(s) for indicator " + indicatorId));
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getOfferByIndicator]: End"));

		return getOfferByIndicatorTO;
	}

	/**
	 * Get the paramTypes used by the OfferOption passed in parameter for the Indicator passed in parameter
	 * 
	 * @param indicatorId
	 * @param offerOptionAlias
	 * @param soaContext
	 * @return
	 * @throws BusinessException
	 */
	public GetTypesForReportTO getTypesForReport(String indicatorId, String offerOptionAlias, SOAContext soaContext)
			throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getTypesForReport]: Start"));

		GetTypesForReportTO getTypesForReportTO = new GetTypesForReportTO();
		getTypesForReportTO.indicatorId = indicatorId;
		getTypesForReportTO.offerOptionAlias = offerOptionAlias;

		List<ReportConfig> reportConfigs = null;
		List<OfferOption> offerOptionList = offerOptionDAO.findBy(OfferOption.FIELD_ALIAS, offerOptionAlias);
		if (offerOptionList == null || offerOptionList.size() != 1) {
			throw new BusinessException(
					"OfferOption " + offerOptionAlias + " does not exist or is multiple in database");
		} else {
			reportConfigs = offerOptionList.get(0).getReportConfigs();
		}

		for (ReportConfig reportConfig : reportConfigs) {
			List<Indicator> indicators = reportConfig.getIndicators();
			for (Indicator indicator : indicators) {
				if (indicator.getIndicatorId().equals(indicatorId)) {
					for (ParamType paramtype : reportConfig.getParamTypes()) {
						GetParamTypeTO paramTypeTO = new GetParamTypeTO(paramtype.getPk(), paramtype.getEntityType(),
								paramtype.getEntitySubtype(), paramtype.getParentType(), paramtype.getParentSubtype(),
								paramtype.getAlias());
						getTypesForReportTO.paramTypeTOs.add(paramTypeTO);

						if (logger.isTraceEnabled()) {
							logger.trace(SOATools.buildSOALogMessage(soaContext,
									"[getTypesForReport]: ParamType Found " + paramtype.toString()));
						}
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[getTypesForReport]: Found " + Integer.toString(getTypesForReportTO.paramTypeTOs.size())
							+ " paramTypes for offer " + offerOptionAlias + " for indicator " + indicatorId));
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getTypesForReport]: End"));

		return getTypesForReportTO;
	}

	/**
	 * Retrieve all the entities of the Reporting group passed in parameter which comply with the paramType (EntityType,
	 * EntitySubType, ParentType, ParentSubtype) passed in parameter If not null, entityLabelFilter is apply for entity
	 * selection
	 * 
	 * @param reportingGroupRef
	 * @param reportingGroupOrigin
	 * @param entityTypeAlias
	 * @param entityLabelStartWith
	 * @param soaContext
	 * @return
	 * @throws BusinessException
	 */
	public GetEntitiesForTypeAndForReportingGroupTO getEntitiesForTypeAndForReportingGroup(String reportingGroupRef,
			String reportingGroupOrigin, String entityTypeAlias, String entityLabelStartWith, SOAContext soaContext)
			throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getEntitiesForTypeForReportingGroupTO]: Start"));

		GetEntitiesForTypeAndForReportingGroupTO result = new GetEntitiesForTypeAndForReportingGroupTO();
		result.entityTypeAlias = entityTypeAlias;
		result.reportingGroupRef = reportingGroupRef;
		result.origin = reportingGroupOrigin;

		ParamType paramType = null;
		List<ParamType> paramTypeList = paramTypeDAO.findBy(ParamType.FIELD_ALIAS, entityTypeAlias);
		if (paramTypeList == null || paramTypeList.size() != 1) {
			throw new BusinessException("ParamType " + entityTypeAlias + " does not exist or is multiple in database");
		} else {
			paramType = paramTypeList.get(0);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[getEntitiesForTypeForReportingGroupTO]: getting Entities of reporting Group " + reportingGroupRef
							+ "/" + reportingGroupOrigin + " matching with " + paramType.toString()));
		}

		List<ReportingEntityTypeTO> reportingEntities = reportingEntityDAO.findReportingEntitiesTypesForAReportingGroup(
				reportingGroupRef, reportingGroupOrigin, paramType.getEntityType(), paramType.getEntitySubtype());
		List<ReportingEntityTypeTO> reportingParentEntities = null;

		if (!paramType.getParentType().isEmpty()) {
			reportingParentEntities = reportingEntityDAO.findReportingEntitiesTypesForAReportingGroup(reportingGroupRef,
					reportingGroupOrigin, paramType.getParentType(), paramType.getParentSubtype());
		}
		for (ReportingEntityTypeTO entity : reportingEntities) {
			if (!paramType.getParentType().isEmpty()) {
				boolean bParentTypeAndSubtypeIsOk = false;
				for (ReportingEntityTypeTO parentEntity : reportingParentEntities) {
					if (entity.getparentEntityPk() == parentEntity.getEntityPk()) {
						bParentTypeAndSubtypeIsOk = true;
					}
				}

				if (bParentTypeAndSubtypeIsOk
						&& (entityLabelStartWith == null || entity.getEntityLabel().startsWith(entityLabelStartWith))) {
					GetReportingEntityTO getReportingEntityTO = new GetReportingEntityTO(entity.getEntityPk(),
							entity.getEntityId(), entity.getOrigin(), entity.getEntityLabel());
					result.reportingEntities.add(getReportingEntityTO);
					if (logger.isTraceEnabled()) {
						logger.trace(SOATools.buildSOALogMessage(soaContext,
								"[getEntitiesForTypeForReportingGroupTO]: ReportingEntity Found :"
										+ getReportingEntityTO.getEntityId()));
					}

				}
			} else if (entityLabelStartWith == null || entity.getEntityLabel().startsWith(entityLabelStartWith)) {
				GetReportingEntityTO getReportingEntityTO = new GetReportingEntityTO(entity.getEntityPk(),
						entity.getEntityId(), entity.getOrigin(), entity.getEntityLabel());
				result.reportingEntities.add(getReportingEntityTO);
				if (logger.isTraceEnabled()) {
					logger.trace(SOATools.buildSOALogMessage(soaContext,
							"[getEntitiesForTypeForReportingGroupTO]: ReportingEntity Found :"
									+ getReportingEntityTO.getEntityId()));
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getEntitiesForTypeForReportingGroupTO]: Found "
					+ Integer.toString(result.reportingEntities.size()) + " entity(s) with type  " + entityTypeAlias));
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getEntitiesForTypeForReportingGroupTO]: End"));

		return result;
	}

	public GetEntitiesForTypeAndForReportingGroupTO getEntitiesForTypeAndForReportingGroupForCassandra(
			String reportingGroupRef, String reportingGroupOrigin, String entityTypeAlias, SOAContext soaContext)
			throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getEntitiesForTypeForReportingGroupTO]: Start"));

		GetEntitiesForTypeAndForReportingGroupTO result = new GetEntitiesForTypeAndForReportingGroupTO();
		result.entityTypeAlias = entityTypeAlias;
		result.reportingGroupRef = reportingGroupRef;
		result.origin = reportingGroupOrigin;

		ParamType paramType = null;
		List<ParamType> paramTypeList = paramTypeDAO.findBy(ParamType.FIELD_ALIAS, entityTypeAlias);
		if (paramTypeList == null || paramTypeList.size() != 1) {
			throw new BusinessException("ParamType " + entityTypeAlias + " does not exist or is multiple in database");
		} else {
			paramType = paramTypeList.get(0);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[getEntitiesForTypeForReportingGroupTO]: getting Entities of reporting Group " + reportingGroupRef
							+ "/" + reportingGroupOrigin + " matching with " + paramType.toString()));
		}

		List<ReportingEntityTypeTO> reportingEntities = reportingEntityDAO.findReportingEntitiesTypesForAReportingGroup(
				reportingGroupRef, reportingGroupOrigin, paramType.getEntityType(), paramType.getEntitySubtype());
		List<ReportingEntityTypeTO> reportingParentEntities = null;

		if (!paramType.getParentType().isEmpty()) {
			reportingParentEntities = reportingEntityDAO.findReportingEntitiesTypesForAReportingGroup(reportingGroupRef,
					reportingGroupOrigin, paramType.getParentType(), paramType.getParentSubtype());
		}
		for (ReportingEntityTypeTO entity : reportingEntities) {

			GetReportingEntityTO getReportingEntityTO = new GetReportingEntityTO(entity.getEntityPk(),
					entity.getEntityId(), entity.getOrigin(), entity.getEntityLabel());
			result.reportingEntities.add(getReportingEntityTO);
			if (logger.isTraceEnabled()) {
				logger.trace(SOATools.buildSOALogMessage(soaContext,
						"[getEntitiesForTypeForReportingGroupTO]: ReportingEntity Found :"
								+ getReportingEntityTO.getEntityId()));
			}

		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext, "[getEntitiesForTypeForReportingGroupTO]: Found "
					+ Integer.toString(result.reportingEntities.size()) + " entity(s) with type  " + entityTypeAlias));
		}

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getEntitiesForTypeForReportingGroupTO]: End"));

		return result;
	}

	/**
	 * Update a bookmark objet in the database. Note: Only the EntityId and the Period can be updated
	 * 
	 * @param bookmarkParam
	 * @param soaContext
	 * @return
	 * @throws BusinessException
	 */
	public FavoriteTO updateBookmark(UpdateBookmarkParameter bookmarkParam, SOAContext soaContext)
			throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[updateBookmark]: Start"));

		Long bookmarkId;
		if (bookmarkParam == null) {
			throw new IllegalArgumentException("Bookmark Id must be specified");
		} else {
			bookmarkId = Long.parseLong(bookmarkParam.getBookmarkId());
			if (bookmarkId == null) {
				throw new IllegalArgumentException("Bookmark Id must be valid");
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[updateBookmark]: updating Bookmark: " + bookmarkParam.getBookmarkId() + " set entityId = "
							+ bookmarkParam.getReportingEntityId() + ", granularity = " + bookmarkParam.getGranularity()
							+ ", report time unit" + bookmarkParam.getReportTimeUnit()));
		}

		ReportsBookmark bookmark = bookmarkDAO.findById(bookmarkId);
		if (bookmark == null) {
			throw new BusinessException("Bookmark " + bookmarkId + " does not exist or is multiple in database");
		}

		ReportingEntity reportingEntity = null;

		if (bookmarkParam.getReportingEntityId() != null) {

			List<ReportingEntity> listReportingEntity = reportingEntityDAO.findBy(ReportingEntity.FIELD_ENTITYID,
					bookmarkParam.getReportingEntityId());
			if (listReportingEntity.size() != 1) {
				throw new BusinessException("ReportingEntity " + bookmarkParam.getReportingEntityId()
						+ " does not exist or is multiple in database");
			}

			reportingEntity = listReportingEntity.get(0);
			if (!reportingEntity.getReportingGroups().contains(bookmark.getReportingGroup())) {
				throw new BusinessException(" Invalid bookmark: reportingEntity " + bookmarkParam.getReportingEntityId()
						+ " does not belong to the reporting group "
						+ bookmark.getReportingGroup().getReportingGroupRef());
			}

			if ("NETWORK".equals(bookmark.getParamType().getAlias())) {
				throw new BusinessException(
						"Bookmark can't be updated with ReportingEntity because bookmark type is not 'NETWORK'");
			}
		} else {
			if (!"NETWORK".equals(bookmark.getParamType().getAlias())) {
				throw new BusinessException(
						"Bookmark can't be updated with no ReportingEntity because bookmark type is 'NETWORK'");
			}
		}

		// FILTERS
		List<OfferOption> offerOptions = offerOptionDAO.findBy(OfferOption.FIELD_ALIAS, bookmarkParam.getOfferOption());
		if (offerOptions.size() != 1)
			throw new BusinessException(" Invalid bookmark: offerOption " + bookmarkParam.getOfferOption()
					+ " does not exist or is multiple in database");

		Filter filterBookmarked = null;
		OfferOption offerOption = offerOptions.get(0);
		if (bookmarkParam.getFilterId() != null) {
			for (Filter filter : offerOption.getFilters()) {
				if (filter.getFilterId().equals(bookmarkParam.getFilterId())) {
					filterBookmarked = filter;
				}
			}

			if (filterBookmarked == null) {
				throw new BusinessException(
						" Invalid bookmark: no Filter found for filterId " + bookmarkParam.getFilterId());
			}
		} else {
			if (offerOption.getFilters().size() > 0) {
				filterBookmarked = offerOption.getFilters().get(0);
			}
		}

		bookmark.setReportingEntity(reportingEntity);
		bookmark.setGranularity(bookmarkParam.getGranularity());
		bookmark.setReportTimeUnit(bookmarkParam.getReportTimeUnit());
		bookmarkDAO.persistAndFlush(bookmark);

		FavoriteTO result = new FavoriteTO();
		result.favoriteKey = bookmark.getBookmarkId();

		logger.info(SOATools.buildSOALogMessage(soaContext, "[updateBookmark]: End"));
		return result;
	}

	/**
	 * Delete the bookmark identified by the bookmarkId passed in parameter
	 * 
	 * @param bookmarkId
	 * @param soaContext
	 * @throws BusinessException
	 */
	public void deleteBookmark(String bookmarkIdString, SOAContext soaContext) throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[deleteBookmark]: Start"));
		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					"[deleteBookmark]: deleting Bookmark: " + bookmarkIdString));
		}

		Long bookmarkId = Long.parseLong(bookmarkIdString);
		if (bookmarkId == null) {
			throw new IllegalArgumentException("Bookmark Id " + bookmarkIdString + " must be valid");
		}

		ReportsBookmark bookmarkToRemove = bookmarkDAO.findById(bookmarkId);
		if (bookmarkToRemove == null) {
			throw new BusinessException("Failed to delete bookmark :" + bookmarkId + ", does not exist in database");
		}

		bookmarkDAO.remove(bookmarkToRemove);

		logger.info(SOATools.buildSOALogMessage(soaContext, "[deleteBookmark]: End"));
	}

	/**
	 * Retrieve the bookmark information for the bookmarkId passed in parameter
	 * 
	 * @param bookmarkId
	 * @param soaContext
	 * @return GetBookmarkTO the bookmark information
	 * @throws BusinessException
	 */
	public GetBookmarkTO getBookmark(String bookmarkIdString, SOAContext soaContext) throws BusinessException {

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getBookmark]: Start"));
		if (logger.isDebugEnabled()) {
			logger.debug(
					SOATools.buildSOALogMessage(soaContext, "[getBookmark]: getting Bookmark: " + bookmarkIdString));
		}

		Long bookmarkId = Long.parseLong(bookmarkIdString);
		if (bookmarkId == null) {
			throw new IllegalArgumentException("Bookmark Id " + bookmarkIdString + " must be valid");
		}

		ReportsBookmark bookmarkToReturn = bookmarkDAO.findById(bookmarkId);
		if (bookmarkToReturn == null) {
			throw new BusinessException(" Failed to get bookmark :" + bookmarkId + ", does not exist in database");
		}

		String entityId = null;
		if (bookmarkToReturn.getReportingEntity() != null) {
			entityId = bookmarkToReturn.getReportingEntity().getEntityId();
		}

		GetBookmarkTO result;
		result = new GetBookmarkTO(bookmarkId, bookmarkToReturn.getIndicator().getIndicatorId(),
				bookmarkToReturn.getReportUser().getPk(), bookmarkToReturn.getOfferOption().getAlias(), entityId,
				bookmarkToReturn.getParamType().getAlias(), bookmarkToReturn.getReportingGroup().getReportingGroupRef(),
				bookmarkToReturn.getReportingGroup().getOrigin(), bookmarkToReturn.getGranularity(),
				bookmarkToReturn.getReportTimeUnit(), bookmarkToReturn.getFilter().getUri(),
				bookmarkToReturn.getReportingGroup().getTimeZone());

		logger.info(SOATools.buildSOALogMessage(soaContext, "[getBookmark]: End"));
		return result;
	}

	public void updateReportUserToReportingGroupLinks(UpdateUserReportingGroupParameter parameter,
			SOAContext soaContext) {
		Set<ReportingGroup> reportingGroupsToAdd = reportingGroupDelegate
				.getReportingGroupsListByReportKey(parameter.added, soaContext);
		Set<ReportingGroup> reportingGroupsToRemove = reportingGroupDelegate
				.getReportingGroupsListByReportKey(parameter.removed, soaContext);

		for (Long srsId : parameter.internalUserIds) {
			try {
				ReportUser user = reportUserDelegate.findReportUserByUserPk(srsId);
				user.getWorkingGroups().addAll(reportingGroupsToAdd);
				user.getWorkingGroups().removeAll(reportingGroupsToRemove);

				reportUserDAO.persistAndFlush(user);
			} catch (BusinessException bex) {
				logger.error(SOATools.buildSOALogMessage(soaContext,
						"Error while updating user's reporting groups, no user found for id " + srsId));
			}
		}

	}

	public void addReportingGroupToReportUser(Long userId, ReportGroupKeyTO reportingGroupKey)
			throws BusinessException {
		ReportingGroup group = reportingGroupDelegate.getReportingGroup(reportingGroupKey);
		ReportUser user = reportUserDelegate.findReportUserByUserPk(userId);

		user.getWorkingGroups().add(group);
		reportUserDAO.persistAndFlush(user);
	}

	public void removeReportingGroupFromReportUser(Long userId, ReportGroupKeyTO reportingGroupKey)
			throws BusinessException {
		ReportingGroup group = reportingGroupDelegate.getReportingGroup(reportingGroupKey);
		ReportUser user = reportUserDelegate.findReportUserByUserPk(userId);

		boolean succes = user.getWorkingGroups().remove(group);

		if (!succes) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + " : reportGroup with key  "
					+ reportingGroupKey + " in group list of user " + userId,
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION_CODE);
		}

		reportUserDAO.persistAndFlush(user);
	}

}
