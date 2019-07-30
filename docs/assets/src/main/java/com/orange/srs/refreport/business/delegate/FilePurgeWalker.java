package com.orange.srs.refreport.business.delegate;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import javax.ejb.EJB;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.technical.JMSConnectionHandler;
import com.orange.srs.statcommon.model.parameter.ShepherdInventoryPurgeParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.NotifyShepherdPurgeJobParameter;

public class FilePurgeWalker extends SimpleFileVisitor<Path> {

	private static Logger logger = Logger.getLogger(FilePurgeWalker.class);

	@EJB
	private JMSConnectionHandler jmsConnectionHandler;

	private SimpleDateFormat sdf = new SimpleDateFormat("YYMMDD");

	@Override
	public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
		final String nom = file.getFileName().toString();

		int i;
		String date = null;
		if (Pattern.matches(".+_inventory_.......h2.db", nom)) {
			i = StringUtils.lastIndexOf(nom, "_") + 1;
			date = nom.substring(i, i + 6);
		}

		try {
			ShepherdInventoryPurgeParameter purgeParam = new ShepherdInventoryPurgeParameter(
					file.toAbsolutePath().toString(), new Timestamp(sdf.parse(date).getTime()));

			NotifyShepherdPurgeJobParameter jobParam = new NotifyShepherdPurgeJobParameter();
			jobParam.parameter = purgeParam;
			// TODO: Rework after delivery G08R00C05
			// jmsConnectionHandler.sendJMSMessage(jobParam,
			// JMSAttributeName.SHEPHERD_PURGE_QUEUE);

			logger.info("[FilePurgeWalker] invetory sent " + nom);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
		FileVisitResult result = FileVisitResult.CONTINUE;
		return result;
	}

}
