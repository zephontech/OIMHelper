/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api.tester;

import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.api.OIMHelperClient;
import com.aptecllc.oim.api.OIMOrganizations;
import com.aptecllc.oim.api.OIMRoles;
import com.aptecllc.oim.api.OIMUsers;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import oracle.iam.identity.orgmgmt.vo.Organization;
import oracle.iam.identity.rolemgmt.vo.Role;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.authopss.vo.AdminRole;
import oracle.iam.platform.authopss.vo.AdminRoleMembership;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platformservice.api.AdminRoleService;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 */
public class ClientTesterRoles extends OIMHelperClient {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private OIMUsers oimUsers;
    private OIMRoles oimRoles;
    private OIMOrganizations oimOrgs;
    
    @Test
    public void mainTest() {
        try {
            loadConfig(null);
            loginWithCustomEnv();
            
            oimUsers = new OIMUsers(getClient());
            oimRoles = new OIMRoles(getClient());
            oimOrgs = new OIMOrganizations(getClient());
        } catch (OIMHelperException e) {
            logger.error("Error", e);
            return;
        }
       /*
        this.setUsersRoles("V", "FINAPPUSER");
        this.setUsersRoles("W", "FINAPPUSER");
        this.setUsersRoles("X", "FINAPPUSER");
        this.setUsersRoles("Y", "FINAPPUSER");
        this.setUsersRoles("Z", "FINAPPUSER");
        
        if (1 == 1)
            return;
        */
        try
        {
            Organization org = oimOrgs.getOrganization("Top",true);
            if (org == null)
            {
                logger.error("Error no org");
            }
            logger.debug("ORG:" + org);
        }
        catch(Exception e)
        {
            logger.error("Error", e);
            return;
        }
        
        try
        {
            List<Object> roles = oimRoles.getAllRoles(true);
            for(Object o : roles)
            {
                if (o instanceof AdminRole)
                {
                    AdminRole ar = (AdminRole)o;
                    logger.debug(ar.getRoleDisplayName() + ":" + ar);
                }
                else
                {
                    logger.debug(o);
                }
            }
            
        }
        catch(Exception e)
        {
            logger.error("Error", e);
            return;
        }
        
        if (1 == 1)
            return;
        
        showRoles("FFORESTER");
        
        
        
        try
        {
            // this gets you BOTH SYSTEM ADMIN and System Admin
            //setUserAdminRole("3007","System Administrator","3");
            oimUsers.grantUserAdminRole("FFORESTER","System Administrator","3");
        }
        catch(Exception e)
        {
            logger.error("Error", e);
        }
        showRoles("FFORESTER");
        
        
        try
        {
            oimUsers.revokeUserRole("FFORESTER","SYSTEM ADMINISTRATORS");
            oimUsers.revokeUserAdminRole("FFORESTER","System Administrator");
            
        }
        catch(Exception e)
        {
            logger.error("Error", e);
        }
        // then you have to remove both
        //removeUsersRoles("FFORESTER","SYSTEM ADMINISTRATORS");
        //removeUsersRoles("FFORESTER","System Administrator");
        
        
        //fullTest();
        showRoles("FFORESTER");
    }
    
    public void showRoles(String user)
    {
        try
        {
            logger.debug("getting User");
            User u = oimUsers.getUser(user);
            logger.debug(u);
            logger.debug("getting User Roles");
            List<Object> uRoles = oimUsers.getAllUsersRoles(u.getId(),true);
            for(Object oimRole : uRoles)
            {
                logger.debug(oimRole);
                
            }

            
            List<AdminRoleMembership> mss = oimUsers.getAdminRoleMemberships(new Long(u.getId()));
            for(AdminRoleMembership am : mss)
            {
                logger.debug(am);
            }
            
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
        }

    }
    
    public void setUserAdminRole(String userKey,String roleName,String actKey)
    {
        AdminRoleService ars = (AdminRoleService)getClient().getService(AdminRoleService.class);
        AdminRoleMembership arm = new AdminRoleMembership();
        List<AdminRole> myroles = ars.getAdminRolesForUser(userKey, null);
        for(AdminRole ar : myroles)
        {
            logger.debug(ar.getRoleDisplayName() + ":" + ar);
        }
        
        List<AdminRole> aroles = ars.getAdminRoles(actKey);
        AdminRole arhit = null;
        boolean validrole = false;
        for(AdminRole ar : aroles)
        {
            logger.debug(ar.getRoleDisplayName() + ":" + ar);
            if (roleName.equals(ar.getRoleDisplayName()))
            {
                validrole = true;
                arhit = ar;
                break;
            }
        }
        if(!validrole)
        {
            logger.debug("Role not in Specified Scope");
            return;
        }
        
        arm.setAdminRole(arhit);
        arm.setScopeId(actKey);
        arm.setUserId(userKey);
        logger.debug("Adding role");
        ars.addAdminRoleMembership(arm);
        
    }
    
    public void removeUsersRoles(String begWith,String roleName)
    {
        try
        {
            SearchCriteria c = new SearchCriteria("User Login",begWith,SearchCriteria.Operator.BEGINS_WITH);
            Set<String> retAttrs = new HashSet<String>();
            retAttrs.add(UserManagerConstants.AttributeName.USER_LOGIN.getId());
            retAttrs.add(UserManagerConstants.AttributeName.USER_KEY.getId());
            HashMap<String,String> config = new HashMap<String,String>();
            config.put("STARTROW", "0");
            config.put("ENDROW",Integer.toString(Integer.MAX_VALUE));
            
            List<User> users = oimUsers.search(c, retAttrs, config);
            for(User u : users)
            {
                logger.debug("User:" + u.getLogin());
                List<Role> uRoles = oimUsers.getAllUsersRoles(u.getId());
                boolean hasRole = false;
                for(Role oimRole : uRoles)
                {
                    logger.debug(oimRole.getName() + ":" + oimRole);
                    if (oimRole.getName().equalsIgnoreCase(roleName))
                        hasRole = true;
                }
                if (hasRole)
                {
                    logger.debug("Removing:" + roleName);
                    oimUsers.revokeUserRole(u.getLogin(),roleName);
                }
                
                AdminRoleService ars = (AdminRoleService)getClient().getService(AdminRoleService.class);
                
                List<AdminRoleMembership> mss = ars.listUsersMembership(u.getId(), null, null, true, null);
                
                List<AdminRole> myroles = ars.getAdminRolesForUser(u.getId(), null);
                for(AdminRole ar : myroles)
                {
                    logger.debug(ar.getRoleDisplayName() + ":" + ar);
                    if (ar.getRoleDisplayName().equalsIgnoreCase(roleName))
                    {
                        String name = ar.getRoleName();
                        for(AdminRoleMembership am : mss)
                        {
                            logger.debug(am);
                            if (am.getAdminRoleName().equalsIgnoreCase(name))
                            {
                                logger.debug("Removing:" + roleName + ":" + am.getScopeId());
                                ars.removeAdminRoleMembership(am);
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            logger.error("ApiError:" + e.toString(),e);
        }
    }
    
    public void setUsersRoles(String begWith,String roleName)
    {
        try
        {
            
            SearchCriteria c = new SearchCriteria("User Login",begWith,SearchCriteria.Operator.BEGINS_WITH);
            Set<String> retAttrs = new HashSet<String>();
            retAttrs.add(UserManagerConstants.AttributeName.USER_LOGIN.getId());
            retAttrs.add(UserManagerConstants.AttributeName.USER_KEY.getId());
            HashMap<String,String> config = new HashMap<String,String>();
            config.put("STARTROW", "0");
            config.put("ENDROW",Integer.toString(Integer.MAX_VALUE));
            
            List<User> users = oimUsers.search(c, retAttrs, config);
            logger.debug("Processing:" + users.size());
            for(User u : users)
            {
                //logger.debug("User:" + u.getLogin());
                List<Role> uRoles = oimUsers.getAllUsersRoles(u.getId());
                boolean hasRole = false;
                for(Role oimRole : uRoles)
                {
                    //logger.debug(oimRole);
                    if (oimRole.getName().equalsIgnoreCase(roleName))
                        hasRole = true;
                }
                if (!hasRole)
                {
                    oimUsers.grantUserRole(u.getLogin(),roleName);
                }
            }
        }
        catch(Exception e)
        {
            logger.error("ApiError:" + e.toString(),e);
        }
    }
    
    public void fullTest() {

        //ClientTesterRoles ctr = new ClientTesterRoles();

        
        try
        {
            logger.debug("getting roles");
            List<Role> allRoles = oimRoles.getAllRoles();

            for(Role oimRole : allRoles)
            {
                logger.debug(oimRole);
            }
            
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
        }
        
        try
        {
            OIMOrganizations aiOrgs = new OIMOrganizations(getClient());
            logger.debug("getting Orgs");
            List<Organization> allOrgs = aiOrgs.getAllOrganizations();

            for(Organization oimOrg : allOrgs)
            {
                logger.debug(oimOrg);
            }
            
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
        }

        boolean hasOperator = false;

        try
        {
            logger.debug("getting User");
            User u = oimUsers.getUser("FFORESTER");
            logger.debug(u);
            logger.debug("getting User Roles");
            List<Role> uRoles = oimUsers.getAllUsersRoles(u.getId());
            for(Role oimRole : uRoles)
            {
                logger.debug(oimRole);
                if (oimRole.getName().equalsIgnoreCase("operators"))
                    hasOperator = true;
            }

            if (hasOperator)
            {
                logger.debug("Revoking OPERATORS Role");
                boolean rc = oimUsers.revokeUserRole(u.getLogin(), "OPERATORS");
            }

            uRoles = oimUsers.getAllUsersRoles(u.getId());
            for(Role oimRole : uRoles)
            {
                logger.debug(oimRole);
                if (oimRole.getName().equalsIgnoreCase("operators"))
                    hasOperator = false;
            }

            if (!hasOperator)
            {
                logger.debug("Adding OPERATORS Role");
                boolean rc = oimUsers.grantUserRole(u.getLogin(), "OPERATORS");
            }

        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
        }


    }
}
