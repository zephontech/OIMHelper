/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.zephon.oim.api;

import java.util.HashMap;
import java.util.List;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.provisioning.api.EntitlementService;
import oracle.iam.provisioning.api.ProvisioningConstants;
import oracle.iam.provisioning.exception.BulkException;
import oracle.iam.provisioning.exception.DuplicateEntitlementException;
import oracle.iam.provisioning.exception.EntitlementNotFoundException;
import oracle.iam.provisioning.exception.FormFieldNotFoundException;
import oracle.iam.provisioning.exception.FormNotFoundException;
import oracle.iam.provisioning.exception.GenericEntitlementServiceException;
import oracle.iam.provisioning.exception.ITResourceNotFoundException;
import oracle.iam.provisioning.exception.LookupValueNotFoundException;
import oracle.iam.provisioning.exception.ObjectNotFoundException;
import oracle.iam.provisioning.vo.Entitlement;

/**
 *
 * @author Frederick.Forester
 */
public class OIMEntitlements {

    private EntitlementService entitlementService = null;

    public OIMEntitlements(EntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    public OIMEntitlements(OIMClient client) {
        this.entitlementService = client.getService(EntitlementService.class);
    }

    /**
     * @param entitlementCode
     * @return
     * @throws GenericEntitlementServiceException
     * @throws EntitlementNotFoundException
     */
    public Entitlement findEntitlement(String entitlementCode) throws GenericEntitlementServiceException, EntitlementNotFoundException {

        // Get specific Entitlement Definitions
        SearchCriteria criteria
                = new SearchCriteria(ProvisioningConstants.EntitlementSearchAttribute.ENTITLEMENT_CODE.getId(), entitlementCode, SearchCriteria.Operator.EQUAL);
        return findEntitlementWithSearchCriteria(criteria);
    }

    /**
     * @param entitlementCode
     * @return
     * @throws GenericEntitlementServiceException
     * @throws EntitlementNotFoundException
     */
    public Entitlement findEntitlementContains(String entitlementCode) throws GenericEntitlementServiceException,
            EntitlementNotFoundException {
        // Get specific Entitlement Definitions
        SearchCriteria criteria
                = new SearchCriteria(ProvisioningConstants.EntitlementSearchAttribute.ENTITLEMENT_CODE.getId(),
                        entitlementCode, SearchCriteria.Operator.CONTAINS);
        return findEntitlementWithSearchCriteria(criteria);
    }

    /**
     * @param criteria
     * @return
     * @throws GenericEntitlementServiceException
     * @throws EntitlementNotFoundException
     */
    private Entitlement findEntitlementWithSearchCriteria(SearchCriteria criteria) throws GenericEntitlementServiceException,
            EntitlementNotFoundException {

        // Get specific Entitlement Definitions
        HashMap<String, Object> entConfigParams = new HashMap<String, Object>();
        List<Entitlement> entitlements = entitlementService.findEntitlements(criteria, entConfigParams);

        if (entitlements.size() < 1) {
            throw new EntitlementNotFoundException("Entitlement Not Found", "Entitlement Not Found", null, null);
        } else if (entitlements.size() > 1) {
            throw new GenericEntitlementServiceException("Multiple Entitlement Found", "Multiple Entitlement Found", null, null);
        } else {
            return entitlements.get(0);
        }
    }

    /**
     * @param entitlementKey
     * @return
     * @throws GenericEntitlementServiceException
     * @throws EntitlementNotFoundException
     */
    public Entitlement findEntitlement(long entitlementKey) throws GenericEntitlementServiceException, EntitlementNotFoundException {
        return entitlementService.findEntitlement(entitlementKey);
    }

    /**
     * @param entitlement
     * @return
     * @throws LookupValueNotFoundException
     * @throws GenericEntitlementServiceException
     * @throws ObjectNotFoundException
     * @throws ITResourceNotFoundException
     * @throws FormNotFoundException
     * @throws FormFieldNotFoundException
     * @throws DuplicateEntitlementException
     */
    public Entitlement addEntitlement(Entitlement entitlement) throws LookupValueNotFoundException, GenericEntitlementServiceException, ObjectNotFoundException, ITResourceNotFoundException, FormNotFoundException, FormFieldNotFoundException, DuplicateEntitlementException {
        return entitlementService.addEntitlement(entitlement);
    }

    /**
     * @param entitlementList
     * @return
     * @throws BulkException
     */
    public List<Entitlement> addEntitlements(List<Entitlement> entitlementList) throws BulkException {
        return entitlementService.addEntitlements(entitlementList);
    }

    /**
     * @param entitlementKey
     * @return
     * @throws GenericEntitlementServiceException
     */
    public boolean deleteEntitlement(long entitlementKey) throws GenericEntitlementServiceException {
        return entitlementService.deleteEntitlement(entitlementKey);
    }

    /**
     * @param entitlementArray
     * @throws BulkException
     */
    public void deleteEntitlements(long[] entitlementArray) throws BulkException {
        entitlementService.deleteEntitlements(entitlementArray);
    }

    /**
     * @param entitlement
     * @return
     * @throws EntitlementNotFoundException
     * @throws GenericEntitlementServiceException
     */
    public Entitlement updateEntitlement(Entitlement entitlement) throws EntitlementNotFoundException, GenericEntitlementServiceException {
        return entitlementService.updateEntitlement(entitlement);

    }

    /**
     * @param entitlementList
     * @return
     * @throws BulkException
     */
    public List<Entitlement> updateEntitlements(List<Entitlement> entitlementList) throws BulkException {
        return entitlementService.updateEntitlements(entitlementList);

    }

}
