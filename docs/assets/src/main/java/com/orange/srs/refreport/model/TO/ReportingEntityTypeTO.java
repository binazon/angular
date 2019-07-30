package com.orange.srs.refreport.model.TO;

public class ReportingEntityTypeTO {

	private Long entityPk;
	private String entityType;
	private Long parentEntityPk;
	private String entityId;
	private String origin;
	private String entityLabel;
	private String entitySubtype;

	/**
	 * Default Constructor
	 */
	public ReportingEntityTypeTO() {
	}

	/**
	 * @param entityPk
	 * @param entityId
	 */
	public ReportingEntityTypeTO(String entityId, String entityType, String entityLabel, String origin,
			Long parentEntityPk, Long entityPk, String entitySubtype) {
		this.entityPk = entityPk;
		this.entityType = entityType;
		this.parentEntityPk = parentEntityPk;
		this.origin = origin;
		this.entityId = entityId;
		this.entityLabel = entityLabel;
		this.entitySubtype = entitySubtype;
	}

	public ReportingEntityTypeTO(String entityId, String entityType, String entityLabel, String origin,
			Long parentEntityPk, Long entityPk) {
		this.entityPk = entityPk;
		this.entityType = entityType;
		this.parentEntityPk = parentEntityPk;
		this.origin = origin;
		this.entityId = entityId;
		this.entityLabel = entityLabel;
	}

	/**
	 * @return the entityPk
	 */
	public Long getEntityPk() {
		return entityPk;
	}

	/**
	 * @param entityPk
	 *            the entityPk to set
	 */
	public void setEntityPk(Long entityPk) {
		this.entityPk = entityPk;
	}

	/**
	 * @return the entityType
	 */
	public String getEntityType() {
		return entityType;
	}

	/**
	 * @param entityType
	 *            the entityType to set
	 */
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	/**
	 * @return the entityId
	 */
	public String getEntityId() {
		return entityId;
	}

	/**
	 * @param entityId
	 *            the entityId to set
	 */
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	/**
	 * @return the entityLabel
	 */
	public String getEntityLabel() {
		return entityLabel;
	}

	/**
	 * @param entityLabel
	 *            the entityLabel to set
	 */
	public void setEntityLabel(String entityLabel) {
		this.entityLabel = entityLabel;
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/**
	 * Get the property parentEntityPk
	 *
	 * @return the parentEntityPk value
	 */
	public long getparentEntityPk() {
		return parentEntityPk;
	}

	/**
	 * Set the property parentEntityPk
	 *
	 * @param parentEntityPk
	 *            the parentEntityPk to set
	 */
	public void setparentEntityPk(long parentEntityPk) {
		this.parentEntityPk = parentEntityPk;
	}

	public String getEntitySubType() {

		return this.entitySubtype;
	}

	public void setEntitySubtype(String entitySubtype) {
		this.entitySubtype = entitySubtype;
	}

	public String toString() {

		String result = "ReportingEntityTypeTO: [entityId=" + entityId + ", entityLabel=" + entityLabel + ", entityPk="
				+ entityPk + ", entityType=" + entityType + ", entitySubtype=" + entitySubtype + ", origin=" + origin
				+ ", parentEntityPk=" + parentEntityPk + "]";
		return result;
	}
}
