/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api;

import com.aptecllc.oim.exceptions.OIMHelperException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import oracle.iam.identity.exception.OrganizationManagerException;
import oracle.iam.identity.orgmgmt.api.OrganizationManager;
import oracle.iam.identity.orgmgmt.api.OrganizationManagerConstants.AttributeName;
import oracle.iam.identity.orgmgmt.vo.Organization;

import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;
import oracle.iam.platform.authz.exception.AccessDeniedException;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import org.apache.log4j.Logger;

/**
 *
 */
public class OIMOrganizations extends BaseHelper {


    private OrganizationManager orgOp;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     *
     * @param OrganizationManager
     */
    public OIMOrganizations(OrganizationManager orgOp) {
        this.orgOp = orgOp;
    }

    /**
     * Constructor
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMOrganizations(OIMClient client) throws OIMHelperException {
        orgOp = client.getService(OrganizationManager.class);
        if (orgOp == null)
        {
            logger.error("Failed to get Org OP");
            throw new OIMHelperException("Org Op Failed");
        }
    }

    /**
     * Constructor adapter mode via Platform
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMOrganizations() throws OIMHelperException {
        orgOp = Platform.getService(OrganizationManager.class);
        if (orgOp == null)
        {
            logger.error("Failed to get Org OP");
            throw new OIMHelperException("Org Op Failed");
        }
    }

    /**
     * Return A List of Organization types based in the searchArg
     *
     * @param searchArg - Can be * or a *Name* or a Name
     * @exception OIMHelperException
     */
    public List<Organization> getAllOrganizations(String searchArg) throws OIMHelperException
    {
        List<Organization> orgList = null;

        SearchCriteria criteria = new SearchCriteria(AttributeName.ORG_NAME.getId(), searchArg, SearchCriteria.Operator.EQUAL);
        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(AttributeName.ID_FIELD.getId());
        retAttrs.add(AttributeName.ORG_PARENT_KEY.getId());
        retAttrs.add(AttributeName.ORG_NAME.getId());
        retAttrs.add(AttributeName.ORG_STATUS.getId());
        retAttrs.add(AttributeName.ORG_TYPE.getId());
        retAttrs.add(AttributeName.ORG_PARENT_NAME.getId());
        
        try {
            orgList = orgOp.search(criteria, retAttrs, null);
        } catch (OrganizationManagerException ex) {
            logger.error("OrganizationManagerException",ex);
            throw new OIMHelperException("OrganizationManagerException",ex);
        } catch (AccessDeniedException ex) {
            logger.error("AccessDeniedException",ex);
            throw new OIMHelperException("AccessDeniedException",ex);
        }

        return orgList;
    }

    /**
     * Return A List of All Organization
     *
     * @exception OIMHelperException
     */
    public List<Organization> getAllOrganizations() throws OIMHelperException
    {
        try
        {
            return getAllOrganizations("*");
        }
        catch(OIMHelperException e)
        {
            logger.error("OIMHelperException",e);
            throw e;
        }
    }

    /**
     * Return A List of All Organization Like
     *
     * @param like - Contructs a *like* search
     * 
     * @exception OIMHelperException
     */
    public List<Organization> getOrganizationsLike(String like) throws OIMHelperException
    {
        String arg = "*" + like + "*";
        try
        {
            return getAllOrganizations(arg);
        }
        catch(OIMHelperException e)
        {
            logger.error("OIMHelperException",e);
            throw e;
        }

    }

    /**
     * Return A List of All Organization Starting with
     *
     * @param like - Contructs a like* search
     *
     * @exception OIMHelperException
     */
    public List<Organization> getOrganizationsStartingWith(String like) throws OIMHelperException
    {
        String arg = like + "*";
        try
        {
            return getAllOrganizations(arg);
        }
        catch(OIMHelperException e)
        {
            logger.error("OIMHelperException",e);
            throw e;
        }

    }

    /**
     * Return A List of All Organization Ending with
     *
     * @param like - Contructs a *like search
     *
     * @exception OIMHelperException
     */
    public List<Organization> getOrganizationsEndingWith(String like) throws OIMHelperException
    {
        String arg = "*" + like;
        try
        {
            return getAllOrganizations(arg);
        }
        catch(OIMHelperException e)
        {
            logger.error("OIMHelperException",e);
            throw e;
        }

    }

    /**
     * Return an Organization with name
     *
     * @param name - The name of the Organization to find.
     *
     * @exception OIMHelperException
     */
    public List<Organization> getOrganization(String name) throws OIMHelperException
    {
        try
        {
            return getAllOrganizations(name);
        }
        catch(OIMHelperException e)
        {
            logger.error("OIMHelperException",e);
            throw e;
        }
    }
    
    /**
     * return a single org object
     * @param name or key
     * @param isOrgName
     * @return
     * @throws OIMHelperException 
     */
    public Organization getOrganization(String name,boolean isOrgName) throws OIMHelperException
    {
        Set<String> retAttrs = new HashSet<String>();
        retAttrs.add(AttributeName.ID_FIELD.getId());
        retAttrs.add(AttributeName.ORG_PARENT_KEY.getId());
        retAttrs.add(AttributeName.ORG_NAME.getId());
        retAttrs.add(AttributeName.ORG_STATUS.getId());
        retAttrs.add(AttributeName.ORG_TYPE.getId());
        retAttrs.add(AttributeName.ORG_PARENT_NAME.getId());
        try
        {
            Organization org = orgOp.getDetails(name, retAttrs, isOrgName);
            return org;
        }
        catch(Exception e)
        {
            logger.error("OIMHelperException",e);
            throw new OIMHelperException(e);
        }
    }


}
