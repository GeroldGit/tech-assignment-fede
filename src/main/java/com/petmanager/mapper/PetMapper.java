package com.petmanager.mapper;

import com.petmanager.dto.PetRequest;
import com.petmanager.dto.PetResponse;
import com.petmanager.entity.Pet;
import org.springframework.stereotype.Component;

@Component
public class PetMapper {

    public Pet toEntity(final PetRequest request) {
        return new Pet(request.getName(), request.getSpecies(), request.getAge(), request.getOwnerName());
    }

    public PetResponse toResponse(final Pet pet) {
        return new PetResponse(pet.getId(), pet.getName(), pet.getSpecies(), pet.getAge(), pet.getOwnerName());
    }

    public void updateEntity(final Pet pet, final PetRequest request) {
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setAge(request.getAge());
        pet.setOwnerName(request.getOwnerName());
    }
}
