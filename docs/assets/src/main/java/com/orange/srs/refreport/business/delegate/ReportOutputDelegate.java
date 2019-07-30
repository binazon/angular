package com.orange.srs.refreport.business.delegate;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.lang.StringUtils;

import com.orange.srs.refreport.consumer.dao.ReportOutputDAO;
import com.orange.srs.refreport.model.ReportOutput;
import com.orange.srs.refreport.model.TO.provisioning.ReportOutputProvisioningTO;
import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;

@Stateless
public class ReportOutputDelegate {

	@EJB
	private ReportOutputDAO reportOutputDao;

	public ReportOutput createReportOutput(ReportOutputProvisioningTO reportOutputProvisioningTO) {
		ReportOutput reportOutput = new ReportOutput();
		reportOutput.setType(ReportOutputTypeEnum.valueOf(reportOutputProvisioningTO.type));
		reportOutput.setFormat(reportOutputProvisioningTO.format);
		// IP2262
		reportOutput.setCompression(reportOutputProvisioningTO.compression);
		reportOutput.setLocationPatternPrefix(reportOutputProvisioningTO.locationPatternPrefix);
		reportOutput.setLocationPatternSuffix(reportOutputProvisioningTO.locationPatternSuffix);
		reportOutput.setUri(reportOutputProvisioningTO.uri);
		reportOutputDao.persistAndFlush(reportOutput);
		return reportOutput;
	}

	public boolean updateReportOutputIfNecessary(ReportOutput reportOutput,
			ReportOutputProvisioningTO reportOutputProvisioningTO) {

		boolean updated = false;

		ReportOutputTypeEnum reportOutputTypeEnumTO = ReportOutputTypeEnum.valueOf(reportOutputProvisioningTO.type);

		if (!reportOutputTypeEnumTO.equals(reportOutput.getType())
				|| !reportOutputProvisioningTO.format.equals(reportOutput.getFormat())
				|| !StringUtils.trimToEmpty(reportOutputProvisioningTO.compression)
						.equals(StringUtils.trimToEmpty(reportOutput.getCompression())) // optional compression
				|| !reportOutputProvisioningTO.locationPatternPrefix.equals(reportOutput.getLocationPatternPrefix())
				|| !reportOutputProvisioningTO.locationPatternSuffix.equals(reportOutput.getLocationPatternSuffix())
				|| !reportOutputProvisioningTO.uri.equals(reportOutput.getUri())) {
			reportOutput.setType(ReportOutputTypeEnum.valueOf(reportOutputProvisioningTO.type));
			reportOutput.setFormat(reportOutputProvisioningTO.format);
			reportOutput.setCompression(reportOutputProvisioningTO.compression);
			reportOutput.setLocationPatternPrefix(reportOutputProvisioningTO.locationPatternPrefix);
			reportOutput.setLocationPatternSuffix(reportOutputProvisioningTO.locationPatternSuffix);
			reportOutput.setUri(reportOutputProvisioningTO.uri);
			reportOutputDao.persistAndFlush(reportOutput);
			updated = true;
		}
		return updated;
	}
}
