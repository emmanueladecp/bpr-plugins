package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutConfirm;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.MInOutLineMA;
import org.compiere.model.MProduct;
import org.compiere.model.MStorageOnHand;
import org.compiere.model.MUOMConversion;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRPicklist;
import com.idempierecloud.bpr.model.MBPRPicklistLine;

public class MInOutEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	private MInOut inout = null;
	BigDecimal qtyIntransitReal;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Minout Event : "+event.getTopic());
		inout = (MInOut) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_PREPARE)) {
			checkProductCost();
			checkMovementDate();
			checkQtySalesOrder();
			checkAvailableQtyProduct();
			checkCustomerReturn();
			checkReversalDocumentNo();
			setProcessedLine();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			setDateShipment();
			checkReversal();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_VOID)) {
			checkShipment(event.getTopic());
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_VOID)) {
			voidShipment();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSEACCRUAL)) {
			checkShipment(event.getTopic());
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSECORRECT)) {
			checkShipment(event.getTopic());
		}
	}
	
	private void checkReversalDocumentNo() {
		if(inout.getReversal_ID()==0)
			return;
		
		inout.setDocumentNo(inout.getReversal().getDocumentNo()+"^");
		inout.saveEx();				
	}
	
	private void setDateShipment() {
		if(inout.isSOTrx()&&
				inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerShipment)) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(currentDateTime);
            inout.setMovementDate(timestamp);
            inout.setDateAcct(timestamp);
		}
	}

	private void checkProductCost() {
		if(inout.isSOTrx()) {
			int M_CostElement_ID_AveragePO=1000004;
			for(MInOutLine line : inout.getLines(true)) {
				BigDecimal MCost_CurrentCostPrice = DB.getSQLValueBD(line.get_TrxName(), "SELECT Coalesce(M_Cost.currentcostprice,0) FROM M_Cost WHERE AD_Org_ID = ? "+
				 " and M_Product_ID = ? and M_CostElement_ID=?",inout.getAD_Org_ID(),line.getM_Product_ID(), M_CostElement_ID_AveragePO);
				if(MCost_CurrentCostPrice==null) {
					throw new AdempiereException("Tidak ditemukan Cost untuk Product : "+line.getM_Product().getName()
												+", Organization :  "+line.getAD_Org_ID()
												+", Cost Elemet : Average PO");
				}else if(MCost_CurrentCostPrice.signum()<=0) {
						throw new AdempiereException("Cost untuk Product : "+line.getM_Product().getName()
								+", Organization :  "+line.getAD_Org_ID()
								+", Cost Elemet : Average PO, Current Cost Price Harus Lebih Besar dari 0");
				}
			}
		}
	}

	private void voidShipment() {
		if(inout.getC_DocType().getDocBaseType().equals("MMS")){
			int confirm_id = DB.getSQLValue(inout.get_TrxName(),  "SELECT "+MInOutConfirm.COLUMNNAME_M_InOutConfirm_ID+" FROM "+MInOutConfirm.Table_Name+" WHERE M_InOut_ID=?", inout.getM_InOut_ID());
			if(confirm_id>0) {//Ticket #request-001323 [BPR] Void Shipment (CR 150)
				MInOutConfirm confirm = new MInOutConfirm(inout.getCtx(),confirm_id,inout.get_TrxName());
				if(confirm.getDocStatus().equals(MInOutConfirm.DOCSTATUS_Drafted)) {
					confirm.setDocAction(MInOut.DOCACTION_Void);
					confirm.saveEx();
					if(!confirm.processIt(MInOut.DOCACTION_Void))
						throw new AdempiereException("Shipment gagal void : "+confirm.getProcessMsg());
					confirm.saveEx();
				}else if(confirm.getDocStatus().equals(MInOutConfirm.DOCSTATUS_InProgress)||
						confirm.getDocStatus().equals(MInOutConfirm.DOCSTATUS_Invalid)){
					int MInOut_ID = DB.getSQLValue(confirm.get_TrxName(), " select distinct (mi2.m_inout_id) from m_inoutlineconfirm mi "
							+ "  join m_inoutline mi2 on mi.m_inoutline_id = mi2.m_inoutline_id "
							+ "  where mi.m_inoutconfirm_id = ? and mi2.m_inout_id not in (?)", confirm.getM_InOutConfirm_ID(),inout.getM_InOut_ID());
					if(MInOut_ID>0) {
						throw new AdempiereException("Gagal Void Shipment, Shipment confrim terdiri lebih dari 1 shipment");					
					}else {
						throw new AdempiereException("Shipment gagal Void, Shipment Confirm sudah tidak berstatus draft");
					}
				}else if(confirm.getDocStatus().equals(MInOutConfirm.DOCSTATUS_Completed)){	
					throw new AdempiereException("Shipment gagal Void, Shipment Confirm sudah berstatus Complete");
				}
			}			
		}
		
	}

	private void checkMovementDate() {
		if(inout.getMovementType().equals("C-")&&inout.isSOTrx()){
			if(inout.getReversal_ID()>0)
				return;
			Date MovementDate = inout.getMovementDate();
			Date Created = inout.getCreated();
			
		    int PeriodCreate = DB.getSQLValue(inout.get_TrxName(), "select cp.c_period_id from c_period cp where ? >= cp.startdate AND ? <= cp.enddate", inout.getCreated(),inout.getCreated());
		    int PeriodMovementDate = DB.getSQLValue(inout.get_TrxName(), "select cp.c_period_id from c_period cp where ? >= cp.startdate AND ? <= cp.enddate", inout.getMovementDate(),inout.getMovementDate());
            
		    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Created = sdf.parse(sdf.format(Created));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }          
            Date date = new Date(Created.getTime());         
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);  

            Date CreatedDate = cal.getTime();
		    
		    
			if(MovementDate.compareTo(CreatedDate)<0) {
				throw new AdempiereException("Movement Date tidak boleh kurang dari tanggal pembuatan Shipment. created : "+inout.getCreated());
			}
			else if(PeriodCreate!=PeriodMovementDate) {
				String DateComplete = DB.getSQLValueString(inout.get_TrxName(), "(with a as(select record_ID,created from ad_changelog where ad_table_ID=319 and record_ID=? "
						+ " and ad_column_ID=4323 and newvalue='IP' order by created desc limit 1),"
						+ " b as(select record_ID,created from ad_changelog where ad_table_ID=319 and record_ID=? "
						+ " and ad_column_ID=4323 and newvalue='CO' order by created desc limit 1)"
						+ " select coalesce(a.created,b.created)from m_inout c left join a on c.m_inout_ID = a.record_ID left join b on c.m_inout_ID = b.record_ID "
						+ " where m_inout_ID=?)", inout.getM_InOut_ID(),inout.getM_InOut_ID(),inout.getM_InOut_ID());				
				if(DateComplete==null) {//untuk validasi shipment tidak boleh beda period hanya ketika DR ke IP
					throw new AdempiereException("Movement Date tidak boleh berbeda period dari tanggal pembuatan Shipment. created : "+inout.getCreated());
				}
			}
		}
	}

	private void checkReversal() {
		if(inout.getReversal_ID()==0 || !inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerReturns))
			return;
		
		for(MInOutLine line : inout.getLines(false)) {
			BigDecimal qty = line.getMovementQty();
			
			int no = MInOutLineMA.deleteInOutLineMA(line.getM_InOutLine_ID(), line.get_TrxName());
			if (no > 0)
				if (log.isLoggable(Level.CONFIG)) log.config("Delete old #" + no);
			
			if(Env.ZERO.compareTo(qty)==0)
				return;
			
			

			MProduct product = line.getProduct();

			//	Attribute Set Instance
			//  Create an  Attribute Set Instance to any receipt FIFO/LIFO
			if (product != null && line.getM_AttributeSetInstance_ID() == 0)
			{
				String MMPolicy = product.getMMPolicy();
				MStorageOnHand[] storages = MStorageOnHand.getWarehouse(inout.getCtx(), 0, line.getM_Product_ID(), 0, 
						null, MClient.MMPOLICY_FiFo.equals(MMPolicy), true, line.getM_Locator_ID(), inout.get_TrxName());
	
				BigDecimal qtyToDeliver = line.getMovementQty().abs();
	
				for (MStorageOnHand storage: storages)
				{
					if (storage.getQtyOnHand().compareTo(qtyToDeliver) >= 0)
					{
						MInOutLineMA ma = new MInOutLineMA (line, 
								storage.getM_AttributeSetInstance_ID(),
								qtyToDeliver.negate(),storage.getDateMaterialPolicy(),true);
						ma.saveEx();		
						qtyToDeliver = Env.ZERO;
						if (log.isLoggable(Level.FINE)) log.fine( ma + ", QtyToDeliver=" + qtyToDeliver);		
					}
					else
					{	
						MInOutLineMA ma = new MInOutLineMA (line, 
									storage.getM_AttributeSetInstance_ID(),
									storage.getQtyOnHand().negate(),storage.getDateMaterialPolicy(),true);
						ma.saveEx();	
						qtyToDeliver = qtyToDeliver.subtract(storage.getQtyOnHand());
						if (log.isLoggable(Level.FINE)) log.fine( ma + ", QtyToDeliver=" + qtyToDeliver);		
					}
					if (qtyToDeliver.signum() == 0)
						break;
				}
								
				//	No AttributeSetInstance found for remainder
				if (qtyToDeliver.signum() != 0)
				{
					MInOutLineMA ma = MInOutLineMA.addOrCreate(line, 0, qtyToDeliver, inout.getMovementDate(),true) ;
					ma.saveEx();
					if (log.isLoggable(Level.FINE)) log.fine("##: " + ma);
					
				}
			}
					
					
		}
	}
	
	private void checkCustomerReturn() {
		if(!inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerReturns) || !inout.get_ValueAsBoolean("IsSusut") || inout.isReversal())
			return;
		
		for(MInOutLine line : inout.getLines()) {
			if(!line.getM_Locator().getM_LocatorType().getName().equals("RETUR"))
				continue;
			
			BigDecimal qty = MUOMConversion.convertProductFrom(line.getCtx(), line.getM_Product_ID(), line.getC_UOM_ID(), line.getQtyEntered());
			BigDecimal timbanganNetAmt = (BigDecimal) line.get_Value("TimbanganNetAmt");
			if(timbanganNetAmt==null || timbanganNetAmt.signum()<=0)
				throw new AdempiereException("TimbanganNetAmt invalid. "+timbanganNetAmt);
			BigDecimal diff = qty.subtract(timbanganNetAmt);
			if(diff.signum()<=0)
				return;
			
			MInOutLine susut = new MInOutLine(inout);
			susut.setM_Product_ID(line.getM_Product_ID());
			susut.setQty(diff);
			susut.setC_UOM_ID(line.getM_Product().getC_UOM_ID());
			int LocatorSusut_ID = DB.getSQLValue(null, "SELECT M_Locator_ID From M_Locator Where M_Locator.M_LocatorType_ID = 1000000 And M_Locator.M_Warehouse_ID=?", inout.getM_Warehouse_ID());
			susut.setM_Locator_ID(LocatorSusut_ID);
			susut.setLine(line.getLine()+20);
			susut.setC_OrderLine_ID(line.getC_OrderLine_ID());
			susut.setDescription("SUSUT");
			susut.saveEx();
		}
		
	}
	private void checkShipment(String topic) {
		if(inout.getC_DocType().getDocBaseType().equals("MMS")&&inout.isSOTrx()&&topic.equals(IEventTopics.DOC_BEFORE_VOID)){
			int confirm_id = DB.getSQLValue(inout.get_TrxName(),  "SELECT "+MInOutConfirm.COLUMNNAME_M_InOutConfirm_ID+" FROM "+MInOutConfirm.Table_Name+" WHERE docstatus not in ('VO','RE') and M_InOut_ID=?", inout.getM_InOut_ID());
			if(confirm_id>0) {//Ticket #request-001323 [BPR] Void Shipment (CR 150)
				MInOutConfirm confirm = new MInOutConfirm(inout.getCtx(),confirm_id,inout.get_TrxName());
				if(confirm.getDocStatus().equals(MInOutConfirm.DOCSTATUS_Drafted)) {
					//cek picklist
					int BPR_Picklist_ID = DB.getSQLValue(inout.get_TrxName(), "select bp2.bpr_picklist_id from bpr_picklistline bp "
							+ " join bpr_picklist bp2 on bp2.bpr_picklist_id = bp.bpr_picklist_id "
							+ " where bp2.docstatus not in ('VO','RE') and bp.m_inout_id = ?", inout.getM_InOut_ID());
					if (BPR_Picklist_ID>0) {
						MBPRPicklist picklist= new MBPRPicklist(inout.getCtx(), BPR_Picklist_ID, inout.get_TrxName());
						for(MBPRPicklistLine picklistLine:picklist.getLines()) {
							picklistLine.setMovementQty(BigDecimal.ZERO);
							picklistLine.set_ValueOfColumn("QtyEntered", BigDecimal.ZERO);
							picklistLine.saveEx();
						}
						
					}
				}
			}			
		}else {
			int BPR_PiclistLine_ID = DB.getSQLValue(inout.get_TrxName(), "select bpr_picklistline_id from bpr_picklistline bp "
					+ " join bpr_picklist bp2 on bp2.bpr_picklist_id = bp.bpr_picklist_id "
					+ " where bp2.docstatus not in ('VO','RE') and bp.m_inout_id = ?", inout.getM_InOut_ID());
			if (BPR_PiclistLine_ID>0) {
				MBPRPicklistLine picklistLine= new MBPRPicklistLine(inout.getCtx(), BPR_PiclistLine_ID, inout.get_TrxName());
				MBPRPicklist picklist = (MBPRPicklist) picklistLine.getBPR_Picklist();
				throw new AdempiereException("GAGAL!! Shipment : "+inout.getDocumentNo()+" Sudah digunakan oleh Picklist : "+picklist.getDocumentNo());
			}
		}
		
		for(MInOutLine line: inout.getLines(false)) {
			int C_InvoiceLine_ID = DB.getSQLValue(inout.get_TrxName(), "select c_invoiceline_id from c_invoiceline ci "
					+ " join c_invoice ci2 on ci2.c_invoice_id = ci.c_invoice_id where ci2.docstatus not in ('VO','RE')"
					+ " and ci.m_inoutline_id = ?", line.getM_InOutLine_ID());
			if(C_InvoiceLine_ID > 0) {
				MInvoiceLine iLine = new MInvoiceLine(line.getCtx(), C_InvoiceLine_ID, line.get_TrxName());
				MInvoice invoice = (MInvoice) iLine.getC_Invoice();
				if(inout.isSOTrx())
					throw new AdempiereException("Shipment : "+inout.getDocumentNo()+"Sudah digunakan Invoice : "+invoice.getDocumentNo());
				else
					throw new AdempiereException("Material receipt : "+inout.getDocumentNo()+"Sudah digunakan Invoice : "+invoice.getDocumentNo());
				
			}
		}
	}
	
	private void checkAvailableQtyProduct() {
		if(!inout.isSOTrx() || !inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerShipment))
			return;
		MInOutLine[] lines = inout.getLines(true);
		for (MInOutLine line : lines) {
			BigDecimal qtyonhand = DB.getSQLValueBD(inout.get_TrxName(), "SELECT COALESCE(SUM(QtyOnHand), 0) FROM M_Storageonhand s"
					+ "	WHERE s.M_Product_ID=? AND s.m_locator_id=?", line.getM_Product_ID(),line.getM_Locator_ID());
			
			BigDecimal qtyIntransit = DB.getSQLValueBD(inout.get_TrxName(), "SELECT COALESCE(SUM(s.confirmedqty), 0)"
					+ "	FROM M_InOutLineConfirm s"
					+ "	JOIN M_InOutConfirm c ON s.M_InOutConfirm_ID=c.M_InOutConfirm_ID"
					+ "	JOIN M_InOutLine iol ON s.M_InOutLine_ID=iol.M_InOutLine_ID"
					+ "	WHERE c.docstatus in ('DR','IP','IN') AND iol.M_Product_ID=? AND iol.m_locator_id=? "
					+ " and iol.m_inoutline_id not in (?)"
					, line.getM_Product_ID(),line.getM_Locator_ID(),line.getM_InOutLine_ID());
			
			BigDecimal qtyAvailable = qtyonhand.subtract(qtyIntransit).subtract(line.getMovementQty());
			
			if(qtyAvailable.signum()<0)
				throw new AdempiereException("Gagal Complete!!"
						+", Quantity Available : "+qtyAvailable.add(line.getMovementQty())
						+", Quantity OnHand : "+qtyonhand
						+", Quantity Intransit : "+qtyIntransit
						+", Quantity Movement : "+line.getMovementQty()
						+", pada Shipment Line : "+line.getLine()
						+", Product : "+line.getM_Product().toString()
						+" Locator "+line.getM_Locator().getValue());
		}
	}
	
	private void checkQtySalesOrder() {
		if(!inout.isSOTrx() || !inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerShipment))
			return;
		MInOutLine[] lines = inout.getLines(true);
		for (MInOutLine line : lines) {
			BigDecimal MovementQty = DB.getSQLValueBD(inout.get_TrxName(), "Select coalesce(sum(mi.movementqty),0) "
					+ "	from m_inoutline mi "
					+ "	join m_inout mi2 ON mi.m_inout_id = mi2.m_inout_id "
					+ "	left join m_inoutlineconfirm mi3 on mi3.m_inoutline_id = mi.m_inoutline_id"
					+ "	left join m_inoutconfirm mi4 on mi3.m_inoutconfirm_id = mi4.m_inoutconfirm_id"
					+ " where mi.c_orderline_id = ? and mi2.docstatus not in ('RE','VO')"
					+ " and mi4.docstatus not in ('RE','VO') and mi.m_inoutline_id not in (?)", line.getC_OrderLine_ID(),line.getM_InOutLine_ID());			
			
			MOrderLine oline = new MOrderLine(line.getCtx(), line.getC_OrderLine_ID(), line.get_TrxName());
			
			BigDecimal qtyAvailable = oline.getQtyOrdered().subtract(MovementQty);
			
			if(qtyAvailable.compareTo(line.getMovementQty())<0) {
				throw new AdempiereException("Gagal Complete!! Quantity Movement melebihi Quantity Ordered pada SO"
						+ ", Quantity Ordered SO : "+ oline.getQtyOrdered()
						+ ", Quantity Available : "+ qtyAvailable
						+ ", Quantity Movement : "+ line.getMovementQty()
						+ ", SUM Quantity Movement : "+ MovementQty
						+ ", pada Shipment Line : "+line.getLine()
						+ ", Product : "+line.getM_Product().toString()
						+ ", Locator "+line.getM_Locator().getValue());
			}
		}
	}
	
	private void setProcessedLine() {
		if(inout.isSOTrx()&&inout.getMovementType().equals("C-")) {
			inout.setProcessed(true);
			for(MInOutLine line : inout.getLines()) {
				line.setProcessed(true);
			}
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
