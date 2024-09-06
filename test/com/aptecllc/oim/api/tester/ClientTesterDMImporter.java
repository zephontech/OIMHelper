/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.api.tester;

import Thor.API.Operations.tcImportOperationsIntf;
import com.aptecllc.oim.api.OIMHelperClient;
import com.aptecllc.oim.exceptions.OIMHelperException;
import com.thortech.xl.vo.ddm.ImportPlanInfo;
import com.thortech.xl.vo.ddm.RootObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author fforester
 */
public class ClientTesterDMImporter extends OIMHelperClient {
    
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private tcImportOperationsIntf importOps;
    private String baseDir = "/Users/fforester/Downloads/export-fsudev";
    
    private String[] categories = {
        //"Lookup",
        //"CustomResourceBundle",
        //"Jar",
        //"Plugin",
        //"eventhandlers",
        //"ITResourceDef",
        //"ITResource",
        //"EmailDef",
        //"PrepopAdapter",
        //"TaskAdapter",
        // manual import of user meta data
        //"scheduledTask",
        //"Job",
        //"Resource", -- not needed. resource is in the process xml
        //"Process Form",
        "Process",
        //"UserGroup",
        //"AccessPolicy"
    };
    
    private String[] misccategories = {"CustomResourceBundle","eventhandlers"};
    
    
    @Test
    public void mainTest()
    {
        
        try
        {
            this.authenticate();
        }
        catch(Exception e)
        {
            return;
        }

        for(String category : this.categories)
        {
            String catDir = this.baseDir + "/" + category;
            File folder = new File(catDir);
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (!file.isFile()) {
                    continue;
                }
                try
                {
                    logger.debug("File:" + file.getAbsolutePath());
                    String content = readFile(file.getAbsolutePath());
                    importFile(file.getAbsolutePath(),content);
                }
                catch(Exception e)
                {
                    logger.error("APIError:" + e.getMessage());
                    //break;
                }
            }
        }


    }
    
    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                //stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }
    
    public void importFile(String fileName,String contents) throws Exception
    {
        
        try
        {
            Long msl = System.currentTimeMillis();
            String ms = msl.toString();
            Collection<RootObject> justImported = importOps.addXMLFile(fileName, contents);
            for(RootObject r : justImported)
            {
                logger.debug("JI:" + r);
                    
            }
            Collection<RootObject> subs = importOps.listPossibleSubstitutions(justImported);
            logger.debug("SUBS:" + subs);
            for(RootObject r : subs)
            {
                logger.debug(r.getName() + ":" + r.getPhysicalType());
                if (r.getPhysicalType().contains("Version"))
                    importOps.addSubstitution(r, ms);
            }
            
            HashMap mapsubs = importOps.getSubstitutions();
            logger.debug("NEWSUBS:" + mapsubs);
            
            Collection<RootObject> missing = importOps.getMissingDependencies(justImported, "*");
            for(RootObject r:missing)
                logger.debug("MIssing:" + r.getName() + ":" + r.getPhysicalType());
            
            HashMap messages = importOps.getImportMessages(justImported);
            Set keys = messages.keySet();
            boolean hasErrors = false;
            for(Object k : keys)
            {
                HashSet hs = (HashSet)messages.get(k);
                Iterator i = hs.iterator();
                while(i.hasNext())
                {
                    ImportPlanInfo o = (ImportPlanInfo)i.next();
                    if (o.getLevel() > 0 && !o.getMessageID().contains("RECENTTARGET"))
                    {
                        hasErrors = true;
                        logger.debug("    ERROR:" +  o.getLevel() + ":" +  o.getMessageID() + ":" + o.getMessage() + ":" + o.getAdditionalInfo());
                    }
                }
                //logger.debug("MSGID:" + k + "MSGDESC:" + messages.get(k));
                //logger.debug("CLASS:" + messages.get(k).getClass());
                //logger.debug("CLASS:" + k.getClass());
            }
            
            if (!hasErrors)
            {
                logger.debug("Importing:" + fileName);
                importOps.performImport(justImported);
            }
            else
            {
                logger.debug("Skipping for errors:" + fileName);
            }
        }
        catch(Exception e)
        {
            logger.error("APIError:" + e.getMessage());
            throw e;
        }
    }
    
    public void authenticate() throws Exception
    {
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();
            importOps = this.getClient().getService(tcImportOperationsIntf.class);
            importOps.acquireLock(true);
        } catch (OIMHelperException e) {
            logger.error("Error", e);
            throw e;
        }
    }
    
}
