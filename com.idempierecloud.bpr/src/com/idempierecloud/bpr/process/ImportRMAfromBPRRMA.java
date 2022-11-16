package com.idempierecloud.bpr.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOutLine;
import org.compiere.model.MRMA;
import org.compiere.model.MRMALine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.model.MBPRRMA;
import com.idempierecloud.bpr.model.MBPRRMALine;

public class ImportRMAfromBPRRMA extends CustomProcess{

	private final static int C_DocType_ID_CustomerReturnMaterial = 1000031;
	private final static int M_RMAType_ID_Return = 1000000;
	private final static int C_Tax_ID_Non_PPN = 1000000;
	public static final String DOCSTATUS_Completed = "CO";
	
	int M_Product_ID 	= 0;
	int C_BPartner_ID 	= 0;
	int p_AD_Client_ID 	= 0;
	int p_AD_Org_ID 	= 0;
	int p_BPR_RMA_ID		= 0;
	int run= 0;
	
	String p_RMA_Name = "";
	
	BigDecimal Qty = BigDecimal.ZERO;
	
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("AD_Org_ID"))
				p_AD_Org_ID = para[i].getParameterAsInt();
			else if (name.equals("Name"))
				p_RMA_Name = para[i].getParameterAsString();
			else if (name.equals("BPR_RMA_ID"))
				p_BPR_RMA_ID = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
		MBPRRMA bRMA = new MBPRRMA(getCtx(), p_BPR_RMA_ID, get_TrxName());
		
		MRMA rma = new MRMA(getCtx(), 0, get_TrxName());
		rma.setAD_Org_ID(p_AD_Org_ID);
		rma.setC_BPartner_ID(bRMA.getC_BPartner_ID());
		rma.setC_DocType_ID(C_DocType_ID_CustomerReturnMaterial);
		rma.setM_RMAType_ID(M_RMAType_ID_Return);
		rma.setName(p_RMA_Name);
		rma.setSalesRep_ID(bRMA.getSalesRep_ID());
		rma.setDocAction(DOCSTATUS_Completed);
		rma.setM_InOut_ID(1000000);//set just for escape mandatory, will set again
		if(bRMA.getDescription()!=null)
			rma.setDescription(bRMA.getDescription());
		rma.save();
		
		MBPRRMALine[] brLines = bRMA.getLines();
		for(MBPRRMALine brLine : brLines){
			Qty = brLine.getQty();
			String sqlStmt = "with x as (select coalesce (sum(qty),0) as qty from m_rmaline mr join m_rma mr2 on mr2.m_rma_id = mr.m_rma_id where mr.M_Product_ID = ? and mr2.c_bpartner_id = ?)"
					+ " Select mi.m_inout_id, mi.movementqty - (select coalesce (qty,0) from x) as movementqty "
					+ " from m_inoutline mi join m_inout mi2 on mi.m_inout_id = mi2.m_inout_id "
					+ " where mi.M_Product_ID = ? and mi2.C_BPartner_ID = ? and mi2.movementtype = 'C-' "
					+ " and mi.movementqty - (select coalesce(qty,0) from x) > 0 and mi2.docstatus = 'CO' order by mi2.movementdate desc";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			
			try {
				pstmt = DB.prepareStatement(sqlStmt, null);
				pstmt.setInt(1, brLine.getM_Product_ID());
				pstmt.setInt(2, brLine.getC_BPartner_ID());
				pstmt.setInt(3, brLine.getM_Product_ID());
				pstmt.setInt(4, brLine.getC_BPartner_ID());
				rs = pstmt.executeQuery();
				while (rs.next()) {					
					int ShipLine_ID = rs.getInt(1);
					if(run==0) {
						run = run + 1;
						MInOutLine ShipLine = new MInOutLine(getCtx(), ShipLine_ID, get_TrxName());
						rma.setM_InOut_ID(ShipLine.getM_InOut_ID());
						rma.save();
					}
					MRMALine rLine = new MRMALine(getCtx(), 0, get_TrxName());
					rLine.setAD_Org_ID(brLine.getAD_Org_ID());
					rLine.setM_Product_ID(brLine.getM_Product_ID());
					rLine.setM_RMA_ID(rma.getM_RMA_ID());
					rLine.setC_Tax_ID(C_Tax_ID_Non_PPN);
					rLine.setM_InOutLine_ID(ShipLine_ID);
					
					if(brLine.getDescription() != null)
						rLine.setDescription(brLine.getDescription());
					
					if(Qty.compareTo(rs.getBigDecimal(2))>=0) 
						rLine.setQty(rs.getBigDecimal(2));
					else
						rLine.setQty(Qty);
					rLine.saveEx();
					brLine.setM_RMALine_ID(rLine.get_ID());
					brLine.setM_InOutLine_ID(ShipLine_ID);
					brLine.setI_IsImported(true);
					brLine.saveEx();
					
					Qty = brLine.getQty().subtract(rs.getBigDecimal(2));
					if(Qty.compareTo(BigDecimal.ZERO)<=0) {
						break;
					}
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, sqlStmt.toString(), e);
			} finally{
				DB.close(rs, pstmt);
				rs = null;
				pstmt = null;
			}
			if(run<1)
				throw new AdempiereException("Please Check Product and Business Partner");
			rma.processIt(DOCSTATUS_Completed);
			rma.saveEx();
		}
		return "";
	}

}
