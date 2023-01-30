package com.idempierecloud.bpr.component;

import java.sql.ResultSet;

import org.adempiere.base.IDocFactory;
import org.compiere.acct.Doc;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MInOut;
import org.compiere.model.MTable;
import org.compiere.util.Env;

import com.idempierecloud.bpr.acct.Doc_InOut;
import com.idempierecloud.bpr.acct.Doc_Production;
import com.idempierecloud.bpr.model.MProductionExt;

public class DocFactory implements IDocFactory {

	@Override
	public Doc getDocument(MAcctSchema as, int AD_Table_ID, ResultSet rs, String trxName) {

		String tableName = MTable.getTableName(Env.getCtx(), AD_Table_ID);
		if(tableName.equals(MProductionExt.Table_Name))
			return new Doc_Production(as, rs, trxName);
		if(tableName.equals(MInOut.Table_Name))
			return new Doc_InOut(as, rs, trxName);
		
		return null;
	}

}
