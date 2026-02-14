package com.itz_lynx197.dynamicecovault.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BankVaultUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    private final double newBalance;
    private final double changeAmount;
    private final String reason;

    public BankVaultUpdateEvent(double newBalance, double changeAmount, String reason) {
        this.newBalance = newBalance;
        this.changeAmount = changeAmount;
        this.reason = reason;
    }

    public double getNewBalance() {
        return newBalance;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
