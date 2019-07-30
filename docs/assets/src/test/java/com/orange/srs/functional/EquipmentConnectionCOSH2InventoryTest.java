package com.orange.srs.functional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.orange.srs.functional.utils.FunctionalTestUtils;

/**
 * Functional Test class for EQUIPMENT, PHYSCICALCONNECTION, LOGICALCONNECTION and COS H2 export in <strong>DEV
 * environment ONLY</strong> <br/>
 * <br/>
 * <strong>PREREQUISITE :</strong>
 * <ul>
 * <li>Comment @AfterClass annotation on {@link EquipmentConnectionCOSProvisionningTest#afterAllTests()} to prevent
 * cleaning provisioning data required for this current test class</li>
 * <li>Run the test class {@link EquipmentConnectionCOSProvisionningTest}</li>
 * </ul>
 * <p>
 * <p>
 * PLease comment @AfterClass annotation on {@link EquipmentConnectionCOSH2InventoryTest#cleaningGeneratedH2Files()} in
 * case you need H2 generated files
 * </p>
 * 
 * @author A688399
 *
 */
public class EquipmentConnectionCOSH2InventoryTest {

	static final Date TODAY_DATE = new Date();
	static final SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
	static final String TODAY_YYYY_MM_DD = YYYY_MM_DD.format(TODAY_DATE);
	static final SimpleDateFormat YYMMDD = new SimpleDateFormat("yyMMdd");
	static final String TODAY_YYMMDD = YYMMDD.format(TODAY_DATE);
	static final String C_STAT_DATA_GROUPS = "C:/stat/data/groups/";

	@BeforeClass
	// @AfterClass
	public static void cleaningGeneratedH2Files() throws IOException {
		if (Files.exists(Paths.get(C_STAT_DATA_GROUPS))) {
			// Comment this line in case you need H2 generated files
			FileUtils.cleanDirectory(new File(C_STAT_DATA_GROUPS));
		}
	}

	@Test
	public void testEquipmentConnectionCOSImport() throws InterruptedException, ParseException, IOException {

		// ========================= PREPARE ==============================================
		// 0- Pre-requisite: RefObject exported files in "C:/stat/data/work/export/"
		FunctionalTestUtils.yellowProvisioning().then().statusCode(200);

		// 1- Importing (entities and attributes) into RefReport
		FunctionalTestUtils.importRefObjectProvisioningIntoRefReport(TODAY_YYYY_MM_DD).then().statusCode(200);

		// *********************** Wait 5s for the import to be processed **********************
		Thread.sleep(5000 * 5);
		// ********************************************************************************************

		// 2- H2 export for each RG
		String h2InventoryFilePattern = C_STAT_DATA_GROUPS + "%s_equant/inventory/%s_equant_inventory_%s.h2.db";
		Arrays.asList("1908", "VPN_1054").stream().forEach(reportingGroupRefId -> {
			FunctionalTestUtils.exportInventory(TODAY_YYYY_MM_DD, "EQUANT", reportingGroupRefId).then().statusCode(200);
			Assert.assertTrue(Files.exists(Paths.get(
					String.format(h2InventoryFilePattern, reportingGroupRefId, reportingGroupRefId, TODAY_YYMMDD))));

		});
	}

}
