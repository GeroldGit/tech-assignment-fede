package com.petmanager.repository;

import com.petmanager.entity.Pet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("dev")
@Import(JpaPetPersistenceAdapter.class)
class JpaPetPersistenceAdapterTest {

    @Autowired
    private JpaPetPersistenceAdapter adapter;

    @Test
    void save_shouldPersistPet() {
        final Pet pet = new Pet("Buddy", "Dog", 3, "John Doe");

        final Pet saved = adapter.save(pet);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Buddy");
    }

    @Test
    void findById_shouldReturnPetWhenExists() {
        final Pet pet = new Pet("Buddy", "Dog", 3, "John Doe");
        final Pet saved = adapter.save(pet);

        final Optional<Pet> result = adapter.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Buddy");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        final Optional<Pet> result = adapter.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllPets() {
        adapter.save(new Pet("Buddy", "Dog", 3, "John Doe"));
        adapter.save(new Pet("Whiskers", "Cat", 2, "Jane Doe"));

        final List<Pet> pets = adapter.findAll();

        assertThat(pets).hasSize(2);
    }

    @Test
    void deleteById_shouldRemovePet() {
        final Pet saved = adapter.save(new Pet("Buddy", "Dog", 3, "John Doe"));

        adapter.deleteById(saved.getId());

        assertThat(adapter.existsById(saved.getId())).isFalse();
    }

    @Test
    void existsById_shouldReturnTrueWhenExists() {
        final Pet saved = adapter.save(new Pet("Buddy", "Dog", 3, "John Doe"));

        assertThat(adapter.existsById(saved.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseWhenNotExists() {
        assertThat(adapter.existsById(999L)).isFalse();
    }
}
