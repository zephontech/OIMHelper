/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.zephon.oim.api.tester;

import java.util.Map;
import org.junit.Test;
import tech.zephon.oim.api.OIMHelperClient;
import tech.zephon.oim.api.OIMlookupUtilities;
import tech.zephon.oim.exceptions.OIMHelperException;
import com.thortech.util.logging.Logger;

/**
 *
 * @author fforester
 */
public class OIDLookups extends OIMHelperClient {
    
    private CustomTestLogger logger = CustomTestLogger.getLogger(this.getClass().getName());
    
    private String[] lookups = {"Lookup.LDAP.Configuration",
                                "Lookup.LDAP.Configuration.Trusted",
                                "Lookup.LDAP.UM.ReconAttrMap"};
    
    @Test
    public void mainTest() {

        //OIDLookups ctl = new OIDLookups();

        try
        {
            loadConfig(null);
            loginWithCustomEnv();
            clearLookups("Lookup.EYActiveDirectory.OrganizationalUnits");
        }
        catch(OIMHelperException e)
        {
            logger.error("Failed to connect");
            return;
        }

        
    }
    
    private void printLookups()
    {
        OIMlookupUtilities myLookup = null;
        
        try
        {
            myLookup = new OIMlookupUtilities(getClient());
            for(int i=0;i<lookups.length;i++)
            {
                logger.debug("Name:" + lookups[i]);
                Map<String,String> valMap = myLookup.getLookupValues(lookups[i]);
                logger.debug("ValuMap " + valMap);
            }

        }
        catch(OIMHelperException e)
        {
            logger.error("Lookup Error ",e);
        }
    }
    
    private void clearLookups(String name)
    {
        OIMlookupUtilities myLookup = null;
        try
        {
            myLookup = new OIMlookupUtilities(getClient());
            myLookup.clearLookup(name);

        }
        catch(OIMHelperException e)
        {
            logger.error("Lookup Error ",e);
        }
    }
    
}
