package com.petmanager.repository;

import com.petmanager.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPetRepository extends JpaRepository<Pet, Long> {
}
