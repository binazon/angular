package com.orange.srs.refreport.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.Table;

import com.orange.srs.refreport.model.enumerate.InputSourceStateEnum;

@Entity
@Table(name = "T_INPUT_SOURCE")
public class InputSource {

	public static final String FIELD_NAME = "name";
	public static final String FIELD_SOURCE_CLASS = "sourceClass";
	public static final String FIELD_POLLING_STATE = "pollingState";
	public static final String FIELD_PROXIES = "proxies";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Lob
	@Column(name = "SOURCE_CONFIGURATION", columnDefinition = "TEXT")
	private String configuration;

	@ManyToOne
	@JoinColumn(name = "SOURCE_CLASS_FK")
	private SourceClass sourceClass;

	@Column(name = "SOURCE_NAME")
	private String name;

	@Column(name = "STATE")
	private InputSourceStateEnum state;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "INPUT_SOURCE_FK")
	@OrderBy(SourceProxy.PROXY_INDEX)
	private List<SourceProxy> proxies;

	@Lob
	@Column(name = "POLLING_STATE", columnDefinition = "TEXT")
	private String pollingState;

	@PostPersist
	public void postPersist() {
		if (sourceClass != null) {
			sourceClass.getInputSources().add(this);
		}
	}

	@PreRemove
	public void preRemove() {
		if (sourceClass != null) {
			sourceClass.getInputSources().remove(this);
		}
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public SourceClass getSourceClass() {
		return sourceClass;
	}

	public void setSourceClass(SourceClass sourceClass) {
		this.sourceClass = sourceClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InputSourceStateEnum getState() {
		return state;
	}

	public void setState(InputSourceStateEnum state) {
		this.state = state;
	}

	public List<SourceProxy> getProxies() {
		if (proxies == null) {
			return new ArrayList<SourceProxy>();
		}
		return proxies;
	}

	public void setProxies(List<SourceProxy> proxies) {
		this.proxies = proxies;
	}

	public String getPollingState() {
		return pollingState;
	}

	public void setPollingState(String pollingState) {
		this.pollingState = pollingState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((pollingState == null) ? 0 : pollingState.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		InputSource other = (InputSource) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		if (pollingState == null) {
			if (other.pollingState != null)
				return false;
		} else if (!pollingState.equals(other.pollingState))
			return false;
		if (state != other.state)
			return false;
		return true;
	}
}
