package com.petmanager.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ValidationErrorResponse {

    private int status;
    private String message;
    private List<FieldError> errors;
    private LocalDateTime timestamp;

    public ValidationErrorResponse() {
    }

    public ValidationErrorResponse(final int status, final String message,
                                   final List<FieldError> errors, final LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(final List<FieldError> errors) {
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static class FieldError {
        private String field;
        private String message;

        public FieldError() {
        }

        public FieldError(final String field, final String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(final String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(final String message) {
            this.message = message;
        }
    }
}
