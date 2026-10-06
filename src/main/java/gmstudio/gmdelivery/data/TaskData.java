package gmstudio.gmdelivery.data;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class TaskData {
    private final String id;
    private final String category;
    private final String name;
    private final List<String> lore;
    private final int levelRequired;
    private final int waitTimeSeconds;
    private final List<String> items;
    private final int rewardXp;
    private final List<String> rewardCommands;
    private final List<String> rewardVouchers;

    public TaskData(String id, String category, String name, List<String> lore, int levelRequired, int waitTimeSeconds, List<String> items, int rewardXp, List<String> rewardCommands, List<String> rewardVouchers) {
        this.id = id;
        this.category = category;
        this.name = name != null ? name : "&eTask #" + id;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.levelRequired = levelRequired;
        this.waitTimeSeconds = waitTimeSeconds;
        this.items = items != null ? items : new ArrayList<>();
        this.rewardXp = rewardXp;
        this.rewardCommands = rewardCommands != null ? rewardCommands : new ArrayList<>();
        this.rewardVouchers = rewardVouchers != null ? rewardVouchers : new ArrayList<>();
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public List<String> getLore() { return lore; }
    public int getLevelRequired() { return levelRequired; }
    public int getWaitTimeSeconds() { return waitTimeSeconds; }
    public List<String> getItems() { return items; }
    public int getRewardXp() { return rewardXp; }
    public List<String> getRewardCommands() { return rewardCommands; }
    public List<String> getRewardVouchers() { return rewardVouchers; }
}
