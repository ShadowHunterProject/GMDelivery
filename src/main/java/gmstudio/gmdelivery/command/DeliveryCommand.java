package gmstudio.gmdelivery.command;

import gmstudio.gmdelivery.GMDelivery;
import gmstudio.gmdelivery.gui.DeliveryGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeliveryCommand implements CommandExecutor {

    private final GMDelivery plugin;

    public DeliveryCommand(GMDelivery plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("gmdelivery.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no_permission"));
                return true;
            }
            plugin.getConfigManager().loadConfigs();
            plugin.getTaskManager().loadTasks();
            sender.sendMessage(plugin.getConfigManager().getMessage("reload_success"));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission("gmdelivery.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no_permission"));
                return true;
            }
            String targetName = args[1];
            Player target = org.bukkit.Bukkit.getPlayer(targetName);
            if (target != null) {
                gmstudio.gmdelivery.data.PlayerDeliveryData pData = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
                if (pData != null) {
                    pData.getCategoryProgressMap().clear();
                    pData.getActiveTasks().clear();
                    plugin.getPlayerDataManager().savePlayer(pData);
                    sender.sendMessage(gmstudio.gmdelivery.utils.Utils.color("&aĐã reset dữ liệu Giao Hàng của người chơi " + target.getName()));
                }
            } else {
                sender.sendMessage(gmstudio.gmdelivery.utils.Utils.color("&cNgười chơi không online!"));
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("resetall")) {
            if (!sender.hasPermission("gmdelivery.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no_permission"));
                return true;
            }
            // Clear all online players
            for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                gmstudio.gmdelivery.data.PlayerDeliveryData pData = plugin.getPlayerDataManager().getPlayerData(p.getUniqueId());
                if (pData != null) {
                    pData.getCategoryProgressMap().clear();
                    pData.getActiveTasks().clear();
                    plugin.getPlayerDataManager().savePlayer(pData);
                }
            }
            // Truncate table
            java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
            if (conn != null) {
                try {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        stmt.execute("DELETE FROM player_progress;");
                        stmt.execute("DELETE FROM player_tasks;");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sender.sendMessage(gmstudio.gmdelivery.utils.Utils.color("&aĐã reset TOÀN BỘ dữ liệu nhiệm vụ giao hàng của máy chủ!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("not_player"));
            return true;
        }

        Player player = (Player) sender;
        new gmstudio.gmdelivery.gui.CategoryGUI(plugin).open(player);
        return true;
    }
}
