/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tech.zephon.oim.api.tester;

import tech.zephon.oim.exceptions.OIMHelperException;
import tech.zephon.oim.api.OIMHelperClient;
import oracle.iam.reconciliation.api.ReconOperationsService;
import org.junit.Test;

/**
 *
 */
public class ClientTesterConnection extends OIMHelperClient {
    
   
    private static ReconOperationsService reconOp;
    private CustomTestLogger logger = CustomTestLogger.getLogger(ClientTesterConnection.class.getName());
    
    @Test
    public void mainTest() {

        //ClientTesterConnection testconn = new ClientTesterConnection();
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();
        } catch (OIMHelperException e) {
            logger.error("Error", e);
            return;
        }
        

        reconOp = getClient().getService(ReconOperationsService.class);

        if (reconOp == null)
        {
            logger.error("failed to get Recon class");
            return;
        }

        logger.info("Connection Success");

        logger.debug("Show Class Loaders");

        ClassLoader current = new ClientTesterConnection().getClass().getClassLoader();

        while(current != null)
        {
            logger.info("Classname:" + current.getClass());
            current = current.getParent();
        }
    }

}
