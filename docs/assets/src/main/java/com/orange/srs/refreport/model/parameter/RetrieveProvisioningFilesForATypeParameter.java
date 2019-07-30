package com.orange.srs.refreport.model.parameter;

import java.util.Calendar;

import com.orange.srs.statcommon.model.enums.EntityTypeEnum;

public class RetrieveProvisioningFilesForATypeParameter {
	public EntityTypeEnum entityType;
	public Calendar date;
	public String chain;

	public EntityTypeEnum getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityTypeEnum entityType) {
		this.entityType = entityType;
	}

	public void SetDate(Calendar date) {
		this.date = date;
	}

	public Calendar GetDate() {
		return date;
	}

	public void SetChain(String chain) {
		this.chain = chain;
	}

	public String GetChain() {
		return chain;
	}

	public RetrieveProvisioningFilesForATypeParameter(EntityTypeEnum entityType, Calendar date, String chain) {
		this.entityType = entityType;
		this.date = date;
		this.chain = chain;
	}
}
