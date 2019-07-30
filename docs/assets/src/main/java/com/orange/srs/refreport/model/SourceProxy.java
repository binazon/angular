package com.orange.srs.refreport.model;

import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.orange.srs.refreport.model.enumerate.SourceProxyStateEnum;

@Entity
@Table(name = "T_SOURCE_PROXY")
public class SourceProxy {

	public static final String PROXY_INDEX = "index";
	public static final String FIELD_PROXY = "proxy";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "PROXY_INDEX")
	private int index;

	@Column(name = "STATE")
	private SourceProxyStateEnum state;

	@Lob
	@Column(name = "KO_CAUSE", columnDefinition = "TEXT")
	private String koCause;

	@ManyToOne
	@JoinColumn(name = "PROXY_FK", nullable = false)
	private Proxy proxy;

	/**
	 * Default Constructor
	 */
	public SourceProxy() {
	}

	/**
	 * Constructor
	 * 
	 * @param pindex
	 * @param pproxy
	 */
	public SourceProxy(int pindex, Proxy pproxy) {
		proxy = pproxy;
		index = pindex;
	}

	/**
	 * Get the property pk
	 *
	 * @return the pk value
	 */
	public Long getPk() {
		return pk;
	}

	/**
	 * Set the property pk
	 *
	 * @param pk
	 *            the pk to set
	 */
	public void setPk(Long pk) {
		this.pk = pk;
	}

	/**
	 * Get the property index
	 *
	 * @return the index value
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Set the property index
	 *
	 * @param index
	 *            the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Get the property state
	 *
	 * @return the state value
	 */
	public SourceProxyStateEnum getState() {
		return state;
	}

	/**
	 * Set the property state
	 *
	 * @param state
	 *            the state to set
	 */
	public void setState(SourceProxyStateEnum state) {
		this.state = state;
	}

	/**
	 * Get the property koCause
	 *
	 * @return the koCause value
	 */
	public String getKoCause() {
		return koCause;
	}

	/**
	 * Set the property koCause
	 *
	 * @param koCause
	 *            the koCause to set
	 */
	public void setKoCause(String koCause) {
		this.state = SourceProxyStateEnum.KO;
		this.koCause = koCause;
	}

	/**
	 * Get the property proxy id
	 *
	 * @return the proxy id value
	 */
	public Long getProxyId() {
		return proxy.getPk();
	}

	/**
	 * Get the property proxy uri
	 *
	 * @return the proxy uri value
	 */
	public URI getUri() {
		return proxy.getUri();
	}

	/**
	 * Get the property proxy name
	 *
	 * @return the proxy name value
	 */
	public String getName() {
		return proxy.getName();
	}

	/**
	 * Get the property proxy version
	 *
	 * @return the proxy version value
	 */
	public String getVersion() {
		return proxy.getVersion();
	}

	/**
	 * Get the property proxy ssl
	 *
	 * @return the proxy ssl value
	 */
	public boolean isSsL() {
		return proxy.isSsl();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + ((koCause == null) ? 0 : koCause.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((proxy == null) ? 0 : proxy.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceProxy other = (SourceProxy) obj;
		if (index != other.index)
			return false;
		if (koCause == null) {
			if (other.koCause != null)
				return false;
		} else if (!koCause.equals(other.koCause))
			return false;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		if (proxy == null) {
			if (other.proxy != null)
				return false;
		} else if (!proxy.equals(other.proxy))
			return false;
		if (state != other.state)
			return false;
		return true;
	}

}
