/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.zephon.oim.api.tester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import org.junit.Test;
import tech.zephon.oim.api.BaseHelper;
import tech.zephon.oim.api.OIMAccessPolicies;
import tech.zephon.oim.api.OIMHelperClient;

/**
 *
 * @author Frederick.Forester
 */
public class ClientTesterAccessPolicies extends OIMHelperClient {

    private AccessPolicyService apService = null;
    private ApplicationInstanceService aiService = null;
    private OIMAccessPolicies oimPols = null;

    private String fileName = "E:\\Fred\\Downloads\\PI1_APs_ParamVals.csv";
    private OIMClient oimClient;
    
    @Test
    public void mainTest() {
        Map<String,Map<String,String>> policyMap = new HashMap();
        Map<String,Queue<Map<String,String>>> policyChildMap = new HashMap();
        /*
        
        try {
            List<HashMap<String,String>> records = BaseHelper.loadFile(fileName);
            
            for(Map<String,String> record : records)
            {
                //System.out.println("" + record);
                String apName = record.get("AP Name");
                String fldName = record.get("Field Name");
                String fldValue = record.get("Field Value");
                Map recMap = policyMap.get(apName);
                if (recMap == null)
                {
                    recMap = new HashMap();
                }
                recMap.put(fldName, fldValue);
                policyMap.put(apName, recMap);
                        
                
            }
            //System.out.println("polmap:" + policyMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (policyMap.isEmpty())
        {
            return;
        }
        */
        try {
            List<HashMap<String,String>> records = BaseHelper.loadFile(fileName);
            
            
            for(Map<String,String> record : records)
            {
                //System.out.println("" + record);
                String apName = record.get("AP Name");
                String fldName = record.get("Field Name");
                String fldValue = record.get("Field Value");
                
                Queue<Map<String,String>> recQue = policyChildMap.get(apName);
                if (recQue == null)
                {
                    recQue = new LinkedList<>();
                }
                Map childMap = new HashMap();
                childMap.put(fldName, fldValue);
                recQue.offer(childMap);
                policyChildMap.put(apName, recQue);
            }
            System.out.println("policyChildMap:" + policyChildMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        
        try {
            loadConfig(null);
            loginWithCustomEnv();
            //apService = this.getClient().getService(AccessPolicyService.class);
            //aiService = this.getClient().getService(ApplicationInstanceService.class);
            
            
            oimPols = new OIMAccessPolicies(this.getClient());
            apService = this.getClient().getService(AccessPolicyService.class);
            aiService = this.getClient().getService(ApplicationInstanceService.class);
            
            
            Queue<Map<String,String>>  children = new LinkedList();
            if(!policyMap.isEmpty())
            {
                Set<String> mapKeys = policyMap.keySet();
                for(String polName : mapKeys)
                {
                    Map<String,String> polattrs = policyMap.get(polName);
                    System.out.println("POLNAME:" + polName);
                    System.out.println("POLATTR:" + polattrs);
                    try
                    {
                        AccessPolicy pol = oimPols.getAccessPolicy(polName);
                        System.out.println("Found PolKey:" + pol.getEntityId());
                        System.out.println("Found PolName:" + pol.getName());
                        accessPolicyUpdate(pol.getEntityId(), polattrs,children,null);
                    }
                    catch(Exception e)
                    {
                        System.out.println("ERROR:" + e.getMessage());
                        continue;
                    }

                }
            }
            
            if (!policyChildMap.isEmpty())
            {
                Set<String> mapKeys = policyChildMap.keySet();
                for(String polName : mapKeys)
                {
                    if ("EBS Prod - JD-04253 ARM".equalsIgnoreCase(polName))
                    {
                        continue;
                    }
                    System.out.println("Policy:" + polName);
                    AccessPolicy pol = oimPols.getAccessPolicy(polName);
                    //Queue<Map<String,String>> recQue = policyChildMap.get(polName);
                    Queue<Map<String,String>> recQue = new LinkedList();
                    Map map1 = new HashMap();
                    Map map2 = new HashMap();
                    map1.put("UD_PARAMET9_PARAMETER_ID", "533~%E%");
                    map2.put("UD_PARAMET9_PARAMETER_VALUE","ON");
                    recQue.offer(map1);
                    recQue.offer(map2);
                    if (recQue.size() > 0)
                    {
                        System.out.println("Children:" + recQue);
                        accessPolicyUpdate(pol.getEntityId(), null,recQue,"UD_PARAMET9");
                    }
                    
                }
            }
            
            
            
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void accessPolicyUpdate(String polKey,Map<String,String> parents,Queue<Map<String,String>> children,String childName) {
        String appId = null;
        try
        {
            ApplicationInstance ai = aiService.findApplicationInstanceByName("PI1SAPGRC");
            Long appIdL = ai.getApplicationInstanceKey();
            appId = appIdL.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return;
        }
        try {
            
            AccessPolicy apVO2 = apService.getAccessPolicy(polKey, true);
            if (apVO2 == null)
            {
                System.out.println("Invalid Access Policy");
                return;
            }
            //this.printPolicy(apVO2);
            System.out.println("Entering accessPolicyUpdate");
            List<AccessPolicyElement> elements = apVO2.getPolicyElements();
            if (parents != null && !parents.isEmpty())
            {
                Set<String> keys = parents.keySet();
                for(String key : keys)
                {
                    String val = (String)parents.get(key);
                    boolean found = this.updateParentValue(elements,key,val,appId);
                    if (!found)
                    {
                        System.out.println("Not Found:" + key);
                        this.addParentAttribute(elements, key, val, appId);
                    }
                }
            }
            
            System.out.println("Entering Child accessPolicyUpdate");
            System.out.println("Entering Child loop:" + children.size());
            if (children != null && children.size() > 0)
            {
                System.out.println("Entering Child loop:" + children.size());
                Iterator<Map<String,String>> iterator = children.iterator();
                while(iterator.hasNext())
                {
                    Map<String,String> child1 = iterator.next();
                    Map<String,String> child2 = iterator.next();
                    
                    Map.Entry<String,String> ent1 = child1.entrySet().iterator().next();
                    Map.Entry<String,String> ent2 = child2.entrySet().iterator().next();
                    Map<String,String> childMap = new HashMap();
                    childMap.put(ent1.getKey(), ent1.getValue());
                    childMap.put(ent2.getKey(), ent2.getValue());
                    System.out.println("Add child:" + ent1);
                    
                    this.addChildRecord(elements, appId, childName, childMap);
                }
            }
            
           
            /* update default data
            this.updateParentValue(elements,"UD_DS1SAPGR_ROOM_NUMBER", "JD-00999");
            this.updateParentValue(elements,"UD_DS1SAPGR_FUNCTION", "EBS Prod - AMPS Developer JD-00999");
            this.updateParentValue(elements,"UD_DS1SAPGR_DEPARTMENT", "FREEFORM");
            this.updateParentValue(elements,"UD_DS1SAPGR_GROUP_NAME", "84~SUSTAINMENT");
            */
            
            /*
            Map<String, String> childInf = new HashMap<String, String>();
            childInf.put("UD_ROLE2_ROLE_SYSTEM_NAME","84~DS1TRST100");
            childInf.put("UD_ROLE2_ROLE_NAME","84~DS1TRST100~YPS:SCM_SU_AMPS_PRD_SUP_DIS");
            this.addChildRecord(elements, "42", "UD_ROLE2", childInf);
            */
            //apVO2.getPolicyElements().get(0).getDefaultData().addChildData("UD_ROLE2",childInf);
            apService.updateAccessPolicy(apVO2);
            
            //apVO2.setPolicyElements(elements);
            //apService.updateAccessPolicy(apVO2);
            //this.printPolicy(apVO2);
            //System.out.println("the chld attrs are " + apVO2.getPolicyElements().get(0).getDefaultData().getChildAttributes().toString());
            //Map<String, String> childInf = new HashMap<String, String>();
            //String localFormName = "UD_ROLE3";
            //long localRecordNumber = 6L; //<NEXT RECORD NUMBER FOR ACCESS POLICY AS SEEN IN POC TABLE
            //childInf.put("UD_ROLE3_ROLE_NAME","66~CN=Replicator,CN=Builtin,DC=lab,DC=test");
            //ChildRecord cr = new ChildRecord(localFormName, localRecordNumber);
            //cr.addAttributes(childInf);
            //List<ChildRecord> crList = new ArrayList<ChildRecord>();
            //crList.add(cr);
            //apVO2.getPolicyElements().get(0).getDefaultData().setChildData(crList);
            //AccessPolicyElement ape = new AccessPolicyElement(42,false,AccessPolicyElement.ACTION_IF_NOT_APPLICABLE.DISABLE);
            //List<AccessPolicyElement> napes = new ArrayList();
            //napes.add(ape);
            //elements.removeAll(deletes);
            //elements.add(ape);
            //apVO2.setPolicyElements(apVO2.getPolicyElements());
            //apVO2.setPolicyElements(napes);
            //apService.updateAccessPolicy(apVO2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addChildRecord(List<AccessPolicyElement> elements, String appId,String childTable,Map childData)
    {
        Long appIdL = Long.valueOf(appId);
        AccessPolicyElement appPol = null;
        for (AccessPolicyElement ape : elements) {
            if (ape.getApplicationInstanceID() == appIdL) {
                ape.getDefaultData().addChildData(childTable,childData);
            }
        }
    }


    private void removeAppInstance(List<AccessPolicyElement> elements, String appId) {
        Long appIdL = Long.valueOf(appId);
        for (AccessPolicyElement ape : elements) {
            if (ape.getApplicationInstanceID() == appIdL) {
                ape.markForDelete();
            }
        }
    }

    private void removeChildrenList(List<ChildRecord> childRecs) {
        for (ChildRecord childRec : childRecs) {
            List<ChildAttribute> crattrs = childRec.getAttributes();
            for (ChildAttribute catr : crattrs) {
                catr.markForDelete();

            }
        }
    }
    
    public void addParentAttribute(List<AccessPolicyElement> elements,String attributeName,String newValue,String appId)
    {
        Long appIdL = Long.valueOf(appId);
        
        for (AccessPolicyElement ape : elements) {

            System.out.println("AppID:" + ape.getApplicationInstanceID());
            if (ape.getApplicationInstanceID() != appIdL)
            {
                continue;
            }
            Record newRec = new Record(attributeName,newValue);
            
            DefaultData dd = ape.getDefaultData();
            dd.getData().add(newRec);
            ape.setDefaultData(dd);
            
        }
    }
    
    // need the appid here
    public boolean updateParentValue(List<AccessPolicyElement> elements,String attributeName,String newValue,String appId)
    {
        Long appIdL = Long.valueOf(appId);
        boolean found = false;
        for (AccessPolicyElement ape : elements) {
            if(ape.getApplicationInstanceID() != appIdL)
            {
                continue;
            }
            System.out.println("AppID:" + ape.getApplicationInstanceID());
            System.out.println("TYPE:" + ape.getEntityType());
            DefaultData dd = ape.getDefaultData();
            List<Record> newRecs = new ArrayList();
            List<Record> recs = dd.getData();
            for (Record rec : recs) {
                String attrName = (String)rec.getAttribute("attributeName");
                if (!attrName.equalsIgnoreCase(attributeName))
                {
                    newRecs.add(rec);
                    continue;
                }
                else
                {
                    rec.setAttributeValue(newValue);
                    newRecs.add(rec);
                    found = true;
                    break;
                }
            }
            dd.setData(recs);
            ape.setDefaultData(dd);
            
        }
        return found;
    }
    
    
    public void updateChildValue(ChildRecord childRec,String attributeName,String newValue)
    {
        List<ChildAttribute> crattrs = childRec.getAttributes();
        for (ChildAttribute catr : crattrs) {
            String attrName = (String)catr.getAttribute("attributeName");
            if (!attrName.equalsIgnoreCase(attributeName))
            {
                continue;
            }
            catr.setAttributeValue(newValue);
        }
    }
    private void addAppInstance(AccessPolicy apVO2,String appId)
    {
        Long appIdL = Long.valueOf(appId);
        AccessPolicyElement ape = new AccessPolicyElement(appIdL,false,AccessPolicyElement.ACTION_IF_NOT_APPLICABLE.DISABLE);
        List<AccessPolicyElement> napes = new ArrayList();
        napes.add(ape);
        /*
        try
        {
            apService.updateAccessPolicy(apVO2);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        */
    }
    
    private ApplicationInstance findAppInstance(String appName)
    {
        try {
            ApplicationInstance ai = this.aiService.findApplicationInstanceByName(appName);
            return ai;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void printPolicy(String polKey) {
        AccessPolicy apVO2 = null;
        try {
            apVO2 = apService.getAccessPolicy(polKey, true);
            if (apVO2 == null)
            {
                System.out.println("ERROR");
                return;
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Policy Name " + apVO2.getName());
        Set<String> attrNames = apVO2.getAttributeNames();
        for (String attrName : attrNames) {
            System.out.println("Name:" + attrName);
            System.out.println("Val:" + apVO2.getAttribute(attrName));
        }

        System.out.println("Policy Elements");
        List<AccessPolicyElement> elements = apVO2.getPolicyElements();
        for (AccessPolicyElement ape : elements) {

            System.out.println("AppID:" + ape.getApplicationInstanceID());
            System.out.println("TYPE:" + ape.getEntityType());
            DefaultData dd = ape.getDefaultData();

            List<Record> recs = dd.getData();
            for (Record rec : recs) {
                System.out.println("Parent formID:" + rec.getAttribute("formID"));
                System.out.println("Parent Attre:" + rec.getAttribute("attributeName"));
                System.out.println("Parent value:" + rec.getAttribute("attributeValue"));
            }
            List<ChildRecord> crecs = ape.getDefaultData().getChildData();
            for (ChildRecord crec : crecs) {
                List<ChildAttribute> crattrs = crec.getAttributes();
                for (ChildAttribute catr : crattrs) {
                    //System.out.println("FormName:" + catr.getFormName());
                    System.out.print("" + catr.getAttribute("recordNumber") + ") ");
                    System.out.print("FormID:" + catr.getAttribute("formID") + " ");
                    System.out.print("Name:" + catr.getAttribute("attributeName"));
                    System.out.println("=" + catr.getAttribute("attributeValue"));

                }

            }

        }

        System.out.println("the chld attrs are " + apVO2.getPolicyElements().get(0).getDefaultData().getChildAttributes().toString());
        //Map<String, String> childInf = new HashMap<String, String>();
        //String localFormName = "UD_ROLE3";
        //long localRecordNumber = 6L; //<NEXT RECORD NUMBER FOR ACCESS POLICY AS SEEN IN POC TABLE
        //childInf.put("UD_ROLE3_ROLE_NAME","66~CN=Replicator,CN=Builtin,DC=lab,DC=test");
        //ChildRecord cr = new ChildRecord(localFormName, localRecordNumber);
        //cr.addAttributes(childInf);
        //List<ChildRecord> crList = new ArrayList<ChildRecord>();
        //crList.add(cr);
        //apVO2.getPolicyElements().get(0).getDefaultData().setChildData(crList);
        //AccessPolicyElement ape = new AccessPolicyElement(42,false,AccessPolicyElement.ACTION_IF_NOT_APPLICABLE.DISABLE);
        //List<AccessPolicyElement> napes = new ArrayList();
        //napes.add(ape);
        //elements.removeAll(deletes);
        //elements.add(ape);
        //apVO2.setPolicyElements(apVO2.getPolicyElements());
        //apVO2.setPolicyElements(napes);
        //apService.updateAccessPolicy(apVO2);
    }

    

}
