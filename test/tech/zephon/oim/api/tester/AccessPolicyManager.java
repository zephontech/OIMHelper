/*
 * AccessPolicyManager - A wrapper class for managing OIM Access Policies
 * Extracted from ClientTesterAccessPolicies for reusable policy operations
 */
package tech.zephon.oim.api.tester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import oracle.iam.accesspolicy.api.AccessPolicyService;
import oracle.iam.accesspolicy.vo.AccessPolicy;
import oracle.iam.accesspolicy.vo.AccessPolicyElement;
import oracle.iam.accesspolicy.vo.ChildAttribute;
import oracle.iam.accesspolicy.vo.ChildRecord;
import oracle.iam.accesspolicy.vo.DefaultData;
import oracle.iam.accesspolicy.vo.Record;
import oracle.iam.platform.OIMClient;
import oracle.iam.provisioning.api.ApplicationInstanceService;
import oracle.iam.provisioning.vo.ApplicationInstance;
import tech.zephon.oim.api.OIMAccessPolicies;

/**
 * AccessPolicyManager provides a comprehensive wrapper for managing OIM Access Policies
 * 
 * This class encapsulates common operations for:
 * - Updating parent attributes in access policies
 * - Adding/removing child records
 * - Managing application instances within policies
 * - Bulk policy updates from structured data
 * 
 * @author Frederick.Forester
 */
public class AccessPolicyManager {
    
    private final AccessPolicyService apService;
    private final ApplicationInstanceService aiService;
    private final OIMAccessPolicies oimPolicies;
    private final OIMClient oimClient;
    
    /**
     * Constructor - Initialize the AccessPolicyManager with OIM services
     * 
     * @param oimClient The OIM client instance
     */
    public AccessPolicyManager(OIMClient oimClient) {
        this.oimClient = oimClient;
        this.apService = oimClient.getService(AccessPolicyService.class);
        this.aiService = oimClient.getService(ApplicationInstanceService.class);
        this.oimPolicies = new OIMAccessPolicies(oimClient);
    }
    
    /**
     * Update an access policy with parent attributes and child records
     * 
     * @param policyKey The access policy key/ID
     * @param parentAttributes Map of parent attribute names to values
     * @param childRecords Queue of child record data
     * @param childTableName Name of the child table to update
     * @param applicationName Name of the application instance
     * @throws Exception if policy update fails
     */
    public void updateAccessPolicy(String policyKey, Map<String, String> parentAttributes, 
            Queue<Map<String, String>> childRecords, String childTableName, String applicationName) throws Exception {
        
        String appId = getApplicationInstanceId(applicationName);
        if (appId == null) {
            throw new Exception("Application instance not found: " + applicationName);
        }
        
        AccessPolicy policy = apService.getAccessPolicy(policyKey, true);
        if (policy == null) {
            throw new Exception("Access Policy not found: " + policyKey);
        }
        
        List<AccessPolicyElement> elements = policy.getPolicyElements();
        
        // Update parent attributes
        if (parentAttributes != null && !parentAttributes.isEmpty()) {
            updateParentAttributes(elements, parentAttributes, appId);
        }
        
        // Add child records
        if (childRecords != null && !childRecords.isEmpty() && childTableName != null) {
            addChildRecords(elements, childRecords, childTableName, appId);
        }
        
        // Commit the changes
        apService.updateAccessPolicy(policy);
    }
    
    /**
     * Update parent attributes for a specific application instance
     * 
     * @param elements List of access policy elements
     * @param parentAttributes Map of attribute names to values
     * @param appId Application instance ID
     */
    public void updateParentAttributes(List<AccessPolicyElement> elements, 
            Map<String, String> parentAttributes, String appId) {
        
        Set<String> keys = parentAttributes.keySet();
        for (String key : keys) {
            String value = parentAttributes.get(key);
            boolean found = updateParentValue(elements, key, value, appId);
            if (!found) {
                System.out.println("Attribute not found, adding new: " + key);
                addParentAttribute(elements, key, value, appId);
            }
        }
    }
    
    /**
     * Add multiple child records from a queue
     * 
     * @param elements List of access policy elements
     * @param childRecords Queue of child record data
     * @param childTableName Name of the child table
     * @param appId Application instance ID
     */
    public void addChildRecords(List<AccessPolicyElement> elements, 
            Queue<Map<String, String>> childRecords, String childTableName, String appId) {
        
        while (!childRecords.isEmpty()) {
            Map<String, String> combinedChild = new HashMap<>();
            
            // Process multiple attributes for a single child record
            if (childRecords.size() > 1) {
                // Determine how many attributes belong to this child record
                // For now, we'll process all remaining records as one combined child
                // This logic might need adjustment based on your specific use case
                while (!childRecords.isEmpty()) {
                    Map<String, String> childAttr = childRecords.poll();
                    Map.Entry<String, String> entry = childAttr.entrySet().iterator().next();
                    combinedChild.put(entry.getKey(), entry.getValue());
                }
            } else {
                // Single attribute remaining
                Map<String, String> child = childRecords.poll();
                Map.Entry<String, String> entry = child.entrySet().iterator().next();
                combinedChild.put(entry.getKey(), entry.getValue());
            }
            
            addChildRecord(elements, appId, childTableName, combinedChild);
        }
    }
    
    /**
     * Update a specific parent attribute value
     * 
     * @param elements List of access policy elements
     * @param attributeName Name of the attribute to update
     * @param newValue New value for the attribute
     * @param appId Application instance ID
     * @return true if attribute was found and updated, false otherwise
     */
    public boolean updateParentValue(List<AccessPolicyElement> elements, String attributeName, 
            String newValue, String appId) {
        
        Long appIdL = Long.valueOf(appId);
        boolean found = false;
        
        for (AccessPolicyElement ape : elements) {
            if (ape.getApplicationInstanceID() != appIdL) {
                continue;
            }
            
            DefaultData dd = ape.getDefaultData();
            List<Record> records = dd.getData();
            
            for (Record rec : records) {
                String attrName = (String) rec.getAttribute("attributeName");
                if (attrName.equalsIgnoreCase(attributeName)) {
                    rec.setAttributeValue(newValue);
                    found = true;
                    break;
                }
            }
            
            if (found) {
                dd.setData(records);
                ape.setDefaultData(dd);
                break;
            }
        }
        
        return found;
    }
    
    /**
     * Add a new parent attribute to the access policy
     * 
     * @param elements List of access policy elements
     * @param attributeName Name of the new attribute
     * @param value Value for the new attribute
     * @param appId Application instance ID
     */
    public void addParentAttribute(List<AccessPolicyElement> elements, String attributeName, 
            String value, String appId) {
        
        Long appIdL = Long.valueOf(appId);
        
        for (AccessPolicyElement ape : elements) {
            if (ape.getApplicationInstanceID() != appIdL) {
                continue;
            }
            
            Record newRecord = new Record(attributeName, value);
            DefaultData dd = ape.getDefaultData();
            dd.getData().add(newRecord);
            ape.setDefaultData(dd);
            break;
        }
    }
    
    /**
     * Add a child record to the access policy
     * 
     * @param elements List of access policy elements
     * @param appId Application instance ID
     * @param childTableName Name of the child table
     * @param childData Map containing child record data
     */
    public void addChildRecord(List<AccessPolicyElement> elements, String appId, 
            String childTableName, Map<String, String> childData) {
        
        Long appIdL = Long.valueOf(appId);
        
        for (AccessPolicyElement ape : elements) {
            if (ape.getApplicationInstanceID() == appIdL) {
                ape.getDefaultData().addChildData(childTableName, childData);
                break;
            }
        }
    }
    
    /**
     * Update a child attribute value
     * 
     * @param childRecord The child record to update
     * @param attributeName Name of the attribute to update
     * @param newValue New value for the attribute
     */
    public void updateChildValue(ChildRecord childRecord, String attributeName, String newValue) {
        List<ChildAttribute> attributes = childRecord.getAttributes();
        
        for (ChildAttribute attr : attributes) {
            String attrName = (String) attr.getAttribute("attributeName");
            if (attrName.equalsIgnoreCase(attributeName)) {
                attr.setAttributeValue(newValue);
                break;
            }
        }
    }
    
    /**
     * Remove an application instance from the access policy
     * 
     * @param elements List of access policy elements
     * @param appId Application instance ID to remove
     */
    public void removeApplicationInstance(List<AccessPolicyElement> elements, String appId) {
        Long appIdL = Long.valueOf(appId);
        
        for (AccessPolicyElement ape : elements) {
            if (ape.getApplicationInstanceID() == appIdL) {
                ape.markForDelete();
                break;
            }
        }
    }
    
    /**
     * Remove all child records from a list
     * 
     * @param childRecords List of child records to remove
     */
    public void removeChildRecords(List<ChildRecord> childRecords) {
        for (ChildRecord childRec : childRecords) {
            List<ChildAttribute> attributes = childRec.getAttributes();
            for (ChildAttribute attr : attributes) {
                attr.markForDelete();
            }
        }
    }
    
    /**
     * Get an access policy by name
     * 
     * @param policyName Name of the access policy
     * @return AccessPolicy object
     * @throws Exception if policy not found
     */
    public AccessPolicy getAccessPolicy(String policyName) throws Exception {
        return oimPolicies.getAccessPolicy(policyName);
    }
    
    /**
     * Get an access policy by key
     * 
     * @param policyKey Key/ID of the access policy
     * @return AccessPolicy object
     * @throws Exception if policy not found
     */
    public AccessPolicy getAccessPolicyByKey(String policyKey) throws Exception {
        return apService.getAccessPolicy(policyKey, true);
    }
    
    /**
     * Create a new access policy with parent attributes and child records
     * 
     * @param policyName Name of the new access policy
     * @param description Description of the access policy
     * @param applicationName Name of the application instance
     * @param parentAttributes Map of parent attribute names to values
     * @param childRecords Queue of child record data
     * @param childTableName Name of the child table
     * @return The created access policy ID
     * @throws Exception if policy creation fails
     */
    public String createAccessPolicy(String policyName, String description, String applicationName,
            Map<String, String> parentAttributes, Queue<Map<String, String>> childRecords, 
            String childTableName) throws Exception {
        
        // Get application instance ID
        String appId = getApplicationInstanceId(applicationName);
        if (appId == null) {
            throw new Exception("Application instance not found: " + applicationName);
        }
        Long appIdL = Long.valueOf(appId);
        
        // Create the main AccessPolicy object
        long priority = oimPolicies.getLowestPriority() + 1;
        AccessPolicy newPolicy = new AccessPolicy(policyName,description,false,priority,AccessPolicy.OwnerType.ROLE,"ownerid");
        newPolicy.setName(policyName);
        newPolicy.setDescription(description);
        
        // Get the lowest priority for this new policy
        
        //newPolicy.setPriority(priority);
        
        // Create AccessPolicyElement for the application instance
        AccessPolicyElement policyElement = new AccessPolicyElement(appIdL, false, AccessPolicyElement.ACTION_IF_NOT_APPLICABLE.DISABLE);
        
        // Create DefaultData for parent attributes
        DefaultData defaultData = new DefaultData();
        
        // Add parent attributes if provided
        if (parentAttributes != null && !parentAttributes.isEmpty()) {
            for (Map.Entry<String, String> entry : parentAttributes.entrySet()) {
                Record record = new Record(entry.getKey(), entry.getValue());
                defaultData.getData().add(record);
            }
        }
        
        // Add child records if provided
        if (childRecords != null && !childRecords.isEmpty() && childTableName != null) {
            while (!childRecords.isEmpty()) {
                Map<String, String> combinedChild = new HashMap<>();
                
                // Process multiple attributes for a single child record
                if (childRecords.size() > 1) {
                    // Determine how many attributes belong to this child record
                    // For now, we'll process all remaining records as one combined child
                    // This logic might need adjustment based on your specific use case
                    while (!childRecords.isEmpty()) {
                        Map<String, String> childAttr = childRecords.poll();
                        Map.Entry<String, String> entry = childAttr.entrySet().iterator().next();
                        combinedChild.put(entry.getKey(), entry.getValue());
                    }
                } else {
                    // Single attribute remaining
                    Map<String, String> child = childRecords.poll();
                    Map.Entry<String, String> entry = child.entrySet().iterator().next();
                    combinedChild.put(entry.getKey(), entry.getValue());
                }
                
                defaultData.addChildData(childTableName, combinedChild);
            }
        }
        
        // Set the default data to the policy element
        policyElement.setDefaultData(defaultData);
        
        // Add the policy element to the access policy
        newPolicy.getPolicyElements().add(policyElement);
        
        // Create the policy using the OIM API
        String policyId = oimPolicies.createAccessPolicy(newPolicy);
        
        System.out.println("Created Access Policy: " + policyName + " with ID: " + policyId);
        return policyId;
    }
    
    /**
     * Find application instance by name and return its ID
     * 
     * @param applicationName Name of the application instance
     * @return Application instance ID as string, null if not found
     */
    public String getApplicationInstanceId(String applicationName) {
        try {
            ApplicationInstance ai = aiService.findApplicationInstanceByName(applicationName);
            if (ai != null) {
                Long aikeyL = ai.getApplicationInstanceKey();
                String aiKey = aikeyL.toString();
                return aiKey;
            }
        } catch (Exception e) {
            System.err.println("Error finding application instance: " + applicationName);
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Find application instance by name
     * 
     * @param applicationName Name of the application instance
     * @return ApplicationInstance object, null if not found
     */
    public ApplicationInstance findApplicationInstance(String applicationName) {
        try {
            return aiService.findApplicationInstanceByName(applicationName);
        } catch (Exception e) {
            System.err.println("Error finding application instance: " + applicationName);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Print detailed information about an access policy (for debugging)
     * 
     * @param policyKey Key/ID of the access policy
     */
    public void printPolicyDetails(String policyKey) {
        try {
            AccessPolicy policy = apService.getAccessPolicy(policyKey, true);
            if (policy == null) {
                System.out.println("Policy not found: " + policyKey);
                return;
            }
            
            System.out.println("=== Policy Details ===");
            System.out.println("Policy Name: " + policy.getName());
            System.out.println("Policy Key: " + policyKey);
            
            // Print policy attributes
            Set<String> attrNames = policy.getAttributeNames();
            System.out.println("\n--- Policy Attributes ---");
            for (String attrName : attrNames) {
                System.out.println(attrName + ": " + policy.getAttribute(attrName));
            }
            
            // Print policy elements
            System.out.println("\n--- Policy Elements ---");
            List<AccessPolicyElement> elements = policy.getPolicyElements();
            for (AccessPolicyElement ape : elements) {
                System.out.println("Application Instance ID: " + ape.getApplicationInstanceID());
                System.out.println("Entity Type: " + ape.getEntityType());
                
                DefaultData dd = ape.getDefaultData();
                
                // Print parent attributes
                System.out.println("  Parent Attributes:");
                List<Record> records = dd.getData();
                for (Record rec : records) {
                    System.out.println("    " + rec.getAttribute("attributeName") + 
                                     " = " + rec.getAttribute("attributeValue"));
                }
                
                // Print child records
                System.out.println("  Child Records:");
                List<ChildRecord> childRecords = dd.getChildData();
                for (ChildRecord crec : childRecords) {
                    List<ChildAttribute> crattrs = crec.getAttributes();
                    for (ChildAttribute catr : crattrs) {
                        System.out.println("    Record " + catr.getAttribute("recordNumber") + 
                                         ": " + catr.getAttribute("attributeName") + 
                                         " = " + catr.getAttribute("attributeValue"));
                    }
                }
            }
            System.out.println("======================");
            
        } catch (Exception e) {
            System.err.println("Error printing policy details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
