package com.orange.srs.refreport.technical;

import java.io.File;
import java.io.FilenameFilter;

/**
 * This class is a FilenameFilter that match only directories
 * 
 * @author trt
 *
 */
public class DirectoryOnlyFilter implements FilenameFilter {

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File dir, String name) {
		return dir.isDirectory();
	}
}
