/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.api.tester;

import java.util.List;
import oracle.iam.request.vo.ApprovalData;
import oracle.iam.request.vo.Beneficiary;
import oracle.iam.request.vo.Request;
import com.aptecllc.oim.api.OIMHelperClient;
import com.aptecllc.oim.exceptions.OIMHelperException;
import org.junit.Test;

/**
 * things have changed in R2 so this needs to be tested with the new req webservices stuff
 */
public class ShowRequestData extends OIMHelperClient {

    private String reqId = "reqid";

    @Test
    public void mainTest()
    {
        
        ShowRequestData ra = new ShowRequestData();

        try {

            ra.loadConfig(null);
            ra.loginWithCustomEnv();
        }
        catch(OIMHelperException e)
        {

        }
        
        
        ra.findRoleApprover(reqId);

    }

    public void findRoleApprover(String reqId)
    {

        try {
            System.out.println("Prototype for invoking an OIM API from a SOA Composite");
            System.out.println("RTM Usecase: Organization Administrator");
            
            String actKey = "";
            String roleApprover = "";
            
            oracle.iam.request.api.RequestService reqSvc =
                    getClient().getService(oracle.iam.request.api.RequestService.class);
            oracle.iam.identity.rolemgmt.api.RoleManager roleSvc =
                    getClient().getService(oracle.iam.identity.rolemgmt.api.RoleManager.class);
            oracle.iam.identity.usermgmt.api.UserManager usersvc =
                    getClient().getService(oracle.iam.identity.usermgmt.api.UserManager.class);
            //Object reqIdXMLElem = getVariableData("inputVariable", "payload","/ns3:process/ns4:RequestID");
            //String reqId = ((oracle.xml.parser.v2.XMLElement) reqIdXMLElem).getText();
            System.out.println("The request ID is " + reqId);
            // invoke the getBasicRequestData() method on the RequestService API
            oracle.iam.request.vo.Request req = reqSvc.getBasicRequestData(reqId);
            showReqData(req);
            java.util.List<oracle.iam.request.vo.Beneficiary> beneficiaries = req.getBeneficiaries();
            List<ApprovalData> aprData = req.getApprovalData();
            System.out.println("ApprovalData " + aprData);
            if (beneficiaries != null) {
                for (oracle.iam.request.vo.Beneficiary benf : beneficiaries) {
                    //get org key
                    java.util.HashSet<String> searchAttrs = new java.util.HashSet<String>();
                    searchAttrs.add(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.USER_LOGIN.getId());
                    searchAttrs.add(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.USER_ORGANIZATION.getId());
                    oracle.iam.identity.usermgmt.vo.User user1 = usersvc.getDetails(benf.getBeneficiaryKey(), searchAttrs, false);
                    System.out.println("Beneficiary " + user1.getLogin());
                    actKey = user1.getAttribute("act_key").toString();
                    //get org admin
                    if (actKey.trim().length() > 0) {
                        Thor.API.Operations.tcOrganizationOperationsIntf orgAPI =
                                (Thor.API.Operations.tcOrganizationOperationsIntf) getClient().getService(
                                Thor.API.Operations.tcOrganizationOperationsIntf.class);
                        Thor.API.tcResultSet rset = orgAPI.getAdministrators(Long.parseLong(actKey));
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < rset.getRowCount(); i++) {
                            rset.goToRow(i);
                            sb.append(rset.getStringValue("Groups.Group Name"));
                            if (i >= 0 && i < (rset.getRowCount() - 1)) {
                                sb.append(",");
                            }
                        }
                        String grpNames = sb.toString();
                        System.out.println("Groups=" + grpNames);
                        setVariableData("orgAdmin", grpNames);
                    }
                    //get role approver
                    java.util.List<oracle.iam.request.vo.RequestBeneficiaryEntity> rbes =
                            benf.getTargetEntities();
                    for (oracle.iam.request.vo.RequestBeneficiaryEntity rbe : rbes) {
                        String key = rbe.getEntityKey();
                        String type = rbe.getEntityType();
                        System.out.println("Beneficiary Entity Type " + type);
                        if (type.equalsIgnoreCase("Role")) {
                            java.util.HashSet<String> roleAttrs = new java.util.HashSet<String>();
                            roleAttrs.add("Role Approver");
                            oracle.iam.identity.rolemgmt.vo.Role role =
                                    roleSvc.getDetails(key, roleAttrs);
                            roleApprover = (String) role.getAttribute("Role Approver");
                            System.out.println("roleApprover=" + roleApprover);
                            setVariableData("roleApprover", roleApprover);
                            break;
                        }
                    }
                    break;
                }
            }
            System.out.println("OrgAdmin=" + getVariableData("orgAdmin").toString());
            System.out.println("roleApprover=" + getVariableData("roleApprover").toString());
        } catch (Exception e) {
            System.out.println("----------------------");
            e.printStackTrace();
            System.out.println("----------------------");
        }
    }

    public void showReqData(Request req)
    {
        System.out.println("ID " + req.getRequestID());
        System.out.println("Status " + req.getRequestStatus());
        System.out.println("RequestorKey " + req.getRequesterKey());
        List<ApprovalData> aprd = req.getApprovalData();
        List<Beneficiary> benes = req.getBeneficiaries();

        for(Beneficiary bene : benes)
        {
            System.out.println("Bene Key " + bene.getBeneficiaryKey());
            System.out.println("Bene Type " + bene.getBeneficiaryType());
            System.out.println("Attributes " + bene.getAttributes());
        }


    }
    
    public void setVariableData(String name,String value)
    {
        
    }


    public String getVariableData(String name)
    {
        return name;
    }


}
