/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tech.zephon.oim.api.tester;

import tech.zephon.oim.exceptions.OIMHelperException;
import tech.zephon.oim.api.OIMHelperClient;
import com.thortech.xl.dataaccess.tcDataSet;
import com.thortech.xl.dataaccess.tcDataSetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

/**
 *
 */
public class ClientTesterInternalDB extends OIMHelperClient {

    private static final Logger logger = Logger.getLogger(ClientTesterDMExporter.class.getName());

    @Test
    public void mainTest()
    {
        //ClientTesterInternalDB testdb = new ClientTesterInternalDB();

        try
        {
            initClient();
            execute();
        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE,"Init failed",e);
            return;
        }
    }

    private void initClient() throws OIMHelperException
    {
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();

        } catch (OIMHelperException e) {
            logger.log(Level.SEVERE,"Error", e);
            throw e;
        }
    }

    private void execute() throws OIMHelperException {

        tcDataSet tmpDataSet = new tcDataSet();
        String frmAddress = "";
        String Subject = "";
        String Body = "";
        ArrayList alToAddresses = new ArrayList();
        Map map = new HashMap();
        // Find the email definition key
        String sEmailDefn = "Auto Delegation Email";
        tmpDataSet.setQuery(getDataBase(),
                "select usr.usr_password,emd.emd_key,emd.emd_subject,emd.usr_key,emd.emd_body,usr_login,usr_email " +
                "from emd,usr " +
                "where usr.usr_key=emd.usr_key and " + "emd_name='" + sEmailDefn + "'");

        try
        {
            tmpDataSet.executeQuery();
            frmAddress = tmpDataSet.getString("usr_email");
            alToAddresses.add("oimwrapper@oimexamples.org");
            Subject = tmpDataSet.getString("emd_subject");
            Body = tmpDataSet.getString("emd_body");
            String pw = tmpDataSet.getString("usr_password");
            logger.fine("frmAddress - " + frmAddress);
            logger.fine("Subject - " + Subject);
            logger.fine("Body - " + Body);
            logger.fine("PW - " + pw);
        }
        catch(tcDataSetException e)
        {
            logger.log(Level.SEVERE,"tcDataSetException",e);
            throw new OIMHelperException("tcDataSetException",e);
        }

        //test update
        /*
        tcDataProvider db = this.getDataBase();
        System.out.print("db"+db);
        PreparedStatementUtil preparedstatementutil = new PreparedStatementUtil();
        preparedstatementutil.setStatement(db,"INSERT INTO TESTROLES(ID, DESCRIPTION, NAME) VALUES (1,'testdesc','testname')");
        try
        {
            preparedstatementutil.executeUpdate();
        }
        catch(tcDataAccessException e)
        {
            logger.log(Level.SEVERE,"tcDataAccessException",e);
            throw new OIMHelperException("tcDataAccessException",e);
        }
        catch(tcDataSetException e)
        {
            logger.log(Level.SEVERE,"tcDataSetException",e);
            throw new OIMHelperException("tcDataSetException",e);
        }
        *
        */
    }

}
