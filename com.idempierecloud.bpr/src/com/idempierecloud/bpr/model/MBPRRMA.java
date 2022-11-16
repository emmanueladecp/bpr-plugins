package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.Query;

public class MBPRRMA extends X_BPR_RMA{

	private static final long serialVersionUID = -5302900548537899896L;
	protected MBPRRMALine[]	m_lines = null;
	
	public MBPRRMA(Properties ctx, int BPR_RMA_ID, String trxName, String[] virtualColumns) {
		super(ctx, BPR_RMA_ID, trxName, virtualColumns);
	}

	public MBPRRMA(Properties ctx, int BPR_RMA_ID, String trxName) {
		super(ctx, BPR_RMA_ID, trxName);
	}

	public MBPRRMA(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	
	public MBPRRMALine[] getLines(){	
		List<MBPRRMALine> list = new Query(getCtx(), MBPRRMALine.Table_Name, COLUMNNAME_BPR_RMA_ID+"=?", get_TrxName())
							.setParameters(get_ID())
							.setOnlyActiveRecords(true)
							.list();
		
		return list.toArray(new MBPRRMALine[list.size()]);
	}
	

}
