package com.petmanager.service;

import com.petmanager.dto.PetRequest;
import com.petmanager.dto.PetResponse;
import com.petmanager.entity.Pet;
import com.petmanager.exception.PetNotFoundException;
import com.petmanager.mapper.PetMapper;
import com.petmanager.repository.PetPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetPersistencePort persistencePort;
    private final PetMapper petMapper;

    @Override
    public PetResponse create(final PetRequest request) {
        final Pet pet = petMapper.toEntity(request);
        final Pet saved = persistencePort.save(pet);
        return petMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> findAll() {
        return persistencePort.findAll()
                .stream()
                .map(petMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PetResponse findById(final Long id) {
        final Pet pet = persistencePort.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));
        return petMapper.toResponse(pet);
    }

    @Override
    public PetResponse update(final Long id, final PetRequest request) {
        final Pet pet = persistencePort.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));
        petMapper.updateEntity(pet, request);
        final Pet updated = persistencePort.save(pet);
        return petMapper.toResponse(updated);
    }

    @Override
    public void delete(final Long id) {
        if (!persistencePort.existsById(id)) {
            throw new PetNotFoundException(id);
        }
        persistencePort.deleteById(id);
    }
}
