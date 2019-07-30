package com.orange.srs.refreport.consumer.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.activation.UnsupportedDataTypeException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.model.TO.GetFilesTO;
import com.orange.srs.refreport.model.parameter.GetProvisioningFilesParameter;
import com.orange.srs.refreport.model.parameter.SourceFileParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3FileAttributes;
import ch.ethz.ssh2.SFTPv3FileHandle;

public class SFTPFileConsumer implements FileConsumer {

	private static Logger logger = Logger.getLogger(FileConsumer.class);

	private static String CLASS_NAME = "SFTPFileConsumer";

	private Connection connection;
	private SFTPv3Client sftpClient;

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 * @see com.orange.srs.openreport.provider.renderer.Renderer#render(java.lang.String,
	 *      com.orange.srs.openreport.model.ReportContext)
	 */
	private Object connect(String sourceName, String user, String password, SOAContext soaContext) throws IOException {
		connection = new Connection(sourceName);
		if (connection == null) {
			throw new IOException("SSH connection building failed");
		}

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					CLASS_NAME + ":[connect] connecting to source by SFTP: " + sourceName));
		}

		connection.connect();

		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext, CLASS_NAME + ":[connect] authentication"));
		}

		try {
			connection.authenticateWithPassword(user, password);
		} catch (UnsupportedDataTypeException e) {
			// compression is not supported
			connection.close();

			// reconnect without compression
			connection.disableCompression();
			connection.connect();
			connection.authenticateWithPassword(user, password);
		}

		if (!connection.isAuthenticationComplete()) {
			connection.close();
			throw new IOException("Bad login/password");
		}
		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					CLASS_NAME + ":[connect] connected to source: " + sourceName));
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.orange.srs.openreport.provider.renderer.Renderer#render(java.lang.String,
	 *      com.orange.srs.openreport.model.ReportContext, java.lang.String)
	 */

	@Override
	public Object close(SOAContext soaContext) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug(SOATools.buildSOALogMessage(soaContext,
					CLASS_NAME + ":[connect] closing connection to source: " + connection.getHostname()));
		}
		if (connection != null) {
			connection.close();
		}
		return null;
	}

	@Override
	public GetFilesTO getFiles(GetProvisioningFilesParameter parameter, SOAContext soaContext) throws IOException {

		GetFilesTO result = new GetFilesTO();

		if (connection == null) {
			connect(parameter.source, parameter.user, parameter.password, soaContext);
		}

		if (connection != null) {
			sftpClient = new SFTPv3Client(connection);
		}

		for (SourceFileParameter file : parameter.FileList) {
			long startTime = Utils.getTime();
			if (logger.isDebugEnabled()) {
				logger.debug(SOATools.buildSOALogMessage(soaContext,
						CLASS_NAME + ":[getFiles]: Getting file: " + file.getSourceFileName()));
			}

			if (copyFile(file, sftpClient, soaContext)) {
				result.addRetrievedFile(file.getDestinationFileName(), startTime);
			} else {
				result.addNotRetrievedFile(file.getSourceFileName());
			}
		}

		return result;
	}

	/**
	 * Copy a file from a source to a local directory through SFTP protocol
	 * 
	 * @param file:
	 *            Paths and fileNames of the Source and Destination Files
	 * @param sftpClient:
	 *            SFTP connection to the source
	 * @param soaContext:
	 *            for logs
	 * @return true if the copy happened without any problem, false either ( file does not exist on source, file is
	 *         empty, transfer problem...)
	 * @throws IOException
	 */
	private boolean copyFile(SourceFileParameter file, SFTPv3Client sftpClient, SOAContext soaContext)
			throws IOException {

		SFTPv3FileHandle sftpFileHandle = null;
		BufferedOutputStream fos = null;
		File transferFile = null;
		long sourceFileSize = -1;
		SFTPv3FileAttributes sftpFileAttributes = null;

		long startTime = Utils.getTime();
		try {
			sftpFileAttributes = sftpClient.stat(file.getSourceFileName());
		} catch (IOException ioe) {
			logger.warn(SOATools.buildSOALogMessage(soaContext, CLASS_NAME + ":[getFiles]: File: "
					+ file.getSourceFileName() + " does not exist on " + connection.getHostname()));
			return false;
		}

		sourceFileSize = sftpFileAttributes.size;

		if (sourceFileSize == -1) {
			logger.info(SOATools.buildSOALogMessage(soaContext,
					CLASS_NAME + ":[getFiles]: File: " + file.getSourceFileName() + " is empty "));
			return false;
		}

		transferFile = new File(file.getDestinationFileName());
		new File(transferFile.getParent()).mkdirs();
		logger.info(SOATools.buildSOALogMessage(soaContext,
				CLASS_NAME + ":[getFiles]: Receiving file: " + file.getSourceFileName() + " to "
						+ transferFile.getAbsolutePath() + " (size: " + FileUtils.byteCountToDisplaySize(sourceFileSize)
						+ ")"));

		try {
			sftpFileHandle = sftpClient.openFileRO(file.getSourceFileName());
		} catch (IOException ioe) {
			logger.warn(SOATools.buildSOALogMessage(soaContext, CLASS_NAME + ":[getFiles]: Failed to read file: "
					+ file.getSourceFileName() + " on " + connection.getHostname()));
			return false;
		}

		fos = null;
		long offset = 0;
		try {
			fos = new BufferedOutputStream(new FileOutputStream(transferFile));
			byte[] buffer = new byte[32768];
			int i = 0;
			while ((i = sftpClient.read(sftpFileHandle, offset, buffer, 0, buffer.length)) != -1) {
				offset += i;
				fos.write(buffer, 0, i);
			}
			fos.flush();

		} catch (IOException e) {
			logger.warn(
					SOATools.buildSOALogMessage(soaContext, CLASS_NAME + ":[getFiles]: Error occurred writing file ["
							+ transferFile.getAbsolutePath() + "]: " + e.getMessage()));
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
				fos = null;
			}
		}

		try {
			sftpClient.closeFile(sftpFileHandle);
			sftpFileHandle = null;
		} catch (IOException e) {
		}

		if (sourceFileSize > 0 && sourceFileSize != transferFile.length()) {
			throw new IOException("remote file size [" + FileUtils.byteCountToDisplaySize(sourceFileSize)
					+ "] and local file size [" + FileUtils.byteCountToDisplaySize(transferFile.length())
					+ "] are different. Number of bytes written to local file: " + offset);
		}

		// protection / by zero
		long duration = Utils.getTime() > startTime ? Utils.getTime() - startTime : 1;
		long throughput = sourceFileSize / duration * 1000;

		logger.info(SOATools.buildSOALogMessage(soaContext,
				CLASS_NAME + ":[getFiles]: File Received: " + file.getSourceFileName() + " to "
						+ transferFile.getAbsolutePath() + " (throughput: "
						+ FileUtils.byteCountToDisplaySize(throughput) + "/s)"));

		return true;

	}
}
