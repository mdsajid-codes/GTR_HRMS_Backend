package com.example.multi_tanent.crm.services;

import com.example.multi_tanent.crm.dto.ContactDto;
import com.example.multi_tanent.crm.dto.ContactRequestDto;
import com.example.multi_tanent.crm.entity.Contact;
import com.example.multi_tanent.crm.repository.ContactRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("tenantTx")
public class ContactService {

  private final ContactRepository contactRepository;
  private final TenantRepository tenantRepository;

  public ContactService(ContactRepository contactRepository, TenantRepository tenantRepository) {
    this.contactRepository = contactRepository;
    this.tenantRepository = tenantRepository;
  }

  private Tenant getCurrentTenant() {
    // In a multi-tenant setup, each tenant's DB has one tenant entry.
    return tenantRepository
        .findFirstByOrderByIdAsc()
        .orElseThrow(
            () -> new IllegalStateException("Tenant information not found in the current database."));
  }

  public List<ContactDto> getAllContacts() {
    return contactRepository.findAll().stream().map(ContactDto::fromEntity).collect(Collectors.toList());
  }

  public ContactDto getContactById(Long id) {
    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    return ContactDto.fromEntity(contact);
  }

  public ContactDto createContact(ContactRequestDto contactRequest) {
    Contact contact = new Contact();
    contact.setTenant(getCurrentTenant()); // Set the tenant from the context
    contact.setFirstName(contactRequest.getFirstName());
    contact.setLastName(contactRequest.getLastName());
    contact.setEmail(contactRequest.getEmail());
    contact.setPhone(contactRequest.getPhone());
    return ContactDto.fromEntity(contactRepository.save(contact));
  }

  public ContactDto updateContact(Long id, ContactRequestDto contactDetails) {
    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    contact.setFirstName(contactDetails.getFirstName());
    contact.setLastName(contactDetails.getLastName());
    contact.setEmail(contactDetails.getEmail());
    contact.setPhone(contactDetails.getPhone());
    return ContactDto.fromEntity(contactRepository.save(contact));
  }

  public void deleteContact(Long id) {
    Contact contact = contactRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    contactRepository.delete(contact);
  }
}
