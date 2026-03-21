package com.petmanager.exception;

public class PetNotFoundException extends RuntimeException {

    public PetNotFoundException(final Long id) {
        super("Pet not found with id: " + id);
    }
}
