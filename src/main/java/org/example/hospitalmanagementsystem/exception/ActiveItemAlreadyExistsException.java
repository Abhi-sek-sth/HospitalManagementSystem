package org.example.hospitalmanagementsystem.exception;

public class ActiveItemAlreadyExistsException extends RuntimeException {
    public ActiveItemAlreadyExistsException( Long prescriptionId,
                                             Long medicationId,
                                             Throwable cause) {
        super( "Medication ID " + medicationId + " already exists in prescription ID " + prescriptionId, cause);
    }
}
