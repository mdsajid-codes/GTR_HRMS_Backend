package com.example.multi_tanent.pos.service;

import com.example.multi_tanent.pos.dto.PosUserRequest;
import com.example.multi_tanent.pos.entity.PosUser;
import com.example.multi_tanent.pos.entity.Store;
import com.example.multi_tanent.pos.entity.Tenant;
import com.example.multi_tanent.pos.repository.PosUserRepository;
import com.example.multi_tanent.pos.repository.StoreRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional("tenantTx")
public class PosUserService {

    private final PosUserRepository posUserRepository;
    private final TenantRepository tenantRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public PosUserService(PosUserRepository posUserRepository,
                          TenantRepository tenantRepository,
                          StoreRepository storeRepository,
                          PasswordEncoder passwordEncoder) {
        this.posUserRepository = posUserRepository;
        this.tenantRepository = tenantRepository;
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant context not found. Cannot perform user operations."));
    }

    public PosUser createPosUser(PosUserRequest userRequest) {
        Tenant currentTenant = getCurrentTenant();

        posUserRepository.findByEmailAndTenantId(userRequest.getEmail(), currentTenant.getId())
                .ifPresent(u -> {
                    throw new RuntimeException("Username '" + userRequest.getEmail() + "' already exists.");
                });

        if (userRequest.getPassword() == null || userRequest.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required for new users.");
        }

        PosUser newUser = new PosUser();
        newUser.setTenant(currentTenant);
        newUser.setEmail(userRequest.getEmail());
        newUser.setDisplayName(userRequest.getDisplayName());
        newUser.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        newUser.setRole(userRequest.getRole());

        if (userRequest.getStoreId() != null) {
            Store store = storeRepository.findByIdAndTenantId(userRequest.getStoreId(), currentTenant.getId())
                    .orElseThrow(() -> new RuntimeException("Store not found with id: " + userRequest.getStoreId()));
            newUser.setStore(store);
        }

        return posUserRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public List<PosUser> getAllPosUsersForCurrentTenant() {
        Tenant currentTenant = getCurrentTenant();
        return posUserRepository.findByTenantId(currentTenant.getId());
    }

    @Transactional(readOnly = true)
    public Optional<PosUser> getPosUserById(Long id) {
        Tenant currentTenant = getCurrentTenant();
        return posUserRepository.findByIdAndTenantId(id, currentTenant.getId());
    }

    public PosUser updatePosUser(Long id, PosUserRequest userRequest) {
        Tenant currentTenant = getCurrentTenant();
        PosUser user = getPosUserById(id)
                .orElseThrow(() -> new RuntimeException("POS User not found with id: " + id));

        if (!user.getEmail().equals(userRequest.getEmail())) {
            posUserRepository.findByEmailAndTenantId(userRequest.getEmail(), currentTenant.getId())
                    .ifPresent(u -> {
                        throw new RuntimeException("Username '" + userRequest.getEmail() + "' is already taken.");
                    });
            user.setEmail(userRequest.getEmail());
        }

        user.setDisplayName(userRequest.getDisplayName());
        user.setEmail(userRequest.getEmail());
        user.setRole(userRequest.getRole());

        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        }

        if (userRequest.getStoreId() != null) {
            Store store = storeRepository.findByIdAndTenantId(userRequest.getStoreId(), currentTenant.getId())
                    .orElseThrow(() -> new RuntimeException("Store not found with id: " + userRequest.getStoreId()));
            user.setStore(store);
        } else {
            user.setStore(null);
        }

        return posUserRepository.save(user);
    }

    public void deletePosUser(Long id) {
        PosUser user = getPosUserById(id)
                .orElseThrow(() -> new RuntimeException("POS User not found with id: " + id));
        posUserRepository.delete(user);
    }
}