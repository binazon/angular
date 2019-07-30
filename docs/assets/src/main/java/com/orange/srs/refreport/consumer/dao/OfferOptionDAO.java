package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryOpenFlowTO;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryPaiTO;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;
import com.orange.srs.statcommon.model.enums.OriginEnum;

public interface OfferOptionDAO extends Dao<OfferOption, Long> {

	public List<String> findOfferOptionAliasIfExistWithType(List<String> offerOptionAliases,
			List<OfferOptionTypeEnum> types);

	public List<OfferOption> findOfferOptionsByTypes(List<OfferOptionTypeEnum> types);

	public List<OfferOption> findOfferOptionsFiltered(List<OfferOptionTypeEnum> types, String reportingGroupRef,
			OriginEnum reportingGroupOrigin);

	public List<ExportSpecificInventoryOpenFlowTO> findExportInventoryForOpenFlowWithOfferOption(
			List<String> offerOptionList);

	public List<ExportSpecificInventoryPaiTO> findExportInventoryForPaiWithOfferOption(List<String> offerOptionList);
}
