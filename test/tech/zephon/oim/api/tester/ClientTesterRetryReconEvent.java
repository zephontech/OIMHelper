/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tech.zephon.oim.api.tester;

import Thor.API.Exceptions.tcAPIException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tech.zephon.oim.exceptions.OIMHelperException;
import tech.zephon.oim.api.OIMHelperClient;
import oracle.iam.reconciliation.api.ReconOperationsService;
import org.junit.Test;

/**
 *
 */
public class ClientTesterRetryReconEvent extends OIMHelperClient {

    private static final Logger logger = Logger.getLogger(ClientTesterDMExporter.class.getName());
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
            logger.log(Level.SEVERE,"Init failed",e);
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
            logger.log(Level.SEVERE,"Error", e);
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
            logger.log(Level.SEVERE,"Failed to get Recon OP");
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
                logger.log(Level.SEVERE,"Retry Error for " + event,e);
            }
        }
    }

}
