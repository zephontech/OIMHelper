/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.api;

import Thor.API.Operations.TaskDefinitionOperationsIntf;
import Thor.API.Operations.tcProvisioningOperationsIntf;
import Thor.API.tcResultSet;
import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.model.OpenTask;
import com.thortech.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;

/**
 *
 * @author fforester
 */
public class OIMProcessTaskOperations {
    
    /**
     * The default logger instance for this instance.
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    private tcProvisioningOperationsIntf provOps;
    private TaskDefinitionOperationsIntf taskOps;
    
    
    /**
     * create using Platform getService
     * @throws OIMHelperException 
     */
    public OIMProcessTaskOperations() throws OIMHelperException {
        
        provOps = Platform.getService(tcProvisioningOperationsIntf.class);
        if (provOps == null) {
            logger.error("Failed to get provOps");
            throw new OIMHelperException("provOps Failed");
        }
        taskOps = Platform.getService(TaskDefinitionOperationsIntf.class);
        if (taskOps == null) {
            logger.error("Failed to get taskOps");
            throw new OIMHelperException("taskOps Failed");
        }

    }
    
    /**
     * create using existing OIMClient
     * @param client
     * @throws OIMHelperException 
     */
    public OIMProcessTaskOperations(OIMClient client) throws OIMHelperException {
        
        provOps = client.getService(tcProvisioningOperationsIntf.class);
        if (provOps == null) {
            logger.error("Failed to get provOps");
            throw new OIMHelperException("provOps Failed");
        }
        taskOps = client.getService(TaskDefinitionOperationsIntf.class);
        if (taskOps == null) {
            logger.error("Failed to get taskOps");
            throw new OIMHelperException("taskOps Failed");
        }

    }
    
    /**
     * return all open rejected tasks as list of Maps
     * @return
     * @throws OIMHelperException 
     */
    public List<Map<String,String>> getAllOpenTasks() throws OIMHelperException
    {
        List<Map<String,String>> retList = new ArrayList<Map<String,String>>();
        try
        {
            tcResultSet rs = provOps.findAllOpenProvisioningTasks(new HashMap(), new String[]{});
            int recCount = rs.getRowCount();
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                String status = rs.getStringValue("Process Instance.Task Details.Status");
                if (status != null && !status.equalsIgnoreCase("R"))
                {
                    continue;
                }
                Map rec = new HashMap<String,String>();
                rec.put("TASK_KEY",rs.getStringValue("Process Instance.Task Details.Key"));
                rec.put("OBJ_NAME",rs.getStringValue("Objects.Name"));
                retList.add(rec);
            }
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        return retList;
    }
    
    /**
     * return all open pending tasks as list of Maps
     * @return
     * @throws OIMHelperException 
     */
    public List<Map<String,String>> getAllOpenPendingTasks() throws OIMHelperException
    {
        List<Map<String,String>> retList = new ArrayList<Map<String,String>>();
        try
        {
            tcResultSet rs = provOps.findAllOpenProvisioningTasks(new HashMap(), new String[]{});
            int recCount = rs.getRowCount();
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                String status = rs.getStringValue("Process Instance.Task Details.Status");
                if (status != null && !status.equalsIgnoreCase("P"))
                {
                    continue;
                }
                Map rec = new HashMap<String,String>();
                rec.put("TASK_KEY",rs.getStringValue("Process Instance.Task Details.Key"));
                rec.put("OBJ_NAME",rs.getStringValue("Objects.Name"));
                retList.add(rec);
            }
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        return retList;
    }
    
    public List<OpenTask> getAllOpenTasks(String resourceName,String taskName) throws OIMHelperException
    {
        
        List<OpenTask> tasksByNameAndResource = new ArrayList<OpenTask>();
        if (resourceName == null || resourceName.isEmpty())
            return tasksByNameAndResource;
        
        
        try
        {
            List<OpenTask> tasksByName = this.getAllOpenTasks(taskName);
            if (tasksByName.isEmpty())
                return tasksByNameAndResource;
            for(OpenTask t : tasksByName)
            {
                if (resourceName.equalsIgnoreCase(t.getObjectName()))
                    tasksByNameAndResource.add(t);
            }
        }
        catch(Exception e)
        {
            throw new OIMHelperException(e);
        }
        return tasksByNameAndResource;
    }
    
    /**
     * get all open rejected tasks with this taskname
     * taskname can be null for all open tasks as a list of open task
     * objects
     * @param taskName
     * @return
     * @throws OIMHelperException 
     */
    public List<OpenTask> getAllOpenTasks(String taskName) throws OIMHelperException
    {
        List<OpenTask> retList = new ArrayList<OpenTask>();
        try
        {
            tcResultSet rs = provOps.findAllOpenProvisioningTasks(new HashMap(), new String[]{});
            int recCount = rs.getRowCount();
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                String status = rs.getStringValue("Process Instance.Task Details.Status");
                if (status != null && !status.equalsIgnoreCase("R"))
                {
                    continue;
                }
                OpenTask ot = new OpenTask();
                long l = rs.getLongValue("Process Instance.Task Information.Assigned To Group Key");
                if (l > 0) {
                    ot.setAssignedGroupKey(l);
                }
                
                l = rs.getLongValue("Process Instance.Task Information.Assigned To User Key");
                if (l > 0) {
                    ot.setAssignedUserKey(l);
                }
                
                ot.setAssignedGroup(rs.getStringValue("Groups.Group Name"));
                ot.setAssignedUser(rs.getStringValue("Process Instance.Task Information.Assignee User ID"));
                ot.setObjectName(rs.getStringValue("Objects.Name"));
                ot.setProcessInstanceKey(rs.getLongValue("Process Instance.Key"));
                ot.setTaskKey(rs.getLongValue("Process Instance.Task Details.Key"));
                ot.setTaskName(rs.getStringValue("Process Definition.Tasks.Task Name"));
                ot.setTargetUser(rs.getStringValue("Process Instance.Task Information.Target User"));
                if (taskName != null)
                {
                    if (ot.getTaskName().equals(taskName)) {
                        retList.add(ot);
                    }
                }
                else
                {
                    retList.add(ot);
                }
            }
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        return retList;
    }
    
    
    public List<OpenTask> getAllOpenPendingTasks(String resourceName,String taskName) throws OIMHelperException
    {
        List<OpenTask> tasksByNameAndResource = new ArrayList<OpenTask>();
        if (resourceName == null || resourceName.isEmpty())
            return tasksByNameAndResource;
        
        try
        {
            List<OpenTask> tasksByName = this.getAllOpenPendingTasks(taskName);
            if (tasksByName.isEmpty())
                return tasksByNameAndResource;
            for(OpenTask t : tasksByName)
            {
                if (resourceName.equalsIgnoreCase(t.getObjectName()))
                    tasksByNameAndResource.add(t);
            }
        }
        catch(Exception e)
        {
            throw new OIMHelperException(e);
        }
        return tasksByNameAndResource;
    }
    
    /**
     * get all open pending tasks with this taskname
     * taskname can be null for all open tasks as a list of open task
     * @param taskName
     * @return
     * @throws OIMHelperException 
     */
    public List<OpenTask> getAllOpenPendingTasks(String taskName) throws OIMHelperException
    {
        List<OpenTask> retList = new ArrayList<OpenTask>();
        try
        {
            tcResultSet rs = provOps.findAllOpenProvisioningTasks(new HashMap(), new String[]{});
            int recCount = rs.getRowCount();
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                String status = rs.getStringValue("Process Instance.Task Details.Status");
                if (status != null && !status.equalsIgnoreCase("P"))
                {
                    continue;
                }
                OpenTask ot = new OpenTask();
                long l = rs.getLongValue("Process Instance.Task Information.Assigned To Group Key");
                if (l > 0) {
                    ot.setAssignedGroupKey(l);
                }
                
                l = rs.getLongValue("Process Instance.Task Information.Assigned To User Key");
                if (l > 0) {
                    ot.setAssignedUserKey(l);
                }
                
                ot.setAssignedGroup(rs.getStringValue("Groups.Group Name"));
                ot.setAssignedUser(rs.getStringValue("Process Instance.Task Information.Assignee User ID"));
                ot.setObjectName(rs.getStringValue("Objects.Name"));
                ot.setProcessInstanceKey(rs.getLongValue("Process Instance.Key"));
                ot.setTaskKey(rs.getLongValue("Process Instance.Task Details.Key"));
                ot.setTaskName(rs.getStringValue("Process Definition.Tasks.Task Name"));
                ot.setTargetUser(rs.getStringValue("Process Instance.Task Information.Target User"));
                if (taskName != null)
                {
                    if (ot.getTaskName().equals(taskName)) {
                        retList.add(ot);
                    }
                }
                else
                {
                    retList.add(ot);
                }
            }
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        return retList;
    }
    
    /**
     * retry these open task objects as a whole
     * @param tasks
     * @throws OIMHelperException 
     */
    public void retryOpenTasks(List<OpenTask> tasks) throws OIMHelperException {
        for (OpenTask tk : tasks) {
            try {
                provOps.retryTask(tk.getTaskKey());
            } catch (Exception e) {
                logger.error("Retry Failed:" + e.getMessage());
            }
        }
    }
    
    /**
     * reassign theses open task objects to the SYS ADMIN Group.
     * @param tasks
     * @throws OIMHelperException 
     */
    public void reassignOpenTasksToSysAdminGroup(List<OpenTask> tasks) throws OIMHelperException {
        long[] taskKey= new long[1];
        for (OpenTask tk : tasks) {
            try {
                taskKey[0] = tk.getTaskKey();
                provOps.reassignTasksToGroup(taskKey, 1);
            } catch (Exception e) {
                logger.error("Retry Failed:" + e.getMessage());
            }
        }
    }
    
    /**
     * retry ALL rejected open tasks
     * @throws OIMHelperException 
     */
    public void retryAllOpenTasks() throws OIMHelperException
    {
        List<String> tasks = new ArrayList<String>();
        try
        {
            tcResultSet rs = provOps.findAllOpenProvisioningTasks(new HashMap(), new String[]{});
            int recCount = rs.getRowCount();
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                String status = rs.getStringValue("Process Instance.Task Details.Status");
                if (status != null && !status.equalsIgnoreCase("R"))
                {
                    continue;
                }
                
                String taskKey = rs.getStringValue("Process Instance.Task Details.Key");
                tasks.add(taskKey);
            }
            if (tasks.isEmpty())
            {
                return;
            }
            for(String tk : tasks)
            {
                provOps.retryTask(Long.parseLong(tk));
            }
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
    }
    
    /**
     * get the process task definition key.
     * @param name
     * @param processInstanceKey
     * @return
     * @throws OIMHelperException 
     */
    public String getProcessTaskDefinition(String name,long processInstanceKey) throws OIMHelperException
    {
        String taskDefKey = null;
        try
        {
            logger.debug("Find Task:" + name);
            tcResultSet rs = taskOps.getTaskDetail(processInstanceKey,new HashMap());
            int recCount = rs.getRowCount();
            
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                String taskName = rs.getStringValue("Process Definition.Tasks.Task Name");
                logger.debug("Found Task:" + taskName);
                if (!name.equals(taskName))
                    continue;
                taskDefKey = rs.getStringValue("Process Definition.Tasks.Key");
                logger.debug("Got Task:" + taskDefKey);
                break;
            }
            
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        return taskDefKey;
    }
    
    /**
     * run a process task by name
     * @param name
     * @param processInstanceKey
     * @throws OIMHelperException 
     */
    public void runProcessTask(String name,long processInstanceKey) throws OIMHelperException
    {
        String taskDefKey = null;
        try
        {
            taskDefKey = getProcessTaskDefinition(name,processInstanceKey);
            
            if (taskDefKey != null)
            {
                long tdk = Long.parseLong(taskDefKey);
                provOps.addProcessTaskInstance(tdk,processInstanceKey);
            }
            
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
    }
    
    /*
     * long processinstance key
     * return the usrKey that owns this process instance.
     */
    public long getObjectOwner(long procKey) throws OIMHelperException
    {
        long userKey = 0;
        try
        {
            tcResultSet rs = provOps.getObjectDetail(procKey);
            int recCount = rs.getRowCount();
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                userKey = rs.getLongValue("Users.Key");
                logger.debug("Found User:" + userKey);
            }
            
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            throw new OIMHelperException(e);
        }
        return userKey;
    }
    
}
