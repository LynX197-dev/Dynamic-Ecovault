package com.itz_lynx197.dynamicecovault.models;

import java.util.UUID;

public class Loan {

    private final UUID playerId;
    private final double originalAmount;
    private final double interest;
    private final double totalAmount;
    private double remainingAmount;
    private final long dueDate;

    public Loan(UUID playerId, double originalAmount, double interest, double totalAmount, long dueDate) {
        this.playerId = playerId;
        this.originalAmount = originalAmount;
        this.interest = interest;
        this.totalAmount = totalAmount;
        this.remainingAmount = totalAmount;
        this.dueDate = dueDate;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public double getInterest() {
        return interest;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public long getDueDate() {
        return dueDate;
    }

    public boolean isOverdue() {
        return System.currentTimeMillis() >= dueDate;
    }
}
