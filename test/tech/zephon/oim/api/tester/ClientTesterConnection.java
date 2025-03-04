/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tech.zephon.oim.api.tester;

import java.util.logging.Level;
import java.util.logging.Logger;
import tech.zephon.oim.exceptions.OIMHelperException;
import tech.zephon.oim.api.OIMHelperClient;
import oracle.iam.reconciliation.api.ReconOperationsService;
import org.junit.Test;

/**
 *
 */
public class ClientTesterConnection extends OIMHelperClient {
    
   
    private static ReconOperationsService reconOp;
    private static final Logger logger = Logger.getLogger(ClientTesterConnection.class.getName());
    
    @Test
    public void mainTest() {

        //ClientTesterConnection testconn = new ClientTesterConnection();
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();
        } catch (OIMHelperException e) {
            logger.log(Level.SEVERE,"Error", e);
            return;
        }
        

        reconOp = getClient().getService(ReconOperationsService.class);

        if (reconOp == null)
        {
            logger.log(Level.SEVERE,"failed to get Recon class");
            return;
        }

        logger.info("Connection Success");

        logger.fine("Show Class Loaders");

        ClassLoader current = new ClientTesterConnection().getClass().getClassLoader();

        while(current != null)
        {
            logger.info("Classname:" + current.getClass());
            current = current.getParent();
        }
    }

}
