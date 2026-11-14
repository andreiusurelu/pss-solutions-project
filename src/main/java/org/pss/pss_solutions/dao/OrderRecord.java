package org.pss.pss_solutions.dao;

public record OrderRecord(String customerName, Double amount) {
    public OrderRecord(String customerName, Double amount) {
        this.customerName = customerName;
        this.amount = amount;
    }
}
