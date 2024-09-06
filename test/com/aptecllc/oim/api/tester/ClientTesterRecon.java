/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api.tester;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.tcResultSet;
import com.aptecllc.oim.api.OIMForms;
import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.api.OIMHelperClient;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oracle.iam.reconciliation.api.ReconOperationsService;
import org.apache.log4j.Logger;
import com.aptecllc.oim.csv.CsvReader;
import java.util.Set;
import org.junit.Test;

/**
 *
 */
public class ClientTesterRecon extends OIMHelperClient {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private String defaultConfigFile = "jndi.properties";

    private String reconFileName = "/netbeans11g/OIMHelper/testrecon.csv";

    private static final String IDFIELD = "UDCIdentifier";

    List<HashMap> recordMapList;
    private String trustedResource = "MSSQLFIMGHRDBTRUSTED_GTC";
    private String targetResource = "FinApp Resource";
    private ReconOperationsService reconOp;
    private OIMForms oimForms;
    
    


    @Test
    public void mainTest() {

        //ClientTesterRecon testRecon = new ClientTesterRecon();

        try {
     
            initClient();
            
            //loadFile(reconFileName);
            //makeMoreUsers(100);
            //logger.debug(recordMapList);
            //runRecon();
            recordMapList = new ArrayList<HashMap>();
            HashMap map = new HashMap();
            map.put("FIRSTNAME","Berhard");
            map.put("LASTNAME","McFarlin");
            map.put("USERID","BM10058");
            map.put("EMAIL","BM10058@fred.com");
            map.put("STATUS", "Enabled");
            map.put("REQUIRED","Y");
            map.put("ITRESOURCE","FinApp ITResource");
            
            recordMapList.add(map);
            runRecon();
            //runDeleteRecon();
            //processEvent(41L);
            //deletetionDetection(targetResource,recordMapList);
        }
        catch(Exception e)
        {
            logger.error("Init failed " + e.getMessage(),e);
            return;
        }
    }

    private void initClient() throws Exception
    {
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();
            reconOp = getClient().getService(ReconOperationsService.class);
            oimForms = new OIMForms(getClient());
        } catch (OIMHelperException e) {
            logger.error("Error", e);
            throw e;
        }
    }

    private void loadFile(String filename) throws Exception
    {
        if (filename != null && filename.length() > 0)
            reconFileName = filename;
        
        CsvReader reader = null;
        try
        {
            reader = new CsvReader(reconFileName);
            reader.readHeaders();
            String[] fileHeaders = reader.getHeaders();

            if (fileHeaders == null || fileHeaders.length == 0)
            {
                logger.error("No Header Record");
                throw new Exception("No Header Record");
            }

            
            recordMapList = new ArrayList<HashMap>();
            while (reader.readRecord())
            {
                HashMap recordMap = new HashMap();
                for(int i=0;i<fileHeaders.length;i++)
                {
                    String x = reader.get(fileHeaders[i]);
                    recordMap.put(fileHeaders[i],x);
                }
                UUID key = UUID.randomUUID();
                String guid = key.toString().toUpperCase();
                guid = guid.replaceAll("-", "");
                //recordMap.put(IDFIELD, guid);
                logger.debug("Record Created " + recordMap);
                recordMapList.add(recordMap);

            }
        }
        catch(FileNotFoundException fnfe)
        {
            logger.error("File Not Found");
            throw new Exception("File Not Found");
        }
        catch(IOException ioe)
        {
            logger.error("File IO Error " + ioe.getMessage());
            throw new Exception("File IO Error " + ioe.getMessage());
        }
        finally
        {
            if (reader != null)
                reader.close();
        }
    }

    private void makeMoreUsers(int num)
    {
        HashMap rec = recordMapList.get(0);
        String uid = (String)rec.get("GPN");
        
        for(int i=0;i<num;i++)
        {
            HashMap newrec = new HashMap();
            newrec.putAll(rec);
            String userid = uid + i;
            newrec.put("GPN",userid);
            recordMapList.add(newrec);
        }
    }
    
    private void runRecon()
    {
        long eventKey = 0l;
       
        for(Map recordMap : recordMapList)
        {
            logger.debug(recordMap);
            try {
                eventKey = reconOp.createReconciliationEvent(targetResource, recordMap, true);
                //reconOp.finishReconciliationEvent(eventKey);
                reconOp.processReconciliationEvent(eventKey);
                logger.debug("Recon Complete " + eventKey);
            }
            //catch (tcEventNotFoundException ex) {
            //    logger.error("Recon Exception tcEventNotFoundException", ex);
            //} catch (tcEventDataReceivedException ex) {
            //    logger.error("Recon Exception tcEventDataReceivedException", ex);
            //}
            catch (tcAPIException ex) {
                logger.error("Recon Exception tcAPIException", ex);
            }
        }
    }
    
    private void runDeleteRecon()
    {
        long eventKey = 0l;
        
        
        for(Map recordMap : recordMapList)
        {
            logger.debug(recordMap);
            try {
                eventKey = reconOp.createDeleteReconciliationEvent(targetResource, recordMap);
                //eventKey = reconOp.createReconciliationEvent(targetResource, recordMap, true);
                //reconOp.finishReconciliationEvent(eventKey);
                reconOp.processReconciliationEvent(eventKey);
                logger.debug("Recon Complete " + eventKey);
            }
            //catch (tcEventNotFoundException ex) {
            //    logger.error("Recon Exception tcEventNotFoundException", ex);
            //} catch (tcEventDataReceivedException ex) {
            //    logger.error("Recon Exception tcEventDataReceivedException", ex);
            //}
            catch (Exception ex) {
                logger.error("Recon Exception tcAPIException", ex);
            }
        }
    }
    
    public void deletetionDetection(String resName,List<HashMap> records)
    {
        HashMap[] maps = new HashMap[records.size()];
        records.toArray(maps);
        logger.debug("Recon Start:" + records);
        
        try {
            // returns orc keys of matching accounts
            Set<Object> items = reconOp.provideDeletionDetectionData(resName,maps);
            for(Object o : items)
            {
                Map matched = oimForms.getProcessFormValues(Long.parseLong(o.toString()));
                logger.debug("Matched:" + matched);
            }
            
            tcResultSet missing = reconOp.getMissingAccounts(resName, items);
            //printResultSet(missing);
            for(int i=0;i<missing.getRowCount();i++)
            {
                missing.goToRow(i);
                long pik = missing.getLongValue("Process Instance.Key");
                Map rec = oimForms.getProcessFormValues(pik);
                logger.debug("Deleting:" + rec);
                
            }
            logger.debug("Recon Complete ");
        } catch (Exception ex) {
            logger.error("Recon Exception tcAPIException", ex);
        }
    }
    public void processEvent(long eventKey) {
        try {
            reconOp.processReconciliationEvent(eventKey);
            logger.debug("Recon Complete " + eventKey);
        } catch (Exception ex) {
            logger.error("Recon Exception tcAPIException", ex);
        }
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
                    logger.debug(name + ":" + rs.getStringValue(name));
                }
                
            }
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
        }
    }

}