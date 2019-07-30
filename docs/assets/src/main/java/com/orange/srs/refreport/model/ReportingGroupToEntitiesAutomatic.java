package com.orange.srs.refreport.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReportingGroupToEntitiesAutomatic.VALUE_REPORTING_GROUP_TO_ENTITIES_TYPE)
public class ReportingGroupToEntitiesAutomatic extends ReportingGroupToEntities {

	public static final String VALUE_REPORTING_GROUP_TO_ENTITIES_TYPE = "AUTOMATIC";
}
