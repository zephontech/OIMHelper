/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.model;

/**
 *
 * @author fforester
 */
public class OpenTask {
    
    private long taskKey;
    private String taskName;
    private String objectName;
    private String assignedUser;
    private String assignedGroup;
    private long assignedUserKey;
    private long assignedGroupKey;
    private String targetUser;
    private long processInstanceKey;

    
    public boolean isAssignedToUser()
    {
        if (assignedUser != null && assignedUserKey > 0)
            return true;
        return false;
    }
    
    public long getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(long taskKey) {
        this.taskKey = taskKey;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }

    public String getAssignedGroup() {
        return assignedGroup;
    }

    public void setAssignedGroup(String assignedGroup) {
        this.assignedGroup = assignedGroup;
    }

    public long getAssignedUserKey() {
        return assignedUserKey;
    }

    public void setAssignedUserKey(long assignedUserKey) {
        this.assignedUserKey = assignedUserKey;
    }

    public long getAssignedGroupKey() {
        return assignedGroupKey;
    }

    public void setAssignedGroupKey(long assignedGroupKey) {
        this.assignedGroupKey = assignedGroupKey;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public long getProcessInstanceKey() {
        return processInstanceKey;
    }

    public void setProcessInstanceKey(long processInstanceKey) {
        this.processInstanceKey = processInstanceKey;
    }

    @Override
    public String toString() {
        return "OpenTask{" + "taskKey=" + taskKey + ", taskName=" + taskName + ", objectName=" + objectName + ", assignedUser=" + assignedUser + ", assignedGroup=" + assignedGroup + ", assignedUserKey=" + assignedUserKey + ", assignedGroupKey=" + assignedGroupKey + ", targetUser=" + targetUser + ", processInstanceKey=" + processInstanceKey + '}';
    }
    
}
