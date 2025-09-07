package com.example.multi_tanent.tenant.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.multi_tanent.tenant.entity.Designation;
import com.example.multi_tanent.tenant.repository.DesignationRepository;
import com.example.multi_tanent.tenant.tenantDto.DesignationRequest;

@RestController
@RequestMapping("/api/designations")
@CrossOrigin(origins = "*")
public class DesignationController {
    private final DesignationRepository designationRepository;

    public DesignationController(DesignationRepository designationRepository){
        this.designationRepository = designationRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<Designation> createDesignation(@RequestBody DesignationRequest designationRequest){
        Designation designation = new Designation();
        designation.setTitle(designationRequest.getTitle());
        designation.setLevel(designationRequest.getLevel());
        designation.setDescription(designationRequest.getDescription());
        designation.setCreatedAt(LocalDateTime.now());
        designation.setUpdatedAt(LocalDateTime.now());
        Designation savDesignation = designationRepository.save(designation);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(savDesignation.getId()).toUri();
        
        return ResponseEntity.created(location).body(savDesignation);
    }

    @GetMapping
    public ResponseEntity<List<Designation>> getDesignations(){
        return ResponseEntity.ok(designationRepository.findAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<String> updateDesignation(@PathVariable Long id, @RequestBody DesignationRequest designationRequest){
        Optional<Designation> existingDesignation = designationRepository.findById(id);
        if(existingDesignation.isPresent()){
            Designation designation = existingDesignation.get();
            designation.setTitle(designationRequest.getTitle());
            designation.setLevel(designationRequest.getLevel());
            designation.setDescription(designationRequest.getDescription());
            designation.setUpdatedAt(LocalDateTime.now());
            designationRepository.save(designation);

            return ResponseEntity.ok("Designation updated successfully");
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<String> deleteDesignation(@PathVariable Long id){
        if (designationRepository.existsById(id)) {
            designationRepository.deleteById(id);
            return ResponseEntity.ok("Designation deleted successfully!");
        }
        return ResponseEntity.notFound().build();
    }
}
