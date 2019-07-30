package com.orange.srs.functional.utils;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class FunctionalTestUtils {

	public static Response importRefObjectProvisioningIntoRefReport(String dateYYYY_MM_DD) {
		return given().queryParam("provisioningDate", dateYYYY_MM_DD)
				.queryParam("provisioningType", "IMPORT_DIFF_AND_UPDATE").contentType(ContentType.XML).when()
				.post("/arbrrt/rs/test/provisioning/type");
	}

	public static Response yellowProvisioning() {
		return given().when().get("/arbrrt/rs/provisioning/yellowPart/force");
	}

	public static Response exportInventory(String dateYYYY_MM_DD, String origin, String reportingGroupRefId) {
		return given().param("date", dateYYYY_MM_DD.replace('-', '/')).pathParam("origin", origin)
				.pathParam("groupRefId", reportingGroupRefId).when()
				.get("/arbrrt/rs/exportInventory/{origin}/{groupRefId}");
	}
}
