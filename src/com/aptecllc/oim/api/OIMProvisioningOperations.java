/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.api;

import com.aptecllc.oim.exceptions.OIMHelperException;
import com.thortech.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.provisioning.api.ProvisioningConstants;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.Account;

/**
 *
 * wrapper for prov service
 */
public class OIMProvisioningOperations {
    
    
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private ProvisioningService provServOps;
    
    /**
     * create using Platform
     * @throws OIMHelperException 
     */
    public OIMProvisioningOperations() throws OIMHelperException {
        
        provServOps = Platform.getService(ProvisioningService.class);
        if (provServOps == null) {
            logger.error("Failed to get provServOps");
            throw new OIMHelperException("provServOps Failed");
        }
    }
    
    /**
     * create with existing OIMClient
     * @param client
     * @throws OIMHelperException 
     */
    public OIMProvisioningOperations(OIMClient client) throws OIMHelperException {
        
        provServOps = client.getService(ProvisioningService.class);
        if (provServOps == null) {
            logger.error("Failed to get provServOps");
            throw new OIMHelperException("provServOps Failed");
        }
    }
    /**
     * get all accounts provisioned to this user
     * @param key
     * @return
     * @throws OIMHelperException 
     */
    public List<Account> getAccountsProvisionedToUser(String key) throws OIMHelperException
    {
        try
        {
            List<Account> accounts = provServOps.getAccountsProvisionedToUser(key);
            return accounts;
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        
    }
    
    /**
     * get all enabled or provisioned objects to this user for this resource
     * @param key
     * @param resourceName
     * @return
     * @throws OIMHelperException 
     */
    public List<Account> getAccountsProvisionedToUser(String key,String resourceName) throws OIMHelperException
    {
        
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
            List<Account> newList = new ArrayList<Account>();
            List<Account> accounts = provServOps.getAccountsProvisionedToUser(key,and,config,true);
            for(Account a : accounts)
            {
                String stat = a.getAccountStatus();
                if (stat.equalsIgnoreCase("enabled") || stat.equalsIgnoreCase("provisioned"))
                    newList.add(a);
            }
            return newList;
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        
    }
    
    /**
     * 
     * @param key
     * @param resourceName
     * @return
     * @throws OIMHelperException 
     */
    public List<Account> getDisabledPrimaryAccount(String key,String resourceName) throws OIMHelperException
    {
        SearchCriteria c2 = new SearchCriteria(ProvisioningConstants.AccountSearchAttribute.ACCOUNT_STATUS.getId(),
                                              "Disabled",
                                              SearchCriteria.Operator.EQUAL);
        SearchCriteria c3 = new SearchCriteria(ProvisioningConstants.AccountSearchAttribute.OBJ_NAME.getId(),
                                              resourceName,
                                              SearchCriteria.Operator.EQUAL);
        SearchCriteria and = new SearchCriteria(c2,c3,SearchCriteria.Operator.AND);
        HashMap config = new HashMap();
        config.put("STARTROW", new Integer(0));
        config.put("ENDROW",new Integer(Integer.MAX_VALUE));
        
        try
        {
            List<Account> accounts = provServOps.getAccountsProvisionedToUser(key,and,config,true);
            return accounts;
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        
    }
    
    public Account getPrimaryAccountProvisionedToUser(String key,String resourceName) throws OIMHelperException
    {
        try
        {
            
            List<Account> accounts = getAccountsProvisionedToUser(key,resourceName);
            for(Account a : accounts)
            {
                if (a.getAccountType().equals(Account.ACCOUNT_TYPE.Primary))
                    return a;
            }
            return null;
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        
    }
    
    public List<Account> getUserAccountsByResource(String key,String resourceName,boolean data) throws OIMHelperException
    {
        
        
        SearchCriteria c3 = new SearchCriteria(ProvisioningConstants.AccountSearchAttribute.OBJ_NAME.getId(),
                                              resourceName,
                                              SearchCriteria.Operator.EQUAL);
        HashMap config = new HashMap();
        config.put("STARTROW", new Integer(0));
        config.put("ENDROW",new Integer(Integer.MAX_VALUE));
        
        try
        {
            List<Account> accounts = provServOps.getAccountsProvisionedToUser(key,c3,config,data);
            return accounts;
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        
    }
    
    public List<Account> getUserAccountsByResourceStatus(String key,String resourceName,List<String> status,boolean data) throws OIMHelperException
    {
        SearchCriteria and = null;
        for(String stat : status)
        {
            if (and == null)
            {
                and = new SearchCriteria(ProvisioningConstants.AccountSearchAttribute.ACCOUNT_STATUS.getId(),
                                              stat,
                                              SearchCriteria.Operator.EQUAL);
                continue;
            }
            SearchCriteria c = new SearchCriteria(ProvisioningConstants.AccountSearchAttribute.ACCOUNT_STATUS.getId(),
                                              stat,
                                              SearchCriteria.Operator.EQUAL);
            and = new SearchCriteria(c,and,SearchCriteria.Operator.OR);
            
        }
        
        SearchCriteria c3 = new SearchCriteria(ProvisioningConstants.AccountSearchAttribute.OBJ_NAME.getId(),
                                              resourceName,
                                              SearchCriteria.Operator.EQUAL);
        
        and = new SearchCriteria(c3,and,SearchCriteria.Operator.AND);
        HashMap config = new HashMap();
        config.put("STARTROW", new Integer(0));
        config.put("ENDROW",new Integer(Integer.MAX_VALUE));
        
        try
        {
            List<Account> accounts = provServOps.getAccountsProvisionedToUser(key,and,config,data);
            return accounts;
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        
    }
    
}
