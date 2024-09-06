/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api;

import com.aptecllc.oim.exceptions.OIMHelperException;
import Thor.API.Security.XLClientSecurityAssociation;
import com.aptecllc.oim.oimutils.WebConfigLoader;
import com.thortech.xl.client.dataobj.tcDataBaseClient;
import com.thortech.xl.dataaccess.tcDataProvider;
import com.thortech.xl.dataaccess.tcDataSet;
import com.thortech.xl.dataaccess.tcDataSetException;
import java.util.Hashtable;
import java.util.Properties;
import javax.security.auth.login.LoginException;
import oracle.iam.platform.OIMClient;
import org.apache.log4j.Logger;

/**
 * Used as a helper for making remote OIM Connections
 */
public class OIMHelperClient {

    private OIMClient client;
    private Logger logger = Logger.getLogger(OIMHelperClient.class.getName());
    private String OIMUserName;
    private String OIMPassword;
    private String OIMURL;
    private String OIMInitialContextFactory;
    private String defaultConfigFile="jndi.properties";
    
    // sys props
    private String xlHomeDir;
    private String xlAuthLogin;

    private tcDataProvider dataBase;
    
    private boolean connected;


    /**
     * authenticate based on the values set via the setters
     * OIMUserName
     * OIMPassword
     * OIMURL
     * OIMInitialContextFactory
     * @throws OIMHelperException 
     */
    public void loginWithCustomEnv()  {

        logger.debug("Creating client....");

        if (!validate())
            throw new RuntimeException("Invalid Connection Args");

        Hashtable env = new Hashtable();

        
        env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL,OIMInitialContextFactory);
        env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, OIMURL);
        System.setProperty("XL.HomeDir", xlHomeDir);
        System.setProperty("java.security.auth.login.config",xlAuthLogin);
        System.setProperty("weblogic.MaxMessageSize", "50000000");
        String type = System.getenv("APPSERVER_TYPE");
        System.setProperty("APPSERVER_TYPE", "wls");
        logger.debug("APPSERVER_TYPE:" + type);
        if (type == null)
            logger.info("Expect the JRF error unless you add APPSERVER_TYPE=wls to the OS env");
       
        try {
            client = new OIMClient(env);
            logger.debug("Logging in");
            client.login(OIMUserName, OIMPassword.toCharArray());
            XLClientSecurityAssociation.setClientHandle(client);
        } catch (LoginException ex) {
            logger.error("LoginException",ex);
            throw new RuntimeException("LoginException",ex);
        }
        connected=true;
        logger.debug("Log in successful");

    }

    /**
     * connect via an Remote Manager Client.
     * OIMInitialContextFactory
     * OIMURL
     * OIMUserName
     * OIMPassword
     * @throws OIMHelperException 
     */
    public void RMLogin() throws OIMHelperException
    {
        logger.debug("Creating RM client....");
        
        Hashtable env = new Hashtable();
        env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL,OIMInitialContextFactory);
        env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, OIMURL);
        client = new OIMClient(env);
        logger.debug("Logging in");
        try {
            client.login(OIMUserName, OIMPassword.toCharArray());
            XLClientSecurityAssociation.setClientHandle(client);
        } catch (LoginException ex) {
            logger.error("LoginException",ex);
            throw new OIMHelperException("LoginException",ex);
        }
        connected=true;
        logger.debug("Log in successful");
        
    }
    
    /**
     * use a config file other than jndi.properties
     * @param fileName
     * @throws OIMHelperException 
     */
    public void loadConfig(String fileName) throws OIMHelperException
    {
        if (fileName == null || fileName.trim().length() == 0)
            fileName = defaultConfigFile;

        WebConfigLoader configLoader = new WebConfigLoader();
        try
        {
            configLoader.getConfig(fileName);
        }
        catch(OIMHelperException ex)
        {
            logger.error("OIMHelperException",ex);
            return;
        }

        Properties props = configLoader.getConfigProps();
        logger.debug(props);
        setOIMInitialContextFactory(props.getProperty("java.naming.factory.initial"));
        setOIMPassword(props.getProperty("java.naming.security.credentials"));
        setOIMURL(props.getProperty("java.naming.provider.url"));
        setOIMUserName(props.getProperty("java.naming.security.principal"));
        setXlHomeDir(props.getProperty("xl.HomeDir"));
        setXlAuthLogin(props.getProperty("java.security.auth.login.config"));

    }
    
    /**
     * you should call logout when done to free up the internal DB connection
     * 
     */
    public void logout()
    {
        logger.debug("Closing Connection");
        
        if (client != null && connected)
        {
            try
            {
                if (dataBase != null && dataBase.isOpen())
                    dataBase.close();
            }
            catch(Exception e)
            {
            }
            client.logout();
            connected=false;
        }
    }

    /**
     * 
     * @param OIMInitialContextFactory 
     */
    public void setOIMInitialContextFactory(String OIMInitialContextFactory) {
        this.OIMInitialContextFactory = OIMInitialContextFactory;
    }

    /**
     * 
     * @param OIMPassword 
     */
    public void setOIMPassword(String OIMPassword) {
        this.OIMPassword = OIMPassword;
    }

    /**
     * 
     * @param OIMURL 
     */
    public void setOIMURL(String OIMURL) {
        this.OIMURL = OIMURL;
    }

    /**
     * 
     * @param OIMUserName 
     */
    public void setOIMUserName(String OIMUserName) {
        this.OIMUserName = OIMUserName;
    }

    /**
     * 
     * @return 
     */
    public OIMClient getClient() {
        return client;
    }

    /**
     * 
     * @return 
     */
    public tcDataProvider getDataBase() {
        if (dataBase == null)
        {
            dataBase = new tcDataBaseClient();
        }
        return dataBase;
    }

    /**
     * 
     * @param xlHomeDir 
     */
    public void setXlHomeDir(String xlHomeDir) {
        this.xlHomeDir = xlHomeDir;
    }

    /**
     * 
     * @param xlAuthLogin 
     */
    public void setXlAuthLogin(String xlAuthLogin) {
        this.xlAuthLogin = xlAuthLogin;
    }

    private boolean validate()
    {
        if (OIMPassword == null || OIMPassword.trim().length() == 0)
            return false;
        if (OIMUserName == null || OIMUserName.trim().length() == 0)
            return false;
        if (OIMURL == null || OIMURL.trim().length() == 0)
            return false;
        if (OIMInitialContextFactory == null || OIMInitialContextFactory.trim().length() == 0)
            return false;
        if (xlHomeDir == null || xlHomeDir.trim().length() == 0)
            return false;
        if (xlAuthLogin == null || xlAuthLogin.trim().length() == 0)
            return false;
        return true;
    }

    /**
     * 
     * @param usrKey
     * @return
     * @throws OIMHelperException 
     */
    public String getOimUserPassword(String usrKey) throws OIMHelperException
    {
        tcDataSet tmpDataSet = new tcDataSet();
        String sql = "select usr_password from usr where usr_key=<key>";
        sql = sql.replace("<key>",usrKey);
        tmpDataSet.setQuery(getDataBase(),sql);
        try
        {
            tmpDataSet.executeQuery();
            String pw = tmpDataSet.getString("usr_password");
            return pw;
        }
        catch(tcDataSetException e)
        {
            logger.error("tcDataSetException",e);
            throw new OIMHelperException("tcDataSetException:" + e.getMessage(),e);
        }
        catch(Exception e)
        {
            logger.error("tcDataSetException",e);
            throw new OIMHelperException("tcDataSetException:" + e.getMessage(),e);
        }
        
    }
    
    
}
