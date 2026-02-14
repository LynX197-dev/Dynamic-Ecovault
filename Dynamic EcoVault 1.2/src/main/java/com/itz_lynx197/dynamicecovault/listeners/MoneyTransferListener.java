package com.itz_lynx197.dynamicecovault.listeners;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import com.itz_lynx197.dynamicecovault.managers.TaxManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class MoneyTransferListener implements Listener {

    private final DynamicEcoVault plugin;

    public MoneyTransferListener(DynamicEcoVault plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        // Check for money transfer commands (Essentials /pay, /give, etc.)
        if (command.startsWith("/pay ") || command.startsWith("/eco give ") || 
            command.startsWith("/money pay ") || command.startsWith("/give ")) {
            
            String[] args = command.split(" ");
            
            if (args.length >= 3) {
                try {
                    // Try to parse amount from different command formats
                    double amount = 0;
                    String targetPlayerName = "";
                    
                    if (command.startsWith("/pay ")) {
                        // /pay <player> <amount>
                        targetPlayerName = args[1];
                        amount = Double.parseDouble(args[2]);
                    } else if (command.startsWith("/eco give ")) {
                        // /eco give <player> <amount>
                        targetPlayerName = args[2];
                        amount = Double.parseDouble(args[3]);
                    } else if (command.startsWith("/money pay ")) {
                        // /money pay <player> <amount>
                        targetPlayerName = args[2];
                        amount = Double.parseDouble(args[3]);
                    }
                    
                    if (amount > 0 && !targetPlayerName.isEmpty()) {
                        Player target = Bukkit.getPlayer(targetPlayerName);
                        
                        if (target != null && target.isOnline()) {
                            // Schedule tax collection after the transaction completes
                            double finalAmount = amount;
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                // Tax the sender (GIVE tax)
                                plugin.getTaxManager().collectTax(player, finalAmount, TaxManager.TaxType.GIVE);
                                
                                // Tax the receiver (RECEIVE tax)
                                plugin.getTaxManager().collectTax(target, finalAmount, TaxManager.TaxType.RECEIVE);
                            }, 2L); // Delay by 2 ticks to ensure transaction completes
                        }
                    }
                } catch (NumberFormatException ignored) {
                    // Invalid amount format, ignore
                }
            }
        }
    }
}
