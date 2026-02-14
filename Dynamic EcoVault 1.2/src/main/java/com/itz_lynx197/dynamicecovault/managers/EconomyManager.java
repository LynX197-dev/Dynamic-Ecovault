package com.itz_lynx197.dynamicecovault.managers;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EconomyManager {

    private final DynamicEcoVault plugin;
    private final Map<Material, PriceData> priceData;
    private final Random random;
    private BukkitTask priceUpdateTask;
    private final File pricesFile;
    private YamlConfiguration pricesConfig;

    public EconomyManager(DynamicEcoVault plugin) {
        this.plugin = plugin;
        this.priceData = new HashMap<>();
        this.random = new Random();
        this.pricesFile = new File(plugin.getDataFolder(), "prices.yml");
        this.pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
        loadPrices();
    }

    public void startPriceUpdater() {
        long updateInterval = plugin.getConfigManager().getConfig().getLong("economy.update-interval", 6000L);
        
        priceUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updatePrices, updateInterval, updateInterval);
    }

    public void stopPriceUpdater() {
        if (priceUpdateTask != null) {
            priceUpdateTask.cancel();
        }
        savePrices();
    }

    private void updatePrices() {
        // Update prices based on supply and demand
        double fluctuationRate = plugin.getConfigManager().getConfig().getDouble("economy.fluctuation-rate", 0.20);
        double minMultiplier = plugin.getConfigManager().getConfig().getDouble("economy.min-price-multiplier", 0.5);
        double maxMultiplier = plugin.getConfigManager().getConfig().getDouble("economy.max-price-multiplier", 2.0);

        for (Map.Entry<Material, PriceData> entry : priceData.entrySet()) {
            PriceData data = entry.getValue();

            // Calculate supply/demand ratio
            double ratio = data.demand > 0 ? (double) data.supply / data.demand : 1.0;

            // Adjust price based on ratio
            double change = (1.0 - ratio) * fluctuationRate;
            double newMultiplier = Math.max(minMultiplier, Math.min(maxMultiplier, data.priceMultiplier + change));

            data.priceMultiplier = newMultiplier;

            // Reset supply/demand counters
            data.supply = 0;
            data.demand = 0;
        }

        savePrices();
    }

    public void recordBuy(Material material, int amount) {
        PriceData data = priceData.computeIfAbsent(material, k -> new PriceData());
        data.demand += amount;

        // Immediate price adjustment for high demand
        double fluctuationRate = plugin.getConfigManager().getConfig().getDouble("economy.fluctuation-rate", 0.20);
        double change = fluctuationRate * 0.05; // Small immediate increase
        double minMultiplier = plugin.getConfigManager().getConfig().getDouble("economy.min-price-multiplier", 0.5);
        double maxMultiplier = plugin.getConfigManager().getConfig().getDouble("economy.max-price-multiplier", 2.0);
        data.priceMultiplier = Math.max(minMultiplier, Math.min(maxMultiplier, data.priceMultiplier + change));
    }

    public void recordSell(Material material, int amount) {
        PriceData data = priceData.computeIfAbsent(material, k -> new PriceData());
        data.supply += amount;

        // Immediate price adjustment for increased supply
        double fluctuationRate = plugin.getConfigManager().getConfig().getDouble("economy.fluctuation-rate", 0.20);
        double change = fluctuationRate * 0.05; // Small immediate decrease
        double minMultiplier = plugin.getConfigManager().getConfig().getDouble("economy.min-price-multiplier", 0.5);
        double maxMultiplier = plugin.getConfigManager().getConfig().getDouble("economy.max-price-multiplier", 2.0);
        data.priceMultiplier = Math.max(minMultiplier, Math.min(maxMultiplier, data.priceMultiplier - change));
    }

    public double getPriceMultiplier(Material material) {
        PriceData data = priceData.get(material);
        return data != null ? data.priceMultiplier : 1.0;
    }

    public double getAdjustedBuyPrice(Material material, double basePrice) {
        double multiplier = getPriceMultiplier(material);
        return basePrice * multiplier;
    }

    public double getAdjustedSellPrice(Material material, double basePrice) {
        double multiplier = getPriceMultiplier(material);
        return basePrice * multiplier;
    }

    public void buyItem(Material material, int amount) {
        recordBuy(material, amount);
    }

    public void sellItem(Material material, int amount) {
        recordSell(material, amount);
    }

    public void resetPrices() {
        for (PriceData data : priceData.values()) {
            data.priceMultiplier = 1.0;
        }
        savePrices();
    }

    public void reloadPrices() {
        this.pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
        loadPrices();
    }

    private static class PriceData {
        double priceMultiplier = 1.0;
        int supply = 0;
        int demand = 0;
    }

    private void loadPrices() {
        for (String key : pricesConfig.getKeys(false)) {
            Material mat = Material.getMaterial(key);
            if (mat != null) {
                double mult = pricesConfig.getDouble(key, 1.0);
                PriceData data = priceData.computeIfAbsent(mat, k -> new PriceData());
                data.priceMultiplier = mult;
            }
        }
    }

    private void savePrices() {
        for (Map.Entry<Material, PriceData> entry : priceData.entrySet()) {
            pricesConfig.set(entry.getKey().name(), entry.getValue().priceMultiplier);
        }
        try {
            pricesConfig.save(pricesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save prices.yml: " + e.getMessage());
        }
    }
}