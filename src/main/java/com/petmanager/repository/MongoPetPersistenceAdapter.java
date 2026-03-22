package com.petmanager.repository;

import com.petmanager.document.PetDocument;
import com.petmanager.entity.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB adapter that implements the PetPersistencePort using Spring Data MongoDB.
 * Active only when the "mongo" Spring profile is enabled.
 *
 * <p>MongoDB uses {@code String} ObjectIds internally. This adapter converts between
 * the domain model's {@code Long} id and MongoDB's {@code String} id transparently,
 * keeping the service layer and the rest of the application completely unaware of
 * the underlying persistence technology.
 *
 * <p>To activate this adapter:
 * <pre>
 *   java -jar pet-manager.jar --spring.profiles.active=mongo
 * </pre>
 */
@Repository
@Profile("mongo")
@RequiredArgsConstructor
@Slf4j
public class MongoPetPersistenceAdapter implements PetPersistencePort {

    private final MongoPetRepository mongoPetRepository;

    @Override
    public Pet save(final Pet pet) {
        final PetDocument document = toDocument(pet);
        final PetDocument saved = mongoPetRepository.save(document);
        return toDomain(saved);
    }

    @Override
    public Optional<Pet> findById(final Long id) {
        return mongoPetRepository.findById(String.valueOf(id))
                .map(this::toDomain);
    }

    @Override
    public List<Pet> findAll() {
        return mongoPetRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(final Long id) {
        mongoPetRepository.deleteById(String.valueOf(id));
    }

    @Override
    public boolean existsById(final Long id) {
        return mongoPetRepository.existsById(String.valueOf(id));
    }

    private PetDocument toDocument(final Pet pet) {
        final String mongoId = pet.getId() != null ? String.valueOf(pet.getId()) : null;
        return new PetDocument(mongoId, pet.getName(), pet.getSpecies(), pet.getAge(), pet.getOwnerName());
    }

    private Pet toDomain(final PetDocument document) {
        final Pet pet = new Pet(document.getName(), document.getSpecies(),
                document.getAge(), document.getOwnerName());
        if (document.getId() != null) {
            try {
                pet.setId(Long.parseLong(document.getId()));
            } catch (final NumberFormatException e) {
                log.warn("Non-numeric MongoDB id '{}', using hashCode as fallback", document.getId(), e);
                pet.setId((long) (document.getId().hashCode() & 0x7fffffff));
            }
        }
        return pet;
    }
}
