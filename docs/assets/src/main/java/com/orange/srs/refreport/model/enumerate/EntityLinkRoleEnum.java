package com.orange.srs.refreport.model.enumerate;

public enum EntityLinkRoleEnum {
	NOMINAL("NOMINAL"), BACKUP("BACKUP"), ORIGINFDN("ORIGINFDN"), ENDFDN("ENDFDN"), EQUIPMENT("EQUIPMENT"), SITE(
			"SITE"), ZONE("ZONE"), IPBXZONE("IPBXZONE"), MEASURE("MEASURE"), PATH("PATH"), VPN(
					"VPN"), CALLLIMITER("CALLLIMITER"), ORIGIN("ORIGIN"), END("END"), CITY("CITY"), COUNTRY("COUNTRY");

	private String value;

	EntityLinkRoleEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}