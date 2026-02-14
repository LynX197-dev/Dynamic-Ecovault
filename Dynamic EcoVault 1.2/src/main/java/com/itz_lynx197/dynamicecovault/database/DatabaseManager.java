package com.itz_lynx197.dynamicecovault.database;

import com.itz_lynx197.dynamicecovault.DynamicEcoVault;
import com.itz_lynx197.dynamicecovault.models.Loan;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private final DynamicEcoVault plugin;
    private Connection connection;

    public DatabaseManager(DynamicEcoVault plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String dbType = plugin.getConfigManager().getConfig().getString("database.type", "sqlite");
        
        try {
            if (dbType.equalsIgnoreCase("sqlite")) {
                initializeSQLite();
            } else if (dbType.equalsIgnoreCase("mysql")) {
                initializeMySQL();
            }
            
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/data.db";
        connection = DriverManager.getConnection(url);
    }

    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfigManager().getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfigManager().getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfigManager().getConfig().getString("database.mysql.database", "dynamicecovault");
        String username = plugin.getConfigManager().getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfigManager().getConfig().getString("database.mysql.password", "password");
        
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
        connection = DriverManager.getConnection(url, username, password);
    }

    private void createTables() throws SQLException {
        String createBalancesTable = """
            CREATE TABLE IF NOT EXISTS balances (
                player_id VARCHAR(36) PRIMARY KEY,
                balance DOUBLE NOT NULL DEFAULT 0.0
            )
        """;

        String createLoansTable = """
            CREATE TABLE IF NOT EXISTS loans (
                player_id VARCHAR(36) PRIMARY KEY,
                original_amount DOUBLE NOT NULL,
                interest DOUBLE NOT NULL,
                total_amount DOUBLE NOT NULL,
                remaining_amount DOUBLE NOT NULL,
                due_date BIGINT NOT NULL
            )
        """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(createBalancesTable);
            statement.execute(createLoansTable);
        }
    }

    public void saveLoan(Loan loan) {
        String sql = """
            INSERT OR REPLACE INTO loans 
            (player_id, original_amount, interest, total_amount, remaining_amount, due_date)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, loan.getPlayerId().toString());
            statement.setDouble(2, loan.getOriginalAmount());
            statement.setDouble(3, loan.getInterest());
            statement.setDouble(4, loan.getTotalAmount());
            statement.setDouble(5, loan.getRemainingAmount());
            statement.setLong(6, loan.getDueDate());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save loan: " + e.getMessage());
        }
    }

    public void updateLoan(Loan loan) {
        String sql = "UPDATE loans SET remaining_amount = ? WHERE player_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, loan.getRemainingAmount());
            statement.setString(2, loan.getPlayerId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update loan: " + e.getMessage());
        }
    }

    public void deleteLoan(UUID playerId) {
        String sql = "DELETE FROM loans WHERE player_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete loan: " + e.getMessage());
        }
    }

    public Map<UUID, Loan> loadAllLoans() {
        Map<UUID, Loan> loans = new HashMap<>();
        String sql = "SELECT * FROM loans";
        
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            
            while (rs.next()) {
                UUID playerId = UUID.fromString(rs.getString("player_id"));
                double originalAmount = rs.getDouble("original_amount");
                double interest = rs.getDouble("interest");
                double totalAmount = rs.getDouble("total_amount");
                long dueDate = rs.getLong("due_date");
                
                Loan loan = new Loan(playerId, originalAmount, interest, totalAmount, dueDate);
                loan.setRemainingAmount(rs.getDouble("remaining_amount"));
                loans.put(playerId, loan);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load loans: " + e.getMessage());
        }
        
        return loans;
    }

    public double getBalance(UUID playerId) {
        String sql = "SELECT balance FROM balances WHERE player_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get balance: " + e.getMessage());
        }
        return 0.0;
    }

    public void setBalance(UUID playerId, double balance) {
        String sql = "INSERT OR REPLACE INTO balances (player_id, balance) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setDouble(2, balance);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set balance: " + e.getMessage());
        }
    }

    public void updateBalance(UUID playerId, double amount) {
        double currentBalance = getBalance(playerId);
        setBalance(playerId, currentBalance + amount);
    }

    public boolean hasBalance(UUID playerId, double amount) {
        return getBalance(playerId) >= amount;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }
}
