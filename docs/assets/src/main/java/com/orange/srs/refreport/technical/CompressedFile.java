package com.orange.srs.refreport.technical;

import java.io.IOException;

import com.orange.srs.statcommon.model.parameter.SOAContext;

public interface CompressedFile {

	/**
	 * @param source
	 * @param destination
	 * @return
	 * @throws IOException
	 */
	public boolean uncompress(String source, String destination, SOAContext soaContext) throws IOException;
}
