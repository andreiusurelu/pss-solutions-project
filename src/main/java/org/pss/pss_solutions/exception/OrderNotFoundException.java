package org.pss.pss_solutions.exception;

import jakarta.persistence.EntityNotFoundException;

public class OrderNotFoundException extends EntityNotFoundException {
    public OrderNotFoundException(String msg) {
        super(msg);
    }
}
