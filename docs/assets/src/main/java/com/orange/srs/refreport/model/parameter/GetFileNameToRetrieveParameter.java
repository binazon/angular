package com.orange.srs.refreport.model.parameter;

import java.util.Calendar;

public class GetFileNameToRetrieveParameter {
	public String fileName;
	public String filePath;
	public String fileExtension;
	public String filePattern;
	public Calendar fileDate;

	/**
	 * 
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 
	 * @return
	 */
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * getter to the fileDate attribute
	 * 
	 * @return the attribute fileDate
	 */
	public Calendar getFileDate() {
		return fileDate;
	}

	public void setFileDate(Calendar fileDate) {
		this.fileDate = fileDate;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	public String getFilePattern() {
		return this.filePattern;
	}

	public GetFileNameToRetrieveParameter(String fileName, String filePath, String fileExtension, Calendar fileDate,
			String filePattern) {

		this.fileName = fileName;
		this.filePath = filePath;
		this.fileExtension = fileExtension;
		this.fileDate = fileDate;
		this.filePattern = filePattern;
	}
}
