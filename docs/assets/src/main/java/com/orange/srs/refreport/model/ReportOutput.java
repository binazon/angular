package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;

/**
 * 
 * @author A159138
 */
@Entity
@Table(name = "T_REPORT_OUTPUT")
public class ReportOutput {

	public static final String FIELD_PK = "pk";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_URI = "uri";
	public static final String FIELD_COMPRESSION = "compression";
	public static final String FIELD_LOCATION_PATTERN_PREFIX = "locationPatternPrefix";
	public static final String FIELD_LOCATION_PATTERN_SUFFIX = "locationPatternSuffix";
	public static final String FIELD_FORMAT = "format";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "TYPE")
	@Enumerated(value = EnumType.STRING)
	private ReportOutputTypeEnum type;

	@Column(name = "URI", nullable = false)
	private String uri;

	@Column(name = "LOCATION_PATTERN_PREFIX", nullable = false)
	private String locationPatternPrefix;

	@Column(name = "LOCATION_PATTERN_SUFFIX", nullable = true)
	private String locationPatternSuffix;

	@Column(name = "FORMAT")
	private String format;

	// IP 2262
	@Column(name = "COMPRESSION")
	private String compression;

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
	 * Get the property type
	 * 
	 * @return the type value
	 */
	public ReportOutputTypeEnum getType() {
		return type;
	}

	/**
	 * Set the property type
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(ReportOutputTypeEnum type) {
		this.type = type;
	}

	/**
	 * Get the property uri
	 * 
	 * @return the uri value
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the property uri
	 * 
	 * @param uri
	 *            the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Get the property locationPatternPrefix
	 * 
	 * @return the locationPatternPrefix value
	 */
	public String getLocationPatternPrefix() {
		return locationPatternPrefix;
	}

	/**
	 * Set the property locationPatternPrefix
	 * 
	 * @param locationPatternPrefix
	 *            the locationPatternPrefix to set
	 */
	public void setLocationPatternPrefix(String locationPatternPrefix) {
		this.locationPatternPrefix = locationPatternPrefix;
	}

	/**
	 * Get the property locationPatternSuffix
	 * 
	 * @return the locationPatternSuffix value
	 */
	public String getLocationPatternSuffix() {
		return locationPatternSuffix;
	}

	/**
	 * Set the property locationPatternSuffix
	 * 
	 * @param locationPatternSuffix
	 *            the locationPatternSuffix to set
	 */
	public void setLocationPatternSuffix(String locationPatternSuffix) {
		this.locationPatternSuffix = locationPatternSuffix;
	}

	/**
	 * Get the property format
	 * 
	 * @return the format value
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Set the property format
	 * 
	 * @param format
	 *            the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	// IP2262
	/**
	 * Get the property compression
	 * 
	 * @return the compression value
	 */
	public String getCompression() {
		return compression;
	}

	/**
	 * Set the property compression
	 * 
	 * @param compression
	 *            the compression to set
	 */
	public void setCompression(String compression) {
		this.compression = compression;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReportOutput [pk=");
		builder.append(pk);
		builder.append(", type=");
		builder.append(type);
		builder.append(", uri=");
		builder.append(uri);
		builder.append(", locationPatternPrefix=");
		builder.append(locationPatternPrefix);
		builder.append(", locationPatternSuffix=");
		builder.append(locationPatternSuffix);
		builder.append(", format=");
		builder.append(format);
		builder.append(", compression=");
		builder.append(compression);
		builder.append("]");
		return builder.toString();
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
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((locationPatternPrefix == null) ? 0 : locationPatternPrefix.hashCode());
		result = prime * result + ((locationPatternSuffix == null) ? 0 : locationPatternSuffix.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		result = prime * result + ((compression == null) ? 0 : compression.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
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
		ReportOutput other = (ReportOutput) obj;

		if (format == null) {
			if (other.format != null) {
				return false;
			}
		} else if (!format.equals(other.format)) {
			return false;
		}
		// IP2262
		if (compression == null) {
			if (other.compression != null) {
				return false;
			}
		} else if (!compression.equals(other.compression)) {
			return false;
		}
		if (locationPatternPrefix == null) {
			if (other.locationPatternPrefix != null) {
				return false;
			}
		} else if (!locationPatternPrefix.equals(other.locationPatternPrefix)) {
			return false;
		}
		if (locationPatternSuffix == null) {
			if (other.locationPatternSuffix != null) {
				return false;
			}
		} else if (!locationPatternSuffix.equals(other.locationPatternSuffix)) {
			return false;
		}
		if (pk == null) {
			if (other.pk != null) {
				return false;
			}
		} else if (!pk.equals(other.pk)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

}
