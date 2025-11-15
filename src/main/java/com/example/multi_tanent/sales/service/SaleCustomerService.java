package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SaleCustomerRequest;
import com.example.multi_tanent.sales.dto.SaleCustomerResponse;
import com.example.multi_tanent.sales.entity.SaleCustomer;
import com.example.multi_tanent.sales.repository.SaleCustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleCustomerService {

    private final SaleCustomerRepository customerRepo;

    public SaleCustomerResponse create(SaleCustomerRequest req) {
        if (customerRepo.existsByCode(req.getCode())) {
            throw new IllegalArgumentException("Customer with code '" + req.getCode() + "' already exists.");
        }

        SaleCustomer customer = new SaleCustomer();
        applyRequestToEntity(req, customer);

        return toResponse(customerRepo.save(customer));
    }

    public SaleCustomerResponse update(Long id, SaleCustomerRequest req) {
        SaleCustomer customer = customerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));

        // Check if the code is being changed to one that already exists
        if (!customer.getCode().equals(req.getCode()) && customerRepo.existsByCode(req.getCode())) {
            throw new IllegalArgumentException("Customer with code '" + req.getCode() + "' already exists.");
        }

        applyRequestToEntity(req, customer);
        return toResponse(customerRepo.save(customer));
    }

    @Transactional(readOnly = true)
    public SaleCustomerResponse getById(Long id) {
        return customerRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SaleCustomerResponse> getAll() {
        return customerRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        SaleCustomer customer = customerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
        customerRepo.delete(customer);
    }

    private void applyRequestToEntity(SaleCustomerRequest req, SaleCustomer entity) {
        entity.setCode(req.getCode());
        entity.setName(req.getName());
        entity.setEmail(req.getEmail());
        entity.setPhone(req.getPhone());
        entity.setBillingAddress(req.getBillingAddress());
        entity.setShippingAddress(req.getShippingAddress());
        entity.setGstOrVatNumber(req.getGstOrVatNumber());
        entity.setStatus(req.getStatus());
    }

    private SaleCustomerResponse toResponse(SaleCustomer entity) {
        return SaleCustomerResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .billingAddress(entity.getBillingAddress())
                .shippingAddress(entity.getShippingAddress())
                .gstOrVatNumber(entity.getGstOrVatNumber())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
