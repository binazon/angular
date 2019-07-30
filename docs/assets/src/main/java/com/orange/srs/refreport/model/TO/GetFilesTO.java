package com.orange.srs.refreport.model.TO;

import java.io.Serializable;
import java.util.ArrayList;

import com.orange.srs.statcommon.technical.Utils;

public class GetFilesTO implements Serializable {

	private static final long serialVersionUID = -4258164341447104710L;

	private ArrayList<FileStatusTO> retrievedFileList;
	private ArrayList<FileStatusTO> notRetrievedFileList;

	public GetFilesTO() {
		retrievedFileList = new ArrayList<>();
		notRetrievedFileList = new ArrayList<>();
	}

	public void addRetrievedFile(String fileName, Long startTime) {
		FileStatusTO file = new FileStatusTO(fileName, Utils.getTime() - startTime);
		retrievedFileList.add(file);
	}

	public void addNotRetrievedFile(String fileName) {
		FileStatusTO file = new FileStatusTO(fileName);
		file.emptyOrError = true;
		notRetrievedFileList.add(file);
	}

	public ArrayList<FileStatusTO> getRetrievedFileFlist() {
		return retrievedFileList;
	}

	public ArrayList<FileStatusTO> getNotRetrievedFileFlist() {
		return notRetrievedFileList;
	}
}
