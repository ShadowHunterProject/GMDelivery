package gmstudio.gmdelivery;

import gmstudio.gmdelivery.command.DeliveryCommand;
import gmstudio.gmdelivery.manager.ConfigManager;
import gmstudio.gmdelivery.manager.DatabaseManager;
import gmstudio.gmdelivery.manager.PlayerDataManager;
import gmstudio.gmdelivery.manager.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GMDelivery extends JavaPlugin {

    private static GMDelivery instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private TaskManager taskManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize Managers
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.connect();
        this.databaseManager.createTables();

        this.taskManager = new TaskManager(this);
        this.taskManager.loadTasks();

        this.playerDataManager = new PlayerDataManager(this);

        // Register Commands
        getCommand("giaohang").setExecutor(new DeliveryCommand(this));

        // Register Listeners
        getServer().getPluginManager().registerEvents(this.playerDataManager, this);

        getLogger().info("GMDelivery enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (this.databaseManager != null) {
            this.databaseManager.disconnect();
        }
        getLogger().info("GMDelivery disabled!");
    }

    public static GMDelivery getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
