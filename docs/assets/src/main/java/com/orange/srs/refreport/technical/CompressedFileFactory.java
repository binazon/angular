package com.orange.srs.refreport.technical;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class CompressedFileFactory {

	public static String GZ_COMPRESSED_FILE = ".gz";
	public static String Z_COMPRESSED_FILE = ".z";

	private static Map<String, CompressedFile> mapCompressedFile;

	@PostConstruct
	public void buildSingleton() {
		mapCompressedFile = new HashMap<String, CompressedFile>();
		mapCompressedFile.put(GZ_COMPRESSED_FILE, new GZCompressedFile());
		mapCompressedFile.put(Z_COMPRESSED_FILE, new ZCompressedFile());

	}

	/**
	 * @param type
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static CompressedFile createCompressedFile(String type)
			throws InstantiationException, IllegalAccessException {
		return mapCompressedFile.get(type.toLowerCase());
	}
}
