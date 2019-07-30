package com.orange.srs.refreport.technical;

import java.io.IOException;
import java.sql.Timestamp;

import de.ruedigermoeller.serialization.FSTBasicObjectSerializer;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

public class FSTTimestampSerializer extends FSTBasicObjectSerializer {
	@Override
	public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo,
			FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
		out.writeFLong(((Timestamp) toWrite).getTime());
	}

	/**
	 * @return true if FST can skip a search for same instances in the serialized ObjectGraph. This speeds up reading
	 *         and writing and makes sense for short immutable such as Integer, Short, Character, Date, .. . For those
	 *         classes it is more expensive (CPU, size) to do a lookup than to just write the Object twice in case.
	 */
	@Override
	public boolean alwaysCopy() {
		return true;
	}

	@Override
	public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
			FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		long l = in.readFLong();
		Object res = new Timestamp(l);
		in.registerObject(res, streamPositioin, serializationInfo, referencee);
		return res;
	}

}