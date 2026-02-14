package com.itz_lynx197.dynamicecovault.integrations;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import com.itz_lynx197.dynamicecovault.models.Loan;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlaceholderIntegration extends PlaceholderExpansion {

    private final DynamicEcoVault plugin;

    public PlaceholderIntegration(DynamicEcoVault plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dynamicecovault";
    }

    @Override
    public @NotNull String getAuthor() {
        return "𝕃ʏи𝕏";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // %dynamicecovault_balance%
        if (params.equals("balance")) {
            return String.format("%.2f", plugin.getEconomy().getBalance(player));
        }

        // %dynamicecovault_bankvault%
        if (params.equals("bankvault")) {
            return String.format("%.2f", plugin.getBankManager().getBankVaultBalance());
        }

        // %dynamicecovault_loan_active%
        if (params.equals("loan_active")) {
            return plugin.getLoanManager().hasActiveLoan(player.getUniqueId()) ? "Yes" : "No";
        }

        // %dynamicecovault_loan_amount%
        if (params.equals("loan_amount")) {
            Loan loan = plugin.getLoanManager().getActiveLoan(player.getUniqueId());
            return loan != null ? String.format("%.2f", loan.getOriginalAmount()) : "0.00";
        }

        // %dynamicecovault_loan_remaining%
        if (params.equals("loan_remaining")) {
            Loan loan = plugin.getLoanManager().getActiveLoan(player.getUniqueId());
            return loan != null ? String.format("%.2f", loan.getRemainingAmount()) : "0.00";
        }

        // %dynamicecovault_loan_duedate%
        if (params.equals("loan_duedate")) {
            Loan loan = plugin.getLoanManager().getActiveLoan(player.getUniqueId());
            if (loan != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
                return sdf.format(new Date(loan.getDueDate()));
            }
            return "N/A";
        }

        // %dynamicecovault_tax_buy%
        if (params.equals("tax_buy")) {
            double rate = plugin.getTaxManager().getTaxRate(com.itz_lynx197.dynamicecovault.managers.TaxManager.TaxType.BUY);
            return String.format("%.2f%%", rate * 100);
        }

        // %dynamicecovault_tax_sell%
        if (params.equals("tax_sell")) {
            double rate = plugin.getTaxManager().getTaxRate(com.itz_lynx197.dynamicecovault.managers.TaxManager.TaxType.SELL);
            return String.format("%.2f%%", rate * 100);
        }

        // %dynamicecovault_tax_give%
        if (params.equals("tax_give")) {
            double rate = plugin.getTaxManager().getTaxRate(com.itz_lynx197.dynamicecovault.managers.TaxManager.TaxType.GIVE);
            return String.format("%.2f%%", rate * 100);
        }

        // %dynamicecovault_tax_receive%
        if (params.equals("tax_receive")) {
            double rate = plugin.getTaxManager().getTaxRate(com.itz_lynx197.dynamicecovault.managers.TaxManager.TaxType.RECEIVE);
            return String.format("%.2f%%", rate * 100);
        }

        return null;
    }
}
