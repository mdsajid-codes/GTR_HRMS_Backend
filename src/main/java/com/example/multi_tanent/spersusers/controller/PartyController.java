package com.example.multi_tanent.spersusers.controller;

import com.example.multi_tanent.spersusers.base.PartyBase;
import com.example.multi_tanent.spersusers.dto.*;
import com.example.multi_tanent.spersusers.service.PartyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PartyController {

    private final PartyService service;

    @PostMapping
    public ResponseEntity<PartyResponse> create(@Valid @RequestBody PartyRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<PartyResponse>> getAll(
            @RequestParam(required = false) PartyBase.PartyType type,
            Pageable pageable) {
        return ResponseEntity.ok(service.getAll(type, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartyResponse> update(@PathVariable Long id, @Valid @RequestBody PartyRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Nested Other Persons Endpoints ---

    @GetMapping("/{partyId}/other-persons")
    public ResponseEntity<List<OtherPersonResponse>> getOtherPersons(@PathVariable Long partyId) {
        return ResponseEntity.ok(service.getOtherPersonsForParty(partyId));
    }

    @PostMapping("/{partyId}/other-persons")
    public ResponseEntity<OtherPersonResponse> addOtherPerson(@PathVariable Long partyId, @Valid @RequestBody OtherPersonRequest request) {
        return new ResponseEntity<>(service.addOtherPersonToParty(partyId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{partyId}/other-persons/{otherPersonId}")
    public ResponseEntity<OtherPersonResponse> updateOtherPerson(@PathVariable Long partyId, @PathVariable Long otherPersonId, @Valid @RequestBody OtherPersonRequest request) {
        return ResponseEntity.ok(service.updateOtherPersonForParty(partyId, otherPersonId, request));
    }

    @DeleteMapping("/{partyId}/other-persons/{otherPersonId}")
    public ResponseEntity<Void> deleteOtherPerson(@PathVariable Long partyId, @PathVariable Long otherPersonId) {
        service.deleteOtherPersonForParty(partyId, otherPersonId);
        return ResponseEntity.noContent().build();
    }


    // --- Nested Bank Details Endpoints ---

    @GetMapping("/{partyId}/bank-details")
    public ResponseEntity<List<BaseBankDetailsResponse>> getBankDetails(@PathVariable Long partyId) {
        return ResponseEntity.ok(service.getBankDetailsForParty(partyId));
    }

    @PostMapping("/{partyId}/bank-details")
    public ResponseEntity<BaseBankDetailsResponse> addBankDetail(@PathVariable Long partyId, @Valid @RequestBody BaseBankDetailsRequest request) {
        return new ResponseEntity<>(service.addBankDetailToParty(partyId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{partyId}/bank-details/{bankDetailId}")
    public ResponseEntity<BaseBankDetailsResponse> updateBankDetail(@PathVariable Long partyId, @PathVariable Long bankDetailId, @Valid @RequestBody BaseBankDetailsRequest request) {
        return ResponseEntity.ok(service.updateBankDetailForParty(partyId, bankDetailId, request));
    }

    @DeleteMapping("/{partyId}/bank-details/{bankDetailId}")
    public ResponseEntity<Void> deleteBankDetail(@PathVariable Long partyId, @PathVariable Long bankDetailId) {
        service.deleteBankDetailForParty(partyId, bankDetailId);
        return ResponseEntity.noContent().build();
    }

    // --- Nested Custom Fields Endpoints ---
    @GetMapping("/{partyId}/custom-fields")
    public ResponseEntity<List<CustomFieldResponse>> getCustomFields(@PathVariable Long partyId) {
        return ResponseEntity.ok(service.getCustomFieldsForParty(partyId));
    }

    @PostMapping("/{partyId}/custom-fields")
    public ResponseEntity<CustomFieldResponse> addCustomField(@PathVariable Long partyId, @Valid @RequestBody CustomFieldRequest request) {
        return new ResponseEntity<>(service.addCustomFieldToParty(partyId, request), HttpStatus.CREATED);
    }

    // NOTE: PUT and DELETE for custom fields can be added here following the same pattern.
}
