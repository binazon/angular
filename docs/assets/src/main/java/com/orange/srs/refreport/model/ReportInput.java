package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "T_REPORT_INPUT", uniqueConstraints = @UniqueConstraint(columnNames = { "GRANULARITY", "REPORT_INPUT_REF",
		"SOURCE_TIME_UNIT" }))
public class ReportInput {

	public static final String FIELD_PK = "pk";
	public static final String FIELD_REPORT_INPUT_REF = "reportInputRef";
	public static final String FIELD_LOCATION_PATTERN_PREFIX = "locationPatternPrefix";
	public static final String FIELD_LOCATION_PATTERN_SUFFIX = "locationPatternSuffix";
	public static final String FIELD_SOURCE = "source";
	public static final String FIELD_GRANULARITY = "granularity";
	public static final String FIELD_SOURCE_TIME_UNIT = "sourceTimeUnit";
	public static final String FIELD_TYPEDB = "typeDb";
	public static final String FIELD_TABLEDB = "tableDb";
	public static final String FIELD_REPORT_INPUT_SOURCE_CLASS = "reportInputSourceClass";
	public static final String FIELD_FORMAT = "format";

	@Id
	@Column(name = "PK")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "REPORT_INPUT_REF", nullable = false)
	private String reportInputRef;

	@Column(name = "LOCATION_PATTERN_PREFIX", nullable = true)
	private String locationPatternPrefix;

	@Column(name = "LOCATION_PATTERN_SUFFIX", nullable = true)
	private String locationPatternSuffix;

	@Column(name = "SOURCE", nullable = false)
	private String source;

	@Column(name = "GRANULARITY", nullable = false)
	private String granularity;

	@Column(name = "SOURCE_TIME_UNIT", nullable = false)
	private String sourceTimeUnit;

	@Column(name = "TYPE_DB")
	private String typeDb;

	@Column(name = "TABLE_DB")
	private String tableDb;

	@ManyToOne
	@JoinColumn(name = "SOURCE_CLASS_FK")
	private SourceClass reportInputSourceClass;

	@ManyToOne
	@JoinColumn(name = "INPUT_FORMAT_FK")
	private InputFormat format;

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getReportInputRef() {
		return reportInputRef;
	}

	public void setReportInputRef(String reportInputRef) {
		this.reportInputRef = reportInputRef;
	}

	public String getLocationPatternPrefix() {
		return locationPatternPrefix;
	}

	public void setLocationPatternPrefix(String locationPatternPrefix) {
		this.locationPatternPrefix = locationPatternPrefix;
	}

	public String getLocationPatternSuffix() {
		return locationPatternSuffix;
	}

	public void setLocationPatternSuffix(String locationPatternSuffix) {
		this.locationPatternSuffix = locationPatternSuffix;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getGranularity() {
		return granularity;
	}

	public void setGranularity(String granularity) {
		this.granularity = granularity;
	}

	public String getSourceTimeUnit() {
		return sourceTimeUnit;
	}

	public void setSourceTimeUnit(String sourceTimeUnit) {
		this.sourceTimeUnit = sourceTimeUnit;
	}

	public String getTypeDb() {
		return typeDb;
	}

	public void setTypeDb(String typeDb) {
		this.typeDb = typeDb;
	}

	public String getTableDb() {
		return tableDb;
	}

	public void setTableDb(String tableDb) {
		this.tableDb = tableDb;
	}

	public SourceClass getReportInputSourceClass() {
		return reportInputSourceClass;
	}

	public void setReportInputSourceClass(SourceClass reportInputSourceClass) {
		this.reportInputSourceClass = reportInputSourceClass;
	}

	public InputFormat getFormat() {
		return format;
	}

	public void setFormat(InputFormat format) {
		this.format = format;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((granularity == null) ? 0 : granularity.hashCode());
		result = prime * result + ((locationPatternPrefix == null) ? 0 : locationPatternPrefix.hashCode());
		result = prime * result + ((locationPatternSuffix == null) ? 0 : locationPatternSuffix.hashCode());
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		result = prime * result + ((reportInputRef == null) ? 0 : reportInputRef.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((sourceTimeUnit == null) ? 0 : sourceTimeUnit.hashCode());
		result = prime * result + ((tableDb == null) ? 0 : tableDb.hashCode());
		result = prime * result + ((typeDb == null) ? 0 : typeDb.hashCode());
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
		ReportInput other = (ReportInput) obj;
		if (granularity == null) {
			if (other.granularity != null)
				return false;
		} else if (!granularity.equals(other.granularity))
			return false;
		if (locationPatternPrefix == null) {
			if (other.locationPatternPrefix != null)
				return false;
		} else if (!locationPatternPrefix.equals(other.locationPatternPrefix))
			return false;
		if (locationPatternSuffix == null) {
			if (other.locationPatternSuffix != null)
				return false;
		} else if (!locationPatternSuffix.equals(other.locationPatternSuffix))
			return false;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		if (reportInputRef == null) {
			if (other.reportInputRef != null)
				return false;
		} else if (!reportInputRef.equals(other.reportInputRef))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (sourceTimeUnit == null) {
			if (other.sourceTimeUnit != null)
				return false;
		} else if (!sourceTimeUnit.equals(other.sourceTimeUnit))
			return false;
		if (tableDb == null) {
			if (other.tableDb != null)
				return false;
		} else if (!tableDb.equals(other.tableDb))
			return false;
		if (typeDb == null) {
			if (other.typeDb != null)
				return false;
		} else if (!typeDb.equals(other.typeDb))
			return false;
		return true;
	}
}
