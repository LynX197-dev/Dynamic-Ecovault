package com.itz_lynx197.dynamicecovault.integrations;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import com.itz_lynx197.dynamicecovault.managers.TaxManager;
import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.api.events.PreTransactionEvent;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class EconomyShopGUIIntegration implements Listener {

    private final DynamicEcoVault plugin;

    public EconomyShopGUIIntegration(DynamicEcoVault plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreTransaction(PreTransactionEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("integrations.economyshopgui.enabled", true)) {
            return;
        }

        // Skip if multiple items (for simplicity)
        if (event.getItems().size() != 1) {
            return;
        }

        ItemStack item = event.getItemStack();
        if (item == null) return;
        Material material = item.getType();
        int quantity = event.getAmount();
        boolean isBuy = event.getTransactionType().toString().equals("BUY");
        double totalBasePrice = event.getPrice();
        double perItemBasePrice = totalBasePrice / quantity;

        // Apply dynamic pricing
        double adjustedPerItemPrice;
        if (isBuy) {
            adjustedPerItemPrice = plugin.getEconomyManager().getAdjustedBuyPrice(material, perItemBasePrice);
        } else {
            adjustedPerItemPrice = plugin.getEconomyManager().getAdjustedSellPrice(material, perItemBasePrice);
        }

        double totalAdjustedPrice = adjustedPerItemPrice * quantity;

        // Apply tax
        TaxManager.TaxType taxType = isBuy ? TaxManager.TaxType.BUY : TaxManager.TaxType.SELL;
        double taxRate = plugin.getTaxManager().getTaxRate(taxType);
        double totalPriceWithTax = totalAdjustedPrice + (totalAdjustedPrice * taxRate);

        event.setPrice(totalPriceWithTax);
    }

    @EventHandler
    public void onPostTransaction(PostTransactionEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("integrations.economyshopgui.enabled", true)) {
            return;
        }

        // Skip if multiple items (for simplicity)
        if (event.getItems().size() != 1) {
            return;
        }

        ItemStack item = event.getItemStack();
        if (item == null) return;
        Material material = item.getType();
        int quantity = event.getAmount();
        boolean isBuy = event.getTransactionType().toString().equals("BUY");
        double totalPriceWithTax = event.getPrice();

        // Apply tax rate again to calculate tax from price
        TaxManager.TaxType taxType = isBuy ? TaxManager.TaxType.BUY : TaxManager.TaxType.SELL;
        double taxRate = plugin.getTaxManager().getTaxRate(taxType);
        double tax = totalPriceWithTax * taxRate / (1 + taxRate);

        // Record transaction for supply/demand
        if (isBuy) {
            plugin.getEconomyManager().recordBuy(material, quantity);
        } else {
            plugin.getEconomyManager().recordSell(material, quantity);
        }

        // Deposit tax to bank vault
        if (tax > 0) {
            plugin.getBankManager().depositToBankVault(tax, "Tax from EconomyShopGUI transaction: " + material.name() + " x" + quantity);
        }
    }
}