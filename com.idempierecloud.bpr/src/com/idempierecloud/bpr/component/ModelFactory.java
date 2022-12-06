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

import com.idempierecloud.bpr.base.CustomModelFactory;
import com.idempierecloud.bpr.model.I_BPR_POBahanBaku;
import com.idempierecloud.bpr.model.I_BPR_POBahanBakuHeader;
import com.idempierecloud.bpr.model.I_BPR_POBahanBakuLine;
import com.idempierecloud.bpr.model.I_BPR_Picklist;
import com.idempierecloud.bpr.model.I_BPR_PicklistLine;
import com.idempierecloud.bpr.model.I_BPR_Timbangan;
import com.idempierecloud.bpr.model.MBPRPOBahanBaku;
import com.idempierecloud.bpr.model.MBPRPOBahanBakuHeader;
import com.idempierecloud.bpr.model.MBPRPOBahanBakuLine;
import com.idempierecloud.bpr.model.MBPRPicklist;
import com.idempierecloud.bpr.model.MBPRPicklistLine;
import com.idempierecloud.bpr.model.MBPRRMA;
import com.idempierecloud.bpr.model.MBPRRMALine;
import com.idempierecloud.bpr.model.MProductionExt;
import com.idempierecloud.bpr.model.MProductionLineExt;
import com.idempierecloud.bpr.model.X_BPR_Timbangan;

/**
 * Model Factory
 */
public class ModelFactory extends CustomModelFactory {

	/**
	 * For initialize class. Register the models to build
	 * 
	 * <pre>
	 * protected void initialize() {
	 * 	registerModel(MTableExample.Table_Name, MTableExample.class);
	 * }
	 * </pre>
	 */
	@Override
	protected void initialize() {
		registerModel(MProductionExt.Table_Name, MProductionExt.class);
		registerModel(MProductionLineExt.Table_Name, MProductionLineExt.class);
		registerModel(I_BPR_Timbangan.Table_Name, X_BPR_Timbangan.class);
		registerModel(MBPRRMA.Table_Name, MBPRRMA.class);
		registerModel(MBPRRMALine.Table_Name, MBPRRMALine.class);
		registerModel(I_BPR_Picklist.Table_Name, MBPRPicklist.class);
		registerModel(I_BPR_PicklistLine.Table_Name, MBPRPicklistLine.class);
		registerModel(I_BPR_POBahanBaku.Table_Name, MBPRPOBahanBaku.class);
		registerModel(I_BPR_POBahanBakuLine.Table_Name, MBPRPOBahanBakuLine.class);
		registerModel(I_BPR_POBahanBakuHeader.Table_Name, MBPRPOBahanBakuHeader.class);
	}

}
