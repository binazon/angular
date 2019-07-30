package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlElement;

public class HierarchyProvisioningTO {

	@XmlElement(required = false)
	public String min;
	@XmlElement(required = false)
	public String max;

	public HierarchyProvisioningTO() {
	}

	public HierarchyProvisioningTO(String min, String max) {
		this.min = min;
		this.max = max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String hierarchy = null;

		if (min == null || min.trim().isEmpty()) {
			hierarchy = ";";
		} else {
			hierarchy = min + ";";
		}

		if (max != null && !max.trim().isEmpty()) {
			hierarchy += max;
		}

		return hierarchy;
	}

}
