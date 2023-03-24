-- Mar 24, 2023, 2:45:38 PM ICT
INSERT INTO AD_Process (AD_Process_ID,AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,Name,IsReport,Value,IsDirectPrint,Classname,AccessLevel,EntityType,Statistic_Count,Statistic_Seconds,IsBetaFunctionality,ShowHelp,CopyFromProcess,AD_Process_UU,AllowMultipleExecution) VALUES (4000000,0,0,'Y',TO_TIMESTAMP('2023-03-24 14:45:38','YYYY-MM-DD HH24:MI:SS'),0,TO_TIMESTAMP('2023-03-24 14:45:38','YYYY-MM-DD HH24:MI:SS'),0,'Sync BP','N','SyncBP','N','com.idempierecloud.bpr.process.SyncBP','3','BPR',0,0,'N','Y','N','ca7b6424-db95-41c9-9a9b-c926dc3c128e','P')
;

-- Mar 24, 2023, 2:46:15 PM ICT
INSERT INTO AD_Process_Para (AD_Process_Para_ID,AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,Name,AD_Process_ID,SeqNo,AD_Reference_ID,IsRange,FieldLength,IsMandatory,ColumnName,IsCentrallyMaintained,EntityType,AD_Process_Para_UU,IsEncrypted,IsAutocomplete) VALUES (4000000,0,0,'Y',TO_TIMESTAMP('2023-03-24 14:46:14','YYYY-MM-DD HH24:MI:SS'),0,TO_TIMESTAMP('2023-03-24 14:46:14','YYYY-MM-DD HH24:MI:SS'),0,'BP Value',4000000,10,10,'N',10,'N','Value','N','BPR','91aae5be-cc2f-48ab-b379-0a762baf3df1','N','N')
;

-- Mar 24, 2023, 2:59:18 PM ICT
INSERT INTO AD_Process_Para (AD_Process_Para_ID,AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,Name,AD_Process_ID,SeqNo,AD_Reference_ID,IsRange,FieldLength,IsMandatory,DefaultValue,ColumnName,IsCentrallyMaintained,EntityType,AD_Process_Para_UU,IsEncrypted,IsAutocomplete) VALUES (4000001,0,0,'Y',TO_TIMESTAMP('2023-03-24 14:59:18','YYYY-MM-DD HH24:MI:SS'),0,TO_TIMESTAMP('2023-03-24 14:59:18','YYYY-MM-DD HH24:MI:SS'),0,'Created',4000000,20,15,'N',0,'N','@#Date@','Created','N','BPR','151ed4a3-6522-4e02-bef3-82a30d7e806d','N','N')
;

-- Mar 24, 2023, 2:59:43 PM ICT
INSERT INTO AD_Menu (AD_Menu_ID,Name,"action",AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,IsSummary,AD_Process_ID,IsSOTrx,IsReadOnly,EntityType,IsCentrallyMaintained,AD_Menu_UU) VALUES (4000000,'Sync BP','P',0,0,'Y',TO_TIMESTAMP('2023-03-24 14:59:43','YYYY-MM-DD HH24:MI:SS'),0,TO_TIMESTAMP('2023-03-24 14:59:43','YYYY-MM-DD HH24:MI:SS'),0,'N',4000000,'Y','N','BPR','Y','935d76d6-5126-4663-8e87-1d6990848174')
;

-- Mar 24, 2023, 2:59:43 PM ICT
INSERT INTO AD_TreeNodeMM (AD_Client_ID,AD_Org_ID, IsActive,Created,CreatedBy,Updated,UpdatedBy, AD_Tree_ID, Node_ID, Parent_ID, SeqNo, AD_TreeNodeMM_UU) SELECT t.AD_Client_ID, 0, 'Y', statement_timestamp(), 0, statement_timestamp(), 0,t.AD_Tree_ID, 4000000, 0, 999, Generate_UUID() FROM AD_Tree t WHERE t.AD_Client_ID=0 AND t.IsActive='Y' AND t.IsAllNodes='Y' AND t.TreeType='MM' AND NOT EXISTS (SELECT * FROM AD_TreeNodeMM e WHERE e.AD_Tree_ID=t.AD_Tree_ID AND Node_ID=4000000)
;

-- Sync BP
SELECT register_migration_script('202303241436_SyncBP.sql') FROM dual;

