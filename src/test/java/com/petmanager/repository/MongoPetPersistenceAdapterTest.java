package com.petmanager.repository;

import com.petmanager.document.PetDocument;
import com.petmanager.entity.Pet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoPetPersistenceAdapterTest {

    @Mock
    private MongoPetRepository mongoPetRepository;

    @InjectMocks
    private MongoPetPersistenceAdapter adapter;

    private PetDocument petDocument;
    private Pet pet;

    @BeforeEach
    void setUp() {
        petDocument = new PetDocument("1", "Buddy", "Dog", 3, "John Doe");
        pet = new Pet("Buddy", "Dog", 3, "John Doe");
        pet.setId(1L);
    }

    @Test
    void save_shouldPersistDocumentAndReturnDomainObject() {
        when(mongoPetRepository.save(any(PetDocument.class))).thenReturn(petDocument);

        final Pet result = adapter.save(pet);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Buddy");
        assertThat(result.getSpecies()).isEqualTo("Dog");
        assertThat(result.getAge()).isEqualTo(3);
        assertThat(result.getOwnerName()).isEqualTo("John Doe");
        verify(mongoPetRepository).save(any(PetDocument.class));
    }

    @Test
    void save_shouldHandlePetWithNullId() {
        final Pet petWithNoId = new Pet("Max", "Cat", 2, null);
        final PetDocument savedDoc = new PetDocument("abc123", "Max", "Cat", 2, null);
        when(mongoPetRepository.save(any(PetDocument.class))).thenReturn(savedDoc);

        final Pet result = adapter.save(petWithNoId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Max");
    }

    @Test
    void findById_shouldReturnDomainObjectWhenDocumentExists() {
        when(mongoPetRepository.findById("1")).thenReturn(Optional.of(petDocument));

        final Optional<Pet> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Buddy");
    }

    @Test
    void findById_shouldReturnEmptyWhenDocumentNotFound() {
        when(mongoPetRepository.findById("99")).thenReturn(Optional.empty());

        final Optional<Pet> result = adapter.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllDocumentsAsDomainObjects() {
        final PetDocument doc2 = new PetDocument("2", "Whiskers", "Cat", 2, "Jane Doe");
        when(mongoPetRepository.findAll()).thenReturn(List.of(petDocument, doc2));

        final List<Pet> results = adapter.findAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Buddy");
        assertThat(results.get(1).getName()).isEqualTo("Whiskers");
    }

    @Test
    void deleteById_shouldDelegateToRepository() {
        adapter.deleteById(1L);

        verify(mongoPetRepository).deleteById("1");
    }

    @Test
    void existsById_shouldReturnTrueWhenDocumentExists() {
        when(mongoPetRepository.existsById("1")).thenReturn(true);

        assertThat(adapter.existsById(1L)).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseWhenDocumentNotFound() {
        when(mongoPetRepository.existsById("99")).thenReturn(false);

        assertThat(adapter.existsById(99L)).isFalse();
    }

    @Test
    void toDomain_shouldHandleNonNumericMongoId() {
        final PetDocument docWithObjectId = new PetDocument("507f1f77bcf86cd799439011", "Rex", "Dog", 1, null);
        when(mongoPetRepository.save(any(PetDocument.class))).thenReturn(docWithObjectId);

        final Pet petToSave = new Pet("Rex", "Dog", 1, null);
        final Pet result = adapter.save(petToSave);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Rex");
        assertThat(result.getId()).isNotNull();
    }
}
