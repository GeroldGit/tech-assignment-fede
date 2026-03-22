package com.petmanager.repository;

import com.petmanager.entity.Pet;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter that implements the PetPersistencePort using Spring Data JPA.
 * Active for all Spring profiles except "mongo".
 * To switch to MongoDB, activate the "mongo" profile — this adapter will be
 * disabled automatically and {@link MongoPetPersistenceAdapter} will be used instead.
 */
@Repository
@Profile("!mongo")
@RequiredArgsConstructor
public class JpaPetPersistenceAdapter implements PetPersistencePort {

    private final JpaPetRepository jpaPetRepository;

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
