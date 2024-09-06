/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.api.tester;

import com.aptecllc.oim.api.OIMHelperClient;
import com.aptecllc.oim.exceptions.OIMHelperException;
import java.util.List;
import java.util.Locale;
import oracle.iam.passwordmgmt.api.PasswordMgmtService;
import oracle.iam.passwordmgmt.vo.PasswordPolicyDescription;
import oracle.iam.passwordmgmt.vo.ValidationResult;
import oracle.iam.passwordmgmt.vo.rules.PasswordRuleDescription;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author fforester
 */
public class ClientTestPasswordMgt extends OIMHelperClient {
    
    
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private PasswordMgmtService passwordOp;
    
    private String password = "1 dmapte$t#";
    private String userName = "ACTIVATIONONE";
    
    @Test
    public void mainTest() {

        //ClientTesterConnection testconn = new ClientTesterConnection();
        /*
        if (this.hasSpaces(password))
        {
            logger.debug("Spaces!");
            return;
        }
        */
        
        logger.info("loginWithCustomEnv");
        try {
            loadConfig(null);
            loginWithCustomEnv();
        } catch (OIMHelperException e) {
            logger.error("Error", e);
            return;
        }
        
        passwordOp = getClient().getService(PasswordMgmtService.class);
        
        PasswordPolicyDescription desc = passwordOp.getSystemDefaultPolicyDescription(Locale.US);
        List<PasswordRuleDescription> rules = desc.getPasswordRulesDescription();
        for(PasswordRuleDescription r : rules)
        {
            //logger.debug("Rule:" + r.getDisplayValue());
        }
        ValidationResult results = passwordOp.validatePasswordAgainstPolicy(password.toCharArray(), userName, Locale.US);
        if (results.isPasswordValid()) {
            logger.debug("Valid Password");
        } else {
            StringBuffer failedRules = new StringBuffer();
            List<PasswordRuleDescription> rulesViolated =
                results.getPolicyViolationsDescription().getPasswordRulesDescription();

            for (PasswordRuleDescription rule : rulesViolated) {
                failedRules.append(rule.getDisplayValue()).append("|");
            }

            logger.debug("Failed:" + failedRules.toString());
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
