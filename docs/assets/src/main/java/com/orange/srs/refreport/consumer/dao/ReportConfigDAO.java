package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.TO.ReportConfigIndicatorIdTO;
import com.orange.srs.refreport.model.TO.ReportConfigParamTypeAliasTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportConfigProvisioningTO;

public interface ReportConfigDAO extends Dao<ReportConfig, Long> {

	public List<ReportConfigParamTypeAliasTO> getAllParamTypeAliasAndReportConfigPk();

	public List<ReportConfig> findReportConfigsForOptionASC(String optionalias);

	public List<ReportConfigProvisioningTO> findAllReportConfigProvisioningTOWithoutParamTypeForOfferOption(
			String offerOptionAlias);

	public List<String> findAllReportRefIdLinkedToReportConfigForOfferOption(String offerOptionAlias);

	public List<ReportConfigIndicatorIdTO> getAllIndicatorIdAndReportConfigPk();
}
