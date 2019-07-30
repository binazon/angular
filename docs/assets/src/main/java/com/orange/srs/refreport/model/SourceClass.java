package com.orange.srs.refreport.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "T_SOURCE_CLASS")
public class SourceClass {

	public static final String FIELD_SOURCE_CLASS = "sourceClass";

	@Id
	@Column(name = "SOURCE_CLASS")
	private String sourceClass;

	@OneToMany(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "SOURCE_CLASS_FK")
	private List<ReportInput> producedInputs;

	@OneToMany(mappedBy = InputSource.FIELD_SOURCE_CLASS, cascade = { CascadeType.DETACH, CascadeType.REFRESH })
	private List<InputSource> inputSources;

	public String getSourceClass() {
		return sourceClass;
	}

	public void setSourceClass(String sourceClass) {
		this.sourceClass = sourceClass;
	}

	public List<ReportInput> getProducedInputs() {
		if (producedInputs == null) {
			producedInputs = new ArrayList<ReportInput>();
		}
		return producedInputs;
	}

	public void setProducedInputs(List<ReportInput> producedInputs) {
		this.producedInputs = producedInputs;
	}

	public List<InputSource> getInputSources() {
		if (inputSources == null) {
			inputSources = new ArrayList<InputSource>();
		}
		return inputSources;
	}

	public void setInputSources(List<InputSource> inputSources) {
		this.inputSources = inputSources;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputSources == null) ? 0 : inputSources.hashCode());
		result = prime * result + ((sourceClass == null) ? 0 : sourceClass.hashCode());
		result = prime * result + ((producedInputs == null) ? 0 : producedInputs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceClass other = (SourceClass) obj;
		if (inputSources == null) {
			if (other.inputSources != null)
				return false;
		} else if (!inputSources.equals(other.inputSources))
			return false;
		if (sourceClass == null) {
			if (other.sourceClass != null)
				return false;
		} else if (!sourceClass.equals(other.sourceClass))
			return false;
		if (producedInputs == null) {
			if (other.producedInputs != null)
				return false;
		} else if (!producedInputs.equals(other.producedInputs))
			return false;
		return true;
	}
}
