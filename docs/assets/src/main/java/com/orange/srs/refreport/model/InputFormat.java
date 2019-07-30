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
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "T_INPUT_FORMAT")
public class InputFormat {

	public static final String FIELD_FORMAT_TYPE = "formatType";
	public static final String FIELD_COLUMNS = "columns";
	public static final String FIELD_PK = "pk";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "FORMAT_TYPE", nullable = false, unique = true)
	private String formatType;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "INPUT_FORMAT_FK")
	private List<InputColumn> columns;

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
	 * Get the property formatType
	 *
	 * @return the formatType value
	 */
	public String getFormatType() {
		return formatType;
	}

	/**
	 * Set the property formatType
	 *
	 * @param formatType
	 *            the formatType to set
	 */
	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}

	/**
	 * Get the property columns
	 *
	 * @return the columns value
	 */
	public List<InputColumn> getColumns() {
		if (columns == null) {
			columns = new ArrayList<InputColumn>();
		}
		return columns;
	}

	/**
	 * Set the property columns
	 *
	 * @param columns
	 *            the columns to set
	 */
	public void setColumns(List<InputColumn> columns) {
		this.columns = columns;
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
		result = prime * result + ((columns == null) ? 0 : columns.hashCode());
		result = prime * result + ((formatType == null) ? 0 : formatType.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
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
		InputFormat other = (InputFormat) obj;
		if (columns == null) {
			if (other.columns != null)
				return false;
		} else if (!columns.equals(other.columns))
			return false;
		if (formatType == null) {
			if (other.formatType != null)
				return false;
		} else if (!formatType.equals(other.formatType))
			return false;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}

}
