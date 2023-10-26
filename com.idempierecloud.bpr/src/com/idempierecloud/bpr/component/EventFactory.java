/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright (C) 2022 REDCLOUD <https://rekaestudigital.id> and contributors (see README.md file).
 */

package com.idempierecloud.bpr.component;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.I_C_InvoiceLine;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_C_Payment;
import org.compiere.model.I_I_Invoice;
import org.compiere.model.I_M_InOut;
import org.compiere.model.I_M_InOutConfirm;
import org.compiere.model.I_M_InOutLine;
import org.compiere.model.I_M_InOutLineConfirm;
import org.compiere.model.I_M_Inventory;
import org.compiere.model.I_M_InventoryLine;
import org.compiere.model.I_M_Movement;
import org.compiere.model.I_M_MovementLine;
import org.compiere.model.I_M_Production;
import org.compiere.model.I_M_ProductionLine;
import org.compiere.model.I_M_Requisition;

import com.idempierecloud.bpr.base.CustomEventFactory;
import com.idempierecloud.bpr.event.CBPartnerLocationEvent;
import com.idempierecloud.bpr.event.CInvoiceEvent;
import com.idempierecloud.bpr.event.CInvoiceLineEvent;
import com.idempierecloud.bpr.event.COrderEvent;
import com.idempierecloud.bpr.event.COrderLineEvent;
import com.idempierecloud.bpr.event.CPaymentEvent;
import com.idempierecloud.bpr.event.ImportInvoiceEvent;
import com.idempierecloud.bpr.event.LoginEvent;
import com.idempierecloud.bpr.event.MBPRPicklistLineEvent;
import com.idempierecloud.bpr.event.MInOutConfirmEvent;
import com.idempierecloud.bpr.event.MInOutEvent;
import com.idempierecloud.bpr.event.MInOutLineConfirmEvent;
import com.idempierecloud.bpr.event.MInOutLineEvent;
import com.idempierecloud.bpr.event.MInventoryEvent;
import com.idempierecloud.bpr.event.MInventoryLineEvent;
import com.idempierecloud.bpr.event.MMovementEvent;
import com.idempierecloud.bpr.event.MMovementLineEvent;
import com.idempierecloud.bpr.event.MProductionEvent;
import com.idempierecloud.bpr.event.MProductionLineEvent;
import com.idempierecloud.bpr.event.MRequisitionEvent;
import com.idempierecloud.bpr.model.I_BPR_PicklistLine;

/**
 * Event Factory
 */
public class EventFactory extends CustomEventFactory {

	/**
	 * For initialize class. Register the custom events to build
	 * 
	 * <pre>
	 * protected void initialize() {
	 * 	registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, MTableExample.Table_Name, EPrintPluginInfo.class);
	 * }
	 * </pre>
	 */
	@Override
	protected void initialize() {
		registerEvent(IEventTopics.AFTER_LOGIN, null, LoginEvent.class);
		
		// C_BPartner_Location
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_C_BPartner_Location.Table_Name, CBPartnerLocationEvent.class);
		registerEvent(IEventTopics.PO_AFTER_NEW, I_C_BPartner_Location.Table_Name, CBPartnerLocationEvent.class);
		registerEvent(IEventTopics.PO_AFTER_CHANGE, I_C_BPartner_Location.Table_Name, CBPartnerLocationEvent.class);
		
		// C_Invoice
		registerEvent(IEventTopics.DOC_BEFORE_VOID, I_C_Invoice.Table_Name, CInvoiceEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_REVERSECORRECT, I_C_Invoice.Table_Name, CInvoiceEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_C_Invoice.Table_Name, CInvoiceEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_C_Invoice.Table_Name, CInvoiceEvent.class);
		
		// C_InvoiceLine
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_C_InvoiceLine.Table_Name, CInvoiceLineEvent.class);
		registerEvent(IEventTopics.PO_AFTER_NEW, I_C_InvoiceLine.Table_Name, CInvoiceLineEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_CHANGE, I_C_InvoiceLine.Table_Name, CInvoiceLineEvent.class);
		registerEvent(IEventTopics.PO_AFTER_CHANGE, I_I_Invoice.Table_Name, ImportInvoiceEvent.class);
		
		
		// C_Order
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_C_Order.Table_Name, COrderEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_CHANGE, I_C_Order.Table_Name, COrderEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_C_Order.Table_Name, COrderEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_REACTIVATE, I_C_Order.Table_Name, COrderEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_VOID, I_C_Order.Table_Name, COrderEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_REVERSECORRECT, I_C_Order.Table_Name, COrderEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_PREPARE, I_C_Order.Table_Name, COrderEvent.class);
		registerEvent(IEventTopics.DOC_AFTER_COMPLETE, I_C_Order.Table_Name, COrderEvent.class);
		registerEvent(IEventTopics.DOC_AFTER_CLOSE, I_C_Order.Table_Name, COrderEvent.class);
		
		// C_OrderLine
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_C_OrderLine.Table_Name, COrderLineEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_CHANGE, I_C_OrderLine.Table_Name, COrderLineEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_DELETE, I_C_OrderLine.Table_Name, COrderLineEvent.class);
		registerEvent(IEventTopics.PO_AFTER_CHANGE, I_C_OrderLine.Table_Name, COrderLineEvent.class);
		registerEvent(IEventTopics.PO_AFTER_NEW, I_C_OrderLine.Table_Name, COrderLineEvent.class);
		registerEvent(IEventTopics.PO_AFTER_DELETE, I_C_OrderLine.Table_Name, COrderLineEvent.class);
		
		// M_Movement
		registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_M_Movement.Table_Name, MMovementEvent.class);
		registerEvent(IEventTopics.DOC_AFTER_COMPLETE, I_M_Movement.Table_Name, MMovementEvent.class);
		registerEvent(IEventTopics.DOC_AFTER_VOID, I_M_Movement.Table_Name, MMovementEvent.class);
		registerEvent(IEventTopics.DOC_AFTER_REVERSECORRECT, I_M_Movement.Table_Name, MMovementEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_REVERSECORRECT, I_M_Movement.Table_Name, MMovementEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_REVERSEACCRUAL, I_M_Movement.Table_Name, MMovementEvent.class);

		// M_MovementLine
		registerEvent(IEventTopics.PO_AFTER_DELETE, I_M_MovementLine.Table_Name, MMovementLineEvent.class);
		
		// M_Inventory
		registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_M_Inventory.Table_Name, MInventoryEvent.class);
		registerEvent(IEventTopics.DOC_AFTER_COMPLETE, I_M_Inventory.Table_Name, MInventoryEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_M_InventoryLine.Table_Name, MInventoryLineEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_CHANGE, I_M_InventoryLine.Table_Name, MInventoryLineEvent.class);
		
		// C_Payment
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_C_Payment.Table_Name, CPaymentEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_C_Payment.Table_Name, CPaymentEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_CHANGE, I_C_Payment.Table_Name, CPaymentEvent.class);
		
		//MProduction
		registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_M_Production.Table_Name, MProductionEvent.class);

		//MProductionLine
		registerEvent(IEventTopics.PO_AFTER_NEW, I_M_ProductionLine.Table_Name, MProductionLineEvent.class);
		registerEvent(IEventTopics.PO_AFTER_CHANGE, I_M_ProductionLine.Table_Name, MProductionLineEvent.class);
		
		// MRequisition
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_M_Requisition.Table_Name, MRequisitionEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_CHANGE, I_M_Requisition.Table_Name, MRequisitionEvent.class);
		registerEvent(IEventTopics.PO_AFTER_NEW, I_M_Requisition.Table_Name, MRequisitionEvent.class);
		registerEvent(IEventTopics.PO_AFTER_CHANGE, I_M_Requisition.Table_Name, MRequisitionEvent.class);
		
		//MInoutConfirm
		registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_M_InOutConfirm.Table_Name, MInOutConfirmEvent.class);
		registerEvent(IEventTopics.DOC_AFTER_COMPLETE, I_M_InOutConfirm.Table_Name, MInOutConfirmEvent.class);

		//MInoutLineConfirm
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_M_InOutLineConfirm.Table_Name, MInOutLineConfirmEvent.class);
		
		//MInOut
		registerEvent(IEventTopics.DOC_BEFORE_PREPARE, I_M_InOut.Table_Name, MInOutEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_M_InOut.Table_Name, MInOutEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_VOID, I_M_InOut.Table_Name, MInOutEvent.class);
		registerEvent(IEventTopics.DOC_AFTER_VOID, I_M_InOut.Table_Name, MInOutEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_REVERSEACCRUAL, I_M_InOut.Table_Name, MInOutEvent.class);
		registerEvent(IEventTopics.DOC_BEFORE_REVERSECORRECT, I_M_InOut.Table_Name, MInOutEvent.class);

		//MInOutLine
		registerEvent(IEventTopics.PO_BEFORE_CHANGE, I_M_InOutLine.Table_Name, MInOutLineEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_M_InOutLine.Table_Name, MInOutLineEvent.class);
		
		//BPR_Picklist
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_BPR_PicklistLine.Table_Name, MBPRPicklistLineEvent.class);
		//registerEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_BPR_Picklist.Table_Name, MBPRPicklistEvent.class);
		
		
	}

}
