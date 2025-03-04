/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.zephon.oim.api.tester;

import Thor.API.Operations.tcImportOperationsIntf;
import tech.zephon.oim.api.OIMHelperClient;
import tech.zephon.oim.exceptions.OIMHelperException;
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
import java.util.logging.Level;
import org.junit.Test;

/**
 *
 * @author fforester
 */
public class ClientTesterDMImporter extends OIMHelperClient {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ClientTesterDMImporter.class.getName());
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
                    logger.fine("File:" + file.getAbsolutePath());
                    String content = readFile(file.getAbsolutePath());
                    importFile(file.getAbsolutePath(),content);
                }
                catch(Exception e)
                {
                    logger.log(Level.SEVERE,"APIError:" + e.getMessage());
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
                logger.fine("JI:" + r);
                    
            }
            Collection<RootObject> subs = importOps.listPossibleSubstitutions(justImported);
            logger.fine("SUBS:" + subs);
            for(RootObject r : subs)
            {
                logger.fine(r.getName() + ":" + r.getPhysicalType());
                if (r.getPhysicalType().contains("Version"))
                    importOps.addSubstitution(r, ms);
            }
            
            HashMap mapsubs = importOps.getSubstitutions();
            logger.fine("NEWSUBS:" + mapsubs);
            
            Collection<RootObject> missing = importOps.getMissingDependencies(justImported, "*");
            for(RootObject r:missing)
                logger.fine("MIssing:" + r.getName() + ":" + r.getPhysicalType());
            
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
                        logger.fine("    ERROR:" +  o.getLevel() + ":" +  o.getMessageID() + ":" + o.getMessage() + ":" + o.getAdditionalInfo());
                    }
                }
                //logger.fine("MSGID:" + k + "MSGDESC:" + messages.get(k));
                //logger.fine("CLASS:" + messages.get(k).getClass());
                //logger.fine("CLASS:" + k.getClass());
            }
            
            if (!hasErrors)
            {
                logger.fine("Importing:" + fileName);
                importOps.performImport(justImported);
            }
            else
            {
                logger.fine("Skipping for errors:" + fileName);
            }
        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE,"APIError:" + e.getMessage());
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
            logger.log(Level.SEVERE,"Error", e);
            throw e;
        }
    }
    
}
