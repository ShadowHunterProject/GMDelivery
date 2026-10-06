package gmstudio.gmdelivery.gui;

import gmstudio.gmdelivery.GMDelivery;
import gmstudio.gmdelivery.data.PlayerDeliveryData;
import gmstudio.gmdelivery.data.TaskData;
import gmstudio.gmdelivery.utils.ItemUtils;
import gmstudio.gmdelivery.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vn.gmcore.GMCore;
import vn.gmcore.gui.ConfigGUI;
import vn.gmcore.gui.GUIItem;

import java.util.ArrayList;
import java.util.List;

public class DeliveryGUI {

    private final GMDelivery plugin;
    private final String category;
    private final TaskData specificTask;

    public DeliveryGUI(GMDelivery plugin, String category, TaskData task) {
        this.plugin = plugin;
        this.category = category;
        this.specificTask = task;
    }

    public void open(Player player) {
        PlayerDeliveryData pData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (pData == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not_player"));
            return;
        }

        TaskData currentTask = specificTask;
        if (currentTask == null) return;
        
        PlayerDeliveryData.ActiveTask activeTask = pData.getTask(currentTask.getId());

        // If player has no active task for this specific task ID, we initialize one in memory.
        if (activeTask == null) {
            activeTask = new PlayerDeliveryData.ActiveTask(currentTask.getId(), 0, "NONE");
        }

        // Check state if waiting
        if (activeTask != null && activeTask.state == PlayerDeliveryData.TaskState.WAITING) {
            if (System.currentTimeMillis() >= activeTask.waitEndTime) {
                activeTask.state = PlayerDeliveryData.TaskState.READY;
                plugin.getPlayerDataManager().savePlayer(pData);
            }
        }

        String title = Utils.color(plugin.getConfigManager().getConfig().getString("gui_task.title", "&8Nhiệm Vụ Giao Hàng"));
        List<String> layout = plugin.getConfigManager().getConfig().getStringList("gui_task.layout");

        ConfigGUI gui = new ConfigGUI()
                .setTitle(title)
                .setLayout(layout);

        // Background
        String bgMat = plugin.getConfigManager().getConfig().getString("gui_task.items.background.material", "BLACK_STAINED_GLASS_PANE");
        ItemStack bgItem = new ItemStack(Material.matchMaterial(bgMat) != null ? Material.matchMaterial(bgMat) : Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bgItem.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(" ");
            bgItem.setItemMeta(bgMeta);
        }
        gui.setIngredient('B', new GUIItem(bgItem));
        gui.setIngredient(' ', new GUIItem(bgItem));

        // Info Item (Shows requirements)
        ItemStack infoItem = createInfoItem(player, pData, currentTask);
        gui.setIngredient('I', new GUIItem(infoItem));

        // Submit/Claim/Waiting Item
        ItemStack actionItem;
        GUIItem guiActionItem;

        final PlayerDeliveryData.ActiveTask finalActiveTask = activeTask;
        final TaskData finalCurrentTask = currentTask;

        if (finalCurrentTask == null || finalActiveTask == null) {
            actionItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = actionItem.getItemMeta();
            meta.setDisplayName(Utils.color("&cKhông Có Nhiệm Vụ"));
            actionItem.setItemMeta(meta);
            guiActionItem = new GUIItem(actionItem);
        } else if (finalActiveTask.state == PlayerDeliveryData.TaskState.READY) {
            actionItem = createConfigItem("claim");
            guiActionItem = new GUIItem(actionItem, event -> {
                event.setCancelled(true);
                claimReward(player, pData, finalActiveTask, finalCurrentTask);
            });
        } else if (finalActiveTask.state == PlayerDeliveryData.TaskState.WAITING) {
            actionItem = createConfigItem("waiting");
            // Update placeholder
            ItemMeta meta = actionItem.getItemMeta();
            if (meta != null) {
                long timeLeft = (finalActiveTask.waitEndTime - System.currentTimeMillis()) / 1000;
                if (timeLeft < 0) timeLeft = 0;
                List<String> lore = meta.getLore();
                if (lore != null) {
                    List<String> newLore = new ArrayList<>();
                    for (String line : lore) {
                        newLore.add(line.replace("%time%", Utils.formatTime((int) timeLeft)));
                    }
                    meta.setLore(newLore);
                }
                actionItem.setItemMeta(meta);
            }
            guiActionItem = new GUIItem(actionItem, event -> event.setCancelled(true));
        } else {
            // NONE state (Needs to submit)
            actionItem = createConfigItem("submit");
            guiActionItem = new GUIItem(actionItem, event -> {
                event.setCancelled(true);
                submitTask(player, pData, finalActiveTask, finalCurrentTask);
            });
        }
        
        gui.setIngredient('S', guiActionItem); // Assuming S is for Submit/Action
        
        // Let's check layout for the action char. The default layout uses 'R' probably for action.
        gui.setIngredient('T', new GUIItem(infoItem)); // Assuming T is for Task info
        gui.setIngredient('R', guiActionItem); // Assuming R is for Action

        GMCore.getInstance().getGuiManager().openGUI(player, gui);
    }

    private ItemStack createConfigItem(String key) {
        String matStr = plugin.getConfigManager().getConfig().getString("gui_task.items." + key + ".material", "STONE");
        String name = plugin.getConfigManager().getConfig().getString("gui_task.items." + key + ".name", "&f" + key);
        List<String> lore = plugin.getConfigManager().getConfig().getStringList("gui_task.items." + key + ".lore");

        ItemStack item = new ItemStack(Material.matchMaterial(matStr) != null ? Material.matchMaterial(matStr) : Material.STONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Utils.color(name));
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(Utils.color(line));
            }
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem(Player player, PlayerDeliveryData pData, TaskData task) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (task != null) {
                meta.setDisplayName(Utils.color(task.getName()));
            } else {
                meta.setDisplayName(Utils.color("&eNhiệm Vụ Giao Hàng"));
            }

            List<String> lore = new ArrayList<>();
            int nextLevel = pData.getLevel() + 1;
            int requiredXp = plugin.getConfigManager().getLevels().getInt("levels." + nextLevel, pData.getXp());
            lore.add(Utils.color("&7Cấp độ: &f" + pData.getLevel()));
            lore.add(Utils.color("&7Kinh nghiệm giao hàng: &b" + pData.getXp() + "&8/&b" + requiredXp));
            lore.add("");
            if (task != null) {
                for (String l : task.getLore()) {
                    lore.add(Utils.color(l));
                }
                if (!task.getLore().isEmpty()) {
                    lore.add("");
                }
                lore.add(Utils.color("&e&lYêu Cầu Giao Nộp:"));
                for (String req : task.getItems()) {
                    ItemStack parsed = ItemUtils.parseItem(req);
                    String name;
                    String amount;
                    if (parsed != null) {
                        name = (parsed.getItemMeta() != null && parsed.getItemMeta().hasDisplayName()) 
                                ? parsed.getItemMeta().getDisplayName() 
                                : parsed.getType().name();
                        amount = String.valueOf(parsed.getAmount());
                    } else {
                        String[] parts = req.split(":");
                        name = parts.length > 2 ? parts[1] : req;
                        amount = parts.length > 2 ? parts[parts.length - 1] : "1";
                    }
                    
                    // Check if player has it
                    boolean has = ItemUtils.hasItems(player, req);
                    String color = has ? "&a&l✔ &a" : "&c&l❌ &c";
                    lore.add(Utils.color("  " + color + name + " &fx" + amount));
                }
            } else {
                lore.add(Utils.color("&cKhông có nhiệm vụ nào!"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void submitTask(Player player, PlayerDeliveryData pData, PlayerDeliveryData.ActiveTask activeTask, TaskData task) {
        if (task == null || activeTask == null) return;
        
        // Check items
        for (String req : task.getItems()) {
            if (!ItemUtils.hasItems(player, req)) {
                player.sendMessage(plugin.getConfigManager().getMessage("not_enough_items"));
                player.closeInventory();
                return;
            }
        }

        // Remove items
        for (String req : task.getItems()) {
            ItemUtils.removeItems(player, req);
        }

        // Calculate time
        int baseTime = task.getWaitTimeSeconds();
        int discountPercent = Utils.getVIPTimeDiscount(player);
        int finalTime = baseTime - (baseTime * discountPercent / 100);
        if (finalTime < 0) finalTime = 0;

        activeTask.waitEndTime = System.currentTimeMillis() + (finalTime * 1000L);
        activeTask.state = PlayerDeliveryData.TaskState.WAITING;
        plugin.getPlayerDataManager().savePlayer(pData);

        player.sendMessage(plugin.getConfigManager().getMessage("task_completed"));
        
        // Reopen GUI to show waiting
        open(player);
    }

    private void claimReward(Player player, PlayerDeliveryData pData, PlayerDeliveryData.ActiveTask activeTask, TaskData task) {
        if (task == null || activeTask == null) return;
        
        // Give XP
        pData.setXp(pData.getXp() + task.getRewardXp());
        
        // Run commands
        for (String cmd : task.getRewardCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
        }

        // Give Vouchers (assuming Vouchers plugin API or just give item)
        // For vouchers, you usually use an API if available, but the prompt says Vouchers is in libs.
        // Let's assume we use Bukkit.dispatchCommand for Vouchers if we can't find an API easily, 
        // or since it's a plugin we could do VouchersPlugin.getInstance().getVoucherManager()... 
        // We will just run commands for vouchers if needed, or use the exact format requested.
        // The config has `gold_voucher:1`. We'll just run command `/voucher give %player% gold_voucher 1` to be safe if no direct API.
        // Wait, Vouchers might have an API. But command is safer and universal.
        for (String voucher : task.getRewardVouchers()) {
            String[] vParts = voucher.split(":");
            if (vParts.length >= 2) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "voucher give " + player.getName() + " " + vParts[0] + " " + vParts[1]);
            }
        }

        // Check Level Up logic
        int nextLevel = pData.getLevel() + 1;
        int maxLevel = plugin.getConfigManager().getMaxLevel();
        if (pData.getLevel() < maxLevel) {
            int xpNeeded = plugin.getConfigManager().getXpRequired(nextLevel);
            if (pData.getXp() >= xpNeeded) {
                pData.setLevel(nextLevel);
                pData.setXp(pData.getXp() - xpNeeded);
                player.sendMessage(Utils.color("&aChúc mừng! Bạn đã thăng lên &eCấp độ " + pData.getLevel() + "&a!"));
            }
        }

        // Remove active task status
        pData.removeTask(activeTask.taskId);
        
        // Advance category progress
        int progress = pData.getCategoryProgress(category);
        pData.setCategoryProgress(category, progress + 1);
        
        plugin.getPlayerDataManager().savePlayer(pData);

        player.sendMessage(plugin.getConfigManager().getMessage("task_claim"));
        player.closeInventory();
        
        // Open the Task List GUI again
        new TaskListGUI(plugin, category).open(player);
    }
}
