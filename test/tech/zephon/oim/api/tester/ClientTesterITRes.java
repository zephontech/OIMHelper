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
import tech.zephon.oim.api.OIMProperties;
import com.thortech.util.logging.Logger;
import org.junit.Test;

/**
 *
 */
public class ClientTesterITRes extends OIMHelperClient {

    private CustomTestLogger logger = CustomTestLogger.getLogger(this.getClass().getName());
    

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
            logger.error("Error", e);
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }
    }
    
    public void showProperties(String resName)
    {
        try
        {
            oimProps = new OIMProperties(getClient());
            Map properties = oimProps.getITResourceProperties(resName);
            logger.debug("Properties:" + properties);
        }
        catch(Exception e)
        {
            logger.error("Error", e);
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
                logger.debug("Defition Field " + field);
                newDefMap.put(field, "DataVal" + field);
            }
            itres.createITResource("New Database Def Tester", resDefKey, newDefMap);

            Map properties = oimProps.getITResourceProperties("New Database Def Tester");

            String dbName = oimProps.getCriticalAttribute(properties,"DatabaseName");

            logger.debug("ITRes Properties " + properties);
            logger.debug("ITRes dbName " + dbName);

            try
            {
                dbName = oimProps.getCriticalAttribute(properties,"DummyField");
            }
            catch(OIMHelperException e)
            {
                logger.error("Error Getting Field " + e.getMessage());
            }

            long itreskey = itres.getItResource("New Database Def Tester");

            itres.removeITResource(itreskey);

            itreskey = itres.getItResource("New Database Def Tester");

            if (itreskey == 0l)
                logger.debug("Resource Deleted");
            else
                logger.error("DELETE FAILED!!!!");

            try
            {
                properties = oimProps.getITResourceProperties("New Database Def Tester");
            }
            catch(OIMHelperException e)
            {
                logger.error("Error Getting ITResource " + e.getMessage());
            }

        }
        catch(OIMHelperException e)
        {
            logger.error("Error", e);
        }

    }

}
