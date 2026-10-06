package gmstudio.gmdelivery.gui;

import gmstudio.gmdelivery.GMDelivery;
import gmstudio.gmdelivery.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vn.gmcore.GMCore;
import vn.gmcore.gui.ConfigGUI;
import vn.gmcore.gui.GUIItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryGUI {

    private final GMDelivery plugin;
    private int page = 1;

    public CategoryGUI(GMDelivery plugin) {
        this.plugin = plugin;
        this.page = 1;
    }

    public CategoryGUI(GMDelivery plugin, int page) {
        this.plugin = plugin;
        this.page = page;
    }

    public void open(Player player) {
        String baseTitle = Utils.color(plugin.getConfigManager().getConfig().getString("gui_category.title", "&8Danh Mục Nhiệm Vụ"));

        ConfigurationSection categoriesConfig = plugin.getConfigManager().getCategories().getConfigurationSection("categories");
        List<String> keys = new ArrayList<>();
        if (categoriesConfig != null) {
            keys.addAll(categoriesConfig.getKeys(false));
        }

        int maxPages = (int) Math.ceil(keys.size() / 21.0);
        if (maxPages == 0) maxPages = 1;
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        List<String> layout = Arrays.asList(
                "BBBBBBBBB",
                "B       B",
                "B       B",
                "B       B",
                "BBBBBBBBB",
                "BBPBBBNBB"
        );

        ConfigGUI gui = new ConfigGUI()
                .setTitle(baseTitle + Utils.color(" &7- Trang " + page + "/" + maxPages))
                .setLayout(layout);

        // Background
        String bgMat = plugin.getConfigManager().getConfig().getString("gui_category.items.background.material", "BLACK_STAINED_GLASS_PANE");
        ItemStack bgItem = new ItemStack(Material.matchMaterial(bgMat) != null ? Material.matchMaterial(bgMat) : Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bgItem.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(" ");
            bgItem.setItemMeta(bgMeta);
        }
        gui.setIngredient('B', new GUIItem(bgItem));
        gui.setIngredient(' ', new GUIItem(bgItem));

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
                new CategoryGUI(plugin, page - 1).open(player);
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
                new CategoryGUI(plugin, page + 1).open(player);
            }));
        } else {
            gui.setIngredient('N', new GUIItem(bgItem));
        }

        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        int startIndex = (page - 1) * 21;
        int endIndex = Math.min(startIndex + 21, keys.size());

        for (int i = 0; i < endIndex - startIndex; i++) {
            int categoryIndex = startIndex + i;
            String key = keys.get(categoryIndex);
            int slot = contentSlots[i];

            String name = categoriesConfig.getString(key + ".display-name");
            String iconStr = categoriesConfig.getString(key + ".icon");
            List<String> lore = categoriesConfig.getStringList(key + ".lore");

            Material mat = Material.matchMaterial(iconStr);
            if (mat == null) mat = Material.PAPER;

            ItemStack item = new ItemStack(mat);
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

            GUIItem guiItem = new GUIItem(item, event -> {
                event.setCancelled(true);
                new TaskListGUI(plugin, key).open(player);
            });
            
            gui.setItem(slot, guiItem);
        }

        GMCore.getInstance().getGuiManager().openGUI(player, gui);
    }
}
