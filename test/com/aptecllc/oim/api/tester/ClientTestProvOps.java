/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.api.tester;

import Thor.API.Operations.tcProvisioningOperationsIntf;
import Thor.API.tcResultSet;
import com.aptecllc.oim.api.OIMHelperClient;
import com.aptecllc.oim.api.OIMProcessTaskOperations;
import com.aptecllc.oim.api.OIMProvisioningOperations;
import com.aptecllc.oim.api.OIMUsers;
import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.model.OpenTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.provisioning.api.ProvisioningConstants;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.Account;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author fforester
 */
public class ClientTestProvOps extends OIMHelperClient {
    
     private Logger logger = Logger.getLogger(this.getClass().getName());
    
     private tcProvisioningOperationsIntf provOps;
     private OIMProcessTaskOperations taskOps;
     private OIMProvisioningOperations oimProvOps;
     private OIMUsers userOps;
     
     
    @Test
    public void mainTest() {
    try
        {
            loadConfig(null);
            loginWithCustomEnv();
            
            provOps = getClient().getService(tcProvisioningOperationsIntf.class);
            taskOps = new OIMProcessTaskOperations(this.getClient());
            oimProvOps = new OIMProvisioningOperations(this.getClient());
            userOps = new OIMUsers(this.getClient());
            
            showUserResources("CA11330","FinApp Resource");
            //getObjectDetails(6012L);
            
        }
        catch(Exception e)
        {
            logger.error("Failed to connect",e);
            return;
        }
    }
    
    public void showTasks()
    {
        try
        {
            tcResultSet rs = provOps.findAllOpenProvisioningTasks(new HashMap(), new String[]{});
            printResultSet(rs);
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
        }
    }
    
    public void getObjectDetails(long key)
    {
        try
        {
            tcResultSet rs = provOps.getProcessDetail(key);
            printResultSet(rs);
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
        }
    }
    
    public void showWrapperTasks()
    {
        try
        {
            List<OpenTask> tasks = taskOps.getAllOpenTasks(null);
            logger.debug("Tasks:" + tasks.size());
            for(OpenTask ot : tasks)
            {
                logger.debug(ot);
            }
            
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
        }
    }
    
    private void reassign()
    {
        try
        {
            List<OpenTask> tasks = taskOps.getAllOpenTasks(null);
            taskOps.reassignOpenTasksToSysAdminGroup(tasks);
            tasks = taskOps.getAllOpenTasks(null);
            for(OpenTask ot : tasks)
            {
                logger.debug(ot);
            }
            
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
        }
    }
    
    public void retry()
    {
        try
        {
            List<OpenTask> tasks = taskOps.getAllOpenTasks("Disable User");
            taskOps.retryOpenTasks(tasks);
            tasks = taskOps.getAllOpenTasks(null);
            for(OpenTask ot : tasks)
            {
                logger.debug(ot);
            }
            
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
        }
    }
    
    public void manuallyClose(String name)
    {
        try
        {
            List<OpenTask> tasks = taskOps.getAllOpenTasks(name);
            logger.debug("Tasks:" + tasks.size());
            long[] close = new long[tasks.size()];
            int c = 0;
            for(OpenTask t : tasks)
            {
                close[c] = t.getTaskKey();
                c++;
            }
            logger.debug(close.length);
            if (close.length > 0)
                provOps.setTasksCompletedManually(close);
           
            
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
        }
    }
    
    private void showUserResources(String user, String resourceName) {
        try {
            
            String key = userOps.getUserKey(user);
            // get all enabled accounts
            logger.debug("get all enabled accounts");
            List<Account> accounts = oimProvOps.getAccountsProvisionedToUser(key, resourceName);

            for (Account a : accounts) {
                logger.debug("Acnt:"
                        + a.getAppInstance().getObjectName() + ":"
                        + a.getAccountStatus() + ":"
                        + a.getAccountType() + ":"
                        + a.getAccountID() + ":"
                        + a.getProcessInstanceKey() + ":"
                        + a.getAccountDescriptiveField() + ":");
            }
            // get all accounts for this resource regardless of status
            logger.debug("get all accounts for this resource regardless of status");
            accounts = oimProvOps.getUserAccountsByResource(key, resourceName,true);
            for (Account a : accounts) {
                logger.debug("Acnt:"
                        + a.getAppInstance().getObjectName() + ":"
                        + a.getAccountStatus() + ":"
                        + a.getAccountType() + ":"
                        + a.getAccountID() + ":"
                        + a.getProcessInstanceKey() + ":"
                        //+ a.getAccountData().getData() + ":"
                        + a.getAccountDescriptiveField() + ":");
            }
            List<String> statList = new ArrayList<String>();
            statList.add("Disabled");
            statList.add("Enabled");
            // get all accounts for this resource regardless of status
            logger.debug("get all accounts for this resource by status");
            accounts = oimProvOps.getUserAccountsByResourceStatus(key, resourceName,statList,true);
            for (Account a : accounts) {
                logger.debug("Acnt:"
                        + a.getAppInstance().getObjectName() + ":"
                        + a.getAccountStatus() + ":"
                        + a.getAccountType() + ":"
                        + a.getAccountID() + ":"
                        + a.getProcessInstanceKey() + ":"
                        //+ a.getAccountData().getData() + ":"
                        + a.getAccountDescriptiveField() + ":");
            }
            // get the primary account for this resource
            logger.debug("get the primary account for this resource");
            Account a = oimProvOps.getPrimaryAccountProvisionedToUser(key, resourceName);
            if (a != null) {
                logger.debug("Primary:"
                        + a.getAppInstance().getObjectName() + ":"
                        + a.getAccountStatus() + ":"
                        + a.getAccountType() + ":"
                        + a.getAccountID() + ":"
                        + a.getProcessInstanceKey() + ":"
                        + a.getAccountDescriptiveField() + ":");
            }
            this.getAccountsProvisionedToUser(key, resourceName);
            logger.debug("get the primary disabled account for this resource");
            accounts = oimProvOps.getDisabledPrimaryAccount(key, resourceName);
            for (Account b : accounts) {
                logger.debug("Acnt:"
                        + b.getAppInstance().getObjectName() + ":"
                        + b.getAccountStatus() + ":"
                        + b.getAccountType() + ":"
                        + b.getAccountID() + ":"
                        + b.getProcessInstanceKey() + ":"
                        //+ a.getAccountData().getData() + ":"
                        + b.getAccountDescriptiveField() + ":");
            }
        } catch (Exception e) {
        }
    }
    
    public List<Account> getAccountsProvisionedToUser(String key,String resourceName) throws OIMHelperException
    {
        ProvisioningService provServOps = getClient().getService(ProvisioningService.class);
        SearchCriteria c1 = new SearchCriteria(ProvisioningConstants.AccountSearchAttribute.ACCOUNT_STATUS.getId(),
                                              "Provisioned",
                                              SearchCriteria.Operator.EQUAL);
        SearchCriteria c2 = new SearchCriteria(ProvisioningConstants.AccountSearchAttribute.ACCOUNT_STATUS.getId(),
                                              "Enabled",
                                              SearchCriteria.Operator.EQUAL);
        SearchCriteria or = new SearchCriteria(c1,c2,SearchCriteria.Operator.OR);
        SearchCriteria c3 = new SearchCriteria(ProvisioningConstants.AccountSearchAttribute.OBJ_NAME.getId(),
                                              resourceName,
                                              SearchCriteria.Operator.EQUAL);
        SearchCriteria and = new SearchCriteria(c3,or,SearchCriteria.Operator.AND);
        HashMap config = new HashMap();
        config.put("STARTROW", new Integer(0));
        config.put("ENDROW",new Integer(Integer.MAX_VALUE));
        
        try
        {
            //List<Account> newList = new ArrayList<Account>();
            List<Account> accounts = provServOps.getAccountsProvisionedToUser(key,and,config,true);
            logger.debug("Count:" + accounts.size());
            /*
            for(Account a : accounts)
            {
                logger.debug("Status:" + a.getAccountStatus());
                String stat = a.getAccountStatus();
                if (stat.equalsIgnoreCase("enabled") || stat.equalsIgnoreCase("provisioned"))
                    newList.add(a);
            }
            */
            return accounts;
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        
    }
    
    private void printResultSet(tcResultSet rs)
    {
        String[] headers = null;
        try
        {
            int recCount = rs.getRowCount();
            if (recCount > 0)
            {
                headers = rs.getColumnNames();
            }
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                for(String name : headers)
                {
                    logger.debug(name + ":" + rs.getStringValue(name));
                }
                
            }
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
        }
    }
    
}
