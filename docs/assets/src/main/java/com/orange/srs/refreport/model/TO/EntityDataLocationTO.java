package com.orange.srs.refreport.model.TO;

import java.io.Serializable;

public class EntityDataLocationTO implements Serializable {

	private static final long serialVersionUID = 5180833059204127016L;

	public Long entityPk;
	public String entityId;
	public String origin;
	public String domain;

	public EntityDataLocationTO(Long entityPk, String entityId, String origin, String domain) {
		this.entityPk = entityPk;
		this.entityId = entityId;
		this.origin = origin;
		this.domain = domain;
	}

	public EntityDataLocationTO(Long entityPk, String origin, String domain) {
		this.entityPk = entityPk;
		this.origin = origin;
		this.domain = domain;
	}

	public Long getEntityPk() {
		return entityPk;
	}

	public void setEntityPk(Long entityPk) {
		this.entityPk = entityPk;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

}
