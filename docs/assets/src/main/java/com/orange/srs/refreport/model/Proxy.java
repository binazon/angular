package com.orange.srs.refreport.model;

import java.io.IOException;
import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.orange.srs.refreport.technical.FSTSerializer;

@Entity
@Table(name = "T_PROXY")
public class Proxy {

	public static final String FIELD_PK = "pk";
	public static final String FIELD_URI = "uri";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_VERSION = "version";
	public static final String FIELD_ISSSL = "isSsl";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "URI", nullable = false)
	private byte[] uri;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "VERSION")
	private String version;

	@Column(name = "NAME")
	private String name;

	@Column(name = "ISSSL")
	private boolean isSsl;

	public Proxy() {
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public URI getUri() {
		URI object = null;
		try {
			object = (URI) FSTSerializer.unserialize(uri);
		} catch (Exception e) {
			throw new RuntimeException("Unable to unserialized URI: " + e.getMessage(), e);
		}
		return object;
	}

	public void setUri(URI uri) {
		byte[] bytes = null;
		try {
			bytes = FSTSerializer.serialize(uri);
		} catch (IOException e) {
			throw new RuntimeException("Unable to serialized URI: " + e.getMessage(), e);
		}
		this.uri = bytes;
		if (uri != null) {
			description = uri.toString();
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSsl() {
		return isSsl;
	}

	public void setSsl(boolean isSsl) {
		this.isSsl = isSsl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (isSsl ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		Proxy other = (Proxy) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (isSsl != other.isSsl)
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
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
}
