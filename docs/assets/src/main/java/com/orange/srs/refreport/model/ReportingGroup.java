package com.orange.srs.refreport.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.orange.srs.refreport.model.enumerate.ReportingGroupTypeEnum;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "REPORTING_GROUP_TYPE", discriminatorType = DiscriminatorType.STRING)
@Table(name = ReportingGroup.TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = { "REPORTING_GROUP_REF",
		"ORIGIN" }))
@DiscriminatorValue(ReportingGroup.DEFAULT_VALUE_REPORTING_GROUP_TYPE)
public class ReportingGroup implements Serializable {

	private static final long serialVersionUID = 8441370041670145337L;

	public static final String DEFAULT_VALUE_REPORTING_GROUP_TYPE = "normal";

	public static final String TABLE_NAME = "T_REPORTING_GROUP";
	public static final String TJ_NAME_OFFER_OPTION = "TJ_REPORTING_GROUP_TO_OFFER_OPTION";
	public static final String TJ_NAME_CRITERIA = "TJ_REPORTING_GROUP_TO_CRITERIA";

	public static final String TJ_COL_NAME_REPORTING_GROUP = "REPORTING_GROUP_FK";
	public static final String TJ_COL_NAME_OFFER_OPTION = "OFFER_OPTION_FK";

	public static final String COL_NAME_PK = "PK";
	public static final String COL_NAME_CREATION_DATE = "CREATION_DATE";
	public static final String COL_NAME_DATA_LOCATION_FK = "DATA_LOCATION_FK";
	@Deprecated
	public static final String COL_NAME_GROUPING_CRITERIA = "GROUPING_CRITERIA";
	@Deprecated
	public static final String COL_NAME_GROUPING_VALUE = "GROUPING_VALUE";
	public static final String COL_NAME_LABEL = "LABEL";
	public static final String COL_NAME_LANGUAGE = "LANGUAGE";
	public static final String COL_NAME_ORIGIN = "ORIGIN";
	public static final String COL_NAME_REPORTING_GROUP_REF = "REPORTING_GROUP_REF";
	public static final String COL_NAME_REPORTING_GROUP_TYPE = "REPORTING_GROUP_TYPE";
	public static final String COL_NAME_SOURCE = "SOURCE";
	public static final String COL_NAME_TIME_ZONE = "TIME_ZONE";
	public static final String COL_NAME_TYPE = "TYPE";
	public static final String COL_NAME_UPDATE_DATE = "UPDATE_DATE";
	public static final String COL_NAME_DELETION_DATE = "DELETION_DATE";

	public static final String FIELD_PK = "pk";
	public static final String FIELD_REPORTING_GROUP_REF = "reportingGroupRef";
	public static final String FIELD_ORIGIN = "origin";
	public static final String FIELD_SOURCE = "source";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_LABEL = "label";
	public static final String FIELD_LANGUAGE = "language";
	@Deprecated
	public static final String FIELD_GROUPING_CRITERIA = "groupingCriteria";
	@Deprecated
	public static final String FIELD_GROUPING_VALUE = "groupingValue";
	public static final String FIELD_REPORTING_GROUP_TYPE = "reportingGroupType";
	public static final String FIELD_TIME_ZONE = "timeZone";

	public static final String FIELD_DATA_LOCATION = "dataLocation";

	public static final String FIELD_REPORT_SOURCE_OPTIONS = "reportSourceOptions";
	public static final String FIELD_GROUP_REPORT_CONFIGS = "groupReportConfigs";
	public static final String FIELD_GROUPING_RULE = "groupingRules";
	public static final String FIELD_ENTITIES = "entities";
	public static final String FIELD_APPLICABLE_REPORTS = "applicableReports";
	public static final String FIELD_FILTER_CONFIGS = "filterConfigs";

	@Id
	@Column(name = COL_NAME_PK)
	private Long pk;

	@Column(name = COL_NAME_REPORTING_GROUP_REF, nullable = false)
	private String reportingGroupRef;

	@Column(name = COL_NAME_ORIGIN, nullable = false)
	private String origin;

	@Column(name = COL_NAME_SOURCE, nullable = false)
	private String source;

	@Enumerated(EnumType.STRING)
	@Column(name = COL_NAME_TYPE, nullable = false)
	private ReportingGroupTypeEnum type;

	@Column(name = COL_NAME_LABEL)
	private String label;

	@Column(name = COL_NAME_LANGUAGE)
	private String language;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COL_NAME_CREATION_DATE)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COL_NAME_UPDATE_DATE)
	private Date updateDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COL_NAME_DELETION_DATE)
	private Date deletionDate;

	@Column(name = COL_NAME_GROUPING_CRITERIA, nullable = false)
	private String groupingCriteria;

	@Lob
	@Column(name = COL_NAME_GROUPING_VALUE, nullable = false, columnDefinition = "TEXT")
	private String groupingValue;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = GroupingRule.FIELD_REPORTING_GROUP, orphanRemoval = true)
	private List<GroupingRule> groupingRules;

	@Column(name = COL_NAME_REPORTING_GROUP_TYPE, insertable = false, updatable = false)
	private String reportingGroupType;

	@Column(name = COL_NAME_TIME_ZONE)
	private String timeZone;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = COL_NAME_DATA_LOCATION_FK)
	private DataLocation dataLocation;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "REPORTING_GROUP_FK", nullable = false)
	private List<GroupReportConfig> groupReportConfigs;

	@ManyToMany(cascade = { CascadeType.REFRESH, CascadeType.MERGE })
	@JoinTable(name = ReportingGroupToEntities.TABLE_NAME, joinColumns = @JoinColumn(name = "REPORTING_GROUP_FK"), inverseJoinColumns = @JoinColumn(name = "REPORTING_ENTITY_FK"))
	private List<ReportingEntity> entities;

	@ManyToMany(cascade = { CascadeType.REFRESH, CascadeType.MERGE })
	@JoinTable(name = TJ_NAME_OFFER_OPTION, joinColumns = @JoinColumn(name = TJ_COL_NAME_REPORTING_GROUP), inverseJoinColumns = @JoinColumn(name = TJ_COL_NAME_OFFER_OPTION))
	private List<OfferOption> reportSourceOptions;

	@ManyToMany(cascade = { CascadeType.DETACH, CascadeType.REFRESH })
	@JoinTable(name = TJ_NAME_CRITERIA, joinColumns = @JoinColumn(name = TJ_COL_NAME_REPORTING_GROUP), inverseJoinColumns = {
			@JoinColumn(name = "CRITERIA_VALUE", referencedColumnName = "CRITERIA_VALUE"),
			@JoinColumn(name = "CRITERIA_TYPE", referencedColumnName = "CRITERIA_TYPE") })
	private Set<Criteria> criterias;

	@OneToMany(fetch = FetchType.LAZY, cascade = {
			CascadeType.REMOVE }, mappedBy = GroupAttribute.FIELD_REPORTING_GROUP)
	private List<GroupAttribute> groupAttributes;

	@OneToMany(fetch = FetchType.LAZY, cascade = {
			CascadeType.REMOVE }, mappedBy = EntityGroupAttribute.FIELD_REPORTING_GROUP)
	private List<EntityGroupAttribute> entityGroupAttributes;

	@OneToMany(fetch = FetchType.LAZY, cascade = {
			CascadeType.REMOVE }, mappedBy = ReportingGroupPartitionStatus.FIELD_REPORTING_GROUP)
	private List<ReportingGroupPartitionStatus> groupPartitions;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = FilterConfig.FIELD_REPORTING_GROUP)
	private List<FilterConfig> filterConfigs;

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getReportingGroupRef() {
		return reportingGroupRef;
	}

	public void setReportingGroupRef(String reportingGroupRef) {
		this.reportingGroupRef = reportingGroupRef;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public ReportingGroupTypeEnum getType() {
		return type;
	}

	public void setType(ReportingGroupTypeEnum type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
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

	public Date getDeletionDate() {
		return deletionDate;
	}

	public void setDeletionDate(Date deletionDate) {
		this.deletionDate = deletionDate;
	}

	public List<GroupingRule> getGroupingRules() {
		if (groupingRules == null) {
			groupingRules = new ArrayList<>();
		}
		return groupingRules;
	}

	public void setGroupingRules(List<GroupingRule> groupingRules) {
		this.groupingRules = groupingRules;
	}

	public String getReportingGroupType() {
		return reportingGroupType;
	}

	public void setReportingGroupType(String reportingGroupType) {
		this.reportingGroupType = reportingGroupType;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public DataLocation getDataLocation() {
		// if(dataLocation == null){
		// dataLocation = new DataLocation();
		// }
		return dataLocation;
	}

	public void setDataLocation(DataLocation dataLocation) {
		this.dataLocation = dataLocation;
	}

	public List<GroupReportConfig> getGroupReportConfigs() {
		if (groupReportConfigs == null) {
			groupReportConfigs = new ArrayList<>();
		}
		return groupReportConfigs;
	}

	public void setGroupReportConfigs(List<GroupReportConfig> groupReportConfigs) {
		this.groupReportConfigs.clear();
		this.groupReportConfigs.addAll(groupReportConfigs);
	}

	public List<ReportingEntity> getEntities() {
		if (entities == null) {
			entities = new ArrayList<>();
		}
		return entities;
	}

	public void setEntities(List<ReportingEntity> entities) {
		this.entities = entities;
	}

	public List<OfferOption> getReportSourceOptions() {
		if (reportSourceOptions == null) {
			reportSourceOptions = new ArrayList<>();
		}
		return reportSourceOptions;
	}

	public void setReportSourceOptions(List<OfferOption> reportSourceOptions) {
		this.reportSourceOptions = reportSourceOptions;
	}

	public Set<Criteria> getCriterias() {
		if (criterias == null) {
			criterias = new HashSet<>();
		}
		return criterias;
	}

	public void setCriterias(Set<Criteria> criterias) {
		this.criterias = criterias;
	}

	public List<GroupAttribute> getGroupAttributes() {
		if (groupAttributes == null) {
			groupAttributes = new ArrayList<>();
		}
		return groupAttributes;
	}

	public void setGroupAttributes(List<GroupAttribute> groupAttributes) {
		this.groupAttributes = groupAttributes;
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

	public List<ReportingGroupPartitionStatus> getGroupPartitions() {
		if (groupPartitions == null) {
			groupPartitions = new ArrayList<>();
		}
		return groupPartitions;
	}

	public void setGroupPartitions(List<ReportingGroupPartitionStatus> groupPartitions) {
		this.groupPartitions = groupPartitions;
	}

	public List<FilterConfig> getFilterConfigs() {
		if (filterConfigs == null) {
			filterConfigs = new ArrayList<>();
		}
		return filterConfigs;
	}

	public void setFilterConfigs(List<FilterConfig> filterConfigs) {
		this.filterConfigs = filterConfigs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((reportingGroupRef == null) ? 0 : reportingGroupRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ReportingGroup)) {
			return false;
		}
		ReportingGroup other = (ReportingGroup) obj;
		if (origin == null) {
			if (other.origin != null) {
				return false;
			}
		} else if (!origin.equals(other.origin)) {
			return false;
		}
		if (reportingGroupRef == null) {
			if (other.reportingGroupRef != null) {
				return false;
			}
		} else if (!reportingGroupRef.equals(other.reportingGroupRef)) {
			return false;
		}
		return true;
	}
}
