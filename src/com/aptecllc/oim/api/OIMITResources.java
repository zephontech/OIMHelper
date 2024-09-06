package com.aptecllc.oim.api;

import com.aptecllc.oim.exceptions.OIMHelperException;
import Thor.API.*;
import Thor.API.Exceptions.DuplicateITResourceInstanceException;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcAttributeMissingException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcITResourceDefinitionNotFoundException;
import Thor.API.Exceptions.tcITResourceNotFoundException;
import Thor.API.Exceptions.tcInvalidAttributeException;
import Thor.API.Operations.*;
//import com.thortech.util.logging.*;
import java.util.*;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;
import org.apache.log4j.Logger;

public class OIMITResources extends BaseHelper {

    /**
     * The default logger instance for this instance.
     */
    //private Logger logger;
    private Logger logger = Logger.getLogger(this.getClass().getName());
    /**
     * The IT resource definition operations instance that backs this object.
     */
    private tcITResourceDefinitionOperationsIntf itResDefOp;
    /**
     * The IT resource instance operations instance that backs this object.
     */
    private tcITResourceInstanceOperationsIntf itResInstOp;

    /**
     * The standardUtilities class that back this object.
     */
    //private standardUtilities standardUtils;
    /**
     * The default constructor.
     *
     * @param ClassLogger The Logger used by the invoking class.
     * @param ItResDefOp The IT Resource definition operations utility from the invoking class.
     * @param ItResInstOp The IT Resource instance operations utility from the invoking class.
     * @param StandardUtils The standard utilities instance from the invoking class.
     */
    @Deprecated
    protected OIMITResources(Logger ClassLogger, tcITResourceDefinitionOperationsIntf ItResDefOp, tcITResourceInstanceOperationsIntf ItResInstOp) {
        //logger = ClassLogger;
        itResDefOp = ItResDefOp;
        itResInstOp = ItResInstOp;
        //standardUtils = StandardUtils;
    }

    /**
     * constructor.
     *
     * @param ItResDefOp The IT Resource definition operations utility from the invoking class.
     * @param ItResInstOp The IT Resource instance operations utility from the invoking class.
     */
    protected OIMITResources(tcITResourceDefinitionOperationsIntf ItResDefOp, tcITResourceInstanceOperationsIntf ItResInstOp) {
        itResDefOp = ItResDefOp;
        itResInstOp = ItResInstOp;
    }

    /**
     * Constructor. adapter mode via Platform
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMITResources() throws OIMHelperException {
        itResDefOp = Platform.getService(tcITResourceDefinitionOperationsIntf.class);
        if (itResDefOp == null) {
            logger.error("Failed to get itResDefOp");
            throw new OIMHelperException("itResDefOp Failed");
        }

        itResInstOp = Platform.getService(tcITResourceInstanceOperationsIntf.class);
        if (itResInstOp == null) {
            logger.error("Failed to get itResInstOp");
            throw new OIMHelperException("itResDefOp Failed");
        }


    }

    /**
     * Constructor
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMITResources(OIMClient client) throws OIMHelperException {
        itResDefOp = client.getService(tcITResourceDefinitionOperationsIntf.class);
        if (itResDefOp == null) {
            logger.error("Failed to get itResDefOp");
            throw new OIMHelperException("itResDefOp Failed");
        }

        itResInstOp = client.getService(tcITResourceInstanceOperationsIntf.class);
        if (itResInstOp == null) {
            logger.error("Failed to get itResInstOp");
            throw new OIMHelperException("itResDefOp Failed");
        }


    }

    /**
     * Determines the instance key for an IT Resource.
     *
     * @param Name The name of the IT resource to get the instance key for.
     * @return The instance key of the IT resource, or 0 if the IT resource is not found.
     * @exception Exception
     */
    public long getItResource(String Name) throws OIMHelperException {
        logger.debug("Entering OIMITResources.getItResource()");
        long result = 0L;
        Map searchFor = new HashMap();
        searchFor.put("IT Resources.Name", Name);
        try {
            tcResultSet results = itResInstOp.findITResourceInstances(searchFor);
            if (results.getRowCount() == 1) {
                results.goToRow(0);
                result = results.getLongValue("IT Resources.Key");
                logger.debug("Found " + Name + " = " + Long.toString(result));
            } else {
                logger.debug("Did not find an exact match for " + Name + "; beginning a slow search");
                searchFor.clear();
                results = itResInstOp.findITResourceInstances(searchFor);
                for (int i = 0; i < results.getRowCount(); i++) {
                    results.goToRow(i);
                    if (results.getStringValue("IT Resources.Name").equalsIgnoreCase(Name)) {
                        result = results.getLongValue("IT Resources.Key");
                        logger.debug("Found " + Name + " = " + Long.toString(result));
                        break;
                    }
                }
            }
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            throw new OIMHelperException(e);
        }

        logger.debug("Exiting OIMITResources.getItResource()");
        return result;
    }

    /**
     * Determines the name for an IT Resource.
     *
     * @param Key The key of the IT resource to get the name for.
     * @return The name of the IT resource, or an empty string if the IT resource is not found.
     * @exception Exception
     */
    public String getItResource(long Key) throws OIMHelperException {
        logger.debug("Entering OIMITResources.getItResource()");
        String result = "";
        Map searchFor = new HashMap();
        searchFor.put("IT Resources.Key", Long.toString(Key));
        try {
            tcResultSet results = itResInstOp.findITResourceInstances(searchFor);
            if (results.getRowCount() == 1) {
                results.goToRow(0);
                result = results.getStringValue("IT Resources.Name");
                logger.debug("Found " + result + " = " + Long.toString(Key));
            }
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            throw new OIMHelperException(e);
        }
        logger.debug("Exiting OIMITResources.getItResource()");
        return result;
    }

    /**
     * Return the Field Names of this IT Resource Definition Type
     *
     * @param Key The key of the IT resource to get the name for.
     * @exception Exception
     */
    public List getITResourceDefinitionFields(long ITResourceDefintionKey) throws OIMHelperException
    {
        List fieldList = null;
        logger.debug("Entering getITResourceDefinitionParameters()");
        try {
            tcResultSet results = itResDefOp.getITResourceDefinitionParameters(ITResourceDefintionKey);
            if (results.getRowCount() > 0 ) {
                
                String[] colNames = results.getColumnNames();
                for(int i=0;i<colNames.length;i++)
                {
                    logger.debug("ColName " + colNames[i]);
                }
                
                int rows = results.getRowCount();
                fieldList = new ArrayList();
                for(int i=0;i<rows;i++)
                {
                    results.goToRow(i);
                    String key = results.getStringValue("IT Resources Type Parameter.Key");
                    String value = results.getStringValue("IT Resources Type Parameter.Name");
                    String deflt = results.getStringValue("IT Resource Type Definition.IT Resource Type Parametr.Default Value");
                    logger.debug("key " + key);
                    logger.debug("value " + value);
                    logger.debug("deflt " + deflt);
                    fieldList.add(value);

                }
            }
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcITResourceDefinitionNotFoundException e) {
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            throw new OIMHelperException(e);
        }
        
        logger.debug("Exiting getITResourceDefinitionParameters()");
        return fieldList;
    }

    /**
     * Determines the definition key for an IT Resource type.
     *
     * @param Type The name of the IT resource type to get the definition key for.
     * @return The definition key of the IT resource, or 0 if the IT resource type is not found.
     * @exception Exception
     */
    public long getItResourceDefinition(String Type) throws OIMHelperException {
        logger.debug("Entering OIMITResources.getItResourceDefinition()");
        long result = 0L;
        Map searchFor = new HashMap();
        searchFor.put("IT Resources Type Definition.Server Type", Type);
        try {
            tcResultSet results = itResDefOp.getITResourceDefinition(searchFor);
            if (results.getRowCount() == 1) {
                results.goToRow(0);
                result = results.getLongValue("IT Resources Type Definition.Key");
                logger.debug("Found " + Type + " = " + Long.toString(result));
            } else {
                logger.debug("Did not find an exact match for " + Type + "; beginning a slow search");
                searchFor.clear();
                results = itResDefOp.getITResourceDefinition(searchFor);
                for (int i = 0; i < results.getRowCount(); i++) {
                    results.goToRow(i);
                    if (results.getStringValue("IT Resources Type Definition.Server Type").equalsIgnoreCase(Type)) {
                        result = results.getLongValue("IT Resources Type Definition.Key");
                        logger.debug("Found " + Type + " = " + Long.toString(result));
                        break;
                    }
                }
            }
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            throw new OIMHelperException(e);
        }
        logger.debug("Exiting OIMITResources.getItResourceDefinition()");
        return result;
    }

    /**
     * This obtains a Map of IT resource parameters and values
     *
     * @param InstanceKey The isntance key of the IT resource to retrieve parameters for.
     * @param HideEncrypted <code>true</code> will mask out any encrypted values from the result set,
     *                      and <code>false</code> will allow the encypted values to pass through.
     * @return A Map containg parameter names and values for that IT resource, or an
     *         empty Map if the IT resource is not found.
     * @exception Exception
     */
    public Map getITResData(long InstanceKey, boolean HideEncrypted) throws OIMHelperException {
        logger.debug("Entering OIMITResources.getITResData()");
        Map result = new HashMap();
        try {
            tcResultSet results = itResInstOp.getITResourceInstanceParameters(InstanceKey);
            for (int i = 0; i < results.getRowCount(); i++) {
                results.goToRow(i);
                String name = results.getStringValue("IT Resources Type Parameter.Name");
                String value = results.getStringValue("IT Resources Type Parameter Value.Value");
                boolean encrypted = results.getStringValue("IT Resources Type Parameter.Encrypted").equalsIgnoreCase("1");
                if (HideEncrypted && encrypted) {
                    result.put(name, "********");
                } else {
                    result.put(name, value);
                }
            }
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcITResourceNotFoundException e) {
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            throw new OIMHelperException(e);
        }
        logger.debug("Exiting OIMITResources.getITResData()");
        return result;
    }

    /**
     * Retrieves an array of Maps containing the data for all IT resources of a specified type.
     *
     * @param ITResourceDefintionKey The key of the type of the IT resource to retrieve all instances of.
     * @return An array of Maps containg data for all IT resources of the specified type.
     * @exception Exception
     */
    public Map[] getITResOfType(long ITResourceDefintionKey) throws OIMHelperException {
        logger.debug("Entering OIMITResources.getITResOfType()");
        List results = new ArrayList();
        try {
            tcResultSet itResResults = itResInstOp.findITResourceInstancesByDefinition(ITResourceDefintionKey);
            for (int i = 0; i < itResResults.getRowCount(); i++) {
                itResResults.goToRow(i);
                String name = itResResults.getStringValue("IT Resources.Name");
                long key = itResResults.getLongValue("IT Resources.Key");
                tcResultSet itResInstResults = itResInstOp.getITResourceInstanceParameters(key);
                Map result = new HashMap();
                result.put("Name", name);
                result.put("Key", Long.toString(key));
                for (int j = 0; j < itResInstResults.getRowCount(); j++) {
                    itResInstResults.goToRow(j);
                    String paramName = itResInstResults.getStringValue("IT Resources Type Parameter.Name");
                    String paramValue = itResInstResults.getStringValue("IT Resources Type Parameter Value.Value");
                    result.put(paramName, paramValue);
                }
                results.add(result);
            }
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcITResourceDefinitionNotFoundException e) {
            throw new OIMHelperException(e);
        } catch (tcITResourceNotFoundException e) {
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            throw new OIMHelperException(e);
        }
        logger.debug("Exiting OIMITResources.getITResOfType()");
        return (Map[]) results.toArray(new HashMap[results.size()]);
    }

    /**
     * Updates parameters on an existing IT resource instance.
     *
     * @param ITResourceKey The IT Resource instance key of the IT resource to update.
     * @param Data A map containg attribute-value pairs of the values to update.
     * @exception Exception
     */
    public void updateITResource(long ITResourceKey, Map Data) throws OIMHelperException {
        logger.debug("Entering OIMITResources.updateITResource()");
        try {
            itResInstOp.updateITResourceInstanceParameters(ITResourceKey, Data);
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcITResourceNotFoundException e) {
            throw new OIMHelperException(e);
        } catch (tcInvalidAttributeException e) {
            throw new OIMHelperException(e);
        }
        logger.debug("Exiting OIMITResources.updateITResource()");
    }

    /**
     * Creates an IT resource instance.
     *
     * @param Name The name of the IT resource to create.
     * @param DefinitionKey The key of the IT resource type definition to use.
     * @param Data A map containg attribute-value pairs of the values to set.
     * @exception Exception
     * @since Requires OIM 9.1
     */
    public void createITResource(String Name, long DefinitionKey, Map Data) throws OIMHelperException {
        logger.debug("Entering OIMITResources.createITResource()");
        Map parameters = new HashMap();
        parameters.put("IT Resource.Name", Name);
        parameters.put("IT Resources Type Definition.Key", Long.toString(DefinitionKey));
        try {
            long itResourceKey = itResInstOp.createITResourceInstance(parameters);
            updateITResource(itResourceKey, Data);
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcAttributeMissingException e) {
            throw new OIMHelperException(e);
        } catch (DuplicateITResourceInstanceException e) {
            throw new OIMHelperException(e);
        } catch (tcInvalidAttributeException e) {
            throw new OIMHelperException(e);
        }

        logger.debug("Exiting OIMITResources.createITResource()");
    }

    /**
     * Remove an existing IT resource instance.
     *
     * @param ITResourceKey The IT Resource instance key of the IT resource to remove.
     * @exception Exception
     * @since Requires OIM 9.1
     */
    public void removeITResource(long ITResourceKey) throws OIMHelperException {
        logger.debug("Entering OIMITResources.removeITResource()");
        try {
            itResInstOp.deleteITResourceInstance(ITResourceKey);
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcITResourceNotFoundException e) {
            throw new OIMHelperException(e);
        }
        logger.debug("Exiting OIMITResources.removeITResource()");
    }

    /**
     * Determines the name for an IT Resource.
     *
     * @param Key The key of the IT resource to get the name for.
     * @return The name of the IT resource, or an empty string if the IT resource is not found.
     * @exception Exception
     */
    public Map getAllITResources() throws OIMHelperException {
        logger.debug("Entering OIMITResources.getItResource()");
        String name = "";
        String key = "";
        long lKey = 0l;
        Map searchFor = new HashMap();
        Map resMap = new HashMap();

        searchFor.put("IT Resources Type Definition.Server Type", "%");
        try {
            tcResultSet results = itResInstOp.findITResourceInstances(searchFor);
            for(int i=0;i<results.getRowCount();i++) {
                results.goToRow(i);
                name = results.getStringValue("IT Resources.Name");
                lKey = results.getLongValue("IT Resources.Key");
                key = Long.toString(lKey);
                logger.debug("Found " + name + " = " + Long.toString(lKey));
                resMap.put(key, name);
            }
        } catch (tcAPIException e) {
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            throw new OIMHelperException(e);
        }
        logger.debug("Exiting OIMITResources.getItResource()");
        return resMap;
    }

    public void printResourceParms(long resourceKey,String resName) throws OIMHelperException
    {
        if (resourceKey == 0) {
            return;
        }
        try {
            tcResultSet resParms = itResInstOp.getITResourceInstanceParameters(resourceKey);
            if (resParms.getRowCount() == 0) {
                System.out.println("No Parms found for resource");
                return;
            }
            String[] parms = resParms.getColumnNames();
            //for (int v=0;v<parms.length;v++)
            //    System.out.println(parms[v]);
            for (int p = 0; p < resParms.getRowCount(); p++) {
                resParms.goToRow(p);
                //System.out.println("");
                String key = resParms.getStringValue("IT Resources Type Parameter.Name");
                //key = key.replaceAll(" ", "\\ ");
                String val = resParms.getStringValue("IT Resource.Parameter.Value");
                if (val == null)
                    val = "";
                val = val.replace("\\","\\\\");
                String line = resName + "." + key + "=" + val;
                line = line.replaceAll(" ", "\\\\ ");
                System.out.println(line);
                //key = resParms.getStringValue("IT Resource.Parameter.Key");
                //val = resParms.getStringValue("IT Resource.Parameter.Value");
            }
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        } catch (tcITResourceNotFoundException ex) {
            logger.error("tcITResourceNotFoundException",ex);
            throw new OIMHelperException("tcITResourceNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        }

    }

    
}
