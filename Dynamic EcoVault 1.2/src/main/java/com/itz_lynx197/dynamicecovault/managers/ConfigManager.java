package com.itz_lynx197.dynamicecovault.managers;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final DynamicEcoVault plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final Map<String, String> messageCache;

    public ConfigManager(DynamicEcoVault plugin) {
        this.plugin = plugin;
        this.messageCache = new HashMap<>();
    }

    public void loadConfigs() {
        // Save default configs
        plugin.saveDefaultConfig();
        saveResource("messages.yml");
        
        // Load configurations
        config = plugin.getConfig();
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        
        // Cache messages
        cacheMessages();
    }

    private void saveResource(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    private void cacheMessages() {
        messageCache.clear();
        for (String key : messages.getKeys(true)) {
            if (messages.isString(key)) {
                messageCache.put(key, messages.getString(key));
            }
        }
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        cacheMessages();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public String getMessage(String path) {
        return messageCache.getOrDefault(path, "&cMessage not found: " + path);
    }

    public String getMessage(String path, Map<String, String> replacements) {
        String message = getMessage(path);
        
        // Apply prefix
        String prefix = getMessage("prefix");
        message = message.replace("{prefix}", prefix);
        
        // Apply replacements
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        // Color codes
        return colorize(message);
    }

    private String colorize(String message) {
        return message.replace("&", "§");
    }
}
