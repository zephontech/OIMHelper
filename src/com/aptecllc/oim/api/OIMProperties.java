/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.api;

import com.aptecllc.oim.exceptions.OIMHelperException;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcITResourceNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcITResourceInstanceOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.tcResultSet;
import com.aptecllc.oim.oimutils.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.thortech.util.logging.Logger;
import java.util.Set;
import java.util.TreeMap;
import oracle.iam.conf.api.SystemConfigurationService;
import oracle.iam.conf.exception.SystemConfigurationServiceException;
import oracle.iam.conf.vo.SystemProperty;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;
import oracle.iam.scheduler.api.SchedulerService;
import oracle.iam.scheduler.exception.SchedulerException;
import oracle.iam.scheduler.vo.JobDetails;
import oracle.iam.scheduler.vo.JobParameter;
import org.apache.log4j.Logger;

/**
 *
 */
public class OIMProperties extends BaseHelper {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private tcITResourceInstanceOperationsIntf resInstOps;
    private tcLookupOperationsIntf lookupOps;
    private SchedulerService scheduleOps;
    private SystemConfigurationService confService;


    /**
     * Constructor used when in adapter mode
     *
     * @param tcITResourceInstanceOperationsIntf
     * @param tcLookupOperationsIntf
     */
    public OIMProperties(tcITResourceInstanceOperationsIntf resInstOps, tcLookupOperationsIntf lookupOps) {
        this.lookupOps = lookupOps;
        this.resInstOps = resInstOps;
    }

    /**
     * Constructor used when in adapter mode
     *
     * @param tcITResourceInstanceOperationsIntf
     * @param tcLookupOperationsIntf
     * @param scheduleOps
     */
    public OIMProperties(tcITResourceInstanceOperationsIntf resInstOps, tcLookupOperationsIntf lookupOps,SchedulerService scheduleOps) {
        this.lookupOps = lookupOps;
        this.resInstOps = resInstOps;
        this.scheduleOps = scheduleOps;
    }
    
    /**
     * Constructor used when in adapter mode
     *
     * @param scheduleOps
     */
    public OIMProperties(SchedulerService scheduleOps) {
        this.scheduleOps = scheduleOps;
    }

    /**
     * Constructor in adapter mode via Platform
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMProperties() throws OIMHelperException {
        resInstOps = Platform.getService(tcITResourceInstanceOperationsIntf.class);
        lookupOps = Platform.getService(Thor.API.Operations.tcLookupOperationsIntf.class);
        scheduleOps = Platform.getService(SchedulerService.class);
        confService = Platform.getService(SystemConfigurationService.class);

        if (resInstOps == null) {
            logger.error("Failed to get itResInstOp");
            throw new OIMHelperException("itResDefOp Failed");
        }
        if (lookupOps == null) {
            logger.error("Failed to get lookupOps");
            throw new OIMHelperException("lookupOps Failed");
        }
        if (scheduleOps == null) {
            logger.error("Failed to get scheduleOps");
            throw new OIMHelperException("scheduleOps Failed");
        }
        if (confService == null) {
            logger.error("Failed to get confService");
            throw new OIMHelperException("confService Failed");
        }

    }

    /**
     * Constructor
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMProperties(OIMClient client) throws OIMHelperException {
        resInstOps = client.getService(tcITResourceInstanceOperationsIntf.class);
        lookupOps = client.getService(Thor.API.Operations.tcLookupOperationsIntf.class);
        scheduleOps = client.getService(SchedulerService.class);
        confService = client.getService(SystemConfigurationService.class);

        if (resInstOps == null) {
            logger.error("Failed to get itResInstOp");
            throw new OIMHelperException("itResDefOp Failed");
        }
        if (lookupOps == null) {
            logger.error("Failed to get lookupOps");
            throw new OIMHelperException("lookupOps Failed");
        }
        if (scheduleOps == null) {
            logger.error("Failed to get scheduleOps");
            throw new OIMHelperException("scheduleOps Failed");
        }
        if (confService == null) {
            logger.error("Failed to get confService");
            throw new OIMHelperException("confService Failed");
        }

    }

     /**
     * Returns a map of the named ITResource Properties
     *
     * @param itResourceName
     * @exception OIMHelperException
     */
    public Map<String, String>  getITResourceProperties(String itResourceName) throws OIMHelperException {

        HashMap<String, String> resourceMap = new HashMap<String, String>();
        try {
            HashMap<String, String> srchMap = new HashMap<String, String>();
            srchMap.put("IT Resources.Name", itResourceName);
            tcResultSet rst = resInstOps.findITResourceInstances(srchMap);

            if (rst.getRowCount() <= 0)
            {
                logger.error("ITResource " + itResourceName + " not found");
                throw new OIMHelperException("ITResource " + itResourceName + " Not Found");
            }

            for (int i = 0; i < rst.getTotalRowCount(); i++) {
                rst.goToRow(i);
                long key = rst.getLongValue("IT Resources.Key");
                tcResultSet paramSet = resInstOps.getITResourceInstanceParameters(key);
                int amRow = paramSet.getRowCount();
                if (amRow == 0) {
                    logger.error("ITResource " + itResourceName + " not found");
                    throw new OIMHelperException("ITResource " + itResourceName + " Not Found");
                }

                for (int j = 0; j < paramSet.getTotalRowCount(); j++) {
                    paramSet.goToRow(j);
                    //Setting login credentials from ITResource
                    String parmkey = paramSet.getStringValue("IT Resources Type Parameter.Name");
                    String parmval = paramSet.getStringValue("IT Resource.Parameter.Value");
                    resourceMap.put(parmkey, parmval);
                    
                }
                //logger.debug("ITResource Parms: " + srchMap);
            }
            logger.info("Finished Retrieving ITResource parameters");
        } catch (tcAPIException e) {
            logger.error("tcAPIException while retrieving ITResource parameters: " + e.getMessage(), e);
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            logger.error("tcColumnNotFoundException while retrieving ITResource parameters: " + e.getMessage(), e);
            throw new OIMHelperException(e);
        } catch (tcITResourceNotFoundException e) {
            logger.error("tcITResourceNotFoundException while retrieving ITResource parameters: " + e.getMessage(), e);
            throw new OIMHelperException(e);
        }
        return resourceMap;
    }

    /**
     * Returns a map of the named Lookup table.
     *
     * @param lookupName
     * @exception OIMHelperException
     */
    public Map<String, String> getLookupProperties(String lookupName) throws OIMHelperException {

        HashMap<String, String> props = new HashMap<String, String>();
        try {
            tcResultSet resultSet = lookupOps.getLookupValues(lookupName);
            int amRow = resultSet.getRowCount();
            if (amRow == 0) {
                logger.error("Lookup Code " + lookupName + " not found");
                throw new OIMHelperException("Lookup Not Found");
            }

            for (int i = 0; i < resultSet.getRowCount(); i++) {
                resultSet.goToRow(i);
                String codeKeyfromResultSet = resultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key");
                String decodeValue = resultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode");
                props.put(codeKeyfromResultSet, decodeValue);

            }
        } catch (tcAPIException e) {
            logger.error("tcAPIException ", e);
            throw new OIMHelperException(e);
        } catch (tcInvalidLookupException e) {
            logger.error("tcInvalidLookupException ", e);
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            logger.error("tcColumnNotFoundException ", e);
            throw new OIMHelperException(e);
        }

        return props;

    }
    
    /**
     * allow for a lookup with multi value keys
     * @param lookupName
     * @return
     * @throws OIMHelperException 
     */
    public TreeMap<String, List<String>> getLookupMultiMapProperties(String lookupName) throws OIMHelperException {

        TreeMap<String, List<String>> props = new TreeMap<String, List<String>>();
        try {
            tcResultSet resultSet = lookupOps.getLookupValues(lookupName);
            int amRow = resultSet.getRowCount();
            if (amRow == 0) {
                logger.error("Lookup Code " + lookupName + " not found");
                throw new OIMHelperException("Lookup Not Found");
            }

            for (int i = 0; i < resultSet.getRowCount(); i++) {
                resultSet.goToRow(i);
                String codeKeyfromResultSet = resultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key");
                String decodeValue = resultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode");
                List l = props.get(codeKeyfromResultSet);
                if (l == null)
                    l = new ArrayList<String>();
                l.add(decodeValue);
                props.put(codeKeyfromResultSet, l);

            }
        } catch (tcAPIException e) {
            logger.error("tcAPIException ", e);
            throw new OIMHelperException(e);
        } catch (tcInvalidLookupException e) {
            logger.error("tcInvalidLookupException ", e);
            throw new OIMHelperException(e);
        } catch (tcColumnNotFoundException e) {
            logger.error("tcColumnNotFoundException ", e);
            throw new OIMHelperException(e);
        }

        return props;

    }

    /**
     * Returns a map of the named Scheduled Task Attributes.
     *
     * @param taskName
     * @exception OIMHelperException
     */
    public Map<String, String> getTaskProperties(String taskName) throws OIMHelperException {

        Map jobProps = new HashMap();

        try {
            //logger.debug("Get Job " + taskName);
            JobDetails jd = scheduleOps.getJobDetail(taskName);

            if (jd == null)
            {
                logger.error("Job Not Found for " + taskName);
                throw new OIMHelperException("Job Not Found for " + taskName);
            }

            Map<String,JobParameter> parms = jd.getAttributes();

            if (parms == null)
            {
                logger.debug("No Parms for " + taskName);
                return jobProps;
            }
            Set<String> keys = parms.keySet();

            for(String key : keys)
            {
                JobParameter jp = parms.get(key);
                //logger.debug("DataType " + jp.getDataType());
                //logger.debug("Name " + jp.getName());
                //logger.debug("Val " + jp.getValue());
                jobProps.put(jp.getName(),jp.getValue().toString());

            }
        } catch (SchedulerException ex) {
            logger.error("SchedulerException",ex);
            throw new OIMHelperException("SchedulerException",ex);
        }
        return jobProps;
        
        
    }

    /**
     * Returns The String Value of the System Property
     *
     * @param propName
     * @exception OIMHelperException
     */
    public String getSystemProperty(String propName) throws OIMHelperException {
        SystemProperty sysProp = null;
        String ptyValue = null;
        
        try {
            sysProp = confService.getSystemProperty(propName);
        } catch (SystemConfigurationServiceException SCSE) {
            logger.error("SystemConfigurationServiceException",SCSE);
            throw new OIMHelperException("SystemConfigurationServiceException",SCSE);
        }

        if (sysProp != null) {
            ptyValue = sysProp.getPtyValue();
        }

        if ((ptyValue != null) && (ptyValue.length() > 0)) {
            ptyValue = "@".concat(ptyValue);
        } else {
            ptyValue = "";
        }
        
        return ptyValue;
    }

    /**
     * Returns a String Array of all Scheduled Job Names
     *
     * @exception OIMHelperException
     */
    public String[] getAllJobs() throws OIMHelperException
    {
        try {
            return scheduleOps.getAllJobs();
        } catch (SchedulerException ex) {
            logger.error("SchedulerException",ex);
            throw new OIMHelperException("SchedulerException",ex);
        }
    }

    /**
     * Gets a string value of an attribute.
     *
     * @param attrMap of the Attributes
     * @param Attribute The name of the task attribute to retrieve.
     * @param Default The default value to use if the attribute is not present.
     * @return The value of that attribute.
     */
    public final String getDefaultAttribute(Map<String, String> attrMap, String Attribute, String Default) {
        String value = attrMap.get(Attribute);
        String result = StringUtils.isBlank(value) ? Default : value;
        return result;
    }

    /**
     * Gets a string value of an attribute, and throws an exception if that attribute is not present.
     *
     * @param attrMap of the Attributes
     * @param Attribute The name of the task attribute to retrieve.
     * @return The value of that attribute.
     * @exception missingAttributeException
     */
    public final String getCriticalAttribute(Map<String, String> attrMap, String Attribute) throws OIMHelperException {
        String result = attrMap.get(Attribute);
        if (result == null || result.length() == 0) {
            throw new OIMHelperException(Attribute);
        }
        return result;
    }

    /**
     * Gets a boolean value based on a task attribute.
     *
     * @param attrMap of the Attributes
     * @param Attribute The name of the task attribute to retrieve.
     * @param Default A default value to return if the attribute is not present.
     * @return The value of that attribute.
     */
    public final boolean getBooleanAttribute(Map<String, String> attrMap, String Attribute, boolean Default) {
        String value = attrMap.get(Attribute);
        boolean result = StringUtils.isBlank(value) ? Default : value.equalsIgnoreCase("true");
        return result;
    }


}
