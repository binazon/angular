package com.orange.srs.refreport.consumer.file;

import java.io.IOException;

import com.orange.srs.refreport.model.TO.GetFilesTO;
import com.orange.srs.refreport.model.parameter.GetProvisioningFilesParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;

public interface FileConsumer {

	/**
	 * Close the connection to the source according to the protocol.
	 * 
	 * @param soaContext
	 * @return
	 * @throws IOException
	 */
	public Object close(SOAContext soaContext) throws IOException;

	/**
	 * Get the files listed in parameter from the source defined in parameter
	 * 
	 * @param parameter
	 * @param soaContext
	 * @return
	 * @throws IOException
	 */
	public GetFilesTO getFiles(GetProvisioningFilesParameter parameter, SOAContext soaContext) throws IOException;

}
