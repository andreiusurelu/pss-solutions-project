package org.pss.pss_solutions.exception;

import org.springframework.beans.InvalidPropertyException;

public class InvalidOrderPropertyException extends IllegalArgumentException {
    public InvalidOrderPropertyException(String msg) {
        super(msg);
    }
}
