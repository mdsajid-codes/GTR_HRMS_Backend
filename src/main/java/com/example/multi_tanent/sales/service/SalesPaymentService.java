package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SalesPaymentRequest;
import com.example.multi_tanent.sales.dto.SalesPaymentResponse;
import com.example.multi_tanent.sales.entity.SaleCustomer;
import com.example.multi_tanent.sales.entity.SalesInvoice;
import com.example.multi_tanent.sales.entity.SalesPayment;
import com.example.multi_tanent.sales.repository.SaleCustomerRepository;
import com.example.multi_tanent.sales.repository.SalesInvoiceRepository;
import com.example.multi_tanent.sales.repository.SalesPaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesPaymentService {

    private final SalesPaymentRepository paymentRepo;
    private final SaleCustomerRepository customerRepo;
    private final SalesInvoiceRepository invoiceRepo;

    public SalesPaymentResponse create(SalesPaymentRequest req) {
        if (paymentRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Payment with number '" + req.getNumber() + "' already exists.");
        }

        SalesPayment payment = new SalesPayment();
        applyRequestToEntity(req, payment);

        return toResponse(paymentRepo.save(payment));
    }

    public SalesPaymentResponse update(Long id, SalesPaymentRequest req) {
        SalesPayment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));

        if (!payment.getNumber().equals(req.getNumber()) && paymentRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Payment with number '" + req.getNumber() + "' already exists.");
        }

        applyRequestToEntity(req, payment);
        return toResponse(paymentRepo.save(payment));
    }

    @Transactional(readOnly = true)
    public SalesPaymentResponse getById(Long id) {
        return paymentRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SalesPaymentResponse> getAll() {
        return paymentRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        SalesPayment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
        paymentRepo.delete(payment);
    }

    private void applyRequestToEntity(SalesPaymentRequest req, SalesPayment entity) {
        entity.setNumber(req.getNumber());
        entity.setDate(req.getDate());
        entity.setMethod(req.getMethod());
        entity.setReference(req.getReference());
        entity.setAmount(req.getAmount());
        entity.setStatus(req.getStatus());

        SaleCustomer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + req.getCustomerId()));
        entity.setCustomer(customer);

        if (req.getInvoiceId() != null) {
            SalesInvoice invoice = invoiceRepo.findById(req.getInvoiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + req.getInvoiceId()));
            entity.setInvoice(invoice);
        }
    }

    private SalesPaymentResponse toResponse(SalesPayment e) {
        return SalesPaymentResponse.builder().id(e.getId()).number(e.getNumber()).date(e.getDate()).method(e.getMethod()).reference(e.getReference()).amount(e.getAmount()).status(e.getStatus()).customerId(e.getCustomer() != null ? e.getCustomer().getId() : null).customerName(e.getCustomer() != null ? e.getCustomer().getName() : null).invoiceId(e.getInvoice() != null ? e.getInvoice().getId() : null).invoiceNumber(e.getInvoice() != null ? e.getInvoice().getNumber() : null).build();
    }
}
