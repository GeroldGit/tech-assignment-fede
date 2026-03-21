package com.petmanager.repository;

import com.petmanager.entity.Pet;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter that implements the PetPersistencePort using Spring Data JPA.
 * To switch to a different database (e.g., MongoDB), create a new adapter
 * implementing PetPersistencePort and register it as the primary bean.
 */
@Repository
public class JpaPetPersistenceAdapter implements PetPersistencePort {

    private final JpaPetRepository jpaPetRepository;

    public JpaPetPersistenceAdapter(final JpaPetRepository jpaPetRepository) {
        this.jpaPetRepository = jpaPetRepository;
    }

    @Override
    public Pet save(final Pet pet) {
        return jpaPetRepository.save(pet);
    }

    @Override
    public Optional<Pet> findById(final Long id) {
        return jpaPetRepository.findById(id);
    }

    @Override
    public List<Pet> findAll() {
        return jpaPetRepository.findAll();
    }

    @Override
    public void deleteById(final Long id) {
        jpaPetRepository.deleteById(id);
    }

    @Override
    public boolean existsById(final Long id) {
        return jpaPetRepository.existsById(id);
    }
}
