package com.aptecllc.oim.api;

import com.aptecllc.oim.exceptions.OIMHelperException;
import Thor.API.*;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcDuplicateLookupCodeException;
import Thor.API.Exceptions.tcInvalidAttributeException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Exceptions.tcInvalidValueException;
import Thor.API.Operations.*;
import com.thortech.xl.dataaccess.*;
import java.util.*;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;
import org.apache.log4j.Logger;

/**
 * This is provides common functions for lookups.
 * 
 */
public final class OIMlookupUtilities extends BaseHelper {

    /**
     * The default logger instance for this instance.
     */
    //private Logger logger;
    private Logger logger = Logger.getLogger(this.getClass().getName());
    /**
     * The lookup operations instance that backs this object.
     */
    private tcLookupOperationsIntf lookupOp;
    /**
     * The data provider used by this object.
     */
    private tcDataProvider database;

    /**
     * constructor. Used when in adapter mode
     *
     * @param ClassLogger The Logger used by the invoking class.
     * @param LookupOp The lookup operations utility from the invoking class.
     * @param Database The data provider from the invoking class.
     */
    @Deprecated
    protected OIMlookupUtilities(Logger ClassLogger, tcLookupOperationsIntf LookupOp, tcDataProvider Database) {
        //logger = ClassLogger;
        lookupOp = LookupOp;
        database = Database;
    }

    /**
     * constructor. Used when in adapter mode
     *
     * @param LookupOp The lookup operations utility from the invoking class.
     * @param Database The data provider from the invoking class.
     */
    protected OIMlookupUtilities(tcLookupOperationsIntf LookupOp, tcDataProvider Database) {
        //logger = ClassLogger;
        lookupOp = LookupOp;
        database = Database;
    }

    /**
     *  constructor.
     *
     * @param OIMClient object
     */
    public OIMlookupUtilities(OIMClient client) throws OIMHelperException {
        
        lookupOp = client.getService(Thor.API.Operations.tcLookupOperationsIntf.class);

        if (lookupOp == null) {
            logger.error("Failed to get lookupOps");
            throw new OIMHelperException("lookupOps Failed");
        }

    }

    /**
     *  constructor. adapter mode via Platform
     *
     * @param OIMClient object
     */
    public OIMlookupUtilities() throws OIMHelperException {

        lookupOp = Platform.getService(Thor.API.Operations.tcLookupOperationsIntf.class);

        if (lookupOp == null) {
            logger.error("Failed to get lookupOps");
            throw new OIMHelperException("lookupOps Failed");
        }

    }

    /**
     * Returns a Map containing the attribute names and values from a lookup.  An empty map is returned if the specified lookup
     * is not found.
     *
     * @param Lookup The name of the lookup to retrieve the values from.
     * @return A map containing the attribute name value pairs from the specified lookup.
     * @exception Exception
     */
    public Map<String,String> getLookupValues(String Lookup) throws OIMHelperException {
        logger.debug("Entering OIMlookupUtilities.getLookupValues()");
        Map result = new HashMap();
        try
        {
            tcResultSet lookupResults = lookupOp.getLookupValues(Lookup);
            for (int i = 0; i < lookupResults.getRowCount(); i++) {
                lookupResults.goToRow(i);
                String key = lookupResults.getStringValue("Lookup Definition.Lookup Code Information.Code Key");
                String value = lookupResults.getStringValue("Lookup Definition.Lookup Code Information.Decode");
                result.put(key, value);
            }
        }
        catch(tcAPIException e)
        {
            throw new OIMHelperException("tcAPIException",e);
        }
        catch(tcInvalidLookupException e)
        {
            throw new OIMHelperException("tcInvalidLookupException",e);
        }
        catch(tcColumnNotFoundException e)
        {
            throw new OIMHelperException("tcColumnNotFoundException",e);
        }
        logger.debug("Exiting OIMlookupUtilities.getLookupValues()");
        return result;
    }

    /**
     * Returns the decode value given a lookup and an encode value.   This method ignores case.
     *
     * @param Lookup The name of the lookup to retrieve the value from.
     * @param Key The name of encode value to retrieve.
     * @return The corresponding decode value, or an empty string if the specified key or lookup is not found.
     * @exception Exception
     */
    public String getLookupValue(String Key, String Lookup) throws OIMHelperException {
        logger.debug("Entering OIMlookupUtilities.getLookupValue()");
        String result = "";

        try
        {
            tcResultSet results = lookupOp.getLookupValues(Lookup);
            for (int i = 0; i < results.getRowCount(); i++) {
                results.goToRow(i);
                if (results.getStringValue("Lookup Definition.Lookup Code Information.Code Key").equalsIgnoreCase(Key)) {
                    logger.debug(Key + " was found in the lookup table");
                    result = results.getStringValue("Lookup Definition.Lookup Code Information.Decode");
                    break;
                }
            }
        }
        catch(tcAPIException e)
        {
            throw new OIMHelperException("tcAPIException",e);
        }
        catch(tcInvalidLookupException e)
        {
            throw new OIMHelperException("tcInvalidLookupException",e);
        }
        catch(tcColumnNotFoundException e)
        {
            throw new OIMHelperException("tcColumnNotFoundException",e);
        }


        logger.debug("Exiting OIMlookupUtilities.getLookupValue()");
        return result;
    }

    /**
     * Returns the decode values given a lookup and an decode value.   This method ignores case.
     *
     * @param Lookup The name of the lookup to retrieve the value from.
     * @param Value The name of decode value to retrieve.
     * @return The corresponding decode value, or an empty string if the specified key or lookup is not found.
     * @exception Exception
     */
    public String[] getLookupKeys(String Value, String Lookup) throws OIMHelperException {
        logger.debug("Entering OIMlookupUtilities.getLookupKeys()");
        List result = new ArrayList();

        try
        {
            tcResultSet results = lookupOp.getLookupValues(Lookup);
            for (int i = 0; i < results.getRowCount(); i++) {
                results.goToRow(i);
                if (results.getStringValue("Lookup Definition.Lookup Code Information.Decode").equalsIgnoreCase(Value)) {
                    logger.debug(Value + " was found in the lookup table");
                    result.add(results.getStringValue("Lookup Definition.Lookup Code Information.Code Key"));
                }
            }
        }
        catch(tcAPIException e)
        {
            throw new OIMHelperException("tcAPIException",e);
        }
        catch(tcInvalidLookupException e)
        {
            throw new OIMHelperException("tcInvalidLookupException",e);
        }
        catch(tcColumnNotFoundException e)
        {
            throw new OIMHelperException("tcColumnNotFoundException",e);
        }
        logger.debug("Exiting OIMlookupUtilities.getLookupKeys()");
        return (String[]) result.toArray(new String[0]);
    }

    /**
     * Adds a new value to a lookup.
     *
     * @param Lookup The name of the lookup to add the value to.
     * @param Key The name of the key to add to the lookup.
     * @param Value the value to add with that key.
     * @exception Exception
     */
    public void addLookupValue(String Key, String Value, String Lookup) throws OIMHelperException {
        logger.debug("Entering OIMlookupUtilities.addLookupValue()");
        try
        {
            lookupOp.addLookupValue(Lookup, Key, Value, Locale.getDefault().getLanguage(), Locale.getDefault().getCountry());
        }
        catch(tcAPIException e)
        {
            throw new OIMHelperException("tcAPIException",e);
        }
        catch(tcInvalidLookupException e)
        {
            throw new OIMHelperException("tcInvalidLookupException",e);
        }
        catch(tcInvalidValueException e)
        {
            throw new OIMHelperException("tcInvalidValueException",e);
        }

        logger.debug("Exiting OIMlookupUtilities.addLookupValue()");
    }

    /**
     * Updates a value within a lookup.
     *
     * @param Key The name of the key to update in the lookup.
     * @param Value The new value to set for that key.
     * @param Lookup The name of the lookup to update the value in.
     * @param ExactMatch <code>true</code> if an exact case match should be used, or
     *                   <code>false</code> if a case-insensitive match should be used.
     * @exception Exception
     */
    public void updateLookupValue(String Key, String Value, String Lookup, boolean ExactMatch) throws OIMHelperException {
        logger.debug("Entering OIMlookupUtilities.updateLookupValue()");

        try
        {
            removeLookupValue(Key, Lookup, ExactMatch);
            addLookupValue(Key, Value, Lookup);
        }
        catch(OIMHelperException e)
        {
            throw e;
        }
        

        logger.debug("Exiting OIMlookupUtilities.updateLookupValue()");
    }

    public void updateLookupValue(String key, String value, String lookup) throws OIMHelperException {
        logger.debug("Entering OIMlookupUtilities.updateLookupValue()");
        Map<String,String> attrMap = new HashMap<String,String>();

        attrMap.put("Lookup Definition.Lookup Code Information.Decode", value);

        try
        {
            lookupOp.updateLookupValue(lookup, key, attrMap);
        }
        catch(tcAPIException e)
        {
            throw new OIMHelperException("tcAPIException",e);
        }
        catch(tcInvalidLookupException e)
        {
            throw new OIMHelperException("tcInvalidLookupException",e);
        }
        catch(tcInvalidValueException e)
        {
            throw new OIMHelperException("tcInvalidValueException",e);
        }
        catch(tcInvalidAttributeException e)
        {
            throw new OIMHelperException("tcInvalidAttributeException",e);
        }


        logger.debug("Exiting OIMlookupUtilities.updateLookupValue()");
    }

    /**
     * Removes a value from a lookup.
     *
     * @param Lookup The name of the lookup to remove the value from.
     * @param Key The name of the key to remove from the lookup.
     * @param ExactMatch <code>true</code> if an exact case match should be used, or
     *                   <code>false</code> if a case-insensitive match should be used.
     * @exception Exception
     */
    public void removeLookupValue(String Key, String Lookup, boolean ExactMatch) throws OIMHelperException {
        logger.debug("Entering OIMlookupUtilities.removeLookupValue()");
        Map searchFor = new HashMap();
        searchFor.put("Lookup Definition.Lookup Code Information.Code Key", Key);
        try
        {
            tcResultSet results = lookupOp.getLookupValues(Lookup, searchFor);
            for (int i = 0; i < results.getRowCount(); i++) {
                results.goToRow(i);
                String key = results.getStringValue("Lookup Definition.Lookup Code Information.Code Key");
                if (!ExactMatch && key.equalsIgnoreCase(Key)) {
                    lookupOp.removeLookupValue(Lookup, key);
                } else if (ExactMatch && key.equals(Key)) {
                    lookupOp.removeLookupValue(Lookup, key);
                }
            }
        }
        catch(tcAPIException e)
        {
            throw new OIMHelperException("tcAPIException",e);
        }
        catch(tcInvalidLookupException e)
        {
            throw new OIMHelperException("tcInvalidLookupException",e);
        }
        catch(tcColumnNotFoundException e)
        {
            throw new OIMHelperException("tcColumnNotFoundException",e);
        }
        catch(tcInvalidValueException e)
        {
            throw new OIMHelperException("tcInvalidValueException",e);
        }

        logger.debug("Exiting OIMlookupUtilities.removeLookupValue()");
    }

    /**
     * Creates a new lookup.
     *
     * @param Lookup The name of the lookup to create.
     * @exception Exception
     */
    public void createLookup(String Lookup) throws OIMHelperException {
        
        logger.debug("Entering OIMlookupUtilities.createLookup()");
        try
        {
            lookupOp.addLookupCode(Lookup);
        }
        catch(tcAPIException e)
        {
            throw new OIMHelperException("tcAPIException",e);
        }
        catch(tcDuplicateLookupCodeException e)
        {
            logger.error("Table Already Exists");
        }
        logger.debug("Exiting OIMlookupUtilities.createLookup()");
    }

    /**
     * Removes all existing values from a lookup.
     *
     * @param Lookup The name of the lookup to remove all existing values from.
     * @exception Exception
     */
    public void clearLookup(String Lookup) throws OIMHelperException {
        logger.debug("Entering OIMlookupUtilities.clearLookup()");
        try
        {
            tcResultSet results = lookupOp.getLookupValues(Lookup);
            for (int i = 0; i < results.getRowCount(); i++) {
                results.goToRow(i);
                String key = results.getStringValue("Lookup Definition.Lookup Code Information.Code Key");
                lookupOp.removeLookupValue(Lookup, key);
            }
        }
        catch(tcAPIException e)
        {
            throw new OIMHelperException("tcAPIException",e);
        }
        catch(tcInvalidLookupException e)
        {
            throw new OIMHelperException("tcInvalidLookupException",e);
        }
        catch(tcColumnNotFoundException e)
        {
            throw new OIMHelperException("tcColumnNotFoundException",e);
        }
        catch(tcInvalidValueException e)
        {
            throw new OIMHelperException("tcInvalidValueException",e);
        }
        logger.debug("Exiting OIMlookupUtilities.clearLookup()");
    }

    /**
     * Removes an existing lookup, including any data within.
     *
     * @param Lookup The name of the lookup to remove.
     * @exception Exception
     */
    public void removeLookup(String Lookup) throws OIMHelperException {
        logger.debug("Entering OIMlookupUtilities.removeLookup()");
        try
        {
            //clearLookup(Lookup);
            lookupOp.removeLookupCode(Lookup);
        }
        catch(tcAPIException e)
        {
            throw new OIMHelperException("tcAPIException",e);
        }
        catch(tcInvalidLookupException e)
        {
            
        }

        logger.debug("Exiting OIMlookupUtilities.removeLookup()");
    }
}
