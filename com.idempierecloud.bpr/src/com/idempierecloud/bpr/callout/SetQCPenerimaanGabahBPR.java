package com.idempierecloud.bpr.callout;

import com.idempierecloud.bpr.base.CustomCallout;
import com.idempierecloud.bpr.model.MBPRTimbangan;

public class SetQCPenerimaanGabahBPR extends CustomCallout{

	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		String isGabah = getTab().get_ValueAsString("isGabah");
		if(isGabah==null || isGabah.equals("N"))
			return null;
		
		int BPR_Timbangan = (int) getValue();
		MBPRTimbangan timbangan = new MBPRTimbangan(getCtx(), BPR_Timbangan, null);
		getTab().setValue("C_BPartner_ID", timbangan.getC_BPartner_ID());
		getTab().setValue("BPR_NoKendaraan", timbangan.getBPR_NoKendaraan());
		getTab().setValue("Tonase", timbangan.getTimbanganNetAmt());
			
		return null;
	}

}
