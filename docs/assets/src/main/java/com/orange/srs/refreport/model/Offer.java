package com.orange.srs.refreport.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "T_OFFER")
public class Offer {

	public static final String FIELD_PK = "pk";
	public static final String FIELD_ALIAS = "alias";
	public static final String FIELD_COMMERCIAL_NAME = "commercialName";
	public static final String FIELD_OFFER_OPTIONS = "offerOptions";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "ALIAS", nullable = false)
	private String alias;

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = OfferOption.FIELD_RELATED_0FFER)
	private List<OfferOption> offerOptions;

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public List<OfferOption> getOfferOptions() {
		if (offerOptions == null) {
			offerOptions = new ArrayList<OfferOption>();
		}
		return offerOptions;
	}

	public void setOfferOptions(List<OfferOption> offerOptions) {
		this.offerOptions = offerOptions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Offer [pk=");
		builder.append(pk);
		builder.append(", name=");
		builder.append(name);
		builder.append(", alias=");
		builder.append(alias);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
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
		Offer other = (Offer) obj;
		if (alias == null) {
			if (other.alias != null) {
				return false;
			}
		} else if (!alias.equals(other.alias)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (pk == null) {
			if (other.pk != null) {
				return false;
			}
		} else if (!pk.equals(other.pk)) {
			return false;
		}
		return true;
	}

}
