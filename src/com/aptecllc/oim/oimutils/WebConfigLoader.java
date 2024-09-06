/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.oimutils;

import com.aptecllc.oim.exceptions.OIMHelperException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 */
public class WebConfigLoader {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private String propertiesFile;
    private Properties configProps;

    public Properties getConfigProps() {
        return configProps;
    }
    
    public boolean getConfig(String fileName) throws OIMHelperException {

        String propertiesPath = "";

        if (StringUtils.isEmpty(fileName))
        {
            throw new OIMHelperException("Invalid Properties File Name");
        }

        propertiesFile = fileName;

        // look on classpath
        logger.debug("search Classpath");
        InputStream configStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);
        if (configStream != null) {
            loadConfiguration(configStream);
            if (configProps != null)
                return true;
            logger.error("File not in classpath");
        }
        
        //String home = System.getProperty("oimhome");
        //if (StringUtils.isEmpty(home))
        //{
        //    throw new OIMHelperException("Error: oimhome System Property is Missing");
        //}

        //home = home + File.separator + "config";
        //propertiesPath = home + File.separator + propertiesFile;

        //if (StringUtils.isEmpty(propertiesPath))
        //    propertiesPath = propertiesFile;
        
        //logger.debug("Properties file " + propertiesPath);

        

        try
        {
            // go to the file system
            logger.debug("search filesys");
            configStream = new ByteArrayInputStream(getBytesFromFile(propertiesFile));
        }
        catch(IOException e)
        {
            logger.error("Error getting config File bytes");
            return false;
        }
        loadConfiguration(configStream);
        if (configProps != null)
                return true;
        logger.error("Error Loading config bytes");
        throw new OIMHelperException("Properties File Not Found " + propertiesPath);
    }

    private static byte[] getBytesFromFile(String FilePath)
            throws IOException {
        File file = new File(FilePath);
        InputStream is = new FileInputStream(file);

        long length = file.length();

        if (length > 2147483647L);
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;

        while ((offset < bytes.length) && ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

    private boolean loadConfiguration(InputStream configStream) {
        configProps = new Properties();

        try {
            configProps.load(configStream);
        } catch (IOException e) {
            configProps = null;
            logger.error("Error Loading Config");
            return false;
        }

        return true;
    }



}
