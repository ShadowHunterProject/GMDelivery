package gmstudio.gmdelivery.gui;

import gmstudio.gmdelivery.GMDelivery;
import gmstudio.gmdelivery.data.PlayerDeliveryData;
import gmstudio.gmdelivery.data.TaskData;
import gmstudio.gmdelivery.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vn.gmcore.GMCore;
import vn.gmcore.gui.ConfigGUI;
import vn.gmcore.gui.GUIItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskListGUI {

    private final GMDelivery plugin;
    private final String category;
    private int page = 1;

    public TaskListGUI(GMDelivery plugin, String category) {
        this.plugin = plugin;
        this.category = category;
        this.page = 1;
    }

    public TaskListGUI(GMDelivery plugin, String category, int page) {
        this.plugin = plugin;
        this.category = category;
        this.page = page;
    }

    public void open(Player player) {
        PlayerDeliveryData pData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (pData == null) return;

        List<TaskData> tasks = plugin.getTaskManager().getTasksForCategory(category);
        if (tasks.isEmpty()) {
            player.sendMessage(Utils.color("&cKhông có nhiệm vụ nào trong danh mục này!"));
            return;
        }

        int maxPages = (int) Math.ceil(tasks.size() / 21.0);
        if (maxPages == 0) maxPages = 1;
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        // Create a basic 54 slot layout for the task list
        List<String> layout = Arrays.asList(
                "BBBBBBBBB",
                "B       B",
                "B       B",
                "B       B",
                "BBBBBBBBB",
                "BBPBXBNBB"
        );

        ConfigGUI gui = new ConfigGUI()
                .setTitle(Utils.color("&8&lDanh Sách Nhiệm Vụ &7- Trang " + page + "/" + maxPages))
                .setLayout(layout);

        // Background
        ItemStack bgItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bgItem.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(" ");
            bgItem.setItemMeta(bgMeta);
        }
        gui.setIngredient('B', new GUIItem(bgItem));
        gui.setIngredient(' ', new GUIItem(bgItem));
        
        // Close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(Utils.color("&c&lTrở Lại"));
            closeItem.setItemMeta(closeMeta);
        }
        gui.setIngredient('X', new GUIItem(closeItem, event -> {
            event.setCancelled(true);
            new CategoryGUI(plugin).open(player);
        }));

        // Pagination buttons
        if (page > 1) {
            ItemStack prevItem = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevItem.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(Utils.color("&e&l⬅ Trang Trước"));
                prevItem.setItemMeta(prevMeta);
            }
            gui.setIngredient('P', new GUIItem(prevItem, event -> {
                event.setCancelled(true);
                new TaskListGUI(plugin, category, page - 1).open(player);
            }));
        } else {
            gui.setIngredient('P', new GUIItem(bgItem));
        }

        if (page < maxPages) {
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextItem.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(Utils.color("&e&lTrang Sau ➡"));
                nextItem.setItemMeta(nextMeta);
            }
            gui.setIngredient('N', new GUIItem(nextItem, event -> {
                event.setCancelled(true);
                new TaskListGUI(plugin, category, page + 1).open(player);
            }));
        } else {
            gui.setIngredient('N', new GUIItem(bgItem));
        }

        int progress = pData.getCategoryProgress(category);

        // Slots for content
        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        int startIndex = (page - 1) * 21;
        int endIndex = Math.min(startIndex + 21, tasks.size());

        for (int i = 0; i < endIndex - startIndex; i++) {
            int taskIndex = startIndex + i;
            TaskData task = tasks.get(taskIndex);
            int slot = contentSlots[i];

            ItemStack taskItem;
            GUIItem guiItem;

            if (taskIndex < progress) {
                // Completed task
                taskItem = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = taskItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(Utils.color(task.getName()));
                    List<String> lore = new ArrayList<>();
                    lore.add(Utils.color("&a&l✔ Đã hoàn thành"));
                    lore.add(Utils.color("&7Bạn đã hoàn thành giao dịch này."));
                    meta.setLore(lore);
                    taskItem.setItemMeta(meta);
                }
                guiItem = new GUIItem(taskItem, event -> event.setCancelled(true));
            } else if (taskIndex == progress) {
                // ACTIVE TASK!
                PlayerDeliveryData.ActiveTask activeTask = pData.getTask(task.getId());
                if (activeTask == null) {
                    activeTask = new PlayerDeliveryData.ActiveTask(task.getId(), 0, "NONE");
                    pData.addTask(activeTask);
                }

                // Check wait time
                if (activeTask.state == PlayerDeliveryData.TaskState.WAITING) {
                    if (System.currentTimeMillis() >= activeTask.waitEndTime) {
                        activeTask.state = PlayerDeliveryData.TaskState.READY;
                        plugin.getPlayerDataManager().savePlayer(pData);
                    }
                }
                
                final PlayerDeliveryData.ActiveTask finalActiveTask = activeTask;

                if (activeTask.state == PlayerDeliveryData.TaskState.READY) {
                    taskItem = new ItemStack(Material.GOLD_BLOCK);
                    ItemMeta meta = taskItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(Utils.color("&6&l★ Nhận Thù Lao ★"));
                        List<String> lore = new ArrayList<>();
                        lore.add(Utils.color("&7▶ &fThương nhân đã kiểm định xong!"));
                        lore.add(Utils.color("   &eNhấn để nhận phần thưởng của bạn."));
                        meta.setLore(lore);
                        taskItem.setItemMeta(meta);
                    }
                    guiItem = new GUIItem(taskItem, event -> {
                        event.setCancelled(true);
                        claimReward(player, pData, finalActiveTask, task);
                    });
                } else if (activeTask.state == PlayerDeliveryData.TaskState.WAITING) {
                    taskItem = new ItemStack(Material.CLOCK);
                    ItemMeta meta = taskItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(Utils.color("&e&l⏳ Đang Kiểm Định..."));
                        long timeLeft = (activeTask.waitEndTime - System.currentTimeMillis()) / 1000;
                        if (timeLeft < 0) timeLeft = 0;
                        List<String> lore = new ArrayList<>();
                        lore.add(Utils.color("&7▶ &fThương nhân đang kiểm tra hàng hóa."));
                        lore.add(Utils.color("   &7Thời gian còn lại: &e" + Utils.formatTime((int) timeLeft)));
                        meta.setLore(lore);
                        taskItem.setItemMeta(meta);
                    }
                    guiItem = new GUIItem(taskItem, event -> {
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        // Just reopen to refresh timer
                        open(player);
                    });
                } else {
                    // NONE - Ready to submit
                    taskItem = new ItemStack(Material.BOOK);
                    ItemMeta meta = taskItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(Utils.color(task.getName()));
                        List<String> lore = new ArrayList<>();
                        for (String l : task.getLore()) {
                            lore.add(Utils.color(l));
                        }
                        lore.add("");
                        lore.add(Utils.color("&e&lYêu Cầu Giao Nộp:"));
                        for (String req : task.getItems()) {
                            ItemStack parsed = gmstudio.gmdelivery.utils.ItemUtils.parseItem(req);
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
                            boolean has = gmstudio.gmdelivery.utils.ItemUtils.hasItems(player, req);
                            String color = has ? "&a&l✔ &a" : "&c&l❌ &c";
                            lore.add(Utils.color("  " + color + name + " &fx" + amount));
                        }
                        lore.add("");
                        lore.add(Utils.color("&a&l[!] &aNhấn vào đây để nộp toàn bộ!"));
                        meta.setLore(lore);
                        taskItem.setItemMeta(meta);
                    }
                    guiItem = new GUIItem(taskItem, event -> {
                        event.setCancelled(true);
                        submitTask(player, pData, finalActiveTask, task);
                    });
                }
            } else {
                // Locked task
                taskItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = taskItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(Utils.color("&c&lChưa Mở Khóa"));
                    List<String> lore = new ArrayList<>();
                    lore.add(Utils.color("&7Nhiệm vụ này đang bị khóa,"));
                    lore.add(Utils.color("&7hãy làm nhiệm vụ trước đó!"));
                    meta.setLore(lore);
                    taskItem.setItemMeta(meta);
                }
                guiItem = new GUIItem(taskItem, event -> {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                });
            }

            gui.setItem(slot, guiItem);
        }

        GMCore.getInstance().getGuiManager().openGUI(player, gui);
    }

    private void submitTask(Player player, PlayerDeliveryData pData, PlayerDeliveryData.ActiveTask activeTask, TaskData task) {
        // Check items
        for (String req : task.getItems()) {
            if (!gmstudio.gmdelivery.utils.ItemUtils.hasItems(player, req)) {
                player.sendMessage(plugin.getConfigManager().getMessage("not_enough_items"));
                return;
            }
        }

        // Remove items
        for (String req : task.getItems()) {
            gmstudio.gmdelivery.utils.ItemUtils.removeItems(player, req);
        }

        // Calculate time
        int baseTime = task.getWaitTimeSeconds();
        int discountPercent = Utils.getVIPTimeDiscount(player);
        int finalTime = baseTime - (baseTime * discountPercent / 100);
        if (finalTime < 0) finalTime = 0;

        activeTask.waitEndTime = System.currentTimeMillis() + (finalTime * 1000L);
        activeTask.state = PlayerDeliveryData.TaskState.WAITING;
        plugin.getPlayerDataManager().savePlayer(pData);

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0);
        player.sendMessage(plugin.getConfigManager().getMessage("task_completed"));
        open(player);
    }

    private void claimReward(Player player, PlayerDeliveryData pData, PlayerDeliveryData.ActiveTask activeTask, TaskData task) {
        // Give XP
        pData.setXp(pData.getXp() + task.getRewardXp());
        
        // Run commands
        for (String cmd : task.getRewardCommands()) {
            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
        }

        for (String voucher : task.getRewardVouchers()) {
            String[] vParts = voucher.split(":");
            if (vParts.length >= 2) {
                org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), "voucher give " + player.getName() + " " + vParts[0] + " " + vParts[1]);
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

        // Advance category progress
        pData.removeTask(activeTask.taskId);
        int progress = pData.getCategoryProgress(category);
        pData.setCategoryProgress(category, progress + 1);
        plugin.getPlayerDataManager().savePlayer(pData);

        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.0f);
        player.spawnParticle(org.bukkit.Particle.FIREWORK, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        player.sendMessage(plugin.getConfigManager().getMessage("task_claim"));
        open(player);
    }
}
