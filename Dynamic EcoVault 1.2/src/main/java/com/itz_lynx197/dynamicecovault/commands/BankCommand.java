package com.itz_lynx197.dynamicecovault.commands;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import com.itz_lynx197.dynamicecovault.managers.TaxManager;
import com.itz_lynx197.dynamicecovault.models.Loan;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class BankCommand implements CommandExecutor, TabCompleter {

    private final DynamicEcoVault plugin;

    public BankCommand(DynamicEcoVault plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usage.bank", null));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "balance" -> handleBalance(sender);
            case "deposit" -> handleDeposit(sender, args);
            case "withdraw" -> handleWithdraw(sender, args);
            case "loan" -> handleLoan(sender, args);
            case "repay" -> handleRepay(sender, args);
            case "settax" -> handleSetTax(sender, args);
            case "vaultbalance" -> handleVaultBalance(sender);
            case "es" -> handleES(sender, args);
            case "taxinfo" -> handleTaxInfo(sender);
            default -> sender.sendMessage(plugin.getConfigManager().getMessage("usage.bank", null));
        }

        return true;
    }

    private void handleBalance(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only", null));
            return;
        }

        if (!player.hasPermission("dynamicecovault.bank.balance")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return;
        }

        double balance = plugin.getEconomy().getBalance(player);
        Map<String, String> replacements = new HashMap<>();
        replacements.put("balance", String.format("%.2f", balance));
        player.sendMessage(plugin.getConfigManager().getMessage("bank.balance", replacements));
    }

    private void handleDeposit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only", null));
            return;
        }

        if (!player.hasPermission("dynamicecovault.bank.deposit")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage.bank-deposit", null));
            return;
        }

        try {
            double amount = Double.parseDouble(args[1]);
            
            if (amount <= 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", null));
                return;
            }

            if (!plugin.getEconomy().has(player, amount)) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("amount", String.format("%.2f", amount));
                player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds", replacements));
                return;
            }

            plugin.getEconomy().withdrawPlayer(player, amount);
            plugin.getEconomy().depositPlayer(player, amount);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.format("%.2f", amount));
            player.sendMessage(plugin.getConfigManager().getMessage("bank.deposit-success", replacements));

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", null));
        }
    }

    private void handleWithdraw(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only", null));
            return;
        }

        if (!player.hasPermission("dynamicecovault.bank.withdraw")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage.bank-withdraw", null));
            return;
        }

        try {
            double amount = Double.parseDouble(args[1]);
            
            if (amount <= 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", null));
                return;
            }

            if (!plugin.getEconomy().has(player, amount)) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("amount", String.format("%.2f", amount));
                player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds", replacements));
                return;
            }

            plugin.getEconomy().withdrawPlayer(player, amount);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.format("%.2f", amount));
            player.sendMessage(plugin.getConfigManager().getMessage("bank.withdraw-success", replacements));

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", null));
        }
    }

    private void handleLoan(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only", null));
            return;
        }

        if (!player.hasPermission("dynamicecovault.bank.loan")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage.bank-loan", null));
            return;
        }

        if (plugin.getLoanManager().hasActiveLoan(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("loan.already-active", null));
            return;
        }

        try {
            double amount = Double.parseDouble(args[1]);
            double minAmount = plugin.getConfigManager().getConfig().getDouble("loans.minimum-amount", 100.0);
            double maxAmount = plugin.getConfigManager().getConfig().getDouble("loans.maximum-amount", 50000.0);

            if (amount < minAmount) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("min", String.format("%.2f", minAmount));
                player.sendMessage(plugin.getConfigManager().getMessage("loan.amount-too-low", replacements));
                return;
            }

            if (amount > maxAmount) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("max", String.format("%.2f", maxAmount));
                player.sendMessage(plugin.getConfigManager().getMessage("loan.amount-too-high", replacements));
                return;
            }

            if (plugin.getLoanManager().requestLoan(player, amount)) {
                Loan loan = plugin.getLoanManager().getActiveLoan(player.getUniqueId());
                Map<String, String> replacements = new HashMap<>();
                replacements.put("amount", String.format("%.2f", amount));
                replacements.put("total", String.format("%.2f", loan.getTotalAmount()));
                replacements.put("date", new SimpleDateFormat("MMM dd, yyyy HH:mm").format(new Date(loan.getDueDate())));
                player.sendMessage(plugin.getConfigManager().getMessage("loan.request-success", replacements));
            }

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", null));
        }
    }

    private void handleRepay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only", null));
            return;
        }

        if (!player.hasPermission("dynamicecovault.bank.repay")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage.bank-repay", null));
            return;
        }

        if (!plugin.getLoanManager().hasActiveLoan(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("loan.no-active-loan", null));
            return;
        }

        try {
            double amount = Double.parseDouble(args[1]);
            Loan loan = plugin.getLoanManager().getActiveLoan(player.getUniqueId());

            if (plugin.getLoanManager().repayLoan(player, amount)) {
                if (loan.getRemainingAmount() <= 0) {
                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("amount", String.format("%.2f", amount));
                    player.sendMessage(plugin.getConfigManager().getMessage("loan.repay-full", replacements));
                } else {
                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("amount", String.format("%.2f", amount));
                    replacements.put("remaining", String.format("%.2f", loan.getRemainingAmount()));
                    player.sendMessage(plugin.getConfigManager().getMessage("loan.repay-success", replacements));
                }
            } else {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("amount", String.format("%.2f", amount));
                player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds", replacements));
            }

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", null));
        }
    }

    private void handleSetTax(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dynamicecovault.bank.settax")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usage.bank-settax", null));
            return;
        }

        String typeStr = args[1].toUpperCase();
        TaxManager.TaxType type;

        try {
            type = TaxManager.TaxType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usage.bank-settax", null));
            return;
        }

        try {
            double rate = Double.parseDouble(args[2]);
            
            if (rate < 0 || rate > 1) {
                sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", null));
                return;
            }

            plugin.getTaxManager().setTaxRate(type, rate);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("type", type.name().toLowerCase());
            replacements.put("rate", String.format("%.2f", rate * 100));
            sender.sendMessage(plugin.getConfigManager().getMessage("tax.rate-updated", replacements));

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount", null));
        }
    }

    private void handleVaultBalance(CommandSender sender) {
        if (!sender.hasPermission("dynamicecovault.bank.vaultbalance")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return;
        }

        double balance = plugin.getBankManager().getBankVaultBalance();
        Map<String, String> replacements = new HashMap<>();
        replacements.put("balance", String.format("%.2f", balance));
        sender.sendMessage(plugin.getConfigManager().getMessage("bank.vault-balance", replacements));
    }

    private void handleES(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dynamicecovault.bank.es.reset")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("reset")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usage.bank-es", null));
            return;
        }

        plugin.getEconomyManager().resetPrices();
        sender.sendMessage(plugin.getConfigManager().getMessage("bank.prices-reset", null));
    }

    private void handleTaxInfo(CommandSender sender) {
        if (!sender.hasPermission("dynamicecovault.bank.taxinfo")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return;
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("buy_rate", String.format("%.2f", plugin.getTaxManager().getTaxRate(TaxManager.TaxType.BUY) * 100));
        replacements.put("sell_rate", String.format("%.2f", plugin.getTaxManager().getTaxRate(TaxManager.TaxType.SELL) * 100));
        replacements.put("give_rate", String.format("%.2f", plugin.getTaxManager().getTaxRate(TaxManager.TaxType.GIVE) * 100));
        replacements.put("receive_rate", String.format("%.2f", plugin.getTaxManager().getTaxRate(TaxManager.TaxType.RECEIVE) * 100));
        replacements.put("loan_interest_rate", String.format("%.2f", plugin.getConfigManager().getConfig().getDouble("loans.interest-rate", 0.15) * 100));
        sender.sendMessage(plugin.getConfigManager().getMessage("tax.info", replacements));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("balance", "deposit", "withdraw", "loan", "repay", "settax", "vaultbalance", "es", "taxinfo"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "deposit", "withdraw", "loan", "repay" -> {
                    completions.addAll(Arrays.asList("100", "500", "1000", "5000", "10000"));
                }
                case "settax" -> {
                    completions.addAll(Arrays.asList("buy", "sell", "give", "receive"));
                }
                case "es" -> {
                    completions.add("reset");
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("settax")) {
            completions.addAll(Arrays.asList("0.01", "0.05", "0.10", "0.15", "0.20"));
        }

        return completions;
    }
}
