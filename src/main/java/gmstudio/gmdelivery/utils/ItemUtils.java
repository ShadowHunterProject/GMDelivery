package gmstudio.gmdelivery.utils;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {

    public static ItemStack parseItem(String itemString) {
        String[] parts = itemString.split(":");
        if (parts.length < 3) return null;

        String type = parts[0].toUpperCase();
        if (type.equals("VANILLA")) {
            Material mat = Material.matchMaterial(parts[1]);
            if (mat == null) return null;
            int amount = Integer.parseInt(parts[2]);
            return new ItemStack(mat, amount);
        } else if (type.equals("MMOITEMS")) {
            if (parts.length < 4) return null;
            String mmoType = parts[1];
            String id = parts[2];
            int amount = Integer.parseInt(parts[3]);
            
            // Generate MMOItem
            ItemStack item = MMOItems.plugin.getItem(mmoType, id);
            if (item != null) {
                item.setAmount(amount);
            }
            return item;
        } else if (type.equals("VOUCHERS")) {
            if (parts.length < 3) return null;
            String id = parts[1];
            int amount = Integer.parseInt(parts[2]);
            
            try {
                ItemStack item = dev.anhcraft.vouchers.api.ApiProvider.getApi().buildVoucher(id);
                if (item != null) {
                    item.setAmount(amount);
                }
                return item;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static boolean hasItems(Player player, String itemString) {
        if (itemString.toUpperCase().startsWith("MMOITEMS:")) {
            String[] parts = itemString.split(":");
            if (parts.length >= 4) {
                String mmoType = parts[1];
                String mmoId = parts[2];
                int reqAmount = Integer.parseInt(parts[3]);
                
                int count = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        io.lumine.mythic.lib.api.item.NBTItem nbt = io.lumine.mythic.lib.api.item.NBTItem.get(item);
                        if (nbt.hasType() && nbt.getType().equals(mmoType) && nbt.getString("MMOITEMS_ITEM_ID").equals(mmoId)) {
                            count += item.getAmount();
                        }
                    }
                }
                return count >= reqAmount;
            }
        }

        ItemStack required = parseItem(itemString);
        if (required == null) return false;
        
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(required)) {
                count += item.getAmount();
            }
        }
        return count >= required.getAmount();
    }

    public static void removeItems(Player player, String itemString) {
        if (itemString.toUpperCase().startsWith("MMOITEMS:")) {
            String[] parts = itemString.split(":");
            if (parts.length >= 4) {
                String mmoType = parts[1];
                String mmoId = parts[2];
                int toRemove = Integer.parseInt(parts[3]);
                
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && item.getType() != Material.AIR) {
                        io.lumine.mythic.lib.api.item.NBTItem nbt = io.lumine.mythic.lib.api.item.NBTItem.get(item);
                        if (nbt.hasType() && nbt.getType().equals(mmoType) && nbt.getString("MMOITEMS_ITEM_ID").equals(mmoId)) {
                            if (item.getAmount() <= toRemove) {
                                toRemove -= item.getAmount();
                                player.getInventory().setItem(i, null);
                            } else {
                                item.setAmount(item.getAmount() - toRemove);
                                toRemove = 0;
                            }
                            if (toRemove <= 0) break;
                        }
                    }
                }
                return;
            }
        }

        ItemStack required = parseItem(itemString);
        if (required == null) return;
        
        int toRemove = required.getAmount();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.isSimilar(required)) {
                if (item.getAmount() <= toRemove) {
                    toRemove -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - toRemove);
                    toRemove = 0;
                }
                if (toRemove <= 0) break;
            }
        }
    }
}
