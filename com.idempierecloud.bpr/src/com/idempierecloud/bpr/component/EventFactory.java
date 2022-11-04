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
import org.compiere.model.I_C_Invoice;
import org.compiere.model.I_C_OrderLine;

import com.idempierecloud.bpr.base.CustomEventFactory;
import com.idempierecloud.bpr.event.CInvoiceEvent;
import com.idempierecloud.bpr.event.COrderLineEvent;
import com.idempierecloud.bpr.event.LoginEvent;

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
		
		// C_Invoice
		registerEvent(IEventTopics.DOC_BEFORE_VOID, I_C_Invoice.Table_Name, CInvoiceEvent.class);
		
		// C_OrderLine
		registerEvent(IEventTopics.PO_BEFORE_NEW, I_C_OrderLine.Table_Name, COrderLineEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_CHANGE, I_C_OrderLine.Table_Name, COrderLineEvent.class);
		registerEvent(IEventTopics.PO_BEFORE_DELETE, I_C_OrderLine.Table_Name, COrderLineEvent.class);
	}

}
