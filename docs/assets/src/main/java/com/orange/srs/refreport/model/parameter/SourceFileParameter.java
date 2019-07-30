package com.orange.srs.refreport.model.parameter;

import java.util.Calendar;

public class SourceFileParameter {
	private String sourceFileName;
	private String destinationFileName;

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName, String extension) {
		this.sourceFileName = sourceFileName + "." + extension;
	}

	public String getDestinationFileName() {
		return destinationFileName;
	}

	public void setDestinationFileName(String destinationFileName, Calendar retrievingDate, String extension) {
		this.destinationFileName = destinationFileName + "." + extension;
	}

	public SourceFileParameter(String sourceFileName, String destinationFileName, String extension,
			Calendar retrievingDate) {

		this.sourceFileName = sourceFileName + "." + extension;
		this.destinationFileName = destinationFileName + "." + extension;
	}

	public SourceFileParameter(String sourceFileName, String destinationFileName) {
		this.sourceFileName = sourceFileName;
		this.destinationFileName = destinationFileName;
	}
}
