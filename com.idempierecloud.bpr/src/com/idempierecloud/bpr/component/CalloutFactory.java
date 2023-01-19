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

import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_InOutLine;
import org.compiere.model.I_M_InOutLineConfirm;
import org.compiere.model.I_M_ProductionLine;

import com.idempierecloud.bpr.base.CustomCalloutFactory;
import com.idempierecloud.bpr.callout.SetDatePromiseMOrderLine;
import com.idempierecloud.bpr.callout.SetLocatorCustomerReturn;
import com.idempierecloud.bpr.callout.SetMovementQtyConfirmLine;
import com.idempierecloud.bpr.callout.SetQCPenerimaanGabahBPR;
import com.idempierecloud.bpr.callout.SetQtyUsedProductionLine;
import com.idempierecloud.bpr.callout.SetSOCreditAvailable;
import com.idempierecloud.bpr.callout.SetUOMOrderLine;
import com.idempierecloud.bpr.model.I_BPR_QualityControl;

/**
 * Callout Factory
 */
public class CalloutFactory extends CustomCalloutFactory {

	/**
	 * For initialize class. Register the custom callout to build
	 * 
	 * <pre>
	 * protected void initialize() {
	 * 	registerCallout(MTableExample.Table_Name, MTableExample.COLUMNNAME_Text, CPrintPluginInfo.class);
	 * }
	 * </pre>
	 */
	@Override
	protected void initialize() {
		// C_Order
		registerCallout(I_C_Order.Table_Name, "C_BPartner_ID", SetSOCreditAvailable.class);
		registerCallout(I_C_Order.Table_Name, "DateOrdered", SetDatePromiseMOrderLine.class);
		// C_OrderLine
		registerCallout(I_C_OrderLine.Table_Name, "M_Product_ID", SetUOMOrderLine.class);
		//BPR_QualityControl
		registerCallout(I_BPR_QualityControl.Table_Name, "BPR_Timbangan_ID", SetQCPenerimaanGabahBPR.class);
		// M_ProductionLine
		registerCallout(I_M_ProductionLine.Table_Name, "QtyEntered", SetQtyUsedProductionLine.class);
		// M_InOutLine
		registerCallout(I_M_InOutLine.Table_Name, "M_Locator_ID", SetLocatorCustomerReturn.class);
		// M_InOutLineConfirm
		registerCallout(I_M_InOutLineConfirm.Table_Name, "QtyEntered", SetMovementQtyConfirmLine.class);
	}

}
