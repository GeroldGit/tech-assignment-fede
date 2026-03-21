package com.petmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petmanager.config.SecurityConfig;
import com.petmanager.dto.PetRequest;
import com.petmanager.dto.PetResponse;
import com.petmanager.exception.GlobalExceptionHandler;
import com.petmanager.exception.PetNotFoundException;
import com.petmanager.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PetService petService;

    private PetResponse petResponse;
    private PetRequest petRequest;

    @BeforeEach
    void setUp() {
        petRequest = new PetRequest("Buddy", "Dog", 3, "John Doe");
        petResponse = new PetResponse(1L, "Buddy", "Dog", 3, "John Doe");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn201WithCreatedPet() throws Exception {
        when(petService.create(any(PetRequest.class))).thenReturn(petResponse);

        mockMvc.perform(post("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(petRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.species").value("Dog"))
                .andExpect(jsonPath("$.age").value(3))
                .andExpect(jsonPath("$.ownerName").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn400WhenNameIsBlank() throws Exception {
        final PetRequest invalidRequest = new PetRequest("", "Dog", 3, "John Doe");

        mockMvc.perform(post("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn400WhenSpeciesIsBlank() throws Exception {
        final PetRequest invalidRequest = new PetRequest("Buddy", "", 3, "John Doe");

        mockMvc.perform(post("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn400WhenAgeIsNegative() throws Exception {
        final PetRequest invalidRequest = new PetRequest("Buddy", "Dog", -1, "John Doe");

        mockMvc.perform(post("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_shouldReturn200WithListOfPets() throws Exception {
        final PetResponse response2 = new PetResponse(2L, "Whiskers", "Cat", 2, "Jane Doe");
        when(petService.findAll()).thenReturn(List.of(petResponse, response2));

        mockMvc.perform(get("/api/v1/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Buddy"))
                .andExpect(jsonPath("$[1].name").value("Whiskers"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void findById_shouldReturn200WhenPetExists() throws Exception {
        when(petService.findById(1L)).thenReturn(petResponse);

        mockMvc.perform(get("/api/v1/pets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void findById_shouldReturn404WhenPetNotFound() throws Exception {
        when(petService.findById(99L)).thenThrow(new PetNotFoundException(99L));

        mockMvc.perform(get("/api/v1/pets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Pet not found with id: 99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturn200WithUpdatedPet() throws Exception {
        final PetResponse updated = new PetResponse(1L, "Buddy Updated", "Dog", 4, "John Doe");
        when(petService.update(eq(1L), any(PetRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/pets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(petRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buddy Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturn404WhenPetNotFound() throws Exception {
        when(petService.update(eq(99L), any(PetRequest.class))).thenThrow(new PetNotFoundException(99L));

        mockMvc.perform(put("/api/v1/pets/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(petRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn204WhenPetDeleted() throws Exception {
        doNothing().when(petService).delete(1L);

        mockMvc.perform(delete("/api/v1/pets/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn404WhenPetNotFound() throws Exception {
        doThrow(new PetNotFoundException(99L)).when(petService).delete(99L);

        mockMvc.perform(delete("/api/v1/pets/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_shouldReturn500WhenUnexpectedExceptionOccurs() throws Exception {
        when(petService.findAll()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/pets"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void create_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(petRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_shouldReturn403WhenUserNotAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(petRequest)))
                .andExpect(status().isForbidden());
    }
}
