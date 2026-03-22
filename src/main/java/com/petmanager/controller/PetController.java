package com.petmanager.controller;

import com.petmanager.dto.PetRequest;
import com.petmanager.dto.PetResponse;
import com.petmanager.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    public ResponseEntity<PetResponse> create(@Valid @RequestBody final PetRequest request) {
        final PetResponse response = petService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PetResponse>> findAll() {
        return ResponseEntity.ok(petService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(petService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> update(@PathVariable final Long id,
                                               @Valid @RequestBody final PetRequest request) {
        return ResponseEntity.ok(petService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        petService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
