/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api.tester;

import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.api.OIMHelperClient;
import com.aptecllc.oim.api.OIMUsers;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 */
public class ClientTesterUser extends OIMHelperClient {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private OIMUsers oimUsers;
    

    @Test
    public void mainTest() {

        
        //ClientTesterUser ctu = new ClientTesterUser();

        try
        {
            loadConfig(null);
            loginWithCustomEnv();
            oimUsers = new OIMUsers(getClient());
            //oimUsers.evaluatePolicies(null);
            //testUser();
            //updateUser("AA10686");
            //searchUsers();
            showUser("FREDDYWOODS");
            
            String number = "3";
            HashMap map = new HashMap();
            //map.put("PREF_FIRST","PrefFirst" + number);
            //map.put("PREF_FIRST",null);
            //map.put("PRI_FIRST","PriFirst" + number);
            //map.put("PREF_LAST","PrefLast" + number);
            //map.put("PREF_LAST",null);
            //map.put("PRI_LAST","PriLast" + number);
            //map.put("PREF_MIDDLE","PrefMiddle" + number);
            //map.put("PREF_MIDDLE",null);
            //map.put("PRI_MIDDLE","PriMiddle" + number);
            //map.put("PREF_SUFFIX","PrefSuffix" + number);
            //map.put("PRI_SUFFIX","PriSuffix" + number);
            //map.put("PREF_PREFIX","PfPrefix" + number);
            //map.put("PRI_PREFIX","PriPrefix" + number);
            
            //map.put("Email1","email1-" + number + "@joe.com");
            //map.put("Email2","email2-" + number + "@joe.com");
            //map.put("Email3","email3-" + number + "@joe.com");
            map.put("Acct_Claimed","Y");
            //updateUser("RG10070",map);
            
            /*
            List<User> users = this.searchUsersByLogin("AA");
            for(User u : users)
            {
                logger.debug("Login:" + u.getLogin());
                oimUsers.grantUserRole(u.getLogin(), "DUMMYAPP_ACTIVE");
            }
            */
        }
        catch(Exception e)
        {
            logger.error("Failed to connect",e);
            return;
        }
        
        //searchUsers();
        /*
        for(int i=0;i<usersU.length;i++)
        {
            String gpn = usersU[i];
            try
            {
                updateUser(gpn);
            }
            catch(Exception e)
            {
                logger.error("ERROR:" + e.getMessage());
            }
        }
        */
    }

    public void searchUsers()
    {
        java.util.Date dt = null;
        try
        {
            dt = new SimpleDateFormat("MM/dd/yy").parse("11/14/12");
        }
        catch(Exception e)
        {
            logger.error("Search Error:" + e.toString(),e);
            return;
        }
        
        SearchCriteria c = new SearchCriteria(UserManagerConstants.AttributeName.HIRE_DATE.getId(),dt,SearchCriteria.Operator.EQUAL);
        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(AttributeName.USER_LOGIN.getId());
        retAttrs.add(AttributeName.USER_KEY.getId());
        retAttrs.add(UserManagerConstants.AttributeName.EMAIL.getId());
        retAttrs.add(UserManagerConstants.AttributeName.FIRSTNAME.getId());
        retAttrs.add(UserManagerConstants.AttributeName.LASTNAME.getId());
        retAttrs.add(UserManagerConstants.AttributeName.HIRE_DATE.getId());
        retAttrs.add(UserManagerConstants.AttributeName.STATUS.getId());
        HashMap<String,Integer> config = new HashMap<String,Integer>();
        config.put("STARTROW", new Integer(0));
        config.put("ENDROW",new Integer(Integer.MAX_VALUE));
        try
        {
            List<User> users = oimUsers.search(c, retAttrs, config);
            for(User u : users)
            {
                logger.debug("User:" + u.getLogin() + ":" + u.getId());
            }
        }
        catch(Exception e)
        {
            logger.error("Search Error:" + e.toString(),e);
        }
        
    }
    
    
    public List<User> searchUsersByLogin(String loginId)
    {
        
        SearchCriteria c = new SearchCriteria(UserManagerConstants.AttributeName.USER_LOGIN.getId(),loginId,SearchCriteria.Operator.BEGINS_WITH);
        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(AttributeName.USER_LOGIN.getId());
        retAttrs.add(AttributeName.USER_KEY.getId());
        retAttrs.add(UserManagerConstants.AttributeName.EMAIL.getId());
        retAttrs.add(UserManagerConstants.AttributeName.FIRSTNAME.getId());
        retAttrs.add(UserManagerConstants.AttributeName.LASTNAME.getId());
        retAttrs.add(UserManagerConstants.AttributeName.HIRE_DATE.getId());
        retAttrs.add(UserManagerConstants.AttributeName.STATUS.getId());
        HashMap<String,Integer> config = new HashMap<String,Integer>();
        config.put("STARTROW", new Integer(0));
        config.put("ENDROW",new Integer(Integer.MAX_VALUE));
        List<User> users = null;
        try
        {
            users = oimUsers.search(c, retAttrs, config);
            
        }
        catch(Exception e)
        {
            logger.error("Search Error:" + e.toString(),e);
        }
        return users;
        
    }
    
    private void updateUser(String userLogin,HashMap userFields)
    {
        logger.debug("Updateuser");
        try
        {
            oimUsers.setUserValue(userLogin,userFields);
        }
        catch(Exception e)
        {
            logger.error("OIMHelperException ",e);
            return;

        }
    }
    
    private void updateUser(String gpn)
    {
        logger.debug("Updateuser");
        try
        {
            //oimUsers.setUserValue("CG010068544@EYQA.NET","Title","Director");
            HashMap upd = new HashMap();
            //upd.put("Title", "Director");
            //upd.put("RANK_CODE","61");
            //upd.put("LYNC_USER_STATUS","ReadyForReinstatement");
            //upd.put("LYNC_ACCNT_EXCEPTION","Reinstate");
            //upd.put("LYNC_USER_STATUS","ReadyForProvisioning");
            //upd.put("LYNC_USER_STATUS","ReadyForTermination");
            //upd.put("LYNC_ACCNT_EXCEPTION","Terminate");
            //oimUsers.setUserValue("GPN",gpn,upd);
            upd.put("EYAD_USER_STATUS","Disabled");
            //upd.put("EMPLOYMENT_STATUS_TEXT","T");
            oimUsers.setUserValue(gpn,upd);
        }
        catch(Exception e)
        {
            logger.error("OIMHelperException ",e);
            return;

        }
    }
    
    private void updateUserAll()
    {
        String[] lyncFields = {"key"};
        String[] data1 = {"val"};
        logger.debug("Updateuser");
        for(int i=6;i<lyncFields.length;i++)
        {
            try
            {
                oimUsers.setUserValue("US013148904@EYQA.NET",lyncFields[i],"FF-" + data1[i]);
            }
            catch(OIMHelperException e)
            {
                logger.error("OIMHelperException ",e);
                return;

            }
        }
    }
    
    private void showUser(String userId)
    {
        try
        {
            OIMUsers oimUsers = new OIMUsers(getClient());
            logger.debug("getting User by ID");
            Set<String> retAttrs = new HashSet<String>();
            retAttrs.add(AttributeName.USER_LOGIN.getId());
            retAttrs.add("CertExpDate");
            retAttrs.add("RegistraDate");
            
            User u = oimUsers.getUser(userId);
            if (u == null)
            {
                logger.error("User Not Found");
                return;
            }
            
            Set<String> attrSet = u.getAttributeNames();
            for(String name : attrSet)
            {
                logger.debug("Name " + name + ":" + u.getAttribute(name));
            }
        }
        catch(Exception e)
        {
            
        }
    }
    
    private void testUser()
    {

        try
        {
            OIMUsers oimUsers = new OIMUsers(getClient());
            logger.debug("getting User by ID");
            User u = oimUsers.getUser("EYOIMADMIN");
            Set<String> attrSet = u.getAttributeNames();
            for(String name : attrSet)
            {

                logger.debug("Name " + name + ":" + u.getAttribute(name));
            }

            
            logger.debug("");
            logger.debug("getting User by UDF Field");
            u = oimUsers.getUser("GPN", "EYOIMADMIN");
            if (u == null)
            {
                logger.error("User Not Found");
                return;
            }

            attrSet = u.getAttributeNames();
            for(String name : attrSet)
            {
                logger.debug("Name " + name + ":" + u.getAttribute(name));
            }

            logger.debug("isUserActive EYOIMADMIN " + oimUsers.isUserActive("EYOIMADMIN"));
            //logger.debug("isUserActive TESTUSER2 " + aiUsers.isUserActive("TESTUSER2"));

            String key = oimUsers.getUserKey("EYOIMADMIN");
            logger.debug("userKey for EYOIMADMIN " + key);
            String login = oimUsers.getUserLoginByKey(key);
            logger.debug("userLogin for key " + key + " = " + login);

            //aiUsers.evaluatePolicies(login);
            //aiUsers.evaluatePolicies(new Long(key).longValue());
            logger.debug("updating User UDF Field");
            oimUsers.setUserValue("EYOIMADMIN","GPN","EYOIMADMINX");
            
            u = oimUsers.getUser("GPN", "EYOIMADMINX");
            if (u == null)
            {
                logger.error("User Not Found");
                return;
            }
            attrSet = u.getAttributeNames();
            for(String name : attrSet)
            {
                if (name.equals("GPN"))
                    logger.debug("Name " + name + ":" + u.getAttribute(name));
            }
            


        }
        catch(OIMHelperException e)
        {
            logger.error("OIMHelperException ",e);
            return;

        }

    }

}
