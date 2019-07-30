package com.orange.srs.refreport.model.parameter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.orange.srs.refreport.model.TO.ProvisioningSourceTypeConfigurationTO;
import com.orange.srs.statcommon.model.TO.ProvisioningSourceTO;
import com.orange.srs.statcommon.model.parameter.ProvisioningSourceParameter;

public class RetrieveProvisioningFileParameter {
	public List<ProvisioningSourceParameter> sourceList;

	public RetrieveProvisioningFileParameter() {
		sourceList = new ArrayList<ProvisioningSourceParameter>();
	}

	public List<ProvisioningSourceParameter> getSourceList() {
		return sourceList;
	}

	public void setSourceList(List<ProvisioningSourceParameter> sourceList) {
		this.sourceList = sourceList;
	}

	public void AddSourceToList(ProvisioningSourceParameter source) {
		this.sourceList.add(source);
	}

	public RetrieveProvisioningFileParameter(List<ProvisioningSourceParameter> sourceList) {

		this.sourceList = sourceList;
	}

	public RetrieveProvisioningFileParameter(ProvisioningSourceTypeConfigurationTO provisioningTypeTO, String origin,
			Calendar date) {

		sourceList = new ArrayList<ProvisioningSourceParameter>();
		List<ProvisioningSourceTO> listSource = new ArrayList<ProvisioningSourceTO>();
		listSource = provisioningTypeTO.getListProvisioningSourceTO();
		for (ProvisioningSourceTO sourceTO : listSource) {
			ProvisioningSourceParameter source = new ProvisioningSourceParameter(sourceTO, origin, date);
			this.AddSourceToList(source);
		}
	}
}
