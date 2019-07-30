package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.RollbackException;
import javax.xml.bind.Marshaller;

import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

public abstract class AbstractProvisioningCommand {

	private static final String XML_ENCODING = "UTF-8";

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void executeAndRollbackIfNecessary(SOAContext soaContext, File provisioningFile,
			boolean forceUpdateFromFileToDatabase) {
		try {
			execute(soaContext, provisioningFile, forceUpdateFromFileToDatabase);
		} catch (BusinessException e) {
			// Throw a RollbackException (runtime exception) instead of business exception
			// in order to rollback the transaction and so cancel commit in database
			throw new RollbackException(e);
		}
	}

	protected abstract void execute(SOAContext soaContext, File provisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException;

	protected Object unmarshallProvisioningTOFromProvisioningFile(File provisioningFile) throws BusinessException {
		try {
			return JAXBRefReportFactory.getUnmarshaller().unmarshal(provisioningFile);
		} catch (Exception e) {
			throw new BusinessException(
					"Unable to unmarshall information in provisioning file " + provisioningFile.getAbsolutePath(), e);
		}
	}

	public static void marshallProvisioningTOToProvisioningFile(Object provisioningTO, File provisioningFile,
			boolean overrideEscape) throws BusinessException {
		long saveLastModificationTime = provisioningFile.lastModified();
		try {
			Marshaller marshaller = JAXBRefReportFactory.getMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, XML_ENCODING);
			if (overrideEscape) {
				marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
					@Override
					public void escape(char[] ch, int start, int length, boolean isAttVal, Writer writer)
							throws IOException {
						writer.write(ch, start, length);
					}
				});
			}
			marshaller.marshal(provisioningTO, provisioningFile);
		} catch (Exception e) {
			throw new BusinessException(
					"Unable to marshall information in provisioning file " + provisioningFile.getAbsolutePath(), e);
		} finally {
			provisioningFile.setLastModified(saveLastModificationTime);
		}

	}

}
