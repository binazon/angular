package com.orange.srs.refreport.model.TO;

public class GetFileNameToRetrieveTO {

	private String sourceFileName;
	private String destFileName;
	private String sourceFilePath;

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public String getDestFileName() {
		return destFileName;
	}

	public void setDestFileName(String destFileName) {
		this.destFileName = destFileName;
	}

	public String getSourceFilePath() {
		return sourceFilePath;
	}

	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	public GetFileNameToRetrieveTO(String sourceFileName, String sourceFilePath, String destFileName) {
		this.destFileName = destFileName;
		this.sourceFilePath = sourceFilePath;
		this.sourceFileName = sourceFileName;
	}
}
