package gmstudio.gmdelivery.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDeliveryData {
    private final UUID uuid;
    private int level;
    private int xp;
    
    public enum TaskState {
        NONE, WAITING, READY
    }

    public static class ActiveTask {
        public String taskId;
        public long waitEndTime;
        public TaskState state;

        public ActiveTask(String taskId, long waitEndTime, String state) {
            this.taskId = taskId;
            this.waitEndTime = waitEndTime;
            try {
                this.state = TaskState.valueOf(state);
            } catch (Exception e) {
                this.state = TaskState.NONE;
            }
        }
    }

    private final Map<String, ActiveTask> activeTasks = new HashMap<>();
    private final Map<String, Integer> categoryProgress = new HashMap<>();

    public PlayerDeliveryData(UUID uuid, int level, int xp) {
        this.uuid = uuid;
        this.level = level;
        this.xp = xp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public Map<String, ActiveTask> getActiveTasks() {
        return activeTasks;
    }
    
    public void addTask(ActiveTask task) {
        activeTasks.put(task.taskId, task);
    }
    
    public void removeTask(String taskId) {
        activeTasks.remove(taskId);
    }
    
    public ActiveTask getTask(String taskId) {
        return activeTasks.get(taskId);
    }

    public Map<String, Integer> getCategoryProgressMap() {
        return categoryProgress;
    }

    public int getCategoryProgress(String category) {
        return categoryProgress.getOrDefault(category, 0);
    }

    public void setCategoryProgress(String category, int progress) {
        categoryProgress.put(category, progress);
    }
}
