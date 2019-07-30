package com.orange.srs.refreport.technical;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

public class FSTSerializer {

	public static FSTConfiguration conf;
	static {
		conf = FSTConfiguration.createDefaultConfiguration();
		conf.registerSerializer(Timestamp.class, new FSTTimestampSerializer(), false);
		conf.setPreferSpeed(false);
	}

	public static byte[] serialize(Object toWrite) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		FSTObjectOutput out = conf.getObjectOutput(b);
		byte[] bytes = null;
		try {
			out.writeObject(toWrite);
			out.flush();
			bytes = b.toByteArray();
		} finally {
			// DON'T out.close() when using factory method;
			b.close();
		}
		return bytes;
	}

	public static Object unserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		FSTObjectInput in = conf.getObjectInput(b);
		Object obj = null;
		try {
			obj = in.readObject();
		} finally {
			// DON'T: in.close(); here prevents reuse and will result in an exception
			b.close();
		}
		return obj;
	}
}
