/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.api;

import Thor.API.Operations.tcExportOperationsIntf;
import com.aptecllc.oim.exceptions.OIMHelperException;
import com.thortech.xl.vo.ddm.RootObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;
import org.apache.log4j.Logger;

/**
 *
 * @author foresfr
 */
public class OIMDeploymentManager extends BaseHelper{
    
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private tcExportOperationsIntf exportOps;
    private String baseDirectory;

    
    
    public OIMDeploymentManager() throws OIMHelperException {
        exportOps = Platform.getService(tcExportOperationsIntf.class);
    }

    public OIMDeploymentManager(OIMClient client) throws OIMHelperException {
        exportOps = client.getService(tcExportOperationsIntf.class);
    }
    
    public OIMDeploymentManager(tcExportOperationsIntf exportOps) throws OIMHelperException {
        this.exportOps = exportOps;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }
    
    
    public Collection<RootObject> getAllObjectsByType(String type) throws OIMHelperException
    {
        Set<RootObject> exportSet = new HashSet<RootObject>();
        
        try {
            exportSet.addAll(exportOps.findObjects(type, "*"));
            //res = exportOp.retrieveChildren(res);
            logger.debug("Objects:" + exportSet.size());
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
        return exportSet;
    }
    
    public List<String> getAllObjectNamesByType(String type) throws OIMHelperException
    {
        Set<RootObject> exportSet = new HashSet<RootObject>();
        List<String> names = new ArrayList();
        
        try {
            exportSet.addAll(exportOps.findObjects(type, "*"));
            //res = exportOp.retrieveChildren(res);
            logger.debug("Objects:" + exportSet.size());
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
        
        for(RootObject r : exportSet)
        {
            logger.debug(r.getName());
            try {
                names.add(r.getName());
            } catch (Exception e) {
                logger.error("APIError:" + e.getMessage());
                throw new OIMHelperException(e);
            }
        }
        return names;
    }
    
    public Collection<RootObject> getRootObject(String type,String name) throws OIMHelperException
    {
        Collection<RootObject> roots;
        try {
            roots = exportOps.findObjects(type, name);
            logger.debug("Objects:" + roots.size());
            return roots;
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
    }
    
    public Collection<RootObject> getObjectChildren(RootObject rootObject) throws OIMHelperException
    {
        Collection<RootObject> roots = new ArrayList();
        roots.add(rootObject);
        return getObjectChildren(roots);
    }
    
    
    public Collection<RootObject> getObjectChildren(Collection<RootObject> rootObjects) throws OIMHelperException
    {
        try {
            Collection<RootObject> children = exportOps.retrieveChildren(rootObjects);
            logger.debug("Objects:" + children.size());
            return children;
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
    }
    
    public Collection<RootObject> getObjectDependencies(Collection<RootObject> rootObjects) throws OIMHelperException
    {
        try {
            Collection<RootObject> deps = exportOps.getDependencies(rootObjects);
            logger.debug("DEPS:" + deps.size());
            return deps;
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
    }
    
    public Collection<RootObject> getObjectDependencies(RootObject rootObject) throws OIMHelperException
    {
        Collection<RootObject> roots = new ArrayList();
        roots.add(rootObject);
        return getObjectDependencies(roots);
    }
    
    public Collection<RootObject> getObjectDependencyTree(RootObject rootObject) throws OIMHelperException
    {
        Collection<RootObject> roots = new ArrayList();
        roots.add(rootObject);
        try {
            roots = exportOps.retrieveDependencyTree(roots);
            logger.debug("Objects:" + roots.size());
            return roots;
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
    }
    
    public Collection<RootObject> getObjectDependencyTree(Collection<RootObject> rootObjects) throws OIMHelperException
    {
        logger.debug("enter");
        Collection<RootObject> objects = new ArrayList();
        try {
            if (rootObjects == null)
                logger.debug("Ops is null");
            Collection<RootObject> tree = exportOps.retrieveDependencyTree(rootObjects);
            if (tree == null)
                return objects;
            logger.debug("Objects:" + tree.size());
            return tree;
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
    }
    
    public Collection<RootObject> getFullObjectTree(String name,String type) throws OIMHelperException
    {
        try
        {
            Collection<RootObject> roots = getRootObject(type, name);
            Collection<RootObject> children = getObjectChildren(roots);
            Collection<RootObject> allObjects = new ArrayList();
            //allObjects.addAll(roots);
            allObjects.addAll(children);
            Collection<RootObject> deps = getObjectDependencies(children);
            for(RootObject r : deps)
            {
                if (!allObjects.contains(r))
                    allObjects.add(r);
            }
            Collection<RootObject> tree = getObjectDependencyTree(allObjects);
            for(RootObject r : tree)
            {
                if (!allObjects.contains(r))
                    allObjects.add(r);
            }
            //tree.addAll(roots);
            return allObjects;
            
        }
        catch(Exception e)
        {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
    }
    
    public Collection<RootObject> removeRoot(Collection<RootObject> objects)
    {
        Set<RootObject> exportSet = new HashSet<RootObject>();
        for(RootObject ro : objects)
        {
            if (ro.getChilds() != null)
                exportSet.addAll(ro.getChilds());
                //return ro.getChilds();
        }
        return exportSet;
    }
    
    public void exportByType(String type) throws OIMHelperException
    {

        Set<RootObject> exportSet = new HashSet<RootObject>();
        Set<RootObject> exportSingle = new HashSet<RootObject>();
        
        try {
            exportSet.addAll(exportOps.findObjects(type, "*"));
            //res = exportOp.retrieveChildren(res);
            logger.debug("Objects:" + exportSet.size());
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
        
        for(RootObject r : exportSet)
        {
            logger.debug(r.getName());
            try {
                exportSingle.addAll(exportOps.findObjects(type, r.getName()));
                String xml = exportOps.getExportXML(exportSingle, r.getName());
                writeXMLUTF8(xml, r.getName());
                exportSingle.clear();
            } catch (Exception e) {
                logger.error("APIError:" + e.getMessage());
                throw new OIMHelperException(e);
            }
        }

        try {
            String xml = exportOps.getExportXML(exportSet, "All-" + type);
            writeXMLUTF8(xml, "All " + type);
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
    }
    
    public void exportByTypeAndName(String type,String name) throws OIMHelperException
    {
        
        Set<RootObject> exportSet = new HashSet<RootObject>();

        
        try {
            exportSet.addAll(exportOps.findObjects(type, name));
            logger.debug("Objects:" + exportSet.size());
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
       
        if (exportSet.isEmpty())
        {
            logger.info("No Export Data found for:" + type + ":" + name);
            return;
        }
        try {
            String xml = exportOps.getExportXML(exportSet, name);
            writeXMLUTF8(xml, name);
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
    }
    
    public void exportObjects(Collection<RootObject> objects,String desc) throws OIMHelperException
    {
        if (objects.isEmpty())
        {
            logger.info("No Export Data found for:" + desc);
            return;
        }
        try {
            String xml = exportOps.getExportXML(objects, desc);
            writeXMLUTF8(xml, desc);
        } catch (Exception e) {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
    }
    
    public void exportSingleObjects(String type) throws OIMHelperException
    {
        try
        {
            List<String> names = getAllObjectNamesByType(type);
            for(String name : names)
            {
                exportByTypeAndName(type, name);
            }
        }
        catch(Exception e)
        {
            logger.error("APIError:" + e.getMessage());
            throw new OIMHelperException(e);
        }
        
    }
    
    
    public void writeXML(String xml,String name) throws OIMHelperException
    {
        String fileName = null;
        try
        {
            fileName = cleanName(name);
            fileName = fileName + ".xml";
            if (this.baseDirectory != null)
            {
                fileName = this.baseDirectory + "/" + fileName;
            }
            logger.debug("Writing:" + fileName);
            File file = new File(fileName);
            if (!file.exists())
                file.createNewFile();
            else
            {
                file.delete();
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(),false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(xml);
            bw.close();
        }
        catch(Exception e)
        {
            logger.error("Failed to Write File:" + fileName);
            throw new OIMHelperException(e);
        }
    }
    
    public void writeXMLUTF8(String xml,String name) throws OIMHelperException
    {
        String fileName = null;
        try
        {
            fileName = cleanName(name);
            fileName = fileName + ".xml";
            if (this.baseDirectory != null)
            {
                fileName = this.baseDirectory + "/" + fileName;
            }
            logger.debug("Writing:" + fileName);
            Path filePath = Paths.get(fileName);
            BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
            writer.write(xml);
            writer.close();
        }
        catch(Exception e)
        {
            logger.error("Failed to Write File:" + fileName);
            throw new OIMHelperException(e);
        }
    }
    
    
    private String cleanName(String name)
    {
        if (name == null)
            return null;
        
        String fileName = name.replace(" ", "-");
        fileName = fileName.replace("/", "-");
        fileName = fileName.replace(":", "");
        fileName = fileName.replace(",", "");
        fileName = fileName.replaceAll("\\p{Cntrl}","");
        return fileName;
    }
}
