package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.X_BPR_Timbangan;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class RequisitionTest extends AbstractTestCase {

	private static final int M_PRODUCT_GABAH_64 = 1000272;
	private static final int C_BPARTNER_CV_PADI_JAYA = 1000003;
	private static final int M_WAREHOUSE_GUDANG_BPR1 = 1000001;

	@Test
	public void test_requisition_bahan_baku() throws Exception{
		X_BPR_Timbangan timbangan = new X_BPR_Timbangan(Env.getCtx(), 0, getTrxName());
		timbangan.setAD_Org_ID(BPR_BPR1_ORG);
		timbangan.setBPR_NoKendaraan("B1234AA");
		timbangan.setM_Product_ID(M_PRODUCT_GABAH_64);
		timbangan.setValue("NOTA1");
		timbangan.setC_BPartner_ID(C_BPARTNER_CV_PADI_JAYA);
		timbangan.setM_Warehouse_ID(M_WAREHOUSE_GUDANG_BPR1);
		timbangan.setTimbangIsi(BigDecimal.valueOf(200));
		timbangan.setTimbangKosong(BigDecimal.valueOf(20));
		timbangan.setTimbanganNetAmt(BigDecimal.valueOf(180));
		timbangan.saveEx();
		
		assertNotNull(timbangan.getBPR_Timbangan_ID());
		
		MRequisition req = new MRequisition(Env.getCtx(), 0, getTrxName());
		req.setAD_Org_ID(BPR_BPR1_ORG);
		req.setC_DocType_ID(1000088); // PR BB BPR
		req.set_ValueOfColumn("C_BPartner_ID", C_BPARTNER_CV_PADI_JAYA);
		req.setDateDoc(getLoginDate());
		req.setDateRequired(getLoginDate());
		req.setM_Warehouse_ID(M_WAREHOUSE_GUDANG_BPR1);
		req.set_ValueOfColumn("BPR_Timbangan_ID", timbangan.getBPR_Timbangan_ID());
		req.setAD_User_ID(getAD_User_ID());
		req.setM_PriceList_ID(1000004);
		req.saveEx();

		assertEquals(timbangan.getBPR_Timbangan_ID(), req.get_ValueAsInt("BPR_Timbangan_ID"));
		
		MRequisitionLine line = new MRequisitionLine(req);
		line.setM_Product_ID(1000060);
		line.setQty(BigDecimal.valueOf(250));
		line.set_ValueOfColumn("QtyPack", BigDecimal.valueOf(5));
		line.setC_BPartner_ID(C_BPARTNER_CV_PADI_JAYA);
		line.saveEx();
		
		MRequisitionLine line2 = new MRequisitionLine(req);
		line2.setM_Product_ID(1000060);
		line2.setQty(BigDecimal.valueOf(250));
		line2.set_ValueOfColumn("QtyPack", BigDecimal.valueOf(5));
		line2.setC_BPartner_ID(C_BPARTNER_CV_PADI_JAYA);
		line2.saveEx();
		
		assertEquals(BigDecimal.valueOf(5), line.get_Value("QtyPack"));
		
		req.set_ValueOfColumn("TimbanganNetAmt", BigDecimal.valueOf(400));
		req.saveEx();
		
		List<MRequisitionLine> lines = new Query(req.getCtx(), MRequisitionLine.Table_Name, MRequisitionLine.COLUMNNAME_M_Requisition_ID+"=?", req.get_TrxName())
				.setParameters(req.getM_Requisition_ID())
				.list();
		
		for(MRequisitionLine reqLine : lines) {
			assertEquals(BigDecimal.valueOf(200), reqLine.getQty().setScale(0));
		}
		

	}
	
	@Test
	public void test_requisition_no_duplicate_bpr_timbangan_id() throws Exception{
		X_BPR_Timbangan timbangan = new X_BPR_Timbangan(Env.getCtx(), 0, getTrxName());
		timbangan.setAD_Org_ID(BPR_BPR1_ORG);
		timbangan.setBPR_NoKendaraan("B1234AA");
		timbangan.setM_Product_ID(M_PRODUCT_GABAH_64);
		timbangan.setValue("NOTA1");
		timbangan.setC_BPartner_ID(C_BPARTNER_CV_PADI_JAYA);
		timbangan.setM_Warehouse_ID(M_WAREHOUSE_GUDANG_BPR1);
		timbangan.setTimbangIsi(BigDecimal.valueOf(200));
		timbangan.setTimbangKosong(BigDecimal.valueOf(20));
		timbangan.setTimbanganNetAmt(BigDecimal.valueOf(180));
		timbangan.saveEx();
		
		assertNotNull(timbangan.getBPR_Timbangan_ID());
		
		MRequisition req = new MRequisition(Env.getCtx(), 0, getTrxName());
		req.setAD_Org_ID(BPR_BPR1_ORG);
		req.setC_DocType_ID(1000088); // PR BB BPR
		req.set_ValueOfColumn("C_BPartner_ID", C_BPARTNER_CV_PADI_JAYA);
		req.setDateDoc(getLoginDate());
		req.setDateRequired(getLoginDate());
		req.setM_Warehouse_ID(M_WAREHOUSE_GUDANG_BPR1);
		req.set_ValueOfColumn("BPR_Timbangan_ID", timbangan.getBPR_Timbangan_ID());
		req.setAD_User_ID(getAD_User_ID());
		req.setM_PriceList_ID(1000004);
		req.saveEx();

		assertEquals(timbangan.getBPR_Timbangan_ID(), req.get_ValueAsInt("BPR_Timbangan_ID"));
		
		MRequisition req2 = new MRequisition(Env.getCtx(), 0, getTrxName());
		req2.setAD_Org_ID(BPR_BPR1_ORG);
		req2.setC_DocType_ID(1000088); // PR BB BPR
		req2.set_ValueOfColumn("C_BPartner_ID", C_BPARTNER_CV_PADI_JAYA);
		req2.setDateDoc(getLoginDate());
		req2.setDateRequired(getLoginDate());
		req2.setM_Warehouse_ID(M_WAREHOUSE_GUDANG_BPR1);
		req2.set_ValueOfColumn("BPR_Timbangan_ID", timbangan.getBPR_Timbangan_ID());
		req2.setAD_User_ID(getAD_User_ID());
		req2.setM_PriceList_ID(1000004);

		assertThrows(AdempiereException.class, ()-> req2.saveEx());
		
	}
	
}
