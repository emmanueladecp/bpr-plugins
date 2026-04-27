package com.idempierecloud.bpr.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.process.ImportProcess;
import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBPartner;
import org.compiere.process.DocAction;
import org.compiere.util.CPreparedStatement;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.model.X_I_BPR_ChargeAR;


public class ImportUpdateChargeARWriteOff extends CustomProcess implements ImportProcess{
	
	private int AD_User_ID = 0;
	private int AD_Org_ID = 1000007; //Hardcode ke Depo Cakung
	private int C_BPartner_ID = 0;
	private int C_Order_ID = 0;
	private int C_CashLine_ID = 0;
	private int C_Currency_ID = 303; //IDR
	private int m_AD_Client_ID = 1000003;
	private int m_C_DocType_ID = 1000010 ;// DocType = Allocation
	private int Table = 0;
	private String DocumentNo = null;
	private int C_Charge_ID = 1000419; //1201003 Cadangan Piutang Usaha Pihak Ketiga

	@Override
	protected void prepare() {
		
	}

	@Override
	protected String doIt() throws Exception {
		AD_User_ID = Env.getAD_User_ID(getCtx());
		StringBuilder sql = null;
		StringBuilder sql2 = null;
		int no = 0;
		String clientCheck = getWhereClause();
		
		
		// 1. Normalize base data
		sql =  new StringBuilder ("UPDATE I_BPR_ChargeAR i ")
				.append("SET AD_Client_ID = 1000003, ")
				.append("    AD_Org_ID = 1000007, ")
				.append("    IsActive = COALESCE(IsActive, 'Y'), ")
				.append("    I_IsImported = 'N' " )
				.append("WHERE I_IsImported <> 'Y' OR I_IsImported IS NULL");

		DB.executeUpdateEx(sql.toString(), get_TrxName());

		
		// 2. Map invoice ID
		sql = new StringBuilder ("UPDATE I_BPR_ChargeAR i ")
				.append("SET C_Invoice_ID = p.C_Invoice_ID ")
				.append(" FROM C_Invoice p ")
				.append("WHERE i.documentno = p.documentno ")
				.append("AND p.AD_Client_ID = i.AD_Client_ID ")
				.append("AND i.C_Invoice_ID IS NULL ")
				.append("AND i.documentno IS NOT NULL ")
				.append("AND i.I_IsImported = 'N' ")
				.append("AND i.AD_Client_ID = 1000003");
			
		DB.executeUpdateEx(sql.toString(), get_TrxName());
		
		
		// 3. Map BP
		sql = new StringBuilder ("UPDATE I_BPR_ChargeAR i ")
				.append("SET C_BPartner_ID = p.C_BPartner_ID  ")
				.append(" FROM C_Invoice p ")
				.append("WHERE i.C_Invoice_ID = p.C_Invoice_ID  ")
				.append("AND p.AD_Client_ID = i.AD_Client_ID ")
				.append("AND i.I_IsImported = 'N' ")
				.append("AND i.AD_Client_ID = 1000003");
			
		DB.executeUpdateEx(sql.toString(), get_TrxName());
		
		
		// 4. Set IsPaid 
		sql = new StringBuilder ("UPDATE I_BPR_ChargeAR i ")
				.append("SET IsPaid = p.IsPaid  ")
				.append(" FROM C_Invoice p ")
				.append("WHERE i.C_Invoice_ID = p.C_Invoice_ID  ")
				.append("AND p.AD_Client_ID = i.AD_Client_ID ")
				.append("AND i.I_IsImported = 'N' ")
				.append("AND i.AD_Client_ID = 1000003");
			
		DB.executeUpdateEx(sql.toString(), get_TrxName());
		
		//5. Set Processed = N jika IsPaid = 'N'
		sql = new StringBuilder ("UPDATE I_BPR_ChargeAR i ")
				.append("SET Processed = 'N'  ")
				.append("WHERE IsPaid = 'N' ");
		
		DB.executeUpdateEx(sql.toString(), get_TrxName());
		
		//6. Set Processed = Y jika IsPaid = 'Y'
		sql = new StringBuilder ("UPDATE I_BPR_ChargeAR i ")
				.append("SET Processed = 'Y'  ")
				.append("WHERE IsPaid = 'Y' ");
		
		DB.executeUpdateEx(sql.toString(), get_TrxName());
		
		
		//7. Fetch OpenAmount 
		sql = new StringBuilder ("UPDATE I_BPR_ChargeAR i ")
				.append("SET OpenAmt  = p.OpenAmt  ")
				.append(" FROM rv_openitem p ")
				.append("WHERE i.C_Invoice_ID = p.C_Invoice_ID  ")
				.append("AND p.AD_Client_ID = i.AD_Client_ID ")
				.append("AND i.I_IsImported = 'N' ")
				.append("AND i.AD_Client_ID = 1000003");
			
		DB.executeUpdateEx(sql.toString(), get_TrxName());
		
		commitEx();
		
		//Jumlahkan open invoice per bp , ini nanti akan jadi nilai charge
		sql = new StringBuilder ("SELECT C_BPartner_ID, SUM(OpenAmt) as OpenAmt FROM I_BPR_ChargeAR ")
				.append("WHERE I_IsImported='N' and IsPaid = 'N' and Processed = 'N' AND AD_Client_ID = 1000003 ")
				.append(" GROUP BY C_BPartner_ID");
		
		
		CPreparedStatement pstmt;
		ResultSet rs;
		
		pstmt = DB.prepareStatement(sql.toString(), null);
		rs = pstmt.executeQuery();
			
			while (rs.next()) 
			{
				C_BPartner_ID = rs.getInt("C_BPartner_ID") ;
				BigDecimal ChargeAmt = rs.getBigDecimal("OpenAmt");
				
				String trxName = org.compiere.util.Trx.createTrxName();
			    org.compiere.util.Trx trx = org.compiere.util.Trx.get(trxName, true);
				
			    try {

			        Timestamp now = new Timestamp(System.currentTimeMillis());

			        MAllocationHdr alloc = new MAllocationHdr(
			                getCtx(),
			                true,
			                now,
			                C_Currency_ID,
			                Env.getContext(getCtx(), "#AD_User_Name"),
			                trxName
			        );

			        alloc.setAD_Org_ID(AD_Org_ID);
			        alloc.setC_DocType_ID(m_C_DocType_ID);
			        alloc.setDateAcct(now);
			        alloc.setDescription("Auto Write Off By System : BA 001/AR-BPR/III-26");
			        alloc.saveEx();


			        // === Load invoice rows for this BP ===
			        String sql3 =
			            "SELECT C_Invoice_ID, OpenAmt " +
			            "FROM I_BPR_ChargeAR " +
			            "WHERE I_IsImported = 'N'  and IsPaid = 'N' " +
			            "AND Processed = 'N' " +
			            "AND C_BPartner_ID = ? " +
			            "AND AD_Client_ID = 1000003";

			        PreparedStatement pstmt2 = DB.prepareStatement(sql3, trxName);
			        pstmt2.setInt(1, C_BPartner_ID);
			        ResultSet rs2 = pstmt2.executeQuery();

			        while (rs2.next()) {

			            int invoiceId = rs2.getInt("C_Invoice_ID");
			            BigDecimal amt = rs2.getBigDecimal("OpenAmt");

			            MAllocationLine line = new MAllocationLine(
			                    alloc,
			                    amt,
			                    Env.ZERO,
			                    Env.ZERO,
			                    Env.ZERO
			            );

			            line.setDocInfo(C_BPartner_ID, 0, invoiceId);
			            line.setPaymentInfo(0, 0);
			            line.saveEx();
			        }

			        DB.close(rs2, pstmt2);


			        // === Charge line ===
			        MAllocationLine chargeLine = new MAllocationLine(
			                alloc,
			                ChargeAmt,
			                Env.ZERO,
			                Env.ZERO,
			                Env.ZERO
			        );

			        chargeLine.setC_Charge_ID(C_Charge_ID);
			        chargeLine.setC_BPartner_ID(C_BPartner_ID);
			        chargeLine.saveEx();


			        // === COMPLETE DOCUMENT ===
			        if (!alloc.processIt(DocAction.ACTION_Complete)) {
			        	addLog("Write Off AR Failed : "+alloc.getProcessMsg());
			            throw new AdempiereException(alloc.getProcessMsg());
			        }

			        alloc.saveEx();


			        // === Update BP credit ===
			        BigDecimal creditUsed = DB.getSQLValueBD(
			                trxName,
			                "SELECT calculate_credituse(?)",
			                C_BPartner_ID
			        );

			        MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, trxName);
			        bp.setSO_CreditUsed(creditUsed);
			        bp.saveEx();


			        // === Mark processed ===
			        DB.executeUpdateEx(
			            "UPDATE I_BPR_ChargeAR SET Processed='Y' WHERE C_BPartner_ID=?",
			            new Object[]{C_BPartner_ID},
			            trxName
			        );


			        trx.commit();

			    } catch (Exception e) {
			        trx.rollback();
			        throw e;
			    } finally {
			        trx.close();
			    }
			}
		
		
		return "";
	}

	@Override
	public String getImportTableName() {
		return X_I_BPR_ChargeAR.Table_Name;
	}

	@Override
	public String getWhereClause() {
		StringBuilder msgreturn = new StringBuilder(" AND AD_Client_ID=").append(m_AD_Client_ID);
		return msgreturn.toString();
	}
}
