package com.petmanager.mapper;

import com.petmanager.dto.PetRequest;
import com.petmanager.dto.PetResponse;
import com.petmanager.entity.Pet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PetMapperTest {

    private PetMapper petMapper;

    @BeforeEach
    void setUp() {
        petMapper = new PetMapper();
    }

    @Test
    void toEntity_shouldMapRequestToEntity() {
        final PetRequest request = new PetRequest("Buddy", "Dog", 3, "John Doe");

        final Pet pet = petMapper.toEntity(request);

        assertThat(pet.getName()).isEqualTo("Buddy");
        assertThat(pet.getSpecies()).isEqualTo("Dog");
        assertThat(pet.getAge()).isEqualTo(3);
        assertThat(pet.getOwnerName()).isEqualTo("John Doe");
    }

    @Test
    void toResponse_shouldMapEntityToResponse() {
        final Pet pet = new Pet("Buddy", "Dog", 3, "John Doe");
        pet.setId(1L);

        final PetResponse response = petMapper.toResponse(pet);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Buddy");
        assertThat(response.getSpecies()).isEqualTo("Dog");
        assertThat(response.getAge()).isEqualTo(3);
        assertThat(response.getOwnerName()).isEqualTo("John Doe");
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        final PetRequest request = new PetRequest("Buddy", "Dog", null, null);

        final Pet pet = petMapper.toEntity(request);

        assertThat(pet.getAge()).isNull();
        assertThat(pet.getOwnerName()).isNull();
    }

    @Test
    void toResponse_shouldHandleNullOptionalFields() {
        final Pet pet = new Pet("Buddy", "Dog", null, null);
        pet.setId(1L);

        final PetResponse response = petMapper.toResponse(pet);

        assertThat(response.getAge()).isNull();
        assertThat(response.getOwnerName()).isNull();
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        final Pet pet = new Pet("Buddy", "Dog", 3, "John Doe");
        pet.setId(1L);
        final PetRequest updateRequest = new PetRequest("Max", "Cat", 5, "Jane Doe");

        petMapper.updateEntity(pet, updateRequest);

        assertThat(pet.getName()).isEqualTo("Max");
        assertThat(pet.getSpecies()).isEqualTo("Cat");
        assertThat(pet.getAge()).isEqualTo(5);
        assertThat(pet.getOwnerName()).isEqualTo("Jane Doe");
    }
}
