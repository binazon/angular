package com.orange.srs.refreport.consumer.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.model.TO.GetFilesTO;
import com.orange.srs.refreport.model.parameter.GetProvisioningFilesParameter;
import com.orange.srs.refreport.model.parameter.SourceFileParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

public class NASFileConsumer implements FileConsumer {

	private static final Logger LOGGER = Logger.getLogger(FileConsumer.class);

	private static final String CLASS_NAME = "NASFileConsumer";

	@Override
	public Object close(SOAContext soaContext) throws IOException {
		return null;
	}

	@Override
	public GetFilesTO getFiles(GetProvisioningFilesParameter parameter, SOAContext soaContext) throws IOException {

		GetFilesTO result = new GetFilesTO();

		for (SourceFileParameter file : parameter.FileList) {
			long startTime = Utils.getTime();
			File source = new File(file.getSourceFileName());
			File destination = new File(file.getDestinationFileName());

			if (!source.exists()) {
				LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
						CLASS_NAME + ":[getFiles]: File: " + file.getSourceFileName() + " does not exist"));
				result.addNotRetrievedFile(file.getDestinationFileName());
			} else {
				try {
					new File(destination.getParent()).mkdirs();
					destination.createNewFile();

					LOGGER.info(SOATools.buildSOALogMessage(soaContext,
							CLASS_NAME + ":[getFiles]: Receiving file: " + file.getSourceFileName() + " to "
									+ destination.getAbsolutePath() + " (size: "
									+ FileUtils.byteCountToDisplaySize(source.length()) + ")"));

					nioBufferCopy(source, destination, 32768);

					result.addRetrievedFile(file.getDestinationFileName(), startTime);

					// protection / by zero
					long duration = Utils.getTime() > startTime ? Utils.getTime() - startTime : 1;
					long throughput = source.length() / duration * 1000;

					LOGGER.info(SOATools.buildSOALogMessage(soaContext,
							CLASS_NAME + ":[getFiles]: File Received: " + file.getSourceFileName() + " to "
									+ destination.getAbsolutePath() + " (throughput: "
									+ FileUtils.byteCountToDisplaySize(throughput) + "/s"));

				} catch (IOException e) {
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
							CLASS_NAME + ":[getFiles]: Error occurred when copying file [" + file.getSourceFileName()
									+ "]: " + e.getMessage(),
							e));
					result.addNotRetrievedFile(file.getDestinationFileName());
				}
			}

		}
		return result;
	}

	private static void nioBufferCopy(File source, File target, int bufferSize)
			throws FileNotFoundException, IOException {
		try (FileInputStream inStream = new FileInputStream(source)) {
			try (FileOutputStream outStream = new FileOutputStream(target)) {
				try (FileChannel inChannel = inStream.getChannel()) {
					try (FileChannel outChannel = outStream.getChannel()) {

						ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
						while (inChannel.read(buffer) != -1) {
							buffer.flip();

							while (buffer.hasRemaining()) {
								outChannel.write(buffer);
							}

							buffer.clear();
						}
					}
				}
			}
		}
	}

}
