package com.petmanager.service;

import com.petmanager.dto.PetRequest;
import com.petmanager.dto.PetResponse;

import java.util.List;

public interface PetService {

    PetResponse create(PetRequest request);

    List<PetResponse> findAll();

    PetResponse findById(Long id);

    PetResponse update(Long id, PetRequest request);

    void delete(Long id);
}
