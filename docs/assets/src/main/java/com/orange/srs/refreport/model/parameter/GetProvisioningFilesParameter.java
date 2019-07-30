package com.orange.srs.refreport.model.parameter;

import java.util.ArrayList;

public class GetProvisioningFilesParameter {
	public ArrayList<SourceFileParameter> FileList;
	public String user;
	public String password;
	public String source;
	public String protocol;

	public ArrayList<SourceFileParameter> getFileFileList() {
		return FileList;
	}

	public void setFileList(ArrayList<SourceFileParameter> FileList) {
		this.FileList = FileList;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public GetProvisioningFilesParameter(ArrayList<SourceFileParameter> FileList, String user, String password,
			String source, String protocol) {

		this.FileList = FileList;
		this.user = user;
		this.password = password;
		this.source = source;
		this.protocol = protocol;
	}
}
