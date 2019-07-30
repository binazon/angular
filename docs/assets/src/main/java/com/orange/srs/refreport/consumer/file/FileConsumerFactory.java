package com.orange.srs.refreport.consumer.file;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class FileConsumerFactory {

	public static String SFTP_CONNECTION = "sftp";
	public static String NAS_CONNECTION = "nas";

	private static Map<String, FileConsumer> mapFileConsumer;

	@PostConstruct
	public void buildSingleton() {
		mapFileConsumer = new HashMap<String, FileConsumer>();
		mapFileConsumer.put(SFTP_CONNECTION, new SFTPFileConsumer());
		mapFileConsumer.put(NAS_CONNECTION, new NASFileConsumer());

	}

	/**
	 * @param type
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static FileConsumer createFileConsumer(String type) throws InstantiationException, IllegalAccessException {
		return mapFileConsumer.get(type.toLowerCase());
	}
}
