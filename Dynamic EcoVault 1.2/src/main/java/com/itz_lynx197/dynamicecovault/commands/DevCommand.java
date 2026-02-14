package com.itz_lynx197.dynamicecovault.commands;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DevCommand implements CommandExecutor {

    private final DynamicEcoVault plugin;

    public DevCommand(DynamicEcoVault plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("dynamicecovault.bank.reload")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission", null));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reloadConfigs();
            plugin.getEconomyManager().reloadPrices();
            sender.sendMessage(plugin.getConfigManager().getMessage("dev.reload-success", null));
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().getMessage("dev.usage", null));
        return true;
    }
}