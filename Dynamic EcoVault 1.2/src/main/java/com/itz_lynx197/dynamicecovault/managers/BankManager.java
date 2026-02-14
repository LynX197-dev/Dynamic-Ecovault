package com.itz_lynx197.dynamicecovault.managers;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import com.itz_lynx197.dynamicecovault.events.BankVaultUpdateEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class BankManager {

    private final DynamicEcoVault plugin;
    private final Economy economy;
    private final String bankVaultName;

    public BankManager(DynamicEcoVault plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.bankVaultName = plugin.getConfigManager().getConfig().getString("bank-vault-name", "BankVault");
        
        // Initialize bank vault account if needed
        initializeBankVault();
    }

    private void initializeBankVault() {
        // Create offline player for bank vault
        OfflinePlayer bankVault = Bukkit.getOfflinePlayer(bankVaultName);
        
        // Check if account exists, if not Vault will handle it
        if (!economy.hasAccount(bankVault)) {
            economy.createPlayerAccount(bankVault);
            plugin.getLogger().info("Created bank vault account: " + bankVaultName);
        }
    }

    public double getBankVaultBalance() {
        OfflinePlayer bankVault = Bukkit.getOfflinePlayer(bankVaultName);
        return economy.getBalance(bankVault);
    }

    public void depositToBankVault(double amount, String reason) {
        OfflinePlayer bankVault = Bukkit.getOfflinePlayer(bankVaultName);
        economy.depositPlayer(bankVault, amount);
        
        // Fire event for Discord integration
        BankVaultUpdateEvent event = new BankVaultUpdateEvent(getBankVaultBalance(), amount, reason);
        Bukkit.getPluginManager().callEvent(event);
    }

    public boolean withdrawFromBankVault(double amount, String reason) {
        OfflinePlayer bankVault = Bukkit.getOfflinePlayer(bankVaultName);
        
        if (economy.getBalance(bankVault) >= amount) {
            economy.withdrawPlayer(bankVault, amount);
            
            // Fire event for Discord integration
            BankVaultUpdateEvent event = new BankVaultUpdateEvent(getBankVaultBalance(), -amount, reason);
            Bukkit.getPluginManager().callEvent(event);
            
            return true;
        }
        
        return false;
    }

    public boolean hasSufficientFunds(double amount) {
        return getBankVaultBalance() >= amount;
    }

    public String getBankVaultName() {
        return bankVaultName;
    }
}
