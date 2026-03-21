package com.petmanager.repository;

import com.petmanager.document.PetDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the {@link PetDocument} collection.
 * Active only when the "mongo" profile is enabled.
 */
@Profile("mongo")
public interface MongoPetRepository extends MongoRepository<PetDocument, String> {
}
