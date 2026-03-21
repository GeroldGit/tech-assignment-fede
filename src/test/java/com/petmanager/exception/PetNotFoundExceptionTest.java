package com.petmanager.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PetNotFoundExceptionTest {

    @Test
    void constructor_shouldSetMessageWithId() {
        final PetNotFoundException exception = new PetNotFoundException(42L);

        assertThat(exception.getMessage()).isEqualTo("Pet not found with id: 42");
    }
}
