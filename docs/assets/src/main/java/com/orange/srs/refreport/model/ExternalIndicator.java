package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import com.orange.srs.statcommon.model.enums.ComputeScopeEnum;

@Entity
@Table(name = "T_EXTERNAL_INDICATOR")
public class ExternalIndicator extends Bookmark {

	public static final String FIELD_LABEL = "label";
	public static final String FIELD_COMPUTE_SCOPE = "computeScope";

	@Id
	@Column(name = "LABEL", unique = true)
	private String label;

	@Column(name = "COMPUTE_SCOPE", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private ComputeScopeEnum computeScope;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ComputeScopeEnum getComputeScope() {
		return computeScope;
	}

	public void setComputeScope(ComputeScopeEnum computeScope) {
		this.computeScope = computeScope;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ExternalIndicator)) {
			return false;
		}
		ExternalIndicator other = (ExternalIndicator) obj;
		if (!StringUtils.equals(label, other.label)) {
			return false;
		}
		if (computeScope != other.computeScope) {
			return false;
		}
		return true;
	}
}
