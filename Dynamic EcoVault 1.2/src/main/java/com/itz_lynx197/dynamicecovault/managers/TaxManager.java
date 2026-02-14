package com.itz_lynx197.dynamicecovault.managers;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TaxManager {

    private final DynamicEcoVault plugin;
    private final Map<TaxType, Double> taxRates;

    public enum TaxType {
        BUY("buy"),
        SELL("sell"),
        GIVE("give"),
        RECEIVE("receive");

        private final String configKey;

        TaxType(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigKey() {
            return configKey;
        }
    }

    public TaxManager(DynamicEcoVault plugin) {
        this.plugin = plugin;
        this.taxRates = new HashMap<>();
        loadTaxRates();
    }

    private void loadTaxRates() {
        for (TaxType type : TaxType.values()) {
            double rate = plugin.getConfigManager().getConfig().getDouble("taxation." + type.getConfigKey(), 0.0);
            taxRates.put(type, rate);
        }
    }

    public double getTaxRate(TaxType type) {
        return taxRates.getOrDefault(type, 0.0);
    }

    public void setTaxRate(TaxType type, double rate) {
        taxRates.put(type, rate);
        plugin.getConfigManager().getConfig().set("taxation." + type.getConfigKey(), rate);
        plugin.saveConfig();
    }

    public double calculateTax(double amount, TaxType type) {
        double rate = getTaxRate(type);
        return amount * rate;
    }

    public void collectTax(Player player, double amount, TaxType type) {
        collectTax(player, amount, type, type.name());
    }

    public void collectTax(Player player, double amount, TaxType type, String detail) {
        double tax = calculateTax(amount, type);

        if (tax <= 0) {
            return;
        }

        // Deduct tax from player
        if (plugin.getEconomy().has(player, tax)) {
            plugin.getEconomy().withdrawPlayer(player, tax);

            // Add to bank vault
            plugin.getBankManager().depositToBankVault(tax, "Tax: " + detail);

            // Notify player
            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.format("%.2f", tax));

            String message = plugin.getConfigManager().getMessage("tax.collected-" + type.getConfigKey(), replacements);
            player.sendMessage(message);

            // Discord notification
            if (plugin.getDiscordIntegration() != null && plugin.getConfigManager().getConfig().getBoolean("discord.notify-tax", true)) {
                plugin.getDiscordIntegration().sendTaxNotification(tax, detail, player.getName());
            }
        }
    }

    public double getAmountAfterTax(double amount, TaxType type) {
        double tax = calculateTax(amount, type);
        return amount - tax;
    }

    public double getAmountWithTax(double amount, TaxType type) {
        double tax = calculateTax(amount, type);
        return amount + tax;
    }

    public double calculateBuyTax(double amount) {
        return calculateTax(amount, TaxType.BUY);
    }

    public void collectBuyTax(Player player, double tax, String itemName) {
        collectTax(player, tax, TaxType.BUY, itemName);
    }

    public double calculateSellTax(double amount) {
        return calculateTax(amount, TaxType.SELL);
    }

    public void collectSellTax(Player player, double tax, String itemName) {
        collectTax(player, tax, TaxType.SELL, itemName);
    }
}
