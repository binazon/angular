package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "T_INPUT_COLUMN")
public class InputColumn {

	public static final String FIELD_ALIAS = "alias";
	public static final String FIELD_COLUMN_NAME = "columnName";
	public static final String FIELD_DATE_FORMAT = "dataFormat";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_DEFAULT_VALUE = "defaultValue";
	public static final String FIELD_COMMENTS = "comments";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "ALIAS")
	private String alias;

	@Column(name = "COLUMN_NAME", length = 1000)
	private String columnName;

	@Column(name = "DATA_FORMAT")
	private String dataFormat;

	@Column(name = "TYPE")
	private String type;

	@Column(name = "DEFAULT_VALUE")
	private String defaultValue;

	@Column(name = "COMMENTS", length = 2000)
	private String comments;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "INPUT_FORMAT_FK")
	private InputFormat inputFormat;

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public InputFormat getInputFormat() {
		return inputFormat;
	}

	public void setInputFormat(InputFormat inputFormat) {
		this.inputFormat = inputFormat;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result + ((comments == null) ? 0 : comments.hashCode());
		result = prime * result + ((dataFormat == null) ? 0 : dataFormat.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
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
		InputColumn other = (InputColumn) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (comments == null) {
			if (other.comments != null)
				return false;
		} else if (!comments.equals(other.comments))
			return false;
		if (dataFormat == null) {
			if (other.dataFormat != null)
				return false;
		} else if (!dataFormat.equals(other.dataFormat))
			return false;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		return true;
	}

}
