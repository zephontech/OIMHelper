/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tech.zephon.oim.api.tester;

import java.util.HashMap;
import tech.zephon.oim.exceptions.OIMHelperException;
import tech.zephon.oim.api.OIMHelperClient;
import tech.zephon.oim.api.OIMITResources;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import tech.zephon.oim.api.OIMProperties;
import org.junit.Test;

/**
 *
 */
public class ClientTesterITRes extends OIMHelperClient {

    private static final Logger logger = Logger.getLogger(ClientTesterDMExporter.class.getName());
    

    private static OIMITResources itres;
    private static OIMProperties oimProps;
    
    @Test
    public void mainTest()
    {
        //ClientTesterITRes testres = new ClientTesterITRes();

        try {
            loadConfig(null);
            loginWithCustomEnv();
            showProperties("LY Active Directory");
            //runMe();
            //testres.printMe();
        } 
        catch (OIMHelperException e)
        {
            logger.log(Level.SEVERE,"Error", e);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE,"Error", e);
        }
    }
    
    public void showProperties(String resName)
    {
        try
        {
            oimProps = new OIMProperties(getClient());
            Map properties = oimProps.getITResourceProperties(resName);
            logger.fine("Properties:" + properties);
        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE,"Error", e);
        }
    }

    public void runMe() throws Exception {

        try
        {
            itres = new OIMITResources(getClient());
            oimProps = new OIMProperties(getClient());
            long resDefKey = itres.getItResourceDefinition("Database");
            List<String> fields = itres.getITResourceDefinitionFields(resDefKey);

            if (fields == null)
            {
                throw new Exception("Definition has no fields");
            }

            Map<String,String> newDefMap = new HashMap<String,String>();
            
            for(String field : fields)
            {
                logger.fine("Defition Field " + field);
                newDefMap.put(field, "DataVal" + field);
            }
            itres.createITResource("New Database Def Tester", resDefKey, newDefMap);

            Map properties = oimProps.getITResourceProperties("New Database Def Tester");

            String dbName = oimProps.getCriticalAttribute(properties,"DatabaseName");

            logger.fine("ITRes Properties " + properties);
            logger.fine("ITRes dbName " + dbName);

            try
            {
                dbName = oimProps.getCriticalAttribute(properties,"DummyField");
            }
            catch(OIMHelperException e)
            {
                logger.log(Level.SEVERE,"Error Getting Field " + e.getMessage());
            }

            long itreskey = itres.getItResource("New Database Def Tester");

            itres.removeITResource(itreskey);

            itreskey = itres.getItResource("New Database Def Tester");

            if (itreskey == 0l)
                logger.fine("Resource Deleted");
            else
                logger.log(Level.SEVERE,"DELETE FAILED!!!!");

            try
            {
                properties = oimProps.getITResourceProperties("New Database Def Tester");
            }
            catch(OIMHelperException e)
            {
                logger.log(Level.SEVERE,"Error Getting ITResource " + e.getMessage());
            }

        }
        catch(OIMHelperException e)
        {
            logger.log(Level.SEVERE,"Error", e);
        }

    }

}
