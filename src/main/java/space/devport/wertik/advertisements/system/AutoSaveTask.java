package space.devport.wertik.advertisements.system;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import space.devport.wertik.advertisements.AdvertPlugin;

public class AutoSaveTask implements Runnable {

    private final AdvertPlugin plugin;

    private BukkitTask task;

    // ticks
    private int interval;

    public AutoSaveTask(AdvertPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        interval = plugin.getConfig().getInt("auto-save.interval") * 20;
    }

    public void start() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, interval, interval);
    }

    public void stop() {
        if (task == null) return;
        task.cancel();
        task = null;
    }

    @Override
    public void run() {
        plugin.getAdvertManager().save();
    }
}
