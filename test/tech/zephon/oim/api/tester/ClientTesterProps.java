/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.zephon.oim.api.tester;

import Thor.API.Operations.tcSchedulerOperationsIntf;
import Thor.API.tcResultSet;
import tech.zephon.oim.exceptions.OIMHelperException;
import tech.zephon.oim.api.OIMHelperClient;
import tech.zephon.oim.api.OIMProperties;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.iam.scheduler.api.SchedulerService;
import oracle.iam.scheduler.vo.JobDetails;
import oracle.iam.scheduler.vo.JobParameter;
import org.junit.Test;

/**
 *
 */
public class ClientTesterProps extends OIMHelperClient {

    private static OIMProperties oimProperties;
    private static final Logger logger = Logger.getLogger(ClientTesterProps.class.getName());

    @Test
    public void mainTest() {

        //ClientTesterProps testprops = new ClientTesterProps();

        try {

            loadConfig(null);
            loginWithCustomEnv();

            oimProperties = new OIMProperties(getClient());
            Map lku = oimProperties.getLookupProperties("Lookup.USR_PROCESS_TRIGGERS");
            logger.fine("Lku:" + lku);
            //showAll();
            oimProperties.getITResourceProperties("APP2USER");
            //doJobProps();
            oimProperties.getTaskProperties("TestOIMHelper");
            setTaskProperties("TestOIMHelper","Test Value One","Better Value One");
            oimProperties.getTaskProperties("TestOIMHelper");
        } catch (OIMHelperException e) {
            logger.log(Level.SEVERE,"Error", e);
        }

    }
    
    public void doJobProps() {
        tcSchedulerOperationsIntf scheduleOps = getClient().getService(tcSchedulerOperationsIntf.class);
        HashMap hashMap = new HashMap();
        hashMap.put("Task Scheduler.Name", "TestMe");
        try {
            tcResultSet tcresultSet = scheduleOps.findScheduleTasks(hashMap);
            printResultSet(tcresultSet);
            tcresultSet.goToRow(0);
            long schTaskKey = tcresultSet.getLongValue("Task Scheduler.Key");
            hashMap.clear();
            hashMap.put("Task Scheduler.Task Attributes.Name", "Latest Token");
            tcResultSet rs1 = scheduleOps.findScheduleTaskAttributes(hashMap);
            hashMap.clear();
            printResultSet(rs1);
            int iRowCnt = rs1.getRowCount();
            long attrKey = 0L;
            for (int i = 0; i < iRowCnt; i++) {
                rs1.goToRow(i);
                long temp = rs1.getLongValue("Task Scheduler.Key");
                if (temp == schTaskKey) {
                    attrKey = rs1.getLongValue("Task Scheduler.Task Attributes.Key");
                    break;
                }
            }

            hashMap.put("Task Scheduler.Task Attributes.Value", "2013-11-23 11:06:38.727");
            logger.fine("TaskKey:" + schTaskKey);
            logger.fine("AttrKey:" + attrKey);
            logger.fine("AttrMap:" + hashMap);
            scheduleOps.updateScheduleTaskAttribute(schTaskKey, attrKey, hashMap);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"APIError:", e);
        }
    }

    public void showAll() {
        logger.fine("Get ITRes Parms");
        try {
            Map propMap = oimProperties.getITResourceProperties("Directory Server");
            logger.info("IT Resource Props " + propMap);
        } catch (OIMHelperException e) {
            logger.log(Level.SEVERE,"OIM Exception", e);
        }
        logger.fine("");
        logger.fine("Get Lookup Parms");
        try {
            Map propMap = oimProperties.getLookupProperties("Lookup.USR_PROCESS_TRIGGERS");
            logger.info("Lookup Props " + propMap);
        } catch (OIMHelperException e) {
            logger.log(Level.SEVERE,"OIM Exception", e);
        }

        logger.fine("");
        logger.fine("Get All Job Parms");
        try {
            String[] jobs = oimProperties.getAllJobs();
            for (String job : jobs) {
                logger.fine("Get Job " + job);
                Map propMap = oimProperties.getTaskProperties(job);
                logger.info("Task Props " + propMap);
            }
        } catch (OIMHelperException e) {
            logger.log(Level.SEVERE,"OIM Exception", e);
        }
    }
    
    public void showJob(String jobName)
    {
        try {
            logger.fine("Get Job " + jobName);
            Map propMap = oimProperties.getTaskProperties(jobName);
            logger.info("Task Props " + propMap);
        } catch (OIMHelperException e) {
            logger.log(Level.SEVERE,"OIM Exception", e);
        }
    }
    
    public void showSpecificITResource(String resName)
    {
        logger.fine("Get ITRes Parms:" + resName);
        try {
            Map propMap = oimProperties.getITResourceProperties(resName);
            logger.info("IT Resource Props " + propMap);
        } catch (OIMHelperException e) {
            logger.log(Level.SEVERE,"OIM Exception", e);
        }
    }
    
    public Map<String, String> getTaskProperties(String taskName) throws OIMHelperException {
        SchedulerService scheduleOps = getClient().getService(SchedulerService.class);
        Map jobProps = new HashMap();

        try {
            //logger.fine("Get Job " + taskName);
            JobDetails jd = scheduleOps.getJobDetail(taskName);

            if (jd == null)
            {
                logger.log(Level.SEVERE,"Job Not Found for " + taskName);
                throw new OIMHelperException("Job Not Found for " + taskName);
            }

            Map<String,JobParameter> parms = jd.getAttributes();

            if (parms == null)
            {
                logger.fine("No Parms for " + taskName);
                return jobProps;
            }
            Set<String> keys = parms.keySet();

            for(String key : keys)
            {
                logger.fine("Parm Key:" + key);
                JobParameter jp = parms.get(key);
                logger.fine("DataType " + jp.getDataType());
                logger.fine("Name " + jp.getName());
                logger.fine("Val " + jp.getValue());
                logger.fine("PKey " + jp.getParameterKey());
                jobProps.put(jp.getName(),jp.getValue().toString());

            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE,"SchedulerException",ex);
            throw new OIMHelperException("SchedulerException",ex);
        }
        return jobProps;
        
        
    }
    
    public void setTaskProperties(String taskName,String propertyName,String value) throws OIMHelperException {
        SchedulerService scheduleOps = getClient().getService(SchedulerService.class);

        try {
            //logger.fine("Get Job " + taskName);
            JobDetails jd = scheduleOps.getJobDetail(taskName);

            if (jd == null)
            {
                logger.log(Level.SEVERE,"Job Not Found for " + taskName);
                throw new OIMHelperException("Job Not Found for " + taskName);
            }

            Map<String,JobParameter> parms = jd.getAttributes();

            if (parms == null)
            {
                logger.fine("No Parms for " + taskName);
                return;
            }
            Set<String> keys = parms.keySet();

            for(String key : keys)
            {
                logger.fine("Parm Key:" + key);
                JobParameter jp = parms.get(key);
                logger.fine("DataType " + jp.getDataType());
                logger.fine("Name " + jp.getName());
                logger.fine("Val " + jp.getValue());
                logger.fine("PKey " + jp.getParameterKey());
                if (jp.getName().equals(propertyName))
                {
                    Serializable s = new String(value);
                    jp.setValue(s);
                }

            }
            scheduleOps.updateJob(jd);
        } catch (Exception ex) {
            logger.log(Level.SEVERE,"SchedulerException",ex);
            throw new OIMHelperException("SchedulerException",ex);
        }
        return;
        
        
    }
    
    
    private void printResultSet(tcResultSet rs)
    {
        String[] headers = null;
        try
        {
            int recCount = rs.getRowCount();
            if (recCount > 0)
            {
                headers = rs.getColumnNames();
            }
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                for(String name : headers)
                {
                    logger.fine(name + ":" + rs.getStringValue(name));
                }
                
            }
        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE,"APIError",e);
        }
    }
}
