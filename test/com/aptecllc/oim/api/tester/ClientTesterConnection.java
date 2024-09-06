/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api.tester;

import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.api.OIMHelperClient;
import oracle.iam.reconciliation.api.ReconOperationsService;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 */
public class ClientTesterConnection extends OIMHelperClient {

    private static ReconOperationsService reconOp;
    private Logger logger = Logger.getLogger(this.getClass().getName());

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

        logger.info("Show Class Loaders");

        ClassLoader current = new ClientTesterConnection().getClass().getClassLoader();

        while(current != null)
        {
            logger.info(current.getClass());
            current = current.getParent();
        }
    }

}
