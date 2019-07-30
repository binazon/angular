package com.orange.srs.functional;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.orange.srs.functional.utils.FunctionalTestUtils;

/**
 * Functional Test class for SIP SITES H2 export in <strong>DEV environment ONLY</strong>
 * <p>
 * <br/>
 * <strong>PREREQUISITE :</strong>
 * <ul>
 * <li>Comment @AfterClass annotation on {@link SipSiteProvisionningTest#cleaningAndSetup()} to prevent cleaning
 * provisioning data required for this current test class</li>
 * <li>Run the test class {@link SipSiteProvisionningTest }</li>
 * </ul>
 * <p>
 * PLease comment @After annotation on {@link SipSiteH2InventoryTest#cleaningGeneratedH2Files()} in case you need H2
 * generated files
 * </p>
 * 
 * @author A688399
 *
 */
public class SipSiteH2InventoryTest {

	static final Date TODAY_DATE = new Date();
	static final SimpleDateFormat YYMMDD = new SimpleDateFormat("yyMMdd");
	static final SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
	static final String TODAY_YYMMDD = YYMMDD.format(TODAY_DATE);
	static final String TODAY_YYYY_MM_DD = YYYY_MM_DD.format(TODAY_DATE);

	// EXPORT FOR REFREPORT =========================================================================================
	static final String C_STAT_DATA_GROUPS = "C:/stat/data/groups/";

	static final List<String> EXPECTED_SCE_REPORTING_GROUPS_REF_IDS = Arrays.asList("ALTEAD", "UNILABS", "VILOGIA",
			"METROLOGIE", "SYNERGIE");

	@BeforeClass
	// @AfterClass
	public void cleaningGeneratedH2Files() throws IOException {
		// Comment this line in case you need H2 generated files
		if (Files.exists(Paths.get(C_STAT_DATA_GROUPS))) {
			FileUtils.cleanDirectory(new File(C_STAT_DATA_GROUPS));
		}
	}

	@Test
	public void testSipSitesH2Export() throws InterruptedException, ParseException, IOException {

		// ========================= PREPARE ==============================================
		// 0- Pre-requisite: RefObject exported files in "C:/stat/data/work/export/"

		// 1- Importing (entities and attributes) into RefReport
		FunctionalTestUtils.importRefObjectProvisioningIntoRefReport(TODAY_YYYY_MM_DD).then().statusCode(200);

		// *********************** Wait 5s for the import to be processed **********************
		Thread.sleep(5000);
		// ********************************************************************************************

		// 2- H2 export for each RG
		String h2InventoryFilePattern = C_STAT_DATA_GROUPS + "%s_sce/inventory/%s_sce_inventory_%s.h2.db";
		EXPECTED_SCE_REPORTING_GROUPS_REF_IDS.stream().forEach(reportingGroupRefId -> {
			FunctionalTestUtils.exportInventory(TODAY_YYYY_MM_DD, "SCE", reportingGroupRefId).then().statusCode(200);
			assertTrue(Files.exists(Paths.get(
					String.format(h2InventoryFilePattern, reportingGroupRefId, reportingGroupRefId, TODAY_YYMMDD))));

		});

	}

}
