package com.itz_lynx197.dynamicecovault.managers;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import com.itz_lynx197.dynamicecovault.models.Loan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoanManager {

    private final DynamicEcoVault plugin;
    private final Map<UUID, Loan> activeLoans;
    private BukkitTask loanCheckerTask;

    public LoanManager(DynamicEcoVault plugin) {
        this.plugin = plugin;
        this.activeLoans = new HashMap<>();
        loadLoansFromDatabase();
    }

    private void loadLoansFromDatabase() {
        // Load all active loans from database
        activeLoans.putAll(plugin.getDatabaseManager().loadAllLoans());
    }

    public boolean hasActiveLoan(UUID playerId) {
        return activeLoans.containsKey(playerId);
    }

    public Loan getActiveLoan(UUID playerId) {
        return activeLoans.get(playerId);
    }

    public boolean requestLoan(Player player, double amount) {
        if (hasActiveLoan(player.getUniqueId())) {
            return false;
        }

        double minAmount = plugin.getConfigManager().getConfig().getDouble("loans.minimum-amount", 100.0);
        double maxAmount = plugin.getConfigManager().getConfig().getDouble("loans.maximum-amount", 50000.0);

        if (amount < minAmount || amount > maxAmount) {
            return false;
        }

        // Check if bank vault has sufficient funds
        if (!plugin.getBankManager().hasSufficientFunds(amount)) {
            return false;
        }

        // Calculate interest
        double interestRate = plugin.getConfigManager().getConfig().getDouble("loans.interest-rate", 0.15);
        double interest = amount * interestRate;
        double totalRepayment = amount + interest;

        // Calculate due date (in-game days)
        int repaymentDays = plugin.getConfigManager().getConfig().getInt("loans.repayment-days", 7);
        long dueDate = System.currentTimeMillis() + (repaymentDays * 20 * 60 * 1000); // 1 day = 20 minutes

        // Create loan
        Loan loan = new Loan(player.getUniqueId(), amount, interest, totalRepayment, dueDate);
        activeLoans.put(player.getUniqueId(), loan);

        // Save to database
        plugin.getDatabaseManager().saveLoan(loan);

        // Withdraw from bank vault
        plugin.getBankManager().withdrawFromBankVault(amount, "Loan to " + player.getName());

        // Deposit to player
        plugin.getEconomy().depositPlayer(player, amount);

        // Discord notification
        if (plugin.getDiscordIntegration() != null && plugin.getConfigManager().getConfig().getBoolean("discord.notify-loans", true)) {
            plugin.getDiscordIntegration().sendLoanNotification(amount, player.getName(), true);
        }

        return true;
    }

    public boolean repayLoan(Player player, double amount) {
        Loan loan = getActiveLoan(player.getUniqueId());
        if (loan == null) {
            return false;
        }

        // Check if player has enough money
        if (!plugin.getEconomy().has(player, amount)) {
            return false;
        }

        // Withdraw from player
        plugin.getEconomy().withdrawPlayer(player, amount);

        // Add to bank vault
        plugin.getBankManager().depositToBankVault(amount, "Loan repayment from " + player.getName());

        // Update loan
        loan.setRemainingAmount(loan.getRemainingAmount() - amount);

        if (loan.getRemainingAmount() <= 0) {
            // Loan fully repaid
            activeLoans.remove(player.getUniqueId());
            plugin.getDatabaseManager().deleteLoan(player.getUniqueId());

            // Discord notification
            if (plugin.getDiscordIntegration() != null && plugin.getConfigManager().getConfig().getBoolean("discord.notify-loans", true)) {
                plugin.getDiscordIntegration().sendLoanNotification(loan.getTotalAmount(), player.getName(), false);
            }
        } else {
            // Update database
            plugin.getDatabaseManager().updateLoan(loan);
        }

        return true;
    }

    public void startLoanChecker() {
        // Check loans every 5 minutes
        loanCheckerTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkOverdueLoans, 6000L, 6000L);
    }

    public void stopLoanChecker() {
        if (loanCheckerTask != null) {
            loanCheckerTask.cancel();
        }
    }

    private void checkOverdueLoans() {
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<UUID, Loan> entry : new HashMap<>(activeLoans).entrySet()) {
            UUID playerId = entry.getKey();
            Loan loan = entry.getValue();

            if (currentTime >= loan.getDueDate()) {
                // Loan is overdue, auto-deduct
                Player player = Bukkit.getPlayer(playerId);
                double remaining = loan.getRemainingAmount();

                if (player != null && player.isOnline()) {
                    // Try to deduct full amount
                    if (plugin.getEconomy().has(player, remaining)) {
                        plugin.getEconomy().withdrawPlayer(player, remaining);
                        plugin.getBankManager().depositToBankVault(remaining, "Auto-repayment from " + player.getName());

                        Map<String, String> replacements = new HashMap<>();
                        replacements.put("amount", String.format("%.2f", remaining));
                        player.sendMessage(plugin.getConfigManager().getMessage("loan.auto-repaid", replacements));
                    } else {
                        // Force deduction (account goes negative)
                        double balance = plugin.getEconomy().getBalance(player);
                        plugin.getEconomy().withdrawPlayer(player, balance);
                        plugin.getBankManager().depositToBankVault(balance, "Partial auto-repayment from " + player.getName());

                        Map<String, String> replacements = new HashMap<>();
                        replacements.put("amount", String.format("%.2f", remaining));
                        player.sendMessage(plugin.getConfigManager().getMessage("loan.overdue", replacements));
                    }
                }

                // Remove loan
                activeLoans.remove(playerId);
                plugin.getDatabaseManager().deleteLoan(playerId);
            }
        }
    }

    public Map<UUID, Loan> getActiveLoans() {
        return new HashMap<>(activeLoans);
    }
}
