package com.orange.srs.refreport.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = ReportingEntity.TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = { "ENTITY_ID",
		"ENTITY_TYPE", "ORIGIN" }), indexes = { @Index(name = "INDEX_ENTITY_TYPE", columnList = "ENTITY_TYPE") })

public class ReportingEntity implements Serializable {

	private static final long serialVersionUID = -706318452433008606L;

	public static final String TABLE_NAME = "T_REPORTING_ENTITY";
	public static final String TJ_NAME_SUBTYPES = "TJ_REPORTING_ENTITY_TO_TYPE_AND_SUBTYPE";

	public static final String COL_NAME_PK = "PK";
	public static final String COL_NAME_CREATION_DATE = "CREATION_DATE";
	public static final String COL_NAME_ENTITY_ID = "ENTITY_ID";
	public static final String COL_NAME_ENTITY_TYPE = "ENTITY_TYPE";
	public static final String COL_NAME_LABEL = "LABEL";
	public static final String COL_NAME_ORIGIN = "ORIGIN";
	public static final String COL_NAME_PARENT = "REPORTING_ENTITY_PARENT_FK";
	public static final String COL_NAME_PARTITION_NUMBER = "PARTITION_NUMBER";
	public static final String COL_NAME_SHORT_LABEL = "SHORT_LABEL";
	public static final String COL_NAME_SOURCE = "SOURCE";
	public static final String COL_NAME_UPDATE_DATE = "UPDATE_DATE";

	public static final String TJ_COL_NAME_REPORTING_ENTITY_FK = "REPORTING_ENTITY_FK";
	public static final String TJ_COL_NAME_SUBTYPE = "SUBTYPE";
	public static final String TJ_COL_NAME_TYPE = "TYPE";

	public static final String FIELD_PK = "pk";
	public static final String FIELD_ENTITYTYPE = "entityType";
	public static final String FIELD_ENTITYID = "entityId";
	public static final String FIELD_LABEL = "label";
	public static final String FIELD_CREATION_DATE = "creationDate";
	public static final String FIELD_UPDATE_DATE = "updateDate";
	public static final String FIELD_SOURCE = "source";
	public static final String FIELD_SHORT_LABEL = "shortLabel";
	public static final String FIELD_ORIGIN = "origin";
	public static final String FIELD_PARENT = "parent";
	public static final String FIELD_PART_OF_ENTITIES = "partOfEntities";
	public static final String FIELD_COMPOSED_OF_ENTITIES = "composedOfEntities";
	public static final String FIELD_ATTRIBUTES = "entityAttributes";
	public static final String FIELD_REPORTINGGROUP = "reportingGroups";
	public static final String FIELD_ATTRIBUTES_LIST = "entityAttributesList";
	public static final String FIELD_DATALOCATION = "dataLocations";
	public static final String FIELD_PARTITION_NUMBER = "partitionNumber";
	public static final String FIELD_SUBTYPES = "subtypes";

	@Id
	@Column(name = COL_NAME_PK)
	private Long pk;

	@Column(name = COL_NAME_ENTITY_TYPE, nullable = false, length = 50)
	private String entityType;

	@Column(name = COL_NAME_ENTITY_ID, nullable = false)
	private String entityId;

	@Column(name = COL_NAME_ORIGIN, nullable = false)
	private String origin;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COL_NAME_CREATION_DATE, nullable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COL_NAME_UPDATE_DATE, nullable = false)
	private Date updateDate;

	@Column(name = COL_NAME_SOURCE, nullable = false)
	private String source;

	@Column(name = COL_NAME_LABEL, nullable = false)
	private String label;

	@Column(name = COL_NAME_SHORT_LABEL, nullable = false)
	private String shortLabel;

	@Column(name = "PARTITION_NUMBER", length = 110)
	private String partitionNumber;

	/*
	 * one ReportingEntity can have 0..n EntityAttribute
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE,
			CascadeType.PERSIST }, mappedBy = EntityAttribute.FIELD_ENTITY)
	private List<EntityAttribute> entityAttributes;

	/*
	 * one ReportingEntity can have 0..n EntityAttributeList
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE }, mappedBy = EntityAttributeList.FIELD_ENTITY)
	private List<EntityAttributeList> entityAttributesList;

	/*
	 * one ReportingEntity can have 0..n EntityGroupAttribute
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = {
			CascadeType.REMOVE }, mappedBy = EntityGroupAttribute.FIELD_REPORTING_ENTITY)
	private List<EntityGroupAttribute> entityGroupAttributes;

	/*
	 * one ReportingEntity can have a ReportingEntity as parent or not Example: one interface has a router as parent
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = COL_NAME_PARENT)
	private ReportingEntity parent;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = DataLocation.FIELD_REPORTING_ENTITY)
	private List<DataLocation> dataLocations;

	@OneToMany(cascade = { CascadeType.REFRESH, CascadeType.MERGE,
			CascadeType.REMOVE }, mappedBy = EntityLink.FIELD_REPORTING_ENTITY_DEST)
	private List<EntityLink> partOfEntities;

	@OneToMany(cascade = { CascadeType.REFRESH, CascadeType.MERGE,
			CascadeType.REMOVE }, mappedBy = EntityLink.FIELD_REPORTING_ENTITY_SRC)
	private List<EntityLink> composedOfEntities;

	@ManyToMany(cascade = { CascadeType.REFRESH, CascadeType.MERGE }, mappedBy = ReportingGroup.FIELD_ENTITIES)
	private List<ReportingGroup> reportingGroups;

	@ManyToMany(cascade = CascadeType.REFRESH)
	@JoinTable(name = TJ_NAME_SUBTYPES, joinColumns = @JoinColumn(name = TJ_COL_NAME_REPORTING_ENTITY_FK), inverseJoinColumns = {
			@JoinColumn(name = TJ_COL_NAME_SUBTYPE, referencedColumnName = "SUBTYPE"),
			@JoinColumn(name = TJ_COL_NAME_TYPE, referencedColumnName = "TYPE") })
	private Set<EntityTypeAndSubtype> subtypes;

	public List<EntityLink> getPartOfEntities() {
		if (partOfEntities == null)
			partOfEntities = new ArrayList<>();
		return partOfEntities;
	}

	public void setPartOfEntities(List<EntityLink> partOfEntities) {
		this.partOfEntities = partOfEntities;
	}

	public List<EntityLink> getComposedOfEntities() {
		// for bulletproof purpose
		if (composedOfEntities == null)
			composedOfEntities = new ArrayList<>();
		return composedOfEntities;
	}

	public void setComposedOfEntities(List<EntityLink> composedOfEntities) {
		this.composedOfEntities = composedOfEntities;
	}

	public List<ReportingGroup> getReportingGroups() {
		// for bulletproof purpose
		if (reportingGroups == null)
			reportingGroups = new ArrayList<>();
		return reportingGroups;
	}

	public void setReportingGroups(List<ReportingGroup> reportingGroups) {
		this.reportingGroups = reportingGroups;
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;
	}

	public String getPartitionNumber() {
		return partitionNumber;
	}

	public void setPartitionNumber(String partitionNumber) {
		this.partitionNumber = partitionNumber;
	}

	public List<EntityAttribute> getEntityAttributes() {
		if (entityAttributes == null) {
			entityAttributes = new ArrayList<>();
		}
		return entityAttributes;
	}

	public List<EntityAttributeI> getEntityAttributesI() {
		List<EntityAttributeI> entityAttributesI = new ArrayList<>();
		for (EntityAttribute anEntityAttribute : getEntityAttributes()) {
			entityAttributesI.add(anEntityAttribute);
		}
		for (EntityAttributeList anEntityAttributeList : getEntityAttributesList()) {
			entityAttributesI.add(anEntityAttributeList);
		}
		return entityAttributesI;
	}

	public void setEntityAttributes(List<EntityAttribute> entityAttributes) {
		this.entityAttributes = entityAttributes;
	}

	public List<EntityGroupAttribute> getEntityGroupAttributes() {
		if (entityGroupAttributes == null) {
			entityGroupAttributes = new ArrayList<>();
		}
		return entityGroupAttributes;
	}

	public void setEntityGroupAttributes(List<EntityGroupAttribute> entityGroupAttributes) {
		this.entityGroupAttributes = entityGroupAttributes;
	}

	public ReportingEntity getParent() {
		return parent;
	}

	public void setParent(ReportingEntity parent) {
		this.parent = parent;
	}

	public List<DataLocation> getDataLocations() {
		if (dataLocations == null) {
			dataLocations = new ArrayList<>();
		}
		return dataLocations;
	}

	public void setDataLocations(List<DataLocation> dataLocations) {
		this.dataLocations = dataLocations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		// 2 reportingEntities are equal if they have same entityId and same origin

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ReportingEntity other = (ReportingEntity) obj;
		if (entityId == null) {
			if (other.entityId != null) {
				return false;
			}
		} else if (!entityId.equals(other.entityId)) {
			return false;
		}
		if (pk == null) {
			if (other.pk != null) {
				return false;
			}
		} else if (!pk.equals(other.pk)) {
			return false;
		}
		if (origin == null) {
			if (other.origin != null) {
				return false;
			}
		} else if (!origin.equals(other.origin)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReportingEntity [pk=");
		builder.append(pk);
		builder.append(", entityType=");
		builder.append(entityType);
		builder.append(", entityId=");
		builder.append(entityId);
		builder.append(", origin=");
		builder.append(origin);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", updateDate=");
		builder.append(updateDate);
		builder.append(", source=");
		builder.append(source);
		builder.append(", partitionNumber=");
		builder.append(partitionNumber);
		builder.append("]");
		return builder.toString();
	}

	public List<EntityAttributeList> getEntityAttributesList() {
		return entityAttributesList;
	}

	public void setEntityAttributesList(List<EntityAttributeList> entityAttributesList) {
		this.entityAttributesList = entityAttributesList;
	}

	public Set<EntityTypeAndSubtype> getSubtype() {
		if (subtypes == null) {
			subtypes = new HashSet<>();
		}
		return subtypes;
	}

	public void setSubtype(Set<EntityTypeAndSubtype> psubtypes) {
		this.subtypes = psubtypes;
	}

}
