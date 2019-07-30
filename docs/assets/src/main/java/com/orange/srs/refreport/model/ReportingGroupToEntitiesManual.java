package com.orange.srs.refreport.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MANUAL")
public class ReportingGroupToEntitiesManual extends ReportingGroupToEntities {
}
