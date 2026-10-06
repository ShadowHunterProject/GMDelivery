package gmstudio.gmdelivery.manager;

import gmstudio.gmdelivery.GMDelivery;
import gmstudio.gmdelivery.data.TaskData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TaskManager {

    private final GMDelivery plugin;
    private final Map<String, TaskData> tasks = new HashMap<>();
    private final Map<String, List<TaskData>> categoryTasks = new HashMap<>();

    public TaskManager(GMDelivery plugin) {
        this.plugin = plugin;
    }

    public void loadTasks() {
        tasks.clear();
        categoryTasks.clear();
        
        File folder = new File(plugin.getDataFolder(), "tasks");
        if (!folder.exists() || (folder.listFiles() != null && folder.listFiles().length == 0)) {
            if (!folder.exists()) folder.mkdirs();
            String[] defaultTasks = {
                "ore.yml", "kynang.yml"
            };
            for (String file : defaultTasks) {
                if (plugin.getResource("tasks/" + file) != null) {
                    plugin.saveResource("tasks/" + file, false);
                }
            }
        }
        
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection tasksSection = fileConfig.getConfigurationSection("tasks");
            if (tasksSection == null) continue;

            for (String key : tasksSection.getKeys(false)) {
                String category = tasksSection.getString(key + ".category", "default");
                String name = tasksSection.getString(key + ".name");
                List<String> lore = tasksSection.getStringList(key + ".lore");
                int levelRequired = tasksSection.getInt(key + ".level_required", 1);
                int waitTime = tasksSection.getInt(key + ".wait_time_seconds", 60);
                List<String> items = tasksSection.getStringList(key + ".items");
                int xp = tasksSection.getInt(key + ".rewards.xp", 10);
                List<String> commands = tasksSection.getStringList(key + ".rewards.commands");
                List<String> vouchers = tasksSection.getStringList(key + ".rewards.vouchers");

                TaskData data = new TaskData(key, category, name, lore, levelRequired, waitTime, items, xp, commands, vouchers);
                tasks.put(key, data);
                categoryTasks.computeIfAbsent(category, k -> new ArrayList<>()).add(data);
            }
        }
        plugin.getLogger().info("Loaded " + tasks.size() + " delivery tasks from folder.");
    }

    public TaskData getTask(String id) {
        return tasks.get(id);
    }

    public List<TaskData> getTasksForCategory(String category) {
        return categoryTasks.getOrDefault(category, new ArrayList<>());
    }
}
