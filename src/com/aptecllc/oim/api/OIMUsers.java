/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api;

import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.oimutils.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import oracle.iam.accesspolicy.api.AccessPolicyService;
import oracle.iam.accesspolicy.exception.AccessPolicyEvaluationException;
import oracle.iam.accesspolicy.exception.AccessPolicyEvaluationUnauthorizedException;
import oracle.iam.accesspolicy.exception.AccessPolicyServiceException;
import oracle.iam.identity.exception.NoSuchUserException;
import oracle.iam.identity.exception.SearchKeyNotUniqueException;
import oracle.iam.identity.exception.UserDisableException;
import oracle.iam.identity.exception.UserEnableException;
import oracle.iam.identity.exception.UserLookupException;
import oracle.iam.identity.exception.UserMembershipException;
import oracle.iam.identity.exception.UserModifyException;
import oracle.iam.identity.exception.ValidationFailedException;
import oracle.iam.platform.OIMClient;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;
import oracle.iam.platform.authz.exception.AccessDeniedException;

import oracle.iam.identity.rolemgmt.api.RoleManager;
import oracle.iam.identity.rolemgmt.vo.Role;

import oracle.iam.identity.orgmgmt.api.OrganizationManager;
import oracle.iam.identity.usermgmt.vo.UserManagerResult;
import oracle.iam.platform.Platform;
import oracle.iam.platform.authopss.vo.AdminRole;
import oracle.iam.platform.authopss.vo.AdminRoleMembership;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platformservice.api.AdminRoleService;

import org.apache.log4j.Logger;


/**
 *
 */
public class OIMUsers extends BaseHelper {

    private UserManager userOp;
    private RoleManager roleOp;
    private AdminRoleService arsOp;
    private OrganizationManager orgOp;
    private AccessPolicyService accessPolicySvc;
    
    private Logger logger = Logger.getLogger(this.getClass().getName());


    /**
     * Constructor, Used when in adapter mode
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMUsers(UserManager userOp,RoleManager roleOp) {
        this.userOp = userOp;
        this.roleOp = roleOp;
    }

    /**
     * Constructor. Used when in adapter mode
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMUsers(UserManager userOp,RoleManager roleOp, OrganizationManager orgOp) {
        this.userOp = userOp;
        this.roleOp = roleOp;
        this.orgOp = orgOp;
    }

    /**
     * Constructor adapter mode via Platform
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMUsers() throws OIMHelperException {
        roleOp = Platform.getService(RoleManager.class);
        if (roleOp == null)
        {
            logger.error("Failed to get Role OP");
            throw new OIMHelperException("Role Op Failed");
        }

        userOp = Platform.getService(UserManager.class);
        if (userOp == null)
        {
            logger.error("Failed to get User OP");
            throw new OIMHelperException("User Op Failed");
        }

        orgOp = Platform.getService(OrganizationManager.class);
        if (orgOp == null)
        {
            logger.error("Failed to get Org OP");
            throw new OIMHelperException("Org Op Failed");
        }

        accessPolicySvc = Platform.getService(AccessPolicyService.class);
        if (accessPolicySvc == null)
        {
            logger.error("Failed to get accessPolicySvc");
            throw new OIMHelperException("accessPolicySvc Failed");
        }
        arsOp = Platform.getService(AdminRoleService.class);
        if (arsOp == null)
        {
            logger.error("Failed to get AdminRole OP");
            throw new OIMHelperException("AdminRole Op Failed");
        }
    }

    /**
     * Constructor
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMUsers(OIMClient client) throws OIMHelperException {
        roleOp = client.getService(RoleManager.class);
        if (roleOp == null)
        {
            logger.error("Failed to get Role OP");
            throw new OIMHelperException("Role Op Failed");
        }

        userOp = client.getService(UserManager.class);
        if (userOp == null)
        {
            logger.error("Failed to get User OP");
            throw new OIMHelperException("User Op Failed");
        }

        orgOp = client.getService(OrganizationManager.class);
        if (orgOp == null)
        {
            logger.error("Failed to get Org OP");
            throw new OIMHelperException("Org Op Failed");
        }

        accessPolicySvc = client.getService(AccessPolicyService.class);
        if (accessPolicySvc == null)
        {
            logger.error("Failed to get accessPolicySvc");
            throw new OIMHelperException("accessPolicySvc Failed");
        }
        arsOp = client.getService(AdminRoleService.class);
        if (arsOp == null)
        {
            logger.error("Failed to get AdminRole OP");
            throw new OIMHelperException("AdminRole Op Failed");
        }
    }

    /**
     * Get User By Login ID
     *
     * returns null of user not found
     * @param userLogin
     * @exception OIMHelperException
     */
    public User getUser(String userLogin) throws OIMHelperException
    {
        // below demonstrates howto use the attributes
        // if we pass null tho we get them all back
        //Set<String> retAttrs = new HashSet<String>();
        //retAttrs.add(AttributeName.USER_KEY.getId());
        //retAttrs.add(AttributeName.USER_LOGIN.getId());
        //retAttrs.add(AttributeName.USERTYPE.getId());
        //retAttrs.add(AttributeName.EMAIL.getId());
        //retAttrs.add(AttributeName.FIRSTNAME.getId());
        //retAttrs.add(AttributeName.LASTNAME.getId());
        //retAttrs.add(AttributeName.STATUS.getId());
        
        User user = null;
        
        try {
            user = userOp.getDetails(userLogin, null, true);
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + userLogin);
        } catch (UserLookupException ex) {
            logger.error("UserLookupException",ex);
            throw new OIMHelperException(ex);
        }
        return user;
    }
    
    /**
     * Get User By Login ID
     *
     * returns null of user not found
     * @param userLogin
     * @exception OIMHelperException
     */
    public User getUser(String userLogin,Set<String> retAttrs) throws OIMHelperException
    {
        // below demonstrates howto use the attributes
        // if we pass null tho we get them all back
        //Set<String> retAttrs = new HashSet<String>();
        //retAttrs.add(AttributeName.USER_KEY.getId());
        //retAttrs.add(AttributeName.USER_LOGIN.getId());
        //retAttrs.add(AttributeName.USERTYPE.getId());
        //retAttrs.add(AttributeName.EMAIL.getId());
        //retAttrs.add(AttributeName.FIRSTNAME.getId());
        //retAttrs.add(AttributeName.LASTNAME.getId());
        //retAttrs.add(AttributeName.STATUS.getId());
        
        User user = null;
        
        try {
            user = userOp.getDetails(userLogin, retAttrs, true);
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + userLogin);
        } catch (UserLookupException ex) {
            logger.error("UserLookupException",ex);
            throw new OIMHelperException(ex);
        }
        return user;
    }
    
    /**
     * Get User By Login ID
     *
     * returns null of user not found
     * @param userLogin
     * @exception OIMHelperException
     */
    public User getUser(String critName,String critValue,Set<String> retAttrs) throws OIMHelperException
    {
        // below demonstrates howto use the attributes
        // if we pass null tho we get them all back
        //Set<String> retAttrs = new HashSet<String>();
        //retAttrs.add(AttributeName.USER_KEY.getId());
        //retAttrs.add(AttributeName.USER_LOGIN.getId());
        //retAttrs.add(AttributeName.USERTYPE.getId());
        //retAttrs.add(AttributeName.EMAIL.getId());
        //retAttrs.add(AttributeName.FIRSTNAME.getId());
        //retAttrs.add(AttributeName.LASTNAME.getId());
        //retAttrs.add(AttributeName.STATUS.getId());
        
        User user = null;
        
        try {
            user = userOp.getDetails(critName,critValue,retAttrs);
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + critName + ":" + critValue);
        } catch (UserLookupException ex) {
            logger.error("UserLookupException",ex);
            throw new OIMHelperException(ex);
        } catch (SearchKeyNotUniqueException ex) {
            logger.error("SearchKeyNotUniqueException",ex);
            throw new OIMHelperException(ex);
        }
        return user;
    }

    /**
     * Check if User Exists
     *
     * @param userLogin
     * @exception OIMHelperException
     */
    public boolean userExists(String userLogin) throws OIMHelperException
    {
        try
        {
            User u = getUser(userLogin);
            if (u == null)
                return false;
            return true;
        }
        catch(OIMHelperException e)
        {
            throw e;
        }
    }

    /**
     * Get User based on Key Value Pair
     *
     * returns null of user not found
     * @param attrName - USR Record Attribute
     * @param attrValue - USR Record Attribute Value
     * @exception OIMHelperException
     */
    public User getUser(String attrName,String attrValue) throws OIMHelperException
    {

        User user = null;

        try {
            user = userOp.getDetails(attrName, attrValue, null);
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + attrName + ":" + attrValue);
        } catch (UserLookupException ex) {
            logger.error("UserLookupException",ex);
            throw new OIMHelperException(ex);
        } catch (SearchKeyNotUniqueException ex) {
            logger.error("SearchKeyNotUniqueException",ex);
            throw new OIMHelperException(ex);
        }
        return user;
    }

    /**
     * Check if User exists based on Key Value Pair
     *
     * @param attrName - USR Record Attribute
     * @param attrValue - USR Record Attribute Value
     * @exception OIMHelperException
     */
    public boolean userExists(String attrName,String attrValue) throws OIMHelperException
    {
        try
        {
            User u = getUser(attrName,attrValue);
            if (u == null)
                return false;
            return true;
        }
        catch(OIMHelperException e)
        {
            throw e;
        }
    }

    /**
     * Check if User is Active
     *
     * @param userLogin
     * @exception OIMHelperException
     */
    public boolean isUserActive(String userLogin) throws OIMHelperException
    {
        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(AttributeName.USER_LOGIN.getId());
        retAttrs.add(AttributeName.STATUS.getId());

        User user = null;

        try {
            user = userOp.getDetails(userLogin, retAttrs, true);
            String status = user.getStatus();
            if (status != null && status.equalsIgnoreCase("active"))
                return true;
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + userLogin);
            throw new OIMHelperException(ex);
        } catch (UserLookupException ex) {
            logger.error("UserLookupException",ex);
            throw new OIMHelperException(ex);
        }
        return false;

    }

    /**
     * Get User Login base on Key Value Criteria
     * returns null of user not found
     * @param attrName - USR Record Attribute
     * @param attrValue - USR Record Attribute Value
     * @exception OIMHelperException
     */
    public String getUserLogin(String attrName,String attrValue) throws OIMHelperException
    {
        
        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(AttributeName.USER_LOGIN.getId());

        try {
            User user = userOp.getDetails(attrName, attrValue, retAttrs);
            return user.getLogin();
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + attrName + ":" + attrValue);
        } catch (UserLookupException ex) {
            logger.error("UserLookupException",ex);
            throw new OIMHelperException(ex);
        } catch (SearchKeyNotUniqueException ex) {
            logger.error("SearchKeyNotUniqueException",ex);
            throw new OIMHelperException(ex);
        }
        return null;
        
    }

    /**
     * Get User Login base on USR_KEY
     * returns null of user not found
     * @param userKey
     * @exception OIMHelperException
     */
    public String getUserLoginByKey(String userKey) throws OIMHelperException
    {

        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(AttributeName.USER_LOGIN.getId());

        try {
            User user = userOp.getDetails(AttributeName.USER_KEY.getId(), userKey, retAttrs);
            return user.getLogin();
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + userKey);
        } catch (UserLookupException ex) {
            logger.error("UserLookupException",ex);
            throw new OIMHelperException(ex);
        } catch (SearchKeyNotUniqueException ex) {
            logger.error("SearchKeyNotUniqueException",ex);
            throw new OIMHelperException(ex);
        }
        return null;

    }

    /**
     * Get User Key base on USR_LOGIN
     * returns null of user not found
     * @param userLogin
     * @exception OIMHelperException
     */
    public String getUserKey(String userLogin) throws OIMHelperException
    {

        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(AttributeName.USER_KEY.getId());

        try {
            User user = userOp.getDetails(userLogin, retAttrs, true);
            return user.getId();
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + userLogin);
        } catch (UserLookupException ex) {
            logger.error("UserLookupException",ex);
            throw new OIMHelperException(ex);
        }
        return null;

    }
    
    public String getUserKey(String attrName,String attrValue) throws OIMHelperException
    {
        
        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(AttributeName.USER_KEY.getId());

        try {
            User user = userOp.getDetails(attrName, attrValue, retAttrs);
            return user.getId();
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + attrName + ":" + attrValue);
        } catch (UserLookupException ex) {
            logger.error("UserLookupException",ex);
            throw new OIMHelperException(ex);
        } catch (SearchKeyNotUniqueException ex) {
            logger.error("SearchKeyNotUniqueException",ex);
            throw new OIMHelperException(ex);
        }
        return null;
        
    }

    /**
     * Disable this user via Login
     * 
     * @param userLogin
     * @exception OIMHelperException
     */
    public void disableUser(String userLogin) throws OIMHelperException
    {

        UserManagerResult res = null;

        try {
            res = userOp.disable(userLogin, true);
            if (!res.getStatus().equalsIgnoreCase("completed"))
            {
                logger.error("Failed to disable user " + userLogin + ":" + res.getFailedResults());
                throw new OIMHelperException("Failed to disable user " + userLogin);
            }
        }
        catch(ValidationFailedException ex)
        {
            logger.error("ValidationFailedException",ex);
            throw new OIMHelperException(ex);
        }
        catch(AccessDeniedException ex)
        {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        }
        catch(UserDisableException ex)
        {
            logger.error("UserDisableException",ex);
            //throw new OIMHelperException(ex);
        }
        catch(NoSuchUserException ex)
        {
            logger.error("NoSuchUserException",ex);
            //throw new OIMHelperException(ex);
        }

        return;
    }

    /**
     * Enable this user via Login
     *
     * @param userLogin
     * @exception OIMHelperException
     */
    public void enableUser(String userLogin) throws OIMHelperException
    {

        UserManagerResult res = null;

        try {
            res = userOp.enable(userLogin, true);
            if (!res.getStatus().equalsIgnoreCase("completed"))
            {
                logger.error("Failed to enable user " + userLogin + ":" + res.getFailedResults());
                throw new OIMHelperException("Failed to enable user " + userLogin);
            }
        }
        catch(ValidationFailedException ex)
        {
            logger.error("ValidationFailedException",ex);
            throw new OIMHelperException(ex);
        }
        catch(AccessDeniedException ex)
        {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        }
        catch(UserEnableException ex)
        {
            logger.error("UserEnableException",ex);
            //throw new OIMHelperException(ex);
        }
        catch(NoSuchUserException ex)
        {
            logger.error("NoSuchUserException",ex);
            //throw new OIMHelperException(ex);
        }

        return;
    }

    /**
     * Get all Roles Associated with this user
     *
     * @param userKey
     * @exception OIMHelperException
     */
    public List<Role> getAllUsersRoles(String userKey) throws OIMHelperException
    {

        List<Role> roles = null;

        try {
            roles = roleOp.getUserMemberships(userKey, true);
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (UserMembershipException ex) {
            logger.error("UserMembershipException",ex);
            throw new OIMHelperException(ex);
        }

        return roles;
    }
    
    /**
     * returns all the roles a user has. caller checks instanceof
     * Role or AdminRole
     * @param includeAdmin
     * @return
     * @throws OIMHelperException 
     */
    public List<Object> getAllUsersRoles(String userKey,boolean includeAdmin) throws OIMHelperException
    {

        List<Object> allRoles = new ArrayList<Object>();

        try {
            List<Role> roles = roleOp.getUserMemberships(userKey, true);
            allRoles.addAll(roles);
            if (includeAdmin)
            {
                List<AdminRole> adminRoles = arsOp.getAdminRolesForUser(userKey, null);
                allRoles.addAll(adminRoles);
            }
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (UserMembershipException ex) {
            logger.error("UserMembershipException",ex);
            throw new OIMHelperException(ex);
        }

        return allRoles;
    }
    
    /**
     * 
     * @param usrKey
     * @return
     * @throws OIMHelperException 
     */
    public List<AdminRoleMembership> getAdminRoleMemberships(Long usrKey) throws OIMHelperException
    {
        try
        {
            List<AdminRoleMembership> mss = arsOp.listUsersMembership(usrKey.toString(), null, null, true, null);
            return mss;
        }
        catch(Exception ex)
        {
            logger.error("OIMHelperException",ex);
            throw new OIMHelperException(ex);
        }
        
    }

    /**
     * Revoke the Role Associated with this user
     *
     * @param userKey
     * @param roleName
     * @exception OIMHelperException
     */
    public boolean revokeUserRole(Long usrKey,String roleName) throws OIMHelperException
    {
        if (usrKey == null)
            throw new OIMHelperException("Invalid usrKey");

        if (StringUtils.isEmpty(roleName) || StringUtils.isBlank(roleName))
            throw new OIMHelperException("Invalid roleName");
        
        OIMRoles aiRoles = new OIMRoles(roleOp);
        try
        {
            List<Role> roles = aiRoles.getRole(roleName);
            if (roles.isEmpty())
                return true;

            Set roleKeySet = new HashSet();

            for(Role role : roles)
            {
                if (role.getName().equalsIgnoreCase(roleName))
                {
                    logger.debug("Adding role to revoke list " + role.getEntityId());
                    roleKeySet.add(role.getEntityId());
                }
            }

            logger.debug("Revoking roles from user id " + usrKey);
            aiRoles.revokeRolesFromUser(usrKey.toString(), roleKeySet);

        }
        catch(OIMHelperException e)
        {
            throw new OIMHelperException(e);
        }


        return true;
    }
    
    /**
     * Revoke the Role Associated with this user
     *
     * @param userKey
     * @param roleName
     * @exception OIMHelperException
     */
    public void revokeUserAdminRole(Long usrKey,String roleName) throws OIMHelperException
    {
        try {
            
            logger.debug("Checking Admin Roles for user id " + usrKey);
            List<AdminRoleMembership> mss = arsOp.listUsersMembership(usrKey.toString(), null, null, true, null);

            List<AdminRole> myroles = arsOp.getAdminRolesForUser(usrKey.toString(), null);
            for (AdminRole ar : myroles) {
                if (ar.getRoleDisplayName().equalsIgnoreCase(roleName)) {
                    String name = ar.getRoleName();
                    for (AdminRoleMembership am : mss) {
                        logger.debug(am);
                        if (am.getAdminRoleName().equalsIgnoreCase(name)) {
                            logger.debug("Removing:" + roleName + ":" + am.getScopeId());
                            arsOp.removeAdminRoleMembership(am);
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new OIMHelperException(e);
        }
    }

    /**
     * Revoke the Role Associated with this user
     *
     * @param userKey
     * @param roleName
     * @exception OIMHelperException
     */
    public boolean revokeUserRole(String userId,String roleName) throws OIMHelperException
    {
        User u = getUser(userId);
        if (u == null)
        {
            logger.error("User Not Found " + userId);
            throw new OIMHelperException("User Not Found in OIM " + userId);
        }
        return revokeUserRole(new Long(u.getId()),roleName);
    }
    
    public void revokeUserAdminRole(String userId,String roleName) throws OIMHelperException
    {
        User u = getUser(userId);
        if (u == null)
        {
            logger.error("User Not Found " + userId);
            throw new OIMHelperException("User Not Found in OIM " + userId);
        }
        revokeUserAdminRole(new Long(u.getId()),roleName);
    }


    /**
     * Grant the Role Associated with this user
     *
     * @param userKey
     * @param roleName
     * @exception OIMHelperException
     */
    public boolean grantUserRole(Long usrKey,String roleName) throws OIMHelperException
    {
        if (usrKey == null)
            throw new OIMHelperException("Invalid usrKey");

        if (StringUtils.isEmpty(roleName) || StringUtils.isBlank(roleName))
            throw new OIMHelperException("Invalid roleName");

        OIMRoles aiRoles = new OIMRoles(roleOp);
        try
        {
            List<Role> roles = aiRoles.getRole(roleName);
            logger.debug("Lookup role to grant " + roles);
            if (roles.isEmpty())
            {
                throw new OIMHelperException("Role does not exist in OIM " + roleName);
            }

            Set roleKeySet = new HashSet();

            for(Role role : roles)
            {
                if (role.getName().equalsIgnoreCase(roleName))
                {
                    logger.debug("Adding role to grant list " + role.getEntityId());
                    roleKeySet.add(role.getEntityId());
                }
            }

            logger.debug("Adding roles to user id " + usrKey);
            aiRoles.grantRolesToUser(usrKey.toString(), roleKeySet);
        }
        catch(OIMHelperException e)
        {
            throw new OIMHelperException(e);
        }
        return true;
    }

    /**
     * Grant the Role Associated with this user
     *
     * @param userId
     * @param roleName
     * @exception OIMHelperException
     */
    public boolean grantUserRole(String userId,String roleName) throws OIMHelperException
    {
        try
        {
            User u = getUser(userId);
            if (u == null)
            {
                logger.error("User Not Found " + userId);
                throw new OIMHelperException("User Not Found in OIM " + userId);
            }
            return grantUserRole(new Long(u.getId()),roleName);
        }
        catch(OIMHelperException ex)
        {
            throw ex;
        }
    }
    
    /**
     * 
     * @param userKey
     * @param roleName
     * @param actKey
     * @throws IllegalArgumentException 
     */
    public void grantUserAdminRole(Long userKey,String roleName,String actKey) throws OIMHelperException
    {
        
        AdminRoleMembership arm = new AdminRoleMembership();
        List<AdminRole> myroles = arsOp.getAdminRolesForUser(userKey.toString(), null);
        for(AdminRole ar : myroles)
        {
            logger.debug(ar.getRoleDisplayName() + ":" + ar);
        }
        
        List<AdminRole> aroles = arsOp.getAdminRoles(actKey);
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
            logger.error("Role not in Specified Scope");
            throw new OIMHelperException("Role not in Specified Scope");
        }
        
        arm.setAdminRole(arhit);
        arm.setScopeId(actKey);
        arm.setUserId(userKey.toString());
        logger.debug("Adding role");
        arsOp.addAdminRoleMembership(arm);
        
    }
    
    public void grantUserAdminRole(String userId,String roleName,String actKey) throws OIMHelperException
    {
        try
        {
            User u = getUser(userId);
            if (u == null)
            {
                logger.error("User Not Found " + userId);
                throw new OIMHelperException("User Not Found in OIM " + userId);
            }
            grantUserAdminRole(new Long(u.getId()),roleName,actKey);
        }
        catch(OIMHelperException ex)
        {
            throw ex;
        }
    }

    /**
     * Revoke all Roles Associated with this user
     *
     * @param userId
     * @param roleName
     * @exception OIMHelperException
     */
    public void removeAllRoles(String userId) throws OIMHelperException
    {
        OIMRoles aiRoles = new OIMRoles(roleOp);
        try
        {
            User u = getUser(userId);
            if (u == null)
            {
                logger.error("User Not Found " + userId);
                throw new OIMHelperException("User Not Found in OIM " + userId);
            }
            List<Role> allRoles = getAllUsersRoles(u.getId());
            if (allRoles == null)
                return;

            Set<String> roleKeySet = new HashSet<String>();
            for(Role role : allRoles)
            {
                roleKeySet.add(role.getEntityId());
            }
            
            if (!roleKeySet.isEmpty())
                aiRoles.revokeRolesFromUser(u.getId(), roleKeySet);
        }
        catch(OIMHelperException ex)
        {
            throw ex;
        }

    }

    /**
     * update attribute via login ID.
     * @param userLogin
     * @param attribute
     * @param value
     * @throws OIMHelperException 
     */
    public void setUserValue(String userLogin,String attribute,String value) throws OIMHelperException
    {
        ArrayList ids = new ArrayList();
        ids.add(userLogin);
        HashMap attributes = new HashMap();
        attributes.put(attribute, value);
        UserManagerResult res = null;
        
        User u = new User(userLogin,attributes);
        
        try {
            res = userOp.modify("User Login",userLogin,u);
        } catch (ValidationFailedException ex) {
            logger.error("ValidationFailedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + userLogin);
        } catch (UserModifyException ex) {
            logger.error("UserModifyException",ex);
            throw new OIMHelperException(ex);
        } catch (SearchKeyNotUniqueException ex) {
            logger.error("SearchKeyNotUniqueException",ex);
            throw new OIMHelperException(ex);
        }
        
    }

    /**
     * update attributes via login ID.
     * @param userLogin
     * @param attributes
     * @throws OIMHelperException 
     */
    public void setUserValue(String userLogin,HashMap attributes) throws OIMHelperException,NoSuchUserException,SearchKeyNotUniqueException
    {
        ArrayList ids = new ArrayList();
        ids.add(userLogin);
        
        UserManagerResult res = null;
        
        User u = new User(userLogin,attributes);
        
        try {
            res = userOp.modify("User Login",userLogin,u);
        } catch (ValidationFailedException ex) {
            logger.error("ValidationFailedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + userLogin);
        } catch (UserModifyException ex) {
            logger.error("UserModifyException",ex);
            throw new OIMHelperException(ex);
        } catch (SearchKeyNotUniqueException ex) {
            logger.error("SearchKeyNotUniqueException",ex);
            throw new OIMHelperException(ex);
        }
    }
    
    public void setUserValue(String searchName,String searchValue,HashMap attributes) throws OIMHelperException
    {
        UserManagerResult res = null;
        
        User u = getUser(searchName,searchValue);
        
        if (u == null)
        {
            logger.error("User Not Found:" + searchName + ":" + searchValue);
            throw new OIMHelperException("User Not Found:" + searchName + ":" + searchValue);
        }
        
        String entityId = u.getEntityId();
        User updu = new User(entityId,attributes);
        try {
            res = userOp.modify(searchName,searchValue,updu);
        } catch (ValidationFailedException ex) {
            logger.error("ValidationFailedException",ex);
            throw new OIMHelperException(ex);
        } catch (NoSuchUserException ex) {
            logger.info("NoSuchUserException:" + searchName + ":" + searchValue);
        } catch (UserModifyException ex) {
            logger.error("UserModifyException",ex);
            throw new OIMHelperException(ex);
        } catch (SearchKeyNotUniqueException ex) {
            logger.error("SearchKeyNotUniqueException",ex);
            throw new OIMHelperException(ex);
        }
        
    }
    
    /**
     * Evaluate User Policies
     *
     * @param userKey
     * @exception OIMHelperException
     */
    public void evaluatePolicies(long userKey) throws OIMHelperException
    {
        try
        {
            String strUserKey = new Long(userKey).toString();
            logger.debug("Evaluate policies for UserKey " + strUserKey);
            accessPolicySvc.evalutePoliciesForUser(strUserKey);
        }
        catch(NoSuchUserException e)
        {
            throw new OIMHelperException("NoSuchUserException",e);
        }
        catch(AccessPolicyEvaluationUnauthorizedException e)
        {
            throw new OIMHelperException("AccessPolicyEvaluationUnauthorizedException",e);
        }
        catch(AccessPolicyServiceException e)
        {
            throw new OIMHelperException("AccessPolicyServiceException",e);
        }
        catch(AccessPolicyEvaluationException e)
        {
            throw new OIMHelperException("AccessPolicyEvaluationException",e);
        }

    }

    /**
     * Evaluate User Policies
     *
     * @param userLogin
     * @exception OIMHelperException
     */
    public void evaluatePolicies(String userLogin) throws OIMHelperException
    {
        try
        {
            String userKey = getUserKey(userLogin);
            if (userKey == null)
                throw new OIMHelperException("NoSuchUser:" + userLogin);
            long longKey = new Long(userKey).longValue();
            logger.debug("Evaluate policies for User " + userLogin + " key " + longKey);
            evaluatePolicies(longKey);
        }
        catch(OIMHelperException e)
        {
            throw e;
        }
    }
    
    /**
     * Change password no notification. user will have to reset at next login
     * @param userID
     * @param password
     * @return
     * @throws OIMHelperException 
     */
    public boolean changePassword(String userID,String password) throws OIMHelperException
    {
        try
        {
            userOp.changePassword(userID, password.toCharArray(), true, false);
            return true;
        }
        catch(NoSuchUserException nsu)
        {
            logger.error("User Not Found:" + userID);
            return false;
        }
        catch(Exception e)
        {
            throw new OIMHelperException(e);
        }
    }
    
    /**
     * change the password without mail or forced reset at next login
     * @param userID
     * @param password
     * @param locale
     * @return
     * @throws OIMHelperException 
     */
    public boolean changePassword(String userID,String password,Locale locale) throws OIMHelperException
    {
        try
        {
            userOp.changePassword(userID, password.toCharArray(), true, locale,false,false);
            return true;
        }
        catch(NoSuchUserException nsu)
        {
            logger.error("User Not Found:" + userID);
            return false;
        }
        catch(Exception e)
        {
            throw new OIMHelperException(e);
        }
    }
    
    /**
     * Resets the password of an user to a randomly generated password. As part of the password reset, 
     * send a notification to the user regarding the new password
     *  NOTE: The email attribute of the user must be set for reset password to work
     * @param userID
     * @param password
     * @return
     * @throws OIMHelperException 
     */
    public boolean resetPassword(String userID) throws OIMHelperException
    {
        try
        {
            userOp.resetPassword(userID, true, true);
            return true;
        }
        catch(NoSuchUserException nsu)
        {
            logger.error("User Not Found:" + userID);
            return false;
        }
        catch(Exception e)
        {
            throw new OIMHelperException(e);
        }
    }
    
    public List<User> search(SearchCriteria criteria,Set retAttrs,HashMap configParams) throws OIMHelperException
    {
        try
        {
            return userOp.search(criteria, retAttrs, configParams);
        }
        catch(Exception e)
        {
            logger.error("APIError:" + e.toString(),e);
            throw new OIMHelperException(e);
        }
    }

}
