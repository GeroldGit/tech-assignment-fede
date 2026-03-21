package com.petmanager.service;

import com.petmanager.dto.PetRequest;
import com.petmanager.dto.PetResponse;
import com.petmanager.entity.Pet;
import com.petmanager.exception.PetNotFoundException;
import com.petmanager.mapper.PetMapper;
import com.petmanager.repository.PetPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceImplTest {

    @Mock
    private PetPersistencePort persistencePort;

    @Mock
    private PetMapper petMapper;

    @InjectMocks
    private PetServiceImpl petService;

    private Pet pet;
    private PetRequest petRequest;
    private PetResponse petResponse;

    @BeforeEach
    void setUp() {
        pet = new Pet("Buddy", "Dog", 3, "John Doe");
        pet.setId(1L);
        petRequest = new PetRequest("Buddy", "Dog", 3, "John Doe");
        petResponse = new PetResponse(1L, "Buddy", "Dog", 3, "John Doe");
    }

    @Test
    void create_shouldSavePetAndReturnResponse() {
        when(petMapper.toEntity(petRequest)).thenReturn(pet);
        when(persistencePort.save(pet)).thenReturn(pet);
        when(petMapper.toResponse(pet)).thenReturn(petResponse);

        final PetResponse result = petService.create(petRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Buddy");
        verify(persistencePort).save(pet);
    }

    @Test
    void findAll_shouldReturnListOfPetResponses() {
        final Pet pet2 = new Pet("Whiskers", "Cat", 2, "Jane Doe");
        pet2.setId(2L);
        final PetResponse response2 = new PetResponse(2L, "Whiskers", "Cat", 2, "Jane Doe");

        when(persistencePort.findAll()).thenReturn(List.of(pet, pet2));
        when(petMapper.toResponse(pet)).thenReturn(petResponse);
        when(petMapper.toResponse(pet2)).thenReturn(response2);

        final List<PetResponse> results = petService.findAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Buddy");
        assertThat(results.get(1).getName()).isEqualTo("Whiskers");
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoPets() {
        when(persistencePort.findAll()).thenReturn(List.of());

        final List<PetResponse> results = petService.findAll();

        assertThat(results).isEmpty();
    }

    @Test
    void findById_shouldReturnPetResponseWhenFound() {
        when(persistencePort.findById(1L)).thenReturn(Optional.of(pet));
        when(petMapper.toResponse(pet)).thenReturn(petResponse);

        final PetResponse result = petService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrowPetNotFoundExceptionWhenMissing() {
        when(persistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.findById(99L))
                .isInstanceOf(PetNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldUpdateAndReturnResponse() {
        final PetRequest updateRequest = new PetRequest("Buddy Updated", "Dog", 4, "John Doe");
        final Pet updatedPet = new Pet("Buddy Updated", "Dog", 4, "John Doe");
        updatedPet.setId(1L);
        final PetResponse updatedResponse = new PetResponse(1L, "Buddy Updated", "Dog", 4, "John Doe");

        when(persistencePort.findById(1L)).thenReturn(Optional.of(pet));
        doNothing().when(petMapper).updateEntity(pet, updateRequest);
        when(persistencePort.save(pet)).thenReturn(updatedPet);
        when(petMapper.toResponse(updatedPet)).thenReturn(updatedResponse);

        final PetResponse result = petService.update(1L, updateRequest);

        assertThat(result.getName()).isEqualTo("Buddy Updated");
        verify(petMapper).updateEntity(pet, updateRequest);
        verify(persistencePort).save(pet);
    }

    @Test
    void update_shouldThrowPetNotFoundExceptionWhenMissing() {
        when(persistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.update(99L, petRequest))
                .isInstanceOf(PetNotFoundException.class)
                .hasMessageContaining("99");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void delete_shouldDeletePetWhenExists() {
        when(persistencePort.existsById(1L)).thenReturn(true);
        doNothing().when(persistencePort).deleteById(1L);

        petService.delete(1L);

        verify(persistencePort).deleteById(1L);
    }

    @Test
    void delete_shouldThrowPetNotFoundExceptionWhenMissing() {
        when(persistencePort.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> petService.delete(99L))
                .isInstanceOf(PetNotFoundException.class)
                .hasMessageContaining("99");

        verify(persistencePort, never()).deleteById(any());
    }
}
