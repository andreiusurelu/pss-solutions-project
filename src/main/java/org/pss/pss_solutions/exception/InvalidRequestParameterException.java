package org.pss.pss_solutions.exception;

public class InvalidRequestParameterException extends IllegalArgumentException {
    public InvalidRequestParameterException(String message) {
        super(message);
    }
}
