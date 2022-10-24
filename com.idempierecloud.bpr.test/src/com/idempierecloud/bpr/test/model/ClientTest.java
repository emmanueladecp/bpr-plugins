package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.compiere.util.DB;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.test.AbstractTestCase;

public class ClientTest extends AbstractTestCase {

	@Test
	public void test_belitang_client_exists() throws Exception{
		String value = DB.getSQLValueString(getTrxName(), "SELECT value FROM AD_Client Where value=?", "Belitang");
		assertEquals("Belitang", value);
	}
	
}
