/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.api.tester;

import com.aptecllc.oim.api.OIMHelperClient;
import com.aptecllc.oim.exceptions.OIMHelperException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import oracle.iam.platformservice.api.PlatformService;
import org.apache.log4j.Logger;
import org.junit.Test;


/**
 *
 * @author foresfr
 */
public class ClientTestRegisterPlugin extends OIMHelperClient {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Test
    public void mainTest() {
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();
        } catch (OIMHelperException e) {
            logger.error("Error", e);
            return;
        }

        
        try
        {
            File file = new File("plugin.zip");
            FileInputStream fis = new FileInputStream(file);
            int size = (int) file.length();
            byte[] b = new byte[size];
            int bytesRead = fis.read(b, 0, size);
            while (bytesRead < size) {
                bytesRead += fis.read(b, bytesRead, size - bytesRead);
            }
            fis.close();
            //service.registerPlugin(b);
            //service.unRegisterPlugin(pluginID, version);
        }
        catch(Exception e)
        {
            logger.error("Error registering plugin",e);
        }
    }
    
    
}
