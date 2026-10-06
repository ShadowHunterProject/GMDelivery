package gmstudio.gmdelivery.manager;

import gmstudio.gmdelivery.GMDelivery;
import gmstudio.gmdelivery.data.PlayerDeliveryData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager implements Listener {

    private final GMDelivery plugin;
    private final Map<UUID, PlayerDeliveryData> cache = new HashMap<>();

    public PlayerDataManager(GMDelivery plugin) {
        this.plugin = plugin;
        
        // Load for online players if reload
        for (Player p : Bukkit.getOnlinePlayers()) {
            loadPlayer(p.getUniqueId());
        }
    }

    public PlayerDeliveryData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    private void loadPlayer(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerDeliveryData data = null;
            Connection conn = plugin.getDatabaseManager().getConnection();
            if (conn == null) return;
            try {
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM player_data WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        int level = rs.getInt("level");
                        int xp = rs.getInt("xp");
                        data = new PlayerDeliveryData(uuid, level, xp);
                    } else {
                        data = new PlayerDeliveryData(uuid, 1, 0);
                        // Insert default
                        try (PreparedStatement insert = conn.prepareStatement("INSERT INTO player_data (uuid, level, xp) VALUES (?, ?, ?)")) {
                            insert.setString(1, uuid.toString());
                            insert.setInt(2, 1);
                            insert.setInt(3, 0);
                            insert.executeUpdate();
                        }
                    }
                }

                // Load tasks
                try (PreparedStatement psTasks = conn.prepareStatement("SELECT * FROM player_tasks WHERE uuid = ?")) {
                    psTasks.setString(1, uuid.toString());
                    ResultSet rsTasks = psTasks.executeQuery();
                    while (rsTasks.next()) {
                        String taskId = rsTasks.getString("task_id");
                        long waitEndTime = rsTasks.getLong("wait_end_time");
                        String state = rsTasks.getString("task_state");
                        data.addTask(new PlayerDeliveryData.ActiveTask(taskId, waitEndTime, state));
                    }
                }

                // Load progress
                try (PreparedStatement psProgress = conn.prepareStatement("SELECT * FROM player_progress WHERE uuid = ?")) {
                    psProgress.setString(1, uuid.toString());
                    ResultSet rsProgress = psProgress.executeQuery();
                    while (rsProgress.next()) {
                        String category = rsProgress.getString("category");
                        int progress = rsProgress.getInt("progress");
                        data.setCategoryProgress(category, progress);
                    }
                }

                cache.put(uuid, data);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void savePlayer(PlayerDeliveryData data) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePlayerSync(data));
    }

    private void savePlayerSync(PlayerDeliveryData data) {
        Connection conn = plugin.getDatabaseManager().getConnection();
        if (conn == null) return;
        try {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE player_data SET level = ?, xp = ? WHERE uuid = ?")) {
                ps.setInt(1, data.getLevel());
                ps.setInt(2, data.getXp());
                ps.setString(3, data.getUuid().toString());
                ps.executeUpdate();
            }

            // Delete all tasks for this user to re-insert
            try (PreparedStatement deleteTasks = conn.prepareStatement("DELETE FROM player_tasks WHERE uuid = ?")) {
                deleteTasks.setString(1, data.getUuid().toString());
                deleteTasks.executeUpdate();
            }
            
            if (!data.getActiveTasks().isEmpty()) {
                String sql = "INSERT INTO player_tasks (uuid, task_id, wait_end_time, task_state) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertTasks = conn.prepareStatement(sql)) {
                    for (PlayerDeliveryData.ActiveTask task : data.getActiveTasks().values()) {
                        insertTasks.setString(1, data.getUuid().toString());
                        insertTasks.setString(2, task.taskId);
                        insertTasks.setLong(3, task.waitEndTime);
                        insertTasks.setString(4, task.state.name());
                        insertTasks.addBatch();
                    }
                    insertTasks.executeBatch();
                }
            }

            // Delete and re-insert progress
            try (PreparedStatement deleteProgress = conn.prepareStatement("DELETE FROM player_progress WHERE uuid = ?")) {
                deleteProgress.setString(1, data.getUuid().toString());
                deleteProgress.executeUpdate();
            }

            if (!data.getCategoryProgressMap().isEmpty()) {
                String sqlProgress = "INSERT INTO player_progress (uuid, category, progress) VALUES (?, ?, ?)";
                try (PreparedStatement insertProgress = conn.prepareStatement(sqlProgress)) {
                    for (Map.Entry<String, Integer> entry : data.getCategoryProgressMap().entrySet()) {
                        insertProgress.setString(1, data.getUuid().toString());
                        insertProgress.setString(2, entry.getKey());
                        insertProgress.setInt(3, entry.getValue());
                        insertProgress.addBatch();
                    }
                    insertProgress.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (cache.containsKey(uuid)) {
            savePlayer(cache.get(uuid));
            cache.remove(uuid);
        }
    }
}
