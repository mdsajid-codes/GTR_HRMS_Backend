package com.example.multi_tanent.spersusers.repository;

import com.example.multi_tanent.spersusers.enitity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
    // Custom query methods can be added here if needed
}