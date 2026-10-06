package gmstudio.gmdelivery.manager;

import gmstudio.gmdelivery.GMDelivery;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final GMDelivery plugin;
    private Connection connection;

    public DatabaseManager(GMDelivery plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            String dbName = plugin.getConfigManager().getConfig().getString("database.file", "database.db");
            File dbFile = new File(plugin.getDataFolder(), dbName);
            
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("Connected to SQLite database.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to SQLite database!");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTables() {
        if (connection == null) return;
        
        // We recreate the table structure for the new category logic
        String sql1 = "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "level INTEGER NOT NULL DEFAULT 1, " +
                "xp INTEGER NOT NULL DEFAULT 0" +
                ");";
                
        String sql2 = "CREATE TABLE IF NOT EXISTS player_tasks (" +
                "uuid VARCHAR(36), " +
                "task_id VARCHAR(64), " +
                "wait_end_time BIGINT NOT NULL DEFAULT 0, " +
                "task_state VARCHAR(20) DEFAULT 'NONE', " +
                "PRIMARY KEY (uuid, task_id)" +
                ");";
                
        String sql3 = "CREATE TABLE IF NOT EXISTS player_progress (" +
                "uuid VARCHAR(36), " +
                "category VARCHAR(64), " +
                "progress INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (uuid, category)" +
                ");";
                
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql1);
            stmt.execute(sql2);
            stmt.execute(sql3);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create database tables!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
