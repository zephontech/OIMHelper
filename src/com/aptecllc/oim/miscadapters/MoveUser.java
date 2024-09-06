/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.miscadapters;

import com.aptecllc.oim.api.OIMForms;
import com.aptecllc.oim.api.OIMITResources;
import com.aptecllc.oim.api.OIMlookupUtilities;
import com.aptecllc.oim.oimutils.StringUtils;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author fforester
 */
public class MoveUser {
    
    private String disabledLookupName;
    private String orgFieldName;
    
    private OIMForms oimForms;
    private OIMITResources oimITRes;
    private OIMlookupUtilities oimLookups;
    
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public MoveUser() {
    }
    
    public void dummy()
    {
        
    }
    
    

    public void setDisabledLookupName(String disabledLookupName) {
        this.disabledLookupName = disabledLookupName;
    }

    public void setOrgFieldName(String orgFieldName) {
        this.orgFieldName = orgFieldName;
    }
    
    
    public String init()
    {
        try
        {
            oimForms = new OIMForms();
            oimITRes = new OIMITResources();
            oimLookups = new OIMlookupUtilities();
        }
        catch(Exception e)
        {
            logger.error("OIM Init Failed");
            return("ERROR OIM Init Failed");
        }
        
        if (StringUtils.isBlank(disabledLookupName))
        {
            logger.error("Invalid Disabled Lookup Name");
            return("Invalid Disabled Lookup Name");
        }
        if (StringUtils.isBlank(orgFieldName))
        {
            logger.error("Invalid orgFieldName");
            return("Invalid orgFieldName");
        }
        return "SUCCESS";
    }
    
    public String moveUser(Long pik)
    {
        
        Map data = null;
        try
        {
            data = oimForms.getProcessFormValues(pik);
            String org = (String)data.get(orgFieldName);
            System.out.println("Org:" + org);
            if (org == null)
                org = "";
            int loc = org.indexOf("~");
            if (loc >= 0)
            {
                org = org.substring(0,loc);
            }
            boolean numeric = true;
            try
            {
                Integer.parseInt(org);
            }
            catch(NumberFormatException nfe)
            {
                numeric = false;
            }
            
            System.out.println("Lookup Org:" + org);
            
            String itresName = null;
            if (numeric)
                itresName = oimITRes.getItResource(new Long(org));
            else
                itresName = org;
            
            
            Map disabledOus = oimLookups.getLookupValues(disabledLookupName);
            String disabledou = (String)disabledOus.get(itresName);
            System.out.println("Itres:" + itresName);
            System.out.println("Ous:" + disabledOus);
            
            if (StringUtils.isBlank(disabledou))
            {
                return "FAILURE: No Disabled OU found for ITResource:" + itresName;
            }
            disabledou = org + "~" + disabledou;
            oimForms.setProcessFormValue(pik, orgFieldName, disabledou);
            
        }
        catch(Exception e)
        {
            logger.error("Error Moving User:" + e.getMessage(),e);
            return "Error Moving User:" + e.getMessage();
        }
        
        return "SUCCESS";
        
    }
    
}
