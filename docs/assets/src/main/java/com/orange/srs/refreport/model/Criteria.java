package com.orange.srs.refreport.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "T_CRITERIA")
public class Criteria {

	public final static String FIELD_CRITERIA_TYPE = "criteriaId.criteriaType";
	public final static String FIELD_CRITERIA_VALUE = "criteriaId.criteriaValue";

	@EmbeddedId
	private CriteriaId criteriaId;

	public CriteriaId getCriteriaId() {
		return criteriaId;
	}

	public void setCriteriaId(CriteriaId criteriaId) {
		this.criteriaId = criteriaId;
	}

	public String getCriteriaType() {
		if (criteriaId == null) {
			criteriaId = new CriteriaId();
		}
		return criteriaId.getCriteriaType();
	}

	public String getCriteriaValue() {
		if (criteriaId == null) {
			criteriaId = new CriteriaId();
		}
		return criteriaId.getCriteriaValue();
	}

	public void setCriteriaType(String criteriaType) {
		if (criteriaId == null) {
			criteriaId = new CriteriaId();
		}
		this.criteriaId.setCriteriaType(criteriaType);
	}

	public void setCriteriaValue(String criteriaValue) {
		if (criteriaId == null) {
			criteriaId = new CriteriaId();
		}
		this.criteriaId.setCriteriaValue(criteriaValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((criteriaId == null) ? 0 : criteriaId.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		Criteria other = (Criteria) obj;
		if (criteriaId == null) {
			if (other.criteriaId != null) {
				return false;
			}
		} else if (!criteriaId.equals(other.criteriaId)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Criteria [criteriaId=");
		builder.append(criteriaId);
		builder.append("]");
		return builder.toString();
	}

}
