package com.example.multi_tanent.spersusers.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.spersusers.base.PartyBase;
import com.example.multi_tanent.spersusers.enitity.*;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.multi_tanent.spersusers.dto.*;
import com.example.multi_tanent.spersusers.repository.*;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class PartyService {

    private final PartyRepository repository;
    private final TenantRepository tenantRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final BaseBankDetailsRepository bankDetailsRepository;
    private final CustomFieldRepository customFieldRepository;

    private Tenant getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
    }

    public PartyResponse create(PartyRequest request) {
        BaseCustomer entity = new BaseCustomer();
        mapRequestToEntity(request, entity);
        entity.setCreatedDate(LocalDateTime.now());
        // entity.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        return PartyResponse.fromEntity(repository.save(entity));
    }

    public PartyResponse update(Long id, PartyRequest request) {
        Long tenantId = getCurrentTenant().getId();
        BaseCustomer entity = repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new EntityNotFoundException("Party not found: " + id));

        mapRequestToEntity(request, entity);
        return PartyResponse.fromEntity(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<PartyResponse> getAll(PartyBase.PartyType type, Pageable pageable) {
        Long tenantId = getCurrentTenant().getId();
        if (type != null) {
            return repository.findByTenantIdAndPartyType(tenantId, type, pageable).map(PartyResponse::fromEntity);
        }
        return repository.findByTenantId(tenantId, pageable).map(PartyResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public PartyResponse getById(Long id) {
        Long tenantId = getCurrentTenant().getId();
        return repository.findByTenantIdAndId(tenantId, id)
                .map(PartyResponse::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Party not found: " + id));
    }

    public void delete(Long id) {
        Long tenantId = getCurrentTenant().getId();
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Party not found: " + id);
        }
        repository.deleteById(id);
    }

    // --- Methods for OtherPerson ---

    public List<OtherPersonResponse> getOtherPersonsForParty(Long partyId) {
        if (!repository.existsById(partyId)) throw new EntityNotFoundException("Party not found: " + partyId);
        return otherPersonRepository.findByPartyId(partyId).stream()
                .map(OtherPersonResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public OtherPersonResponse addOtherPersonToParty(Long partyId, OtherPersonRequest request) {
        BaseCustomer party = repository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException("Party not found: " + partyId));
        OtherPerson otherPerson = new OtherPerson();
        otherPerson.setParty(party);
        // map fields from request
        otherPerson.setSalutation(request.getSalutation());
        otherPerson.setFirstName(request.getFirstName());
        otherPerson.setLastName(request.getLastName());
        otherPerson.setEmailAddress(request.getEmailAddress());
        otherPerson.setWorkPhone(request.getWorkPhone());
        otherPerson.setMobile(request.getMobile());
        return OtherPersonResponse.fromEntity(otherPersonRepository.save(otherPerson));
    }

    public OtherPersonResponse updateOtherPersonForParty(Long partyId, Long otherPersonId, OtherPersonRequest request) {
        if (!repository.existsById(partyId)) throw new EntityNotFoundException("Party not found: " + partyId);
        OtherPerson otherPerson = otherPersonRepository.findById(otherPersonId)
                .orElseThrow(() -> new EntityNotFoundException("OtherPerson not found: " + otherPersonId));
        if (!otherPerson.getParty().getId().equals(partyId)) {
            throw new IllegalArgumentException("OtherPerson does not belong to the specified Party.");
        }
        // map fields from request
        otherPerson.setSalutation(request.getSalutation());
        otherPerson.setFirstName(request.getFirstName());
        otherPerson.setLastName(request.getLastName());
        otherPerson.setEmailAddress(request.getEmailAddress());
        otherPerson.setWorkPhone(request.getWorkPhone());
        otherPerson.setMobile(request.getMobile());
        return OtherPersonResponse.fromEntity(otherPersonRepository.save(otherPerson));
    }

    public void deleteOtherPersonForParty(Long partyId, Long otherPersonId) {
        if (!repository.existsById(partyId)) throw new EntityNotFoundException("Party not found: " + partyId);
        otherPersonRepository.deleteById(otherPersonId);
    }

    // --- Methods for BaseBankDetails ---

    public List<BaseBankDetailsResponse> getBankDetailsForParty(Long partyId) {
        if (!repository.existsById(partyId)) throw new EntityNotFoundException("Party not found: " + partyId);
        return bankDetailsRepository.findByPartyId(partyId).stream()
                .map(BaseBankDetailsResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public BaseBankDetailsResponse addBankDetailToParty(Long partyId, BaseBankDetailsRequest request) {
        BaseCustomer party = repository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException("Party not found: " + partyId));
        BaseBankDetails bankDetails = new BaseBankDetails();
        bankDetails.setParty(party);
        // map fields from request
        bankDetails.setBankName(request.getBankName());
        bankDetails.setAccountNumber(request.getAccountNumber());
        bankDetails.setIfsCode(request.getIfsCode());
        bankDetails.setIbanCode(request.getIbanCode());
        bankDetails.setCorporateId(request.getCorporateId());
        bankDetails.setLocationBranch(request.getLocationBranch());
        bankDetails.setBranchAddress(request.getBranchAddress());
        bankDetails.setBeneficiaryMailId(request.getBeneficiaryMailId());
        return BaseBankDetailsResponse.fromEntity(bankDetailsRepository.save(bankDetails));
    }

    public BaseBankDetailsResponse updateBankDetailForParty(Long partyId, Long bankDetailId, BaseBankDetailsRequest request) {
        if (!repository.existsById(partyId)) throw new EntityNotFoundException("Party not found: " + partyId);
        BaseBankDetails bankDetails = bankDetailsRepository.findById(bankDetailId)
                .orElseThrow(() -> new EntityNotFoundException("BankDetail not found: " + bankDetailId));
        if (!bankDetails.getParty().getId().equals(partyId)) {
            throw new IllegalArgumentException("BankDetail does not belong to the specified Party.");
        }
        // map fields from request
        bankDetails.setBankName(request.getBankName());
        bankDetails.setAccountNumber(request.getAccountNumber());
        // ... map other fields
        return BaseBankDetailsResponse.fromEntity(bankDetailsRepository.save(bankDetails));
    }

    public void deleteBankDetailForParty(Long partyId, Long bankDetailId) {
        if (!repository.existsById(partyId)) throw new EntityNotFoundException("Party not found: " + partyId);
        bankDetailsRepository.deleteById(bankDetailId);
    }

    // --- Methods for CustomField ---

    public List<CustomFieldResponse> getCustomFieldsForParty(Long partyId) {
        if (!repository.existsById(partyId)) throw new EntityNotFoundException("Party not found: " + partyId);
        return customFieldRepository.findByPartyId(partyId).stream()
                .map(CustomFieldResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CustomFieldResponse addCustomFieldToParty(Long partyId, CustomFieldRequest request) {
        BaseCustomer party = repository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException("Party not found: " + partyId));
        CustomField customField = new CustomField();
        customField.setParty(party);
        // map fields from request
        customField.setFieldName(request.getFieldName());
        customField.setFieldValue(request.getFieldValue());
        return CustomFieldResponse.fromEntity(customFieldRepository.save(customField));
    }

    public CustomFieldResponse updateCustomFieldForParty(Long partyId, Long customFieldId, CustomFieldRequest request) {
        if (!repository.existsById(partyId)) throw new EntityNotFoundException("Party not found: " + partyId);
        CustomField customField = customFieldRepository.findById(customFieldId)
                .orElseThrow(() -> new EntityNotFoundException("CustomField not found: " + customFieldId));
        if (!customField.getParty().getId().equals(partyId)) {
            throw new IllegalArgumentException("CustomField does not belong to the specified Party.");
        }
        // map fields from request
        customField.setFieldName(request.getFieldName());
        customField.setFieldValue(request.getFieldValue());
        return CustomFieldResponse.fromEntity(customFieldRepository.save(customField));
    }

    public void deleteCustomFieldForParty(Long partyId, Long customFieldId) {
        if (!repository.existsById(partyId)) throw new EntityNotFoundException("Party not found: " + partyId);
        customFieldRepository.deleteById(customFieldId);
    }

    private void mapRequestToEntity(PartyRequest req, BaseCustomer entity) {
        // Map PartyBase fields
        if (req.getPartyType() == null) {
            throw new IllegalArgumentException("PartyType must be provided and cannot be null.");
        }
        entity.setPartyType(req.getPartyType());
        entity.setTenant(getCurrentTenant());
        entity.setUnder(req.getUnder());
        entity.setPriceCategory(req.getPriceCategory());
        entity.setVendorCustomerCode(req.getVendorCustomerCode());
        entity.setCustomerCode(req.getCustomerCode());
        entity.setPrimaryContactTitle(req.getPrimaryContactTitle());
        entity.setPrimaryFirstName(req.getPrimaryFirstName());
        entity.setPrimaryLastName(req.getPrimaryLastName());
        entity.setPrimaryContactPerson(req.getPrimaryContactPerson());
        entity.setMobile(req.getMobile());
        entity.setContactEmail(req.getContactEmail());
        entity.setContactPhone(req.getContactPhone());
        entity.setWorkPhone(req.getWorkPhone());
        entity.setSkypeNameOrNumber(req.getSkypeNameOrNumber());
        entity.setDesignation(req.getDesignation());
        entity.setDepartment(req.getDepartment());
        entity.setCompanyName(req.getCompanyName());
        entity.setWebsite(req.getWebsite());
        entity.setOwnerCeoName(req.getOwnerCeoName());
        entity.setOwnerCeoContact(req.getOwnerCeoContact());
        entity.setOwnerCeoEmail(req.getOwnerCeoEmail());
        entity.setPanNumber(req.getPanNumber());
        entity.setTanNumber(req.getTanNumber());
        entity.setCinNo(req.getCinNo());
        entity.setVatNumber(req.getVatNumber());
        entity.setVatTreatment(req.getVatTreatment());
        entity.setVatTrnNumber(req.getVatTrnNumber());
        entity.setCity(req.getCity());
        entity.setRegion(req.getRegion());
        entity.setCurrency(req.getCurrency());
        entity.setTermsAndConditionsInternal(req.getTermsAndConditionsInternal());
        entity.setTermsAndConditionsDisplay(req.getTermsAndConditionsDisplay());
        entity.setModeOfPayment(req.getModeOfPayment());
        entity.setDeliveryType(req.getDeliveryType());
        entity.setPaymentTerms(req.getPaymentTerms());
        entity.setTransportDispatchThrough(req.getTransportDispatchThrough());
        entity.setFreightTerms(req.getFreightTerms());
        entity.setSplInstruction(req.getSplInstruction());
        entity.setSalesValuePreviousYear(req.getSalesValuePreviousYear());
        entity.setFacebook(req.getFacebook());
        entity.setTwitter(req.getTwitter());
        entity.setTaxDeducted(req.getTaxDeducted());
        entity.setOpeningBalance(req.getOpeningBalance());
        entity.setOpeningBalanceType(req.getOpeningBalanceType());
        entity.setCreditLimitAllowed(req.getCreditLimitAllowed());
        entity.setCreditPeriodAllowed(req.getCreditPeriodAllowed());
        entity.setBillingAddress(mapAddress(req.getBillingAddress()));
        entity.setShippingAddress(mapAddress(req.getShippingAddress()));
        entity.setShippingSameAsBilling(req.getShippingSameAsBilling());
        entity.setRemarks(req.getRemarks());

        // Map BaseCustomer specific fields
        entity.setPrimaryContactPersonFull(req.getPrimaryContactPersonFull());
        entity.setActive(req.getActive());

        // Map nested lists
        entity.getOtherPersons().clear();
        if (req.getOtherPersons() != null) {
            req.getOtherPersons().forEach(opReq -> {
                OtherPerson op = new OtherPerson();
                op.setParty(entity); // Link to parent
                op.setSalutation(opReq.getSalutation());
                op.setFirstName(opReq.getFirstName());
                op.setLastName(opReq.getLastName());
                op.setEmailAddress(opReq.getEmailAddress());
                op.setWorkPhone(opReq.getWorkPhone());
                op.setMobile(opReq.getMobile());
                entity.getOtherPersons().add(op);
            });
        }

        entity.getCustomFields().clear();
        if (req.getCustomFields() != null) {
            req.getCustomFields().forEach(cfReq -> {
                CustomField cf = new CustomField();
                cf.setParty(entity); // Link to parent
                cf.setFieldName(cfReq.getFieldName());
                cf.setFieldValue(cfReq.getFieldValue());
                entity.getCustomFields().add(cf);
            });
        }

        entity.getBankDetails().clear();
        if (req.getBankDetails() != null) {
            req.getBankDetails().forEach(bdReq -> {
                BaseBankDetails bd = new BaseBankDetails();
                bd.setParty(entity); // Link to parent
                bd.setBankName(bdReq.getBankName());
                bd.setAccountNumber(bdReq.getAccountNumber());
                bd.setIfsCode(bdReq.getIfsCode());
                bd.setIbanCode(bdReq.getIbanCode());
                bd.setCorporateId(bdReq.getCorporateId());
                bd.setLocationBranch(bdReq.getLocationBranch());
                bd.setBranchAddress(bdReq.getBranchAddress());
                bd.setBeneficiaryMailId(bdReq.getBeneficiaryMailId());
                entity.getBankDetails().add(bd);
            });
        }
    }

    private PartyBase.Address mapAddress(PartyRequest.AddressRequest req) {
        if (req == null) return null;
        return new PartyBase.Address(req.getAttention(), req.getAddressLine(), req.getCity(), req.getState(), req.getZipCode(), req.getCountry(), req.getPhone(), req.getFax());
    }
}