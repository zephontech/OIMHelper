/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tech.zephon.oim.api.tester;

import tech.zephon.oim.exceptions.OIMHelperException;
import tech.zephon.oim.api.OIMHelperClient;
import tech.zephon.oim.api.OIMOrganizations;
import tech.zephon.oim.api.OIMRoles;
import tech.zephon.oim.api.OIMUsers;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.iam.identity.orgmgmt.vo.Organization;
import oracle.iam.identity.rolemgmt.vo.Role;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.authopss.vo.AdminRole;
import oracle.iam.platform.authopss.vo.AdminRoleMembership;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platformservice.api.AdminRoleService;
import org.junit.Test;

/**
 *
 */
public class ClientTesterRoles extends OIMHelperClient {

    private static final Logger logger = Logger.getLogger(ClientTesterDMExporter.class.getName());
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
            logger.log(Level.SEVERE,"Error", e);
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
                logger.log(Level.SEVERE,"Error no org");
            }
            logger.fine("ORG:" + org);
        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE,"Error", e);
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
                    logger.fine(ar.getRoleDisplayName() + ":" + ar);
                }
                else
                {
                    logger.fine("Unknown:" + o);
                }
            }
            
        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE,"Error", e);
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
            logger.log(Level.SEVERE,"Error", e);
        }
        showRoles("FFORESTER");
        
        
        try
        {
            oimUsers.revokeUserRole("FFORESTER","SYSTEM ADMINISTRATORS");
            oimUsers.revokeUserAdminRole("FFORESTER","System Administrator");
            
        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE,"Error", e);
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
            logger.fine("getting User");
            User u = oimUsers.getUser(user);
            logger.fine("User:" + u);
            logger.fine("getting User Roles");
            List<Object> uRoles = oimUsers.getAllUsersRoles(u.getId(),true);
            for(Object oimRole : uRoles)
            {
                logger.fine("Role:" + oimRole);
                
            }

            
            List<AdminRoleMembership> mss = oimUsers.getAdminRoleMemberships(new Long(u.getId()));
            for(AdminRoleMembership am : mss)
            {
                logger.fine("AMMember:" + am);
            }
            
        }
        catch(OIMHelperException ex)
        {
            logger.log(Level.SEVERE,"OIMHelperException",ex);
        }

    }
    
    public void setUserAdminRole(String userKey,String roleName,String actKey)
    {
        AdminRoleService ars = (AdminRoleService)getClient().getService(AdminRoleService.class);
        AdminRoleMembership arm = new AdminRoleMembership();
        List<AdminRole> myroles = ars.getAdminRolesForUser(userKey, null);
        for(AdminRole ar : myroles)
        {
            logger.fine(ar.getRoleDisplayName() + ":" + ar);
        }
        
        List<AdminRole> aroles = ars.getAdminRoles(actKey);
        AdminRole arhit = null;
        boolean validrole = false;
        for(AdminRole ar : aroles)
        {
            logger.fine(ar.getRoleDisplayName() + ":" + ar);
            if (roleName.equals(ar.getRoleDisplayName()))
            {
                validrole = true;
                arhit = ar;
                break;
            }
        }
        if(!validrole)
        {
            logger.fine("Role not in Specified Scope");
            return;
        }
        
        arm.setAdminRole(arhit);
        arm.setScopeId(actKey);
        arm.setUserId(userKey);
        logger.fine("Adding role");
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
                logger.fine("User:" + u.getLogin());
                List<Role> uRoles = oimUsers.getAllUsersRoles(u.getId());
                boolean hasRole = false;
                for(Role oimRole : uRoles)
                {
                    logger.fine(oimRole.getName() + ":" + oimRole);
                    if (oimRole.getName().equalsIgnoreCase(roleName))
                        hasRole = true;
                }
                if (hasRole)
                {
                    logger.fine("Removing:" + roleName);
                    oimUsers.revokeUserRole(u.getLogin(),roleName);
                }
                
                AdminRoleService ars = (AdminRoleService)getClient().getService(AdminRoleService.class);
                
                List<AdminRoleMembership> mss = ars.listUsersMembership(u.getId(), null, null, true, null);
                
                List<AdminRole> myroles = ars.getAdminRolesForUser(u.getId(), null);
                for(AdminRole ar : myroles)
                {
                    logger.fine(ar.getRoleDisplayName() + ":" + ar);
                    if (ar.getRoleDisplayName().equalsIgnoreCase(roleName))
                    {
                        String name = ar.getRoleName();
                        for(AdminRoleMembership am : mss)
                        {
                            logger.fine("AMMember:" + am);
                            if (am.getAdminRoleName().equalsIgnoreCase(name))
                            {
                                logger.fine("Removing:" + roleName + ":" + am.getScopeId());
                                ars.removeAdminRoleMembership(am);
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE,"ApiError:" + e.toString(),e);
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
            logger.fine("Processing:" + users.size());
            for(User u : users)
            {
                //logger.fine("User:" + u.getLogin());
                List<Role> uRoles = oimUsers.getAllUsersRoles(u.getId());
                boolean hasRole = false;
                for(Role oimRole : uRoles)
                {
                    //logger.fine(oimRole);
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
            logger.log(Level.SEVERE,"ApiError:" + e.toString(),e);
        }
    }
    
    public void fullTest() {

        //ClientTesterRoles ctr = new ClientTesterRoles();

        
        try
        {
            logger.fine("getting roles");
            List<Role> allRoles = oimRoles.getAllRoles();

            for(Role oimRole : allRoles)
            {
                logger.fine("Role:" + oimRole);
            }
            
        }
        catch(OIMHelperException ex)
        {
            logger.log(Level.SEVERE,"OIMHelperException",ex);
        }
        
        try
        {
            OIMOrganizations aiOrgs = new OIMOrganizations(getClient());
            logger.fine("getting Orgs");
            List<Organization> allOrgs = aiOrgs.getAllOrganizations();

            for(Organization oimOrg : allOrgs)
            {
                logger.fine("ORG:" + oimOrg);
            }
            
        }
        catch(OIMHelperException ex)
        {
            logger.log(Level.SEVERE,"OIMHelperException",ex);
        }

        boolean hasOperator = false;

        try
        {
            logger.fine("getting User");
            User u = oimUsers.getUser("FFORESTER");
            logger.fine("User:" + u);
            logger.fine("getting User Roles");
            List<Role> uRoles = oimUsers.getAllUsersRoles(u.getId());
            for(Role oimRole : uRoles)
            {
                logger.fine("Role:" + oimRole);
                if (oimRole.getName().equalsIgnoreCase("operators"))
                    hasOperator = true;
            }

            if (hasOperator)
            {
                logger.fine("Revoking OPERATORS Role");
                boolean rc = oimUsers.revokeUserRole(u.getLogin(), "OPERATORS");
            }

            uRoles = oimUsers.getAllUsersRoles(u.getId());
            for(Role oimRole : uRoles)
            {
                logger.fine("Role:" + oimRole);
                if (oimRole.getName().equalsIgnoreCase("operators"))
                    hasOperator = false;
            }

            if (!hasOperator)
            {
                logger.fine("Adding OPERATORS Role");
                boolean rc = oimUsers.grantUserRole(u.getLogin(), "OPERATORS");
            }

        }
        catch(OIMHelperException ex)
        {
            logger.log(Level.SEVERE,"OIMHelperException",ex);
        }


    }
}
