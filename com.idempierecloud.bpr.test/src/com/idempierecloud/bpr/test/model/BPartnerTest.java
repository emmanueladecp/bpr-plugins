package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.RandomStringUtils;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.test.AbstractTestCase;

public class BPartnerTest extends AbstractTestCase {

	private static final int BP_BGROUP_SUPPLIER_GENERAL = 1000002;

	@Test
	public void test_update_bpartner_location() throws Exception{
		String bpValue = RandomStringUtils.randomAlphabetic(10);
		
		MBPartner bp = new MBPartner(Env.getCtx(), 0, getTrxName());
		bp.setValue(bpValue);
		bp.setName(bpValue);
		bp.setIsCustomer(true);
		bp.setC_BP_Group_ID(BP_BGROUP_SUPPLIER_GENERAL);
		bp.saveEx();
		
		assertEquals(bpValue, bp.getName());
		
		MLocation location = new MLocation(bp.getCtx(), 209, 1000010, "JAKARTA BARAT", getTrxName());
		location.setAddress1("test");
		location.saveEx();
		
		assertEquals("test", location.getAddress1());
		
		MBPartnerLocation bpLocation = new MBPartnerLocation(bp);
		bpLocation.setName("location");
		bpLocation.set_ValueOfColumn("C_Country_ID", 209); // INDONESIA
		bpLocation.set_ValueOfColumn("C_Region_ID", 1000010); // DKI JAKARTA
		bpLocation.set_ValueOfColumn("C_City_ID", 1000157); // JAKARTA BARAT
		bpLocation.set_ValueOfColumn("BPR_District_ID", 1000445); // CENGKARENG
		bpLocation.setC_Location_ID(location.get_ID());
		bpLocation.saveEx();
		
		assertEquals("location", bpLocation.getC_Location().getAddress1());
	}
	
}
