package gmstudio.gmdelivery.utils;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;

public class Utils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String message) {
        if (message == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String color = matcher.group(1);
            StringBuilder builder = new StringBuilder("&x");
            for (char c : color.toCharArray()) {
                builder.append("&").append(c);
            }
            message = message.replace(matcher.group(), builder.toString());
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatTime(int totalSeconds) {
        if (totalSeconds <= 0) return "0s";
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }
    
    public static int getVIPTimeDiscount(Player player) {
        int maxDiscount = 0;
        for (org.bukkit.permissions.PermissionAttachmentInfo pio : player.getEffectivePermissions()) {
            String perm = pio.getPermission();
            if (perm.startsWith("gmdelivery.time.")) {
                try {
                    String[] parts = perm.split("\\.");
                    int discount = Integer.parseInt(parts[parts.length - 1]);
                    if (discount > maxDiscount) {
                        maxDiscount = discount;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return maxDiscount;
    }

    public static int getMaxTasks(Player player) {
        int max = 1; // Default
        for (org.bukkit.permissions.PermissionAttachmentInfo pio : player.getEffectivePermissions()) {
            String perm = pio.getPermission();
            if (perm.startsWith("gmdelivery.tasks.")) {
                try {
                    String[] parts = perm.split("\\.");
                    int count = Integer.parseInt(parts[parts.length - 1]);
                    if (count > max) {
                        max = count;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return max;
    }
}
