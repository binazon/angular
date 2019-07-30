package com.orange.srs.refreport.model.enumerate;

public enum EntityLinkTypeEnum {
	SITE("SITE"), SHADOW_SITE("SHADOW_SITE"), IPBXZONE("IPBXZONE"), ZONE("ZONE"), SBC("SBC"), MEASURE(
			"MEASURE"), ASSOCIATED_DEVICE("ASSOCIATED_DEVICE"), PATH(
					"PATH"), VPN("VPN"), CALLLIMITER("CALLLIMITER"), CITY("CITY"), COUNTRY("COUNTRY");

	private String value;

	EntityLinkTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}