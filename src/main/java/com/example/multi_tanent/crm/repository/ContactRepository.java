package com.example.multi_tanent.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.crm.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    
}
