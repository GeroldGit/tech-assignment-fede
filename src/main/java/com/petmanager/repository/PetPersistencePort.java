package com.petmanager.repository;

import com.petmanager.entity.Pet;

import java.util.List;
import java.util.Optional;

/**
 * Port defining persistence operations for the Pet domain.
 * Implement this interface to provide a different persistence adapter
 * (e.g., MongoDB, Cassandra) without modifying the service layer.
 */
public interface PetPersistencePort {

    Pet save(Pet pet);

    Optional<Pet> findById(Long id);

    List<Pet> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
