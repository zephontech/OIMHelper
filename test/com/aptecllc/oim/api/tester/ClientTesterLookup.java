/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api.tester;

import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.api.OIMHelperClient;
import com.aptecllc.oim.api.OIMlookupUtilities;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 */
public class ClientTesterLookup extends OIMHelperClient {

    private String testTableName = "Lookup.MyTestLookup";

    private Logger logger = Logger.getLogger(this.getClass().getName());
    OIMlookupUtilities oimLookup;

    @Test
    public void mainTest() {

        //ClientTesterLookup ctl = new ClientTesterLookup();

        try
        {
            loadConfig(null);
            loginWithCustomEnv();
            oimLookup = new OIMlookupUtilities(getClient());
            //removeIt(testTableName);
            testLookup();
        }
        catch(OIMHelperException e)
        {
            logger.error("Failed to connect");
            return;
        }

        
    }

    public void testLookup()
    {
        try
        {
            oimLookup = new OIMlookupUtilities(getClient());

            oimLookup.createLookup(testTableName);
            oimLookup.clearLookup(testTableName);

            oimLookup.addLookupValue("1", "One", testTableName);
            oimLookup.addLookupValue("2", "Two", testTableName);
            oimLookup.addLookupValue("3", "Three", testTableName);
            oimLookup.addLookupValue("4", "Four", testTableName);
            oimLookup.addLookupValue("5", "Five", testTableName);
            oimLookup.addLookupValue("6", "Six", testTableName);
            oimLookup.addLookupValue("7", "Seven", testTableName);
            oimLookup.addLookupValue("8", "Eight", testTableName);
            oimLookup.addLookupValue("9", "Nine", testTableName);
            oimLookup.addLookupValue("0", "Zero", testTableName);

            Map<String,String> valMap = oimLookup.getLookupValues(testTableName);

            logger.debug("ValuMap " + valMap);

            Set<String> keySet = valMap.keySet();

            for(String key : keySet)
            {
                String val = oimLookup.getLookupValue(key, testTableName);
                logger.debug("Key/Val " + key + "/" + val);
            }
            
            Collection<String> valSet = valMap.values();

            for(String val : valSet)
            {
                String[] key = oimLookup.getLookupKeys(val, testTableName);
                for (int i=0;i<key.length;i++)
                    logger.debug("Val/Key " + val + "/" + key[i]);
            }

            oimLookup.updateLookupValue("1", "Onezees", testTableName);
            oimLookup.updateLookupValue("2", "Twozees", testTableName);
            oimLookup.updateLookupValue("3", "Threezees", testTableName);
            oimLookup.updateLookupValue("4", "Fourzees", testTableName);
            oimLookup.updateLookupValue("5", "Fivezees", testTableName);
            oimLookup.updateLookupValue("6", "Sixzees", testTableName);
            oimLookup.updateLookupValue("7", "Sevenzees", testTableName);
            oimLookup.updateLookupValue("8", "Eightzees", testTableName);
            oimLookup.updateLookupValue("9", "Ninezees", testTableName);
            oimLookup.updateLookupValue("0", "Zerozees", testTableName);

            valMap = oimLookup.getLookupValues(testTableName);
            logger.debug("ValuMap " + valMap);
            
        }
        catch(OIMHelperException e)
        {
            logger.error("Lookup Error ",e);
        }
    }

    public void removeIt(String name)
    {
        try
        {
            oimLookup.removeLookup(testTableName);
        }
        catch(Exception e)
        {
            
        }
    }
}
