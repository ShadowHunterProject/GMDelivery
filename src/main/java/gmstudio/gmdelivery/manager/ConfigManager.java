package gmstudio.gmdelivery.manager;

import gmstudio.gmdelivery.GMDelivery;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final GMDelivery plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration levels;
    private FileConfiguration categories;

    public ConfigManager(GMDelivery plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig(); // saves config.yml
        config = plugin.getConfig();

        messages = loadFile("messages.yml");
        levels = loadFile("levels.yml");
        categories = loadFile("categories.yml");
    }

    private FileConfiguration loadFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public String getMessage(String key) {
        String prefix = messages.getString("prefix", "&8[&eGMDelivery&8] &r");
        String msg = messages.getString(key, "&cMessage not found: " + key);
        return gmstudio.gmdelivery.utils.Utils.color(prefix + msg);
    }

    public FileConfiguration getLevels() {
        return levels;
    }

    public FileConfiguration getCategories() {
        return categories;
    }

    public int getMaxLevel() {
        return levels.getInt("max_level", 100);
    }

    public int getXpRequired(int nextLevel) {
        return levels.getInt("levels." + nextLevel, nextLevel * 100); // Fallback to nextLevel * 100 if not defined
    }
}
