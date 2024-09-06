/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api.tester;

import Thor.API.Exceptions.tcAPIException;
import com.aptecllc.oim.exceptions.OIMHelperException;
import com.aptecllc.oim.api.OIMHelperClient;
import oracle.iam.reconciliation.api.ReconOperationsService;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 */
public class ClientTesterRetryReconEvent extends OIMHelperClient {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private String defaultConfigFile = "jndi.properties";
    
    private String[] args = {"evtid1","evtid2"};
    @Test
    public void mainTest() {

        //ClientTesterRetryReconEvent testRecon = new ClientTesterRetryReconEvent();

        try
        {
            initClient();
            runRecon(args);
        }
        catch(Exception e)
        {
            logger.error("Init failed",e);
            return;
        }
    }

    private void initClient() throws Exception
    {
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(defaultConfigFile);
            loginWithCustomEnv();
        } catch (OIMHelperException e) {
            logger.error("Error", e);
            throw e;
        }
    }

    private void runRecon(String[] args)
    {
        if (args == null)
            return;

        ReconOperationsService reconOp;

        reconOp = getClient().getService(ReconOperationsService.class);
        if (reconOp == null)
        {
            logger.error("Failed to get Recon OP");
            return;
        }

        for(int i=0;i<args.length;i++)
        {
            long event = new Long(args[i]).longValue();
            try
            {
            reconOp.processReconciliationEvent(event);
            }
            catch(tcAPIException e)
            {
                logger.error("Retry Error for " + event,e);
            }
        }
    }

}
