/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api.tester;

import Thor.API.Operations.tcFormDefinitionOperationsIntf;
import Thor.API.Operations.tcFormInstanceOperationsIntf;
import Thor.API.tcResultSet;
import com.aptecllc.oim.api.OIMForms;
import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.api.OIMHelperClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.junit.Test;


/**
 *
 */
public class ClientTesterForms extends OIMHelperClient {

    private static tcFormDefinitionOperationsIntf formDefOps;
    private static tcFormInstanceOperationsIntf formInstanceOps;
    private Logger logger = Logger.getLogger(ClientTesterForms.class.getName());

    @Test
    public void mainTest() {

        //ClientTesterForms testform = new ClientTesterForms();

        try
        {
            loadConfig(null);
            loginWithCustomEnv();

            formDefOps = getClient().getService(Thor.API.Operations.tcFormDefinitionOperationsIntf.class);
            formInstanceOps = getClient().getService(Thor.API.Operations.tcFormInstanceOperationsIntf.class);

            logger.debug("update form");
           
            
            /*
            Map map = new HashMap();
            
            map.put("UD_EYUSER_USERPRINCIPALNAME","FREDFORESTER@FREDFORESTER.COM");
            //updateProcessForm(42167L, map);
            Map rec = showProcessForm(42167L);
            rec.put("UD_EYUSER_USERPRINCIPALNAME","FREDFORESTER@FREDFORESTER.COM");
            rec.put("UD_EYUSER_FULLNAME","Freddy Woods");
            updateProcessForm(42167L, rec);
            */
            //String term = "23~OU=Terminated,OU=ATT,OU=OIM,OU=EYSplObjects,OU=EY,DC=eydev,DC=net";
            //String active = "23~OU=Users,OU=ATT,OU=OIM,OU=EYSplObjects,OU=EY,DC=eydev,DC=net";
            OIMForms oimForms = new OIMForms(formDefOps, formInstanceOps);
            long pik = 6020L;
            //oimForms.setProcessFormValue(pik, "UD_PAUSER_LOCKED", "1");
            
            prePopTest(pik);
            

        }
        catch (OIMHelperException e)
        {
            logger.error("Error", e);
        }
    }
    
    public void prePopTest(long pik)
    {
        ClientTesterForms testform = new ClientTesterForms();

        try
        {
            testform.loadConfig(null);
            testform.loginWithCustomEnv();

            formDefOps = getClient().getService(Thor.API.Operations.tcFormDefinitionOperationsIntf.class);
            formInstanceOps = getClient().getService(Thor.API.Operations.tcFormInstanceOperationsIntf.class);

            Map current = showProcessForm(pik);
            Map form = getPrePoppedForm(pik,false);
            
            if (form == null)
            {
                logger.error("No Form");
                return;
            }
            logger.debug("PrePopped:" + form);
            Set<String> keys = form.keySet();
            for(String k : keys)
            {
                String val = (String)form.get(k);
                if (val != null && val.trim().length() > 0)
                {
                    current.put(k,val);
                }
            }
            //updateProcessForm(pik, current);
            logger.debug("update form");
          

        }
        catch (OIMHelperException e)
        {
            testform.logger.error("Error", e);
        }
    }
    public Map showProcessForm(long key) {
        OIMForms oimForms = new OIMForms(formDefOps, formInstanceOps);
        try {
            Map rec = oimForms.getProcessFormValues(key);
            logger.info("Parent Rec " + rec);
            return rec;
        } catch (Exception e) {
            logger.error("Error", e);
            return null;
        }

    }
    
    public void showProcessFormChild(long key,String childTable)
    {
        OIMForms oimForms = new OIMForms(formDefOps,formInstanceOps);
        try {
                Map[] recs = oimForms.getProcessFormChildValues(key, childTable, false, null);

                if (recs != null && recs.length > 0) {
                    for (int i = 0; i < recs.length; i++) {
                        logger.info("ChildRec " + recs[i]);
                    }
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
    }
    
    public void updateProcessForm(long key,Map values)
    {
        OIMForms oimForms = new OIMForms(formDefOps, formInstanceOps);
        try {
            oimForms.setProcessFormValues(key, values);
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }
    
    public Map getPrePoppedForm(long key, boolean combine)
    {
        OIMForms oimForms = new OIMForms(formDefOps, formInstanceOps);
        try {
            Map data = new HashMap();
            
            Map rec = oimForms.getPrepopData(key,"yyyy-mm-dd hh:mm:ss",combine);
            logger.info("Prepop Rec " + rec);
            return rec;
        } catch (Exception e) {
            logger.error("Error", e);
            return null;
        }
    }

}
