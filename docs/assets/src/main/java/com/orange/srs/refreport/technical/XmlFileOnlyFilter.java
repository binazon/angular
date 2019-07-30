package com.orange.srs.refreport.technical;

import java.io.File;
import java.io.FilenameFilter;

/**
 * This class is a FilenameFilter that match only template files
 * 
 * @author trt
 *
 */
public class XmlFileOnlyFilter implements FilenameFilter {

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File dir, String name) {
		String path = dir.getAbsolutePath() + File.separator + name;
		File f = new File(path);
		if (f.isFile()) {
			int dotPos = name.lastIndexOf(".");
			return (dotPos != -1 && name.substring(dotPos).equals(".xml"));
		} else {
			return false;
		}
	}
}
