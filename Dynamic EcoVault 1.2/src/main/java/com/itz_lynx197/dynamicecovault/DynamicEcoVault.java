package com.itz_lynx197.dynamicecovault;


import com.itz_lynx197.dynamicecovault.commands.BankCommand;
import com.itz_lynx197.dynamicecovault.commands.DevCommand;

import com.itz_lynx197.dynamicecovault.database.DatabaseManager;
import com.itz_lynx197.dynamicecovault.integrations.DiscordIntegration;
import com.itz_lynx197.dynamicecovault.integrations.EconomyShopGUIIntegration;
import com.itz_lynx197.dynamicecovault.integrations.PlaceholderIntegration;


import com.itz_lynx197.dynamicecovault.listeners.MoneyTransferListener;

import com.itz_lynx197.dynamicecovault.managers.BankManager;
import com.itz_lynx197.dynamicecovault.managers.ConfigManager;
import com.itz_lynx197.dynamicecovault.managers.EconomyManager;
import com.itz_lynx197.dynamicecovault.managers.LoanManager;

import com.itz_lynx197.dynamicecovault.managers.TaxManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class DynamicEcoVault extends JavaPlugin {

    private static DynamicEcoVault instance;
    private Economy economy;
    
    // Managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private BankManager bankManager;
    private TaxManager taxManager;
    private LoanManager loanManager;
    private EconomyManager economyManager;

    
    // Integrations
    private DiscordIntegration discordIntegration;
    private PlaceholderIntegration placeholderIntegration;

    @Override
    public void onEnable() {
        instance = this;
        
        // Check for required dependencies
        if (!checkDependencies()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Setup Vault Economy
        if (!setupEconomy()) {
            getLogger().warning("Vault Economy not available! Shop transactions will not work until Vault and an economy plugin (like Essentials) are installed.");
        }
        
        // Initialize managers
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        bankManager = new BankManager(this);
        taxManager = new TaxManager(this);
        loanManager = new LoanManager(this);
        economyManager = new EconomyManager(this);

        
        // Initialize integrations
        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
            discordIntegration = new DiscordIntegration(this);
        }
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderIntegration = new PlaceholderIntegration(this);
            placeholderIntegration.register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }

        if (Bukkit.getPluginManager().getPlugin("EconomyShopGUI") != null && configManager.getConfig().getBoolean("integrations.economy-shop-gui.enabled", true)) {
            EconomyShopGUIIntegration esgIntegration = new EconomyShopGUIIntegration(this);
            Bukkit.getPluginManager().registerEvents(esgIntegration, this);
            getLogger().info("EconomyShopGUI integration enabled!");
        }
        
        // Register commands
        getCommand("bank").setExecutor(new BankCommand(this));
        getCommand("dev").setExecutor(new DevCommand(this));

        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new MoneyTransferListener(this), this);

        
        // Start managers
        loanManager.startLoanChecker();
        if (configManager.getConfig().getBoolean("economy.enabled")) {
            economyManager.startPriceUpdater();
        }
        
        getLogger().info("Dynamic EcoVault by 𝕃ʏи𝕏 has been enabled!");
      // ASCII
        Bukkit.getConsoleSender().sendMessage(AsciiBanner.DESIGN_CREDIT);
    }

    @Override
    public void onDisable() {
        if (loanManager != null) {
            loanManager.stopLoanChecker();
        }
        if (economyManager != null) {
            economyManager.stopPriceUpdater();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (placeholderIntegration != null) {
            placeholderIntegration.unregister();
        }
        
        getLogger().info("Dynamic EcoVault has been disabled!");
    }
    
    private boolean checkDependencies() {
        // No hard dependencies required - plugin can function with limited features
        return true;
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    // Getters
    public static DynamicEcoVault getInstance() {
        return instance;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public BankManager getBankManager() {
        return bankManager;
    }
    
    public TaxManager getTaxManager() {
        return taxManager;
    }
    
    public LoanManager getLoanManager() {
        return loanManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public DiscordIntegration getDiscordIntegration() {
        return discordIntegration;
    }



}
