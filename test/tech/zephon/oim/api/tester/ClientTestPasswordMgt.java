/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.zephon.oim.api.tester;

import tech.zephon.oim.api.OIMHelperClient;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.iam.passwordmgmt.api.PasswordMgmtService;
import oracle.iam.passwordmgmt.vo.PasswordPolicyDescription;
import oracle.iam.passwordmgmt.vo.ValidationResult;
import oracle.iam.passwordmgmt.vo.rules.PasswordRuleDescription;
import org.junit.Test;

/**
 *
 * @author fforester
 */
public class ClientTestPasswordMgt extends OIMHelperClient {
    
    
    private static final Logger logger = Logger.getLogger(ClientTestPasswordMgt.class.getName());
    private PasswordMgmtService passwordOp;
    
    private String password = "1 dmapte$t#";
    private String userName = "ACTIVATIONONE";
    
    @Test
    public void mainTest() {

        //ClientTesterConnection testconn = new ClientTesterConnection();
        /*
        if (this.hasSpaces(password))
        {
            logger.fine("Spaces!");
            return;
        }
        */
        
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error", e);
            return;
        }
        
        passwordOp = getClient().getService(PasswordMgmtService.class);
        
        PasswordPolicyDescription desc = passwordOp.getSystemDefaultPolicyDescription(Locale.US);
        List<PasswordRuleDescription> rules = desc.getPasswordRulesDescription();
        for(PasswordRuleDescription r : rules)
        {
            //logger.fine("Rule:" + r.getDisplayValue());
        }
        ValidationResult results = passwordOp.validatePasswordAgainstPolicy(password.toCharArray(), userName, Locale.US);
        if (results.isPasswordValid()) {
            logger.fine("Valid Password");
        } else {
            StringBuffer failedRules = new StringBuffer();
            List<PasswordRuleDescription> rulesViolated =
                results.getPolicyViolationsDescription().getPasswordRulesDescription();

            for (PasswordRuleDescription rule : rulesViolated) {
                failedRules.append(rule.getDisplayValue()).append("|");
            }

            logger.fine("Failed:" + failedRules.toString());
        }
    }
    
    public boolean hasSpaces(String s) {
        int spaces = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i)))
                spaces++;
        }
        if (spaces > 0)
            return true;
        return false;

    }
}
