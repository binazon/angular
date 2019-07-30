package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CriteriaId implements Serializable {

	private static final long serialVersionUID = -8134898075680859573L;

	@Column(name = "CRITERIA_TYPE", nullable = false)
	private String criteriaType;

	@Column(name = "CRITERIA_VALUE", nullable = false)
	private String criteriaValue;

	public String getCriteriaType() {
		return criteriaType;
	}

	public String getCriteriaValue() {
		return criteriaValue;
	}

	public void setCriteriaType(String criteriaType) {
		this.criteriaType = criteriaType;
	}

	public void setCriteriaValue(String criteriaValue) {
		this.criteriaValue = criteriaValue;
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
		result = prime * result + ((criteriaType == null) ? 0 : criteriaType.hashCode());
		result = prime * result + ((criteriaValue == null) ? 0 : criteriaValue.hashCode());
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
		CriteriaId other = (CriteriaId) obj;
		if (criteriaType == null) {
			if (other.criteriaType != null) {
				return false;
			}
		} else if (!criteriaType.equals(other.criteriaType)) {
			return false;
		}
		if (criteriaValue == null) {
			if (other.criteriaValue != null) {
				return false;
			}
		} else if (!criteriaValue.equals(other.criteriaValue)) {
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
		builder.append("CriteriaId [criteriaType=");
		builder.append(criteriaType);
		builder.append(", criteriaValue=");
		builder.append(criteriaValue);
		builder.append("]");
		return builder.toString();
	}
}
