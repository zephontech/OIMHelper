/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api;

import com.aptecllc.oim.exceptions.OIMHelperException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import oracle.iam.identity.exception.NoSuchRoleException;
import oracle.iam.identity.exception.RoleAlreadyExistsException;
import oracle.iam.identity.exception.RoleCreateException;
import oracle.iam.identity.exception.RoleDeleteException;
import oracle.iam.identity.exception.RoleGrantException;
import oracle.iam.identity.exception.RoleGrantRevokeException;
import oracle.iam.identity.exception.RoleLookupException;
import oracle.iam.identity.exception.RoleModifyException;
import oracle.iam.identity.exception.RoleSearchException;
import oracle.iam.identity.exception.SearchKeyNotUniqueException;
import oracle.iam.identity.exception.ValidationFailedException;
import oracle.iam.identity.rolemgmt.api.RoleManager;
import oracle.iam.identity.rolemgmt.vo.Role;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.authz.exception.AccessDeniedException;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.identity.rolemgmt.api.RoleManagerConstants.RoleAttributeName;
import oracle.iam.identity.rolemgmt.vo.RoleManagerResult;
import oracle.iam.platform.Platform;
import oracle.iam.platform.authopss.vo.AdminRole;
import oracle.iam.platform.authopss.vo.AdminRoleMembership;
import oracle.iam.platformservice.api.AdminRoleService;
import org.apache.log4j.Logger;

/**
 *
 */
public class OIMRoles extends BaseHelper {

    private RoleManager roleOp;
    private AdminRoleService arsOp;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    

     /**
     * Constructor
     *
     * @param OrganizationManager
     */
    public OIMRoles(RoleManager roleOp) {
        this.roleOp = roleOp;
    }

    /**
     * Constructor
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMRoles(OIMClient client) throws OIMHelperException {
        roleOp = client.getService(RoleManager.class);
        arsOp = client.getService(AdminRoleService.class);
        if (roleOp == null)
        {
            logger.error("Failed to get Role OP");
            throw new OIMHelperException("Role Op Failed");
        }
        if (arsOp == null)
        {
            logger.error("Failed to get AdminRole OP");
            throw new OIMHelperException("AdminRole Op Failed");
        }
    }

    /**
     * Constructor in adapter mode via Platform
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMRoles() throws OIMHelperException {
        roleOp = Platform.getService(RoleManager.class);
        arsOp = Platform.getService(AdminRoleService.class);
        if (roleOp == null)
        {
            logger.error("Failed to get Role OP");
            throw new OIMHelperException("Role Op Failed");
        }
        if (arsOp == null)
        {
            logger.error("Failed to get AdminRole OP");
            throw new OIMHelperException("AdminRole Op Failed");
        }
    }

    

    /**
     * Return A List of Role types based in the searchArg
     *
     * @param searchArg - Can be * or a *Name* or a Name
     * @exception OIMHelperException
     */
    public List<Role> getAllRoles(String searchArg) throws OIMHelperException
    {
        List<Role> roleList = null;

        SearchCriteria criteria = new SearchCriteria(RoleAttributeName.NAME.getId(), searchArg, SearchCriteria.Operator.EQUAL);
        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(RoleAttributeName.CATEGORY_KEY.getId());
        retAttrs.add(RoleAttributeName.CREATE_DATE.getId());
        retAttrs.add(RoleAttributeName.DATA_LEVEL.getId());
        retAttrs.add(RoleAttributeName.DESCRIPTION.getId());
        retAttrs.add(RoleAttributeName.DISPLAY_NAME.getId());
        retAttrs.add(RoleAttributeName.EMAIL.getId());
        retAttrs.add(RoleAttributeName.KEY.getId());
        retAttrs.add(RoleAttributeName.LDAP_DN.getId());
        retAttrs.add(RoleAttributeName.LDAP_GUID.getId());
        retAttrs.add(RoleAttributeName.NAME.getId());
        retAttrs.add(RoleAttributeName.NAMESPACE.getId());
        retAttrs.add(RoleAttributeName.OWNER_KEY.getId());
        retAttrs.add(RoleAttributeName.UNIQUE_NAME.getId());
        retAttrs.add(RoleAttributeName.UPDATE_DATE.getId());
        retAttrs.add(RoleAttributeName.UPDATED_BY.getId());

        try {
            roleList = roleOp.search(criteria, retAttrs, null);
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        } catch (RoleSearchException ex) {
            logger.error("RoleSearchException",ex);
            throw new OIMHelperException(ex);
        }
        return roleList;
    }

    /**
     * Return A List of All Roles
     *
     * @exception OIMHelperException
     */
    public List<Role> getAllRoles() throws OIMHelperException
    {
        try
        {
            return getAllRoles("*");
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }

    /**
     * Return A List of All Roles Like
     *
     * @param like - Contructs a *like* search
     *
     * @exception OIMHelperException
     */
    public List<Role> getRolesLike(String like) throws OIMHelperException
    {
        String arg = "*" + like + "*";
        try
        {
            return getAllRoles(arg);
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }

    /**
     * Return A List of All Roles Starting with
     *
     * @param like - Contructs a like* search
     *
     * @exception OIMHelperException
     */
    public List<Role> getRolesStartingWith(String like) throws OIMHelperException
    {
        String arg = like + "*";
        try
        {
            return getAllRoles(arg);
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }

    /**
     * Return A List of All Roles Ending with
     *
     * @param like - Contructs a *like search
     *
     * @exception OIMHelperException
     */
    public List<Role> getRolesEndingWith(String like) throws OIMHelperException
    {
        String arg = "*" + like;
        try
        {
            return getAllRoles(arg);
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }

    /**
     * returns all the roles a user has. caller checks instanceof
     * Role or AdminRole
     * @param includeAdmin
     * @return
     * @throws OIMHelperException 
     */
    public List<Object> getAllRoles(boolean includeAdmin) throws OIMHelperException
    {
        List<Object> allRoles = new ArrayList<Object>();
        try
        {
            List<Role> roles = getAllRoles();
            allRoles.addAll(roles);
            
            if (includeAdmin)
            {
                List<AdminRole> adminRoles = arsOp.getAdminRoles();
                allRoles.addAll(adminRoles);
            }
            
            return allRoles;
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }
    
    
    /**
     * Return an Role with name
     *
     * @param name - The name of the Organization to find.
     *
     * @exception OIMHelperException
     */
    public List<Role> getRole(String name) throws OIMHelperException
    {
        try
        {
            return getAllRoles(name);
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }

    /**
     * Remove Roles from User
     *
     * @param usrKey - The name of the Organization to find.
     * @param roleKeySet - The name of the Organization to find.
     * 
     * @exception OIMHelperException
     */
    public void revokeRolesFromUser(String usrKey, Set roleKeySet) throws OIMHelperException
    {
        try
        {
            RoleManagerResult res = roleOp.revokeRoleGrants(usrKey, roleKeySet);
            Map resMap = res.getFailedResults();
            if (resMap == null)
                return;
            if (!resMap.isEmpty())
            {
                logger.error(res.getFailedResults());
                throw new OIMHelperException("Failed to revoke role(s) to user " + usrKey);
            }
        }
        catch(ValidationFailedException vfe)
        {
            logger.error("ValidationFailedException",vfe);
            throw new OIMHelperException(vfe);
        }
        catch (AccessDeniedException ex)
        {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        }
        catch(RoleGrantRevokeException ex)
        {
            logger.error("RoleGrantRevokeException",ex);
            throw new OIMHelperException(ex);
        }

    }

    /**
     * Grant Roles to User
     *
     * @param usrKey - The name of the Organization to find.
     * @param roleKeySet - The name of the Organization to find.
     *
     * @exception OIMHelperException
     */
    public void grantRolesToUser(String usrKey,Set roleKeySet) throws OIMHelperException
    {
        try
        {
            RoleManagerResult res = roleOp.grantRoles(usrKey,roleKeySet);
            Map resMap = res.getFailedResults();
            if (resMap == null)
                return;
            if (!resMap.isEmpty())
            {
                logger.error(res.getFailedResults());
                throw new OIMHelperException("Failed to grant role(s) to user " + usrKey);
            }
        }
        catch(ValidationFailedException vfe)
        {
            if (vfe.getMessage() != null && vfe.getMessage().contains("is already granted to user"))
                return;
            logger.error("ValidationFailedException",vfe);
            throw new OIMHelperException(vfe);
        }
        catch (AccessDeniedException ex)
        {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException(ex);
        }
        catch(RoleGrantException ex)
        {
            logger.error("RoleGrantException",ex);
            throw new OIMHelperException(ex);
        }

    }
    

    /**
     * Create Role
     *
     * @param Role
     *
     * @exception OIMHelperException
     */
    public void create(Role role) throws OIMHelperException
    {
        try
        {
            RoleManagerResult res = roleOp.create(role);
            Map resMap = res.getFailedResults();
            if (resMap == null)
                return;
            if (!resMap.isEmpty())
            {
                logger.error(res.getFailedResults());
                throw new OIMHelperException("Failed to create role " + role.getName());
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
        catch(RoleAlreadyExistsException ex)
        {
            logger.error("RoleAlreadyExistsException",ex);
            throw new OIMHelperException(ex);
        }
        catch(RoleCreateException ex)
        {
            logger.error("RoleCreateException",ex);
            throw new OIMHelperException(ex);
        }

    }

    /**
     * Update Role. do not use the role key in the role
     *
     * @param Role
     *
     * @exception OIMHelperException
     */
    public void update(Role role) throws OIMHelperException
    {
        try
        {
            RoleManagerResult res = roleOp.modify(role);
            Map resMap = res.getFailedResults();
            if (resMap == null)
                return;
            if (!resMap.isEmpty())
            {
                logger.error(res.getFailedResults());
                throw new OIMHelperException("Failed to update role " + role.getName());
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
        catch(RoleModifyException ex)
        {
            logger.error("RoleModifyException",ex);
            throw new OIMHelperException(ex);
        }
        catch(NoSuchRoleException ex)
        {
            logger.error("NoSuchRoleException",ex);
            throw new OIMHelperException(ex);
        }

    }

    /**
     * Update Role by Role Name
     *
     * @param Role
     *
     * @exception OIMHelperException
     */
    public void updateByName(Role role) throws OIMHelperException
    {
        try
        {
            RoleManagerResult res = roleOp.modify(RoleAttributeName.NAME.getId(),role.getName(),role);
            Map resMap = res.getFailedResults();
            if (resMap == null)
                return;
            if (!resMap.isEmpty())
            {
                logger.error(res.getFailedResults());
                throw new OIMHelperException("Failed to update role " + role.getName());
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
        catch(RoleModifyException ex)
        {
            logger.error("RoleModifyException",ex);
            throw new OIMHelperException(ex);
        }
        catch(NoSuchRoleException ex)
        {
            logger.error("NoSuchRoleException",ex);
            throw new OIMHelperException(ex);
        }
        catch(SearchKeyNotUniqueException ex)
        {
            logger.error("SearchKeyNotUniqueException",ex);
            throw new OIMHelperException(ex);
        }
        catch(RoleLookupException ex)
        {
            logger.error("RoleLookupException",ex);
            throw new OIMHelperException(ex);
        }

    }

    /**
     * Delete Role
     *
     * @param Role
     *
     * @exception OIMHelperException
     */
    public void delete(Role role) throws OIMHelperException
    {
        try
        {
            RoleManagerResult res = roleOp.delete(role.getEntityId());
            Map resMap = res.getFailedResults();
            if (resMap == null)
                return;
            if (!resMap.isEmpty())
            {
                logger.error(res.getFailedResults());
                throw new OIMHelperException("Failed to delete role " + role.getName());
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
        catch(RoleDeleteException ex)
        {
            logger.error("RoleDeleteException",ex);
            throw new OIMHelperException(ex);
        }
        catch(NoSuchRoleException ex)
        {
            logger.error("NoSuchRoleException",ex);
            throw new OIMHelperException(ex);
        }

    }


}
