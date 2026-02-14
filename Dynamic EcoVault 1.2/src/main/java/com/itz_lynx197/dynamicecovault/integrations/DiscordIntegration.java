package com.itz_lynx197.dynamicecovault.integrations;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import com.itz_lynx197.dynamicecovault.events.BankVaultUpdateEvent;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;

public class DiscordIntegration implements Listener {

    private final DynamicEcoVault plugin;
    private boolean enabled;

    public DiscordIntegration(DynamicEcoVault plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigManager().getConfig().getBoolean("discord.enabled", true);

        if (enabled) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("DiscordSRV integration enabled!");
        }
    }

    @EventHandler
    public void onBankVaultUpdate(BankVaultUpdateEvent event) {
        if (!enabled) return;
        
        sendBankVaultUpdate(event.getNewBalance(), event.getChangeAmount(), event.getReason());
    }

    public void sendTaxNotification(double amount, String type, String playerName) {
        if (!enabled || !plugin.getConfigManager().getConfig().getBoolean("discord.notify-tax", true)) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("💰 Tax Collected")
                .addField("Amount", String.format("$%.2f", amount), true)
                .addField("Type", type, true)
                .addField("Player", playerName, true)
                .setColor(Color.YELLOW)
                .setTimestamp(java.time.Instant.now());

        sendEmbed(embed);
    }

    public void sendLoanNotification(double amount, String playerName, boolean issued) {
        if (!enabled || !plugin.getConfigManager().getConfig().getBoolean("discord.notify-loans", true)) {
            return;
        }

        EmbedBuilder embed;
        
        if (issued) {
            embed = new EmbedBuilder()
                    .setTitle("🏦 Loan Issued")
                    .addField("Amount", String.format("$%.2f", amount), true)
                    .addField("Player", playerName, true)
                    .setColor(Color.BLUE);
        } else {
            embed = new EmbedBuilder()
                    .setTitle("✅ Loan Repaid")
                    .addField("Amount", String.format("$%.2f", amount), true)
                    .addField("Player", playerName, true)
                    .setColor(Color.GREEN);
        }

        embed.setTimestamp(java.time.Instant.now());
        sendEmbed(embed);
    }

    public void sendLargeTransactionNotification(String playerName, String type, double amount) {
        if (!enabled || !plugin.getConfigManager().getConfig().getBoolean("discord.notify-large-transactions", true)) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("💸 Large Transaction")
                .addField("Player", playerName, true)
                .addField("Type", type, true)
                .addField("Amount", String.format("$%.2f", amount), true)
                .setColor(Color.ORANGE)
                .setTimestamp(java.time.Instant.now());

        sendEmbed(embed);
    }

    private void sendBankVaultUpdate(double newBalance, double changeAmount, String reason) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏛️ Bank Vault Updated")
                .addField("New Balance", String.format("$%.2f", newBalance), true)
                .addField("Change", String.format("%s$%.2f", changeAmount >= 0 ? "+" : "", changeAmount), true)
                .addField("Reason", reason, false)
                .setColor(changeAmount >= 0 ? Color.GREEN : Color.RED)
                .setTimestamp(java.time.Instant.now());

        sendEmbed(embed);
    }

    private void sendEmbed(EmbedBuilder embed) {
        if (DiscordSRV.getPlugin() == null) {
            return;
        }

        String channelId = plugin.getConfigManager().getConfig().getString("discord.channel-id", "");
        TextChannel channel;

        if (channelId.isEmpty()) {
            channel = DiscordSRV.getPlugin().getMainTextChannel();
        } else {
            channel = DiscordSRV.getPlugin().getJda().getTextChannelById(channelId);
        }

        if (channel != null) {
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }
}
