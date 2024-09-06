/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.api.tester;

import Thor.API.Operations.tcExportOperationsIntf;
import com.aptecllc.oim.api.OIMDeploymentManager;
import com.aptecllc.oim.api.OIMHelperClient;
import com.aptecllc.oim.exceptions.OIMHelperException;
import com.thortech.xl.vo.ddm.RootObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author foresfr
 */
public class ClientTesterDMExporter extends OIMHelperClient {
    
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private OIMDeploymentManager deployOps;
    private tcExportOperationsIntf exportOps;
    private String baseDir = "C:\\Users\\fforester\\Downloads\\export-fsudev";
    
    private String[] processTypes = {"Process","EventHandler","Resource"};
    
    private String[] categories = {
        "Jar","Plugin","PrepopAdapter","TaskAdapter","eventhandlers","ITResourceDef","ITResource",
        "scheduledTask","EmailDef","AccessPolicy","CustomResourceBundle"
    };
    
    private String[] misccategories = {"UserGroup"};
    private String[] largecategory = {"Lookup"};
    /*
    private String[] categories = {
        "User Metadata"
    };
    */
    
    @Test
    public void mainTest()
    {
        try
        {
            this.authenticate();
            
        }
        catch(Exception e)
        {
            logger.error("APIError",e);
            return;
        }
        
        try
        {
            //this.showCategoryNames();
            //if (1 == 1)
            //    return;
            
        }
        catch(Exception e)
        {
            logger.error("APIError:" + e.getMessage());
        }
        
        
        
        // export user meta data
        logger.info("Export User MetaData");
        this.exportUserMetadata();
        logger.info("Export Process definition and resources");
        this.exportAllProcesses();
        logger.info("Export Process forms");
        this.exportAllProcessForms();
        logger.info("Export Basic categories");
        this.exportBasicCategories(categories);
        logger.info("Export Misc categories");
        this.exportBasicCategories(misccategories);
        logger.info("Export Large categories");
        this.exportBasicCategories(largecategory);
        logger.info("Export Jobs");
        this.exportAllJobs();
        
        
    }
    
    
    public void exportBasicCategories(String[] categories)
    {
        try
        {
            for(String category : categories)
            {
                logger.info("Export category:" + category);
                String dirname = this.baseDir + "/" + category;
                File newdir = new File(dirname);
                newdir.mkdir();
                deployOps.setBaseDirectory(dirname);
                deployOps.exportSingleObjects(category);
                //this.getClient().logout();
            }
        }
        catch(Exception e)
        {
            logger.error("APIError:" + e.getMessage(),e);
            return;
        }
    }
    
    public void exportAllProcesses() 
    {
        List<String> processTypes = Arrays.asList(this.processTypes);
        String dirname = this.baseDir + "/" + "Process";
        deployOps.setBaseDirectory(dirname);
        File newdir = new File(dirname);
        newdir.mkdir();
        List<String> names;
        try {
            names = deployOps.getAllObjectNamesByType("Process");
            for (String name : names) 
            {
                Collection<RootObject> all = deployOps.getFullObjectTree(name, "Process");
                Iterator i = all.iterator();
                while(i.hasNext())
                {
                    RootObject r = (RootObject)i.next();
                    logger.debug("R:" + r.getName() + ":" + r.getPhysicalType());
                    String type = r.getPhysicalType();
                    if (!processTypes.contains(type))
                        i.remove();
                        
                }
                deployOps.exportObjects(all, name);
            }
        } catch (Exception e) 
        {
            logger.error("APIError:" + e.getMessage());
            return;
        }
    }
    
    public void exportAllJobs() 
    {
        String dirname = this.baseDir + "/" + "Job";
        deployOps.setBaseDirectory(dirname);
        File newdir = new File(dirname);
        newdir.mkdir();
        List<String> names;
        try {
            names = deployOps.getAllObjectNamesByType("scheduledTask");
           
            for (String name : names) 
            {
                logger.debug("Jobname:" + name);
                boolean hasTask = false;
                Collection<RootObject> all = new ArrayList();
                Collection<RootObject> roots = deployOps.getFullObjectTree(name,"scheduledTask");
                for(RootObject r : roots)
                {
                    logger.debug("ROOT:" + r.getName() + ":" + r.getPhysicalType());
                    if (!all.contains(r))
                    {
                        all.add(r);
                        if (r.getChilds() != null && r.getChilds().size() > 0)
                        {
                            logger.debug("has children:" + r.getChilds());
                            all.addAll(r.getChilds());

                        }
                        deployOps.exportObjects(all, r.getName());
                        break;
                    }
                }
                /*
                all.addAll(roots);
                Collection<RootObject> children = deployOps.getObjectChildren(roots);
                for(RootObject r : children)
                {
                    if (!all.contains(r))
                        all.add(r);
                }
                children = deployOps.getObjectDependencies(roots);
                for(RootObject r : children)
                {
                    if (!all.contains(r))
                        all.add(r);
                }
                for (RootObject r : all) 
                {
                    logger.debug("R:" + r.getName() + ":" + r.getPhysicalType());
                    if (r.getPhysicalType().equals("Job"))
                        hasTask = true;
                }
                
                if (hasTask)
                {
                    //deployOps.exportObjects(all, name);
                }
                else
                    logger.error("Skipping missing task for job:" + name);
                */
            }
        } catch (Exception e) 
        {
            logger.error("APIError:" + e.getMessage());
            return;
        }
    }
    
    public void exportAllProcessForms()
    {
        // export all process forms
        List<String> names;
        try
        {
            names = deployOps.getAllObjectNamesByType("Process Form");
            names = this.exportProcessFormsNew(names);
            logger.info("Exporting Missed Forms:" + names);
            for(String name : names)
            {
                deployOps.exportByTypeAndName("Process Form", name);
            }
        }
        catch(Exception e)
        {
            logger.error("APIError:" + e.getMessage());
            return;
        }
    }
    public void exportUserMetadata()
    {
        // export user meta data
        try
        {
            String dirname = this.baseDir + "/" + "UserMetadata";
            deployOps.setBaseDirectory(dirname);
            File newdir = new File(dirname);
            newdir.mkdir();
            Collection<RootObject> usermeta = deployOps.getAllObjectsByType("User Metadata");
            Collection<RootObject> usermetaDeps = exportOps.retrieveDependencyTree(usermeta);
            usermetaDeps.addAll(usermeta);
            deployOps.exportObjects(usermetaDeps, "UserMetadata");
        }
        catch(Exception e)
        {
            logger.error("APIError:" + e.getMessage());
            return;
        }
    }
    
    public List<String> exportProcessFormsNew(List<String> names) throws Exception
    {
        List<String> namesCopy = new ArrayList();
        namesCopy.addAll(names);
        String dirname = this.baseDir + "/" + "Process Form";
        deployOps.setBaseDirectory(dirname);
        File newdir = new File(dirname);
        newdir.mkdir();
        
        for(String name : names)
        {
            Collection<RootObject> roots = deployOps.getRootObject("Process Form", name);
            for(RootObject r : roots)
            {
                logger.debug("ROOT:" + r);
            }
            Collection<RootObject> children = deployOps.getObjectChildren(roots);
            
            boolean skip = false;
            for(RootObject r : children)
            {
                logger.debug("CHILD:" + r);
                if (r.getChilds().size() > 0)
                    logger.debug("Has Children:" + r.getName());
                else
                    skip = true;
            }
            
            if (skip)
            {
                logger.debug("Skipping:" + name);
                continue;
            }
            
            Collection<RootObject> allObjects = new ArrayList();
            allObjects.addAll(roots);
            allObjects.addAll(children);
            Collection<RootObject> deps = deployOps.getObjectDependencies(children);
            for(RootObject r : deps)
            {
                logger.debug("DEP:" + r);
                if (!allObjects.contains(r))
                    allObjects.add(r);
            }
            Collection<RootObject> tree = deployOps.getObjectDependencyTree(allObjects);
            Iterator i = tree.iterator();
            while(i.hasNext())
            {
                RootObject r = (RootObject)i.next();
                //logger.debug("TREE:" + r.getName() + ":" + r.getPhysicalType());
                if (!r.getPhysicalType().contains("Form"))
                    i.remove();
            }
            tree.addAll(roots);
            for(RootObject r : tree)
            {
                logger.debug("TREE:" + r.getName() + ":" + r.getPhysicalType());
                namesCopy.remove(r.getName());
            }
            logger.debug("Writing:" + name);
            deployOps.exportObjects(tree, name);
        }
        return namesCopy;
    }
    
    public List<String> exportProcessForms(List<String> names) throws Exception
    {
        List<String> namesCopy = new ArrayList();
        namesCopy.addAll(names);
        String dirname = this.baseDir + "/" + "Process Form";
        deployOps.setBaseDirectory(dirname);
        File newdir = new File(dirname);
        newdir.mkdir();
        
        for(String name : names)
        {
            try
            {
                Collection<RootObject> roots = deployOps.getRootObject("Process Form", name);
                
                for(RootObject ro : roots)
                {
                    logger.debug("Root:" + ro);
                    Collection<RootObject> children = deployOps.getObjectChildren(ro);
                    children = deployOps.removeRoot(children);
                    logger.debug("Children:" + children);
                    if (children.isEmpty())
                    {
                        continue;
                    }
                    for(RootObject child : children)
                    {
                        logger.debug("Removing:" + child.getName());
                        namesCopy.remove(child.getName());
                    }
                    children = deployOps.getObjectChildren(ro);
                    deployOps.exportObjects(children, name);
                    for(RootObject child : children)
                    {
                        logger.debug("Removing:" + child.getName());
                        namesCopy.remove(child.getName());
                    }
                    //Collection<RootObject> deps = deployOps.getObjectDeps(ro);
                    //deps = deployOps.removeRoot(deps);
                    //logger.debug("Deps:" + deps);
                }
            }
            catch(Exception e)
            {
                logger.error("APIError:" + e.getMessage(),e);
                throw e;
            }
        }
        return namesCopy;
    }
    
    
    
    public void showCategoryNames() throws Exception
    {
        try
        {
            Collection<String> cats = exportOps.retrieveCategories();
            logger.debug("cats:" + cats);
            for(String ro : cats)
            {
                System.out.println("Cat:" + ro);
            }
        }
        catch(Exception e)
        {
            logger.error("APIError:" + e.getMessage(),e);
            throw e;
        }
    }
    
    public void authenticate() throws Exception
    {
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();
            deployOps = new OIMDeploymentManager(this.getClient());
            exportOps = this.getClient().getService(tcExportOperationsIntf.class);
            deployOps.setBaseDirectory(this.baseDir);
        } catch (OIMHelperException e) {
            logger.error("Error", e);
            throw e;
        }
    }
}
