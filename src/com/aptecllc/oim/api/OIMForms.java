package com.aptecllc.oim.api;

import com.aptecllc.oim.exceptions.OIMHelperException;
import Thor.API.*;
import Thor.API.Exceptions.*;
import Thor.API.Operations.*;
//import com.thortech.util.logging.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;
import org.apache.log4j.Logger;

public class OIMForms extends BaseHelper {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private tcFormDefinitionOperationsIntf formDefOp;
    private tcFormInstanceOperationsIntf formInstOp;

    /**
     * Constructor
     *
     * @param tcFormDefinitionOperationsIntf
     * @param tcFormInstanceOperationsIntf
     */
    public OIMForms(tcFormDefinitionOperationsIntf FormDefOp, tcFormInstanceOperationsIntf FormInstOp) {
        formDefOp = FormDefOp;
        formInstOp = FormInstOp;
    }

    /**
     * Constructor
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMForms(OIMClient client) throws OIMHelperException {
        formDefOp = client.getService(Thor.API.Operations.tcFormDefinitionOperationsIntf.class);
        formInstOp = client.getService(Thor.API.Operations.tcFormInstanceOperationsIntf.class);
        if (formDefOp == null) {
            logger.error("Failed to get formDefOp");
            throw new OIMHelperException("formDefOp Failed");
        }
        if (formInstOp == null) {
            logger.error("Failed to get formInstOp");
            throw new OIMHelperException("formInstOp Failed");
        }
    }

    /**
     * Constructor in adapter mode via Platform
     *
     * @param OIMClient object
     * @exception OIMHelperException
     */
    public OIMForms() throws OIMHelperException {
        formDefOp = Platform.getService(Thor.API.Operations.tcFormDefinitionOperationsIntf.class);
        formInstOp = Platform.getService(Thor.API.Operations.tcFormInstanceOperationsIntf.class);
        if (formDefOp == null) {
            logger.error("Failed to get formDefOp");
            throw new OIMHelperException("formDefOp Failed");
        }
        if (formInstOp == null) {
            logger.error("Failed to get formInstOp");
            throw new OIMHelperException("formInstOp Failed");
        }
    }


    /**
     * Return the Form Version
     *
     * @param ProcessInstanceKey
     * @exception OIMHelperException
     */
    public int getFormVersion(long ProcessInstanceKey)  throws OIMHelperException {
        logger.debug("Entering OIMForms.getFormVersion()");
        int result = 0;
        try {
            try {
                result = formInstOp.getProcessFormVersion(ProcessInstanceKey);
            } catch (tcNotAtomicProcessException ex) {
                logger.error("tcNotAtomicProcessException",ex);
                throw new OIMHelperException("tcNotAtomicProcessException",ex);
            } catch (tcFormNotFoundException ex) {
                logger.error("tcFormNotFoundException",ex);
                throw new OIMHelperException("tcFormNotFoundException",ex);
            } catch (tcAPIException ex) {
                logger.error("tcAPIException",ex);
                throw new OIMHelperException("tcAPIException",ex);
            }
        } catch (tcVersionNotFoundException e) {
            try {
                long formDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
                result = formInstOp.getActiveVersion(formDefKey);
            } catch (tcVersionNotDefinedException ex) {
                logger.error("tcVersionNotDefinedException",ex);
                throw new OIMHelperException("tcVersionNotDefinedException",ex);
            } catch (tcAPIException ex) {
                logger.error("tcAPIException",ex);
                throw new OIMHelperException("tcAPIException",ex);
            } catch (tcProcessNotFoundException ex) {
                logger.error("tcProcessNotFoundException",ex);
                throw new OIMHelperException("tcProcessNotFoundException",ex);
            } catch (tcFormNotFoundException ex) {
                logger.error("tcFormNotFoundException",ex);
                throw new OIMHelperException("tcFormNotFoundException",ex);
            } 
        }
        logger.debug("Exiting OIMForms.getFormVersion():" + result);
        return result;

    }


    /**
     * Checks a Field to be the given Type
     *
     * @param Field - The Form Field to Check
     * @param Type - The Type to Compare
     * @param FormDefKey - The key of the Form Definition that contains
     *                     the Field
     * @exception OIMHelperException
     */
    public boolean isFieldType(String Field, String Type, int FormVersion, long FormDefKey) throws OIMHelperException {
        boolean result = false;
        try {
            logger.debug("Entering OIMForms.isFieldType()");
            
            tcResultSet fields = formDefOp.getFormFields(FormDefKey, FormVersion);
            for (int i = 0; i < fields.getRowCount(); i++) {
                fields.goToRow(i);
                String fieldName = fields.getStringValue("Structure Utility.Additional Columns.Name");
                if (fieldName.equalsIgnoreCase(Field)) {
                    String fieldType = fields.getStringValue("Structure Utility.Additional Columns.Field Type");
                    result = fieldType.equalsIgnoreCase(Type);
                    break;
                }
            }
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        }
        logger.debug("Exiting OIMForms.isFieldType()");
        return result;
    }

    /**
     * Return A Map of the Field names and values of a process form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @exception OIMHelperException
     */
    public Map getProcessFormValues(long ProcessInstanceKey) throws Exception {
        return getProcessFormValues(ProcessInstanceKey, false, "yyyy-MM-dd");
    }

    /**
     * Return A Map of the Field names and values of a process form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param HideEncrypted - Change encrypted values to all asterisks
     * @param DateFormat - Date fields will be converted to this format
     * @exception OIMHelperException
     */
    public Map getProcessFormValues(long ProcessInstanceKey, boolean HideEncrypted, String DateFormat) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.getProcessFormValues()");
            Map result = new HashMap();
            SimpleDateFormat dateFormat = DateFormat == null ? null : new SimpleDateFormat(DateFormat);
            Map labels = getProcessFormDisplayNames(ProcessInstanceKey);
            tcResultSet results = formInstOp.getProcessFormData(ProcessInstanceKey);
            if (results.getRowCount() == 1) {
                results.goToRow(0);
                String[] columns = results.getColumnNames();
                for (int i = 0; i < columns.length; i++) {
                    if (!labels.containsKey(columns[i])) {
                        continue;
                    }
                    if (HideEncrypted && isEncryptedField(ProcessInstanceKey, columns[i])) {
                        result.put(columns[i], "********");
                    } else if (isDateField(ProcessInstanceKey, columns[i])) {
                        java.util.Date value = results.getDate(columns[i]);
                        if (value.getTime() == 0L) {
                            result.put(columns[i], "");
                        } else if (DateFormat == null) {
                            result.put(columns[i], (new Timestamp(value.getTime())).toString());
                        } else {
                            result.put(columns[i], dateFormat.format(results.getDate(columns[i])));
                        }
                    } else {
                        result.put(columns[i], results.getStringValue(columns[i]));
                    }
                }
            } else {
                logger.error("Too Many Records for Instance " + ProcessInstanceKey);
                throw new OIMHelperException("Too many process instance results ", ProcessInstanceKey);
            }
            logger.debug("Exiting OIMForms.getProcessFormValues()");
            return result;
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        } catch (tcNotAtomicProcessException ex) {
            logger.error("tcNotAtomicProcessException",ex);
            throw new OIMHelperException("tcNotAtomicProcessException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        }
    }

    /**
     * Return A Value from a process form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param Attribute - Process Form Field to return
     * @param HideEncrypted - Change encrypted values to all asterisks
     * @param DateFormat - Date fields will be converted to this format
     * @exception OIMHelperException
     */
    public String getProcessFormValue(long ProcessInstanceKey, String Attribute, boolean HideEncrypted, String DateFormat) throws OIMHelperException {

        logger.debug("Entering OIMForms.getProcessFormValue()");
        Map data = null;
        try {
            data = getProcessFormValues(ProcessInstanceKey, HideEncrypted, DateFormat);
        } catch (OIMHelperException ex) {
            logger.error("getProcessFormValue",ex);
            throw ex;
        }
        String result = data.get(Attribute) == null ? "" : (String) data.get(Attribute);
        logger.debug("Exiting OIMForms.getProcessFormValue()");
        return result;
    }

    /**
     * Update A Value from a process form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param Attribute - Process Form Field to update
     * @param Value - New Value
     * @exception OIMHelperException
     */
    public void setProcessFormValue(long ProcessInstanceKey, String Attribute, String Value) throws OIMHelperException {

        logger.debug("Entering OIMForms.setProcessFormValue()");
        Map data = new HashMap();
        data.put(Attribute, Value);
        try {
            setProcessFormValues(ProcessInstanceKey, data);
        } catch (OIMHelperException ex) {
            logger.error("setProcessFormValue",ex);
            throw ex;
        }
        logger.debug("Exiting OIMForms.setProcessFormValue()");
    }
    
    /**
     * Update A Value from a process form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param Attribute - Process Form Field to update
     * @param Value - New Value
     * @exception OIMHelperException
     */
    public void setProcessFormValue(long ProcessInstanceKey, String Attribute, Long Value) throws OIMHelperException {

        logger.debug("Entering OIMForms.setProcessFormValue()");
        Map data = new HashMap();
        data.put(Attribute, Value);
        try {
            setProcessFormValues(ProcessInstanceKey, data);
        } catch (OIMHelperException ex) {
            logger.error("setProcessFormValue",ex);
            throw ex;
        }
        logger.debug("Exiting OIMForms.setProcessFormValue()");
    }


    /**
     * Update A Value from a process form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param Attribute - Process Form Field to update
     * @param Value - New Date Value
     * @exception OIMHelperException
     */
    public void setProcessFormValue(long ProcessInstanceKey, String Attribute, java.util.Date Value) throws OIMHelperException {

        logger.debug("Entering OIMForms.setProcessFormValue()");
        Map data = new HashMap();
        Timestamp timestamp = new Timestamp(Value.getTime());
        data.put(Attribute, timestamp.toString());
        try {
            setProcessFormValues(ProcessInstanceKey, data);
        } catch (OIMHelperException ex) {
            logger.error("setProcessFormValue",ex);
            throw ex;
        }
        logger.debug("Exiting OIMForms.setProcessFormValue()");
    }

    /**
     * Update A Process form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param Data - Map containing the key value pairs of the Form Fields to update
     * @exception OIMHelperException
     */
    public void setProcessFormValues(long ProcessInstanceKey, Map Data) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.setProcessFormValues()");
            formInstOp.setProcessFormData(ProcessInstanceKey, Data);
            logger.debug("Exiting OIMForms.setProcessFormValues()");
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcInvalidValueException ex) {
            logger.error("tcInvalidValueException",ex);
            throw new OIMHelperException("tcInvalidValueException",ex);
        } catch (tcNotAtomicProcessException ex) {
            logger.error("tcNotAtomicProcessException",ex);
            throw new OIMHelperException("tcNotAtomicProcessException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcRequiredDataMissingException ex) {
            logger.error("tcRequiredDataMissingException",ex);
            throw new OIMHelperException("tcRequiredDataMissingException",ex);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        }
    }

    /**
     * Return a Map of the Data that was Pre Populated in the form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param dateFormat - All dates will be return in this format
     * @exception OIMHelperException
     */
    public Map getPrepopData(long ProcessInstanceKey, String dateFormat) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.getPrepopData()");
            Map result = new HashMap();
            Map existingData = getProcessFormValues(ProcessInstanceKey, false, dateFormat);
            long processFormDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
            tcResultSet results = formInstOp.prepopulateProcessForm(ProcessInstanceKey, processFormDefKey, existingData);
            if (results.getRowCount() == 1) {
                results.goToRow(0);
                String[] columns = results.getColumnNames();
                for (int i = 0; i < columns.length; i++) {
                    result.put(columns[i], results.getStringValue(columns[i]));
                }
            } else {
                throw new OIMHelperException("too many process instance records", ProcessInstanceKey);
            }
            logger.debug("Exiting OIMForms.getPrepopData()");
            return result;
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcAtcFormNotFoundExceptionPIException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        }
    }
    
    /**
     * Return a Map of the Data that was Pre Populated in the form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param dateFormat - All dates will be return in this format
     * @exception OIMHelperException
     */
    public Map getPrepopData(long ProcessInstanceKey, String dateFormat,boolean includeCurrent) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.getPrepopData()");
            Map result = new HashMap();
            Map existingData = new HashMap();
            if (includeCurrent)
                existingData = getProcessFormValues(ProcessInstanceKey, false, dateFormat);
            long processFormDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
            tcResultSet results = formInstOp.prepopulateProcessForm(ProcessInstanceKey, processFormDefKey, existingData);
            if (results.getRowCount() == 1) {
                results.goToRow(0);
                String[] columns = results.getColumnNames();
                for (int i = 0; i < columns.length; i++) {
                    result.put(columns[i], results.getStringValue(columns[i]));
                }
            } else {
                throw new OIMHelperException("too many process instance records", ProcessInstanceKey);
            }
            logger.debug("Exiting OIMForms.getPrepopData()");
            return result;
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcAtcFormNotFoundExceptionPIException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        }
    }

    /**
     * Return a Map of the Data that was Pre Populated in the form
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @exception OIMHelperException
     */
    public Map getPrepopData(long ProcessInstanceKey) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.getPrepopData()");
            Map result = new HashMap();
            Map existingData = new HashMap();
            tcResultSet results = formInstOp.getProcessFormData(ProcessInstanceKey);
            if (results.getRowCount() == 1) {
                results.goToRow(0);
                String[] columns = results.getColumnNames();
                for (int i = 0; i < columns.length; i++) {
                    existingData.put(columns[i], results.getStringValue(columns[i]));
                }
            } else {
                throw new OIMHelperException("process instance", ProcessInstanceKey);
            }
            long processFormDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
            results = formInstOp.prepopulateProcessForm(ProcessInstanceKey, processFormDefKey, existingData);
            if (results.getRowCount() == 1) {
                results.goToRow(0);
                String[] columns = results.getColumnNames();
                for (int i = 0; i < columns.length; i++) {
                    result.put(columns[i], results.getStringValue(columns[i]));
                }
            } else {
                throw new OIMHelperException("too many process instance records", ProcessInstanceKey);
            }
            logger.debug("Exiting OIMForms.getPrepopData()");
            return result;
        } catch (tcNotAtomicProcessException ex) {
            logger.error("tcNotAtomicProcessException",ex);
            throw new OIMHelperException("tcNotAtomicProcessException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        }
    }

    /**
     * Return a Child Form Process Definition Key
     *
     * @param ProcessInstanceKey - Process Instance Key of the Parent Process
     * @param ChildTable - Name of the child table
     * @exception OIMHelperException
     */
    public long getChildFormDefKey(long ProcessInstanceKey, String ChildTable) throws OIMHelperException {

        logger.debug("Entering OIMForms.getChildFormDefKey()");
        long result = 0L;
        int formVersion;
        try {
            formVersion = getFormVersion(ProcessInstanceKey);
        } catch (OIMHelperException ex) {
            logger.error("getChildFormDefKey/getFormVersion",ex);
            throw ex;
        }
        try
        {
        long formDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
        tcResultSet childFormDefs = formInstOp.getChildFormDefinition(formDefKey, formVersion);
        for (int i = 0; i < childFormDefs.getRowCount(); i++) {
            childFormDefs.goToRow(i);
            if (childFormDefs.getStringValue("Structure Utility.Table Name").equalsIgnoreCase(ChildTable)) {
                result = childFormDefs.getLongValue("Structure Utility.Child Tables.Child Key");
                break;
            }
        }
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcVersionNotDefinedException ex) {
            logger.error("tcVersionNotDefinedException",ex);
            throw new OIMHelperException("tcVersionNotDefinedException",ex);
        }

        logger.debug("Exiting OIMForms.getChildFormDefKey():" + result);
        return result;
    }

    /**
     * Return a Child Form Version
     *
     * @param ProcessInstanceKey - Process Instance Key of the Parent Process
     * @param ChildTable - Name of the child table
     * @exception OIMHelperException
     */
    public int getChildFormVersion(long ProcessInstanceKey, String ChildTable) throws OIMHelperException  {
        try {
            logger.debug("Entering OIMForms.getChildFormDefKey()");
            int result = 0;
            int formVersion = getFormVersion(ProcessInstanceKey);
            long formDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
            tcResultSet childFormDefs = formInstOp.getChildFormDefinition(formDefKey, formVersion);
            for (int i = 0; i < childFormDefs.getRowCount(); i++) {
                childFormDefs.goToRow(i);
                if (childFormDefs.getStringValue("Structure Utility.Table Name").equalsIgnoreCase(ChildTable)) {
                    result = childFormDefs.getIntValue("Structure Utility.Child Tables.Child Version");
                    break;
                }
            }
            logger.debug("Exiting OIMForms.getChildFormDefKey():" + result);
            return result;
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcVersionNotDefinedException ex) {
            logger.error("tcVersionNotDefinedException",ex);
            throw new OIMHelperException("tcVersionNotDefinedException",ex);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (OIMHelperException ex) {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }


    /**
     * Return an of Child Table Names for this Process Instance
     *
     * @param ProcessInstanceKey - Process Instance Key of the Parent Process
     * @exception OIMHelperException
     */
    public String[] getChildTables(long ProcessInstanceKey) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.getChildTables()");
            int formVersion = getFormVersion(ProcessInstanceKey);
            long formDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
            tcResultSet childFormDefs = formInstOp.getChildFormDefinition(formDefKey, formVersion);
            String[] result = new String[childFormDefs.getRowCount()];
            for (int i = 0; i < childFormDefs.getRowCount(); i++) {
                childFormDefs.goToRow(i);
                result[i] = childFormDefs.getStringValue("Structure Utility.Table Name");
            }
            logger.debug("Exiting OIMForms.getChildTables()");
            return result;
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        } catch (tcVersionNotDefinedException ex) {
            logger.error("tcVersionNotDefinedException",ex);
            throw new OIMHelperException("tcVersionNotDefinedException",ex);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (OIMHelperException ex) {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }

    /**
     * Return a an Array of Child Form Maps for this Process Instance Key
     *
     * @param ProcessInstanceKey - Process Instance Key of the Parent Process
     * @param ChildTable - Name of the child table
     * @param HideEncrypted - Change Encrypted values to all asterisks
     * @param DateFormat - Date will be returned in this format
     * @exception OIMHelperException
     */
    public Map[] getProcessFormChildValues(long ProcessInstanceKey, String ChildTable, boolean HideEncrypted, String DateFormat) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.getProcessFormChildValues()");
            List result = new ArrayList();
            SimpleDateFormat dateFormat = DateFormat == null ? null : new SimpleDateFormat(DateFormat);
            Map labels = getProcessChildFormDisplayNames(ProcessInstanceKey, ChildTable);
            long childDefKey = getChildFormDefKey(ProcessInstanceKey, ChildTable);
            tcResultSet results = formInstOp.getProcessFormChildData(childDefKey, ProcessInstanceKey);
            for (int i = 0; i < results.getRowCount(); i++) {
                results.goToRow(i);
                Map childRecord = new HashMap();
                childRecord.put(ChildTable + "_KEY", results.getStringValue(ChildTable + "_KEY"));
                String[] columns = results.getColumnNames();
                for (int j = 0; j < columns.length; j++) {
                    if (!labels.containsKey(columns[j])) {
                        continue;
                    }
                    if (HideEncrypted && isEncryptedField(ProcessInstanceKey, columns[j])) {
                        childRecord.put(columns[j], "********");
                    } else if (isDateField(ProcessInstanceKey, columns[j])) {
                        java.util.Date value = results.getDate(columns[j]);
                        if (value.getTime() == 0L) {
                            childRecord.put(columns[j], "");
                        } else if (DateFormat == null) {
                            childRecord.put(columns[j], (new Timestamp(value.getTime())).toString());
                        } else {
                            childRecord.put(columns[j], dateFormat.format(results.getDate(columns[j])));
                        }
                    } else {
                        childRecord.put(columns[j], results.getStringValue(columns[j]));
                    }
                }
                result.add(childRecord);
            }
            logger.debug("Exiting OIMForms.getProcessFormChildValues()");
            return (Map[]) result.toArray(new HashMap[0]);
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (OIMHelperException ex) {
            logger.error("getProcessFormChildValues",ex);
            throw ex;
        }

    }

    /**
     * Return the Key of this Child Record
     *
     * @param ChildTable - Name of the child table
     * @param Data - A map contain the Child Form Key Value Pairs
     * @exception OIMHelperException
     */
    public long getProcessFormChildValueKey(String ChildTable, Map Data) {

        logger.debug("Entering OIMForms.getProcessFormChildValueKey()");
        long result = Data.get(ChildTable.toUpperCase() + "_KEY") != null ? Long.parseLong((String) Data.get(ChildTable.toUpperCase() + "_KEY")) : 0L;
        logger.debug("Exiting OIMForms.getProcessFormChildValueKey()");
        return result;

    }

    /**
     * Create a new Child Entry
     *
     * @param ProcessInstanceKey - Process Instance of Parent
     * @param ChildTable - Name of the child table
     * @param Data - A map contain the Child Form Key Value Pairs
     * @exception OIMHelperException
     */
    public void addProcessFormChildValue(long ProcessInstanceKey, String ChildTable, Map Data) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.addProcessFormChildValue()");
            long childDefKey = getChildFormDefKey(ProcessInstanceKey, ChildTable);
            formInstOp.addProcessFormChildData(childDefKey, ProcessInstanceKey, Data);
            logger.debug("Exiting OIMForms.addProcessFormChildValue()");
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcRequiredDataMissingException ex) {
            logger.error("tcRequiredDataMissingException",ex);
            throw new OIMHelperException("tcRequiredDataMissingException",ex);
        } catch (tcInvalidValueException ex) {
            logger.error("tcInvalidValueException",ex);
            throw new OIMHelperException("tcInvalidValueException",ex);
        } catch (tcNotAtomicProcessException ex) {
            logger.error("tcNotAtomicProcessException",ex);
            throw new OIMHelperException("tcNotAtomicProcessException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (OIMHelperException ex) {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }

    /**
     * Update a Child Entry
     *
     * @param ProcessInstanceKey - Process Instance of Parent
     * @param ChildTable - Name of the child table
     * @param ChildDataKey - The Key of the Child Record to update
     * @param Data - A map contain the Child Form Key Value Pairs
     * @exception OIMHelperException
     */
    public void updateProcessFormChildValue(long ProcessInstanceKey, String ChildTable, long ChildDataKey, Map Data) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.updateProcessFormChildValue()");
            long childDefKey = getChildFormDefKey(ProcessInstanceKey, ChildTable);
            formInstOp.updateProcessFormChildData(childDefKey, ChildDataKey, Data);
            logger.debug("Exiting OIMForms.updateProcessFormChildValue()");
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcFormEntryNotFoundException ex) {
            logger.error("tcFormEntryNotFoundException",ex);
            throw new OIMHelperException("tcFormEntryNotFoundException",ex);
        } catch (tcInvalidValueException ex) {
            logger.error("tcInvalidValueException",ex);
            throw new OIMHelperException("tcInvalidValueException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (OIMHelperException ex) {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }

    /**
     * Delete a Child Entry
     *
     * @param ProcessInstanceKey - Process Instance of Parent
     * @param ChildTable - Name of the child table
     * @param ChildDataKey - The Key of the Child Record to delete
     * @exception OIMHelperException
     */
    public void removeProcessDataChildValue(long ProcessInstanceKey, String ChildTable, long ChildDataKey) throws OIMHelperException {

        logger.debug("Entering OIMForms.removeProcessDataChildValue()");
        long childDefKey = getChildFormDefKey(ProcessInstanceKey, ChildTable);
        try {
            formInstOp.removeProcessFormChildData(childDefKey, ChildDataKey);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcFormEntryNotFoundException ex) {
            logger.error("tcFormEntryNotFoundException",ex);
            throw new OIMHelperException("tcFormEntryNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        }
        
        logger.debug("Exiting OIMForms.removeProcessDataChildValue()");
    }

    /**
     * Delete all Child Entries
     *
     * @param ProcessInstanceKey - Process Instance of Parent
     * @param ChildTable - Name of the child table
     * @param ChildTableKey - The Name of the Field that contains the Key of the Child Record
     * @exception OIMHelperException
     */
    public void removeAllChildren(long instanceKey, String ChildTable, String ChildTableKey) throws OIMHelperException {
        try {
            Map[] recs = getProcessFormChildValues(instanceKey, ChildTable, false, null);
            if (recs != null && recs.length > 0) {
                for (int i = 0; i < recs.length; i++) {
                    Map rec = recs[i];
                    logger.debug("Rec " + rec);
                    String childKey = (String) rec.get(ChildTableKey);
                    long lchildKey = new Long(childKey).longValue();
                    removeProcessDataChildValue(instanceKey, ChildTable, lchildKey);
                }
            }
        } catch (OIMHelperException ex) {
            logger.error("OIMHelperException",ex);
            throw ex;
        }


    }

    /**
     * Delete all Child Entries Except for one matching the values of the
     * of the Child Tables Key Field Name.
     *
     * @param ProcessInstanceKey - Process Instance of Parent
     * @param ChildTable - Name of the child table
     * @param ChildTableKey - The Name of the Field that contains the Key of the Child Record to keep
     * @exception OIMHelperException
     */
    public void removeOldChildren(long instanceKey, String newValue, String childTable, String matchField, String childTableKey) throws OIMHelperException {
        try {
            Map[] recs = null;
            recs = getProcessFormChildValues(instanceKey, childTable, false, null);
            if (recs != null && recs.length > 0) {
                for (int i = 0; i < recs.length; i++) {
                    Map rec = recs[i];
                    logger.debug("Rec " + rec);
                    String oldVal = (String) rec.get(matchField);
                    if (!oldVal.equalsIgnoreCase(newValue)) {
                        String childKey = (String) rec.get(childTableKey);
                        long lchildKey = new Long(childKey).longValue();
                        removeProcessDataChildValue(instanceKey, childTable, lchildKey);
                    }
                }
            }
        } catch (OIMHelperException ex) {
            logger.error("OIMHelperException",ex);
            throw ex;
        }

    }

    /**
     * Checks Field for ITResource Type
     * 
     * @param ProcessInstanceKey - Process Instance Key
     * @param Field - The Form Field to Check
     * 
     * @exception OIMHelperException
     */
    public boolean isITResourceField(long ProcessInstanceKey, String Field) throws OIMHelperException {

        logger.debug("Entering OIMForms.isITResourceField()");
        int formVersion;
        try {
            formVersion = getFormVersion(ProcessInstanceKey);
        } catch (OIMHelperException ex) {
            logger.error("isITResourceField/getFormVersion");
            throw ex;
        }
        long formDefKey = 0l;
        try {
            formDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        }
        
        boolean result=false;;
        try {
            result = isFieldType(Field, "ITResourceLookupField", formVersion, formDefKey);
        } catch (OIMHelperException ex) {
            logger.error("isITResourceField/isFieldType");
            throw ex;
        }
        logger.debug("Exiting OIMForms.isITResourceField()");
        return result;
    }

    /**
     * Checks Child Table Field for ITResource Type
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param ChildTable - Name of the Child Table
     * @param Field - The Form Field to Check
     *
     * @exception OIMHelperException
     */
    public boolean isITResourceField(long ProcessInstanceKey, String ChildTable, String Field) throws OIMHelperException {

        logger.debug("Entering OIMForms.isITResourceField()");
        int childFormVersion = getChildFormVersion(ProcessInstanceKey, ChildTable);
        long childFormDefKey = getChildFormDefKey(ProcessInstanceKey, ChildTable);
        boolean result = false;
        try {
            result = isFieldType(Field, "ITResourceLookupField", childFormVersion, childFormDefKey);
        } catch (OIMHelperException ex) {
            logger.error("isITResourceField/isFieldType");
            throw ex;
        }
        logger.debug("Exiting OIMForms.isITResourceField()");
        return result;
    }

    /**
     * Checks Field for Password Type
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param Field - The Form Field to Check
     *
     * @exception OIMHelperException
     */
    public boolean isPasswordField(long ProcessInstanceKey, String Field) throws OIMHelperException {

        logger.debug("Entering OIMForms.isPasswordField()");
        int formVersion = 0;
        try {
            formVersion = getFormVersion(ProcessInstanceKey);
        } catch (OIMHelperException ex) {
            logger.error("isPasswordField/getFormVersion");
            throw ex;
        }
        long formDefKey = 0l;
        try {
            formDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        }

        boolean result = false;
        try {
            result = isFieldType(Field, "PasswordField", formVersion, formDefKey);
        } catch (OIMHelperException ex) {
            logger.error("isPasswordField/isFieldType");
            throw ex;
        }
        logger.debug("Exiting OIMForms.isPasswordField()");
        return result;
    }

    /**
     * Checks Child Table Field for Password Type
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param ChildTable - Name of the Child Table
     * @param Field - The Form Field to Check
     *
     * @exception OIMHelperException
     */
    public boolean isPasswordField(long ProcessInstanceKey, String ChildTable, String Field) throws OIMHelperException {

        logger.debug("Entering OIMForms.isPasswordField()");
        int childFormVersion = getChildFormVersion(ProcessInstanceKey, ChildTable);
        long childFormDefKey = getChildFormDefKey(ProcessInstanceKey, ChildTable);
        boolean result = false;
        try {
            result = isFieldType(Field, "PasswordField", childFormVersion, childFormDefKey);
        } catch (OIMHelperException ex) {
            logger.error("isPasswordField/isFieldType");
            throw ex;
        }
        logger.debug("Exiting OIMForms.isPasswordField()");
        return result;

    }

    /**
     * Checks Field for Encrypted Type
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param Field - The Form Field to Check
     *
     * @exception OIMHelperException
     */
    public boolean isEncryptedField(long ProcessInstanceKey, String Field) throws OIMHelperException {

        logger.debug("Entering OIMForms.isEncryptedField()");
        boolean result = false;
        int formVersion;
        try {
            formVersion = getFormVersion(ProcessInstanceKey);
        } catch (OIMHelperException ex) {
            logger.error("isEncryptedField/getFormVersion");
            throw ex;
        }
        try
        {
            long formDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
            tcResultSet fields = formDefOp.getFormFields(formDefKey, formVersion);
            for (int i = 0; i < fields.getRowCount(); i++) {
                fields.goToRow(i);
                String fieldName = fields.getStringValue("Structure Utility.Additional Columns.Name");
                if (fieldName.equalsIgnoreCase(Field)) {
                    String fieldEncrypted = fields.getStringValue("Structure Utility.Additional Columns.Encrypted");
                    result = fieldEncrypted.equalsIgnoreCase("1");
                    break;
                }
            }
        }
        catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        }
        logger.debug("Exiting OIMForms.isEncryptedField()");
        return result;

    }

    /**
     * Checks Child Table Field for Encrypted Type
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param ChildTable - Name of the Child Table
     * @param Field - The Form Field to Check
     *
     * @exception OIMHelperException
     */
    public boolean isEncryptedField(long ProcessInstanceKey, String ChildTable, String Field) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.isEncryptedField()");
            boolean result = false;
            int childFormVersion = getChildFormVersion(ProcessInstanceKey, ChildTable);
            long childFormDefKey = getChildFormDefKey(ProcessInstanceKey, ChildTable);
            tcResultSet fields = formDefOp.getFormFields(childFormDefKey, childFormVersion);
            for (int i = 0; i < fields.getRowCount(); i++) {
                fields.goToRow(i);
                String fieldName = fields.getStringValue("Structure Utility.Additional Columns.Name");
                if (fieldName.equalsIgnoreCase(Field)) {
                    String fieldEncrypted = fields.getStringValue("Structure Utility.Additional Columns.Encrypted");
                    result = fieldEncrypted.equalsIgnoreCase("1");
                    break;
                }
            }
            logger.debug("Exiting OIMForms.isEncryptedField()");
            return result;
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        }

    }

    /**
     * Checks Field for Date Type
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param Field - The Form Field to Check
     *
     * @exception OIMHelperException
     */
    public boolean isDateField(long ProcessInstanceKey, String Field) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.isDateField()");
            int formVersion = getFormVersion(ProcessInstanceKey);
            long formDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
            boolean result = isFieldType(Field, "DateFieldDlg", formVersion, formDefKey);
            logger.debug("Exiting OIMForms.isITResourceField()");
            return result;
        } catch (OIMHelperException ex) {
            logger.error("OIMHelperException",ex);
            throw ex;
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcAPIException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        }
    }

    /**
     * Checks Child Table Field for Date Type
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param ChildTable - Name of the Child Table
     * @param Field - The Form Field to Check
     *
     * @exception OIMHelperException
     */
    public boolean isDateField(long ProcessInstanceKey, String ChildTable, String Field) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.isITResourceField()");
            int childFormVersion = getChildFormVersion(ProcessInstanceKey, ChildTable);
            long childFormDefKey = getChildFormDefKey(ProcessInstanceKey, ChildTable);
            boolean result = isFieldType(Field, "DateFieldDlg", childFormVersion, childFormDefKey);
            logger.debug("Exiting OIMForms.isITResourceField()");
            return result;
        } catch (OIMHelperException ex) {
            logger.error("OIMHelperException",ex);
            throw ex;
        }
    }

    /**
     * Returns a Map of Column Names and their associated display names
     *
     * @param ProcessInstanceKey - Process Instance Key
     *
     * @exception OIMHelperException
     */
    public Map getProcessFormDisplayNames(long ProcessInstanceKey) throws OIMHelperException {

        logger.debug("Entering OIMForms.getProcessFormDisplayNames()");
        Map result = new HashMap();
        int formVersion;
        try {
            formVersion = getFormVersion(ProcessInstanceKey);
        } catch (OIMHelperException ex) {
            logger.error("getProcessFormDisplayNames/getFormVersion");
            throw ex;
        }

        try
        {
            long processFormDefKey = formInstOp.getProcessFormDefinitionKey(ProcessInstanceKey);
            tcResultSet results = formDefOp.getFormFields(processFormDefKey, formVersion);
            for (int i = 0; i < results.getRowCount(); i++) {
                results.goToRow(i);
                String field = results.getStringValue("Structure Utility.Additional Columns.Name");
                String label = results.getStringValue("Structure Utility.Additional Columns.Field Label");
                result.put(field, label);
            }
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcProcessNotFoundException ex) {
            logger.error("tcProcessNotFoundException",ex);
            throw new OIMHelperException("tcProcessNotFoundException",ex);
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        }
        logger.debug("Exiting OIMForms.getProcessFormDisplayNames()");
        return result;

    }

    /**
     * Returns a Map of Column Names and their associated display names
     * for the ChildTable
     *
     * @param ProcessInstanceKey - Process Instance Key
     * @param ChildTable - Name of the Child Table
     *
     * @exception OIMHelperException
     */
    public Map getProcessChildFormDisplayNames(long ProcessInstanceKey, String ChildTable) throws OIMHelperException {
        try {
            logger.debug("Entering OIMForms.getProcessFormDisplayNames()");
            Map result = new HashMap();
            int childFormVersion = getChildFormVersion(ProcessInstanceKey, ChildTable);
            long childFormDefKey = getChildFormDefKey(ProcessInstanceKey, ChildTable);
            tcResultSet results = formDefOp.getFormFields(childFormDefKey, childFormVersion);
            for (int i = 0; i < results.getRowCount(); i++) {
                results.goToRow(i);
                String field = results.getStringValue("Structure Utility.Additional Columns.Name");
                String label = results.getStringValue("Structure Utility.Additional Columns.Field Label");
                result.put(field, label);
            }
            logger.debug("Exiting OIMForms.getProcessFormDisplayNames()");
            return result;
        } catch (tcAPIException ex) {
            logger.error("tcAPIException",ex);
            throw new OIMHelperException("tcAPIException",ex);
        } catch (tcFormNotFoundException ex) {
            logger.error("tcFormNotFoundException",ex);
            throw new OIMHelperException("tcFormNotFoundException",ex);
        } catch (tcColumnNotFoundException ex) {
            logger.error("tcColumnNotFoundException",ex);
            throw new OIMHelperException("tcColumnNotFoundException",ex);
        }
    }
}
