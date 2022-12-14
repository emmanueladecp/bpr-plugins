package com.idempierecloud.bpr.process;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MLocator;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;

import com.idempierecloud.bpr.base.CustomProcess;

public class CancelAndMoveShipment extends CustomProcess {

	private int m_M_InOut_ID;
	private int m_M_Warehouse_ID;

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("M_InOut_ID")) {
				m_M_InOut_ID = para[i].getParameterAsInt();
			}else if (name.equals("M_Warehouse_ID")) {
				m_M_Warehouse_ID = para[i].getParameterAsInt();
			}
		}
	}

	@Override
	protected String doIt() throws Exception {
		MInOut shipment = new MInOut(getCtx(), m_M_InOut_ID, get_TrxName());
		if(!shipment.processIt(MInOut.ACTION_Void))
			throw new AdempiereException(shipment.getProcessMsg());
		shipment.saveEx();
		
		MMovement move = new MMovement(getCtx(), 0, get_TrxName());
		move.setDescription(shipment.getDocumentNo());
		move.setAD_Org_ID(shipment.getAD_Org_ID());
		move.setC_DocType_ID(1000022);
		move.setM_Warehouse_ID(shipment.getM_Warehouse_ID());
		move.setM_WarehouseTo_ID(m_M_Warehouse_ID);
		move.saveEx();
		
		for(MInOutLine line : shipment.getLines()) {
			MLocator locatorTo = new Query(getCtx(), MLocator.Table_Name, "M_LocatorType_ID=? AND M_Warehouse_ID=?", get_TrxName())
					.setParameters(line.getM_Locator().getM_LocatorType_ID(), m_M_Warehouse_ID)
					.first();
			
			MMovementLine moveLine = new MMovementLine(move);
			moveLine.setLine(line.getLine());
			moveLine.setM_Product_ID(line.getM_Product_ID());
			moveLine.setMovementQty(line.getMovementQty());
			moveLine.setM_Locator_ID(line.getM_Locator_ID());
			moveLine.setM_LocatorTo_ID(locatorTo.getM_Locator_ID());
			moveLine.saveEx();
		}
		
		if(!move.processIt(MMovement.ACTION_Complete))
			throw new AdempiereException(move.getProcessMsg());
		
		move.saveEx();
		
		return "Success. Inventory Move "+move.getDocumentNo();
	}

}
