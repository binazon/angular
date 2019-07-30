package com.orange.srs.refreport.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PreRemove;
import javax.persistence.Table;

@Entity
@Table(name = "T_PARAM_TYPE")
public class ParamType {

	public static final String FIELD_ENTITY_TYPE = "entityTypeAndSubtype.entityTypeAndSubtypeId.type";
	public static final String FIELD_ENTITY_SUBTYPE = "entityTypeAndSubtype.entityTypeAndSubtypeId.subtype";
	public static final String FIELD_PARENT_TYPE = "parentTypeAndSubtype.entityTypeAndSubtypeId.type";
	public static final String FIELD_PARENT_SUBTYPE = "parentTypeAndSubtype.entityTypeAndSubtypeId.subtype";
	public static final String FIELD_ALIAS = "alias";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumns({ @JoinColumn(name = "ENTITY_TYPE", referencedColumnName = "TYPE"),
			@JoinColumn(name = "ENTITY_SUBTYPE", referencedColumnName = "SUBTYPE") })
	private EntityTypeAndSubtype entityTypeAndSubtype;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumns({ @JoinColumn(name = "PARENT_TYPE", referencedColumnName = "TYPE"),
			@JoinColumn(name = "PARENT_SUBTYPE", referencedColumnName = "SUBTYPE") })
	private EntityTypeAndSubtype parentTypeAndSubtype;

	@Column(name = "ALIAS", nullable = false, unique = true)
	private String alias;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = ReportConfig.FIELD_PARAM_TYPES)
	private Set<ReportConfig> reportConfigs;

	@PreRemove
	public void preRemove() {
		for (ReportConfig reportConfig : this.getReportConfigs()) {
			reportConfig.getParamTypes().remove(this);
		}
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getEntityType() {
		return entityTypeAndSubtype.getType();
	}

	public String getEntitySubtype() {
		return entityTypeAndSubtype.getSubtype();
	}

	public String getParentType() {
		return parentTypeAndSubtype.getType();
	}

	public String getParentSubtype() {
		return parentTypeAndSubtype.getSubtype();
	}

	public EntityTypeAndSubtype getEntityTypeAndSubtype() {
		return entityTypeAndSubtype;
	}

	public void setEntityTypeAndSubtype(EntityTypeAndSubtype entityTypeAndSubtype) {
		this.entityTypeAndSubtype = entityTypeAndSubtype;
	}

	public EntityTypeAndSubtype getParentTypeAndSubtype() {
		return parentTypeAndSubtype;
	}

	public void setParentTypeAndSubtype(EntityTypeAndSubtype parentTypeAndSubtype) {
		this.parentTypeAndSubtype = parentTypeAndSubtype;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Set<ReportConfig> getReportConfigs() {
		if (reportConfigs == null) {
			reportConfigs = new HashSet<ReportConfig>();
		}
		return reportConfigs;
	}

	public void setReportConfigs(Set<ReportConfig> reportConfigs) {
		this.reportConfigs = reportConfigs;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ParamType [pk=");
		builder.append(pk);
		builder.append(", entityType=");
		builder.append(entityTypeAndSubtype.getType());
		builder.append(", entitySubtype=");
		builder.append(entityTypeAndSubtype.getSubtype());
		builder.append(", parentType=");
		builder.append(parentTypeAndSubtype.getType());
		builder.append(", parentSubtype=");
		builder.append(parentTypeAndSubtype.getSubtype());
		builder.append(", alias=");
		builder.append(alias);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityTypeAndSubtype == null) ? 0 : entityTypeAndSubtype.hashCode());
		result = prime * result + ((parentTypeAndSubtype == null) ? 0 : parentTypeAndSubtype.hashCode());
		// Do not create hashCode with variables pk and alias
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		ParamType other = (ParamType) obj;
		if (entityTypeAndSubtype == null) {
			if (other.getEntityTypeAndSubtype() != null) {
				return false;
			}
		} else if (!entityTypeAndSubtype.equals(other.getEntityTypeAndSubtype())) {
			return false;
		}
		if (parentTypeAndSubtype == null) {
			if (other.getParentTypeAndSubtype() != null) {
				return false;
			}
		} else if (!parentTypeAndSubtype.equals(other.getParentTypeAndSubtype())) {
			return false;
		}
		// Do not compare on the pk and alias variables
		return true;
	}
}
