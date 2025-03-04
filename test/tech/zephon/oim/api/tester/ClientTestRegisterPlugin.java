/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.zephon.oim.api.tester;

import tech.zephon.oim.api.OIMHelperClient;
import tech.zephon.oim.exceptions.OIMHelperException;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;


/**
 *
 * @author foresfr
 */
public class ClientTestRegisterPlugin extends OIMHelperClient {

    private static final Logger logger = Logger.getLogger(ClientTestRegisterPlugin.class.getName());

    @Test
    public void mainTest() {
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();
        } catch (OIMHelperException e) {
            logger.log(Level.SEVERE,"Error", e);
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
            logger.log(Level.SEVERE,"Error registering plugin",e);
        }
    }
    
    
}
