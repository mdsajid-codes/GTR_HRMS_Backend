package com.example.multi_tanent.crm.controller;

import com.example.multi_tanent.crm.dto.ContactDto;
import com.example.multi_tanent.crm.dto.ContactRequestDto;
import com.example.multi_tanent.crm.services.ContactService;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm/contacts")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CRM_ADMIN')")
public class ContactController {

  private final ContactService contactService;

  public ContactController(ContactService contactService) {
    this.contactService = contactService;
  }

  @GetMapping
  public List<ContactDto> getAllContacts() {
    return contactService.getAllContacts();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ContactDto> getContactById(@PathVariable Long id) {
    ContactDto contact = contactService.getContactById(id);
    return ResponseEntity.ok(contact);
  }

  @PostMapping
  public ContactDto createContact(@RequestBody ContactRequestDto contact) {
    return contactService.createContact(contact);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ContactDto> updateContact(
      @PathVariable Long id, @RequestBody ContactRequestDto contactDetails) {
    ContactDto updatedContact = contactService.updateContact(id, contactDetails);
    return ResponseEntity.ok(updatedContact);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
    contactService.deleteContact(id);
    return ResponseEntity.noContent().build();
  }
}
