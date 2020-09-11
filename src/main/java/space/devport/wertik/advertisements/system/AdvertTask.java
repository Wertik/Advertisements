package space.devport.wertik.advertisements.system;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import space.devport.wertik.advertisements.AdvertPlugin;
import space.devport.wertik.advertisements.system.struct.Advert;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

public class AdvertTask implements Runnable {

    private final AdvertPlugin plugin;

    private final Deque<Advert> loadedAdverts = new ArrayDeque<>();

    // ticks
    private int interval;
    private BukkitTask task;

    public AdvertTask(AdvertPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.interval = plugin.getConfig().getInt("adverts.interval", 300) * 20;
    }

    public void start() {
        if (task != null)
            stop();

        if (loadedAdverts.isEmpty())
            queueAdverts();

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, interval, interval);
        plugin.getConsoleOutput().info("Started advertisement schedule.");
    }

    public void stop() {
        if (task == null) return;

        task.cancel();
        task = null;
    }

    /**
     * Returns true if we loaded any adverts.
     */
    public boolean queueAdverts() {
        loadedAdverts.addAll(plugin.getAdvertManager().collectAdverts());
        return !loadedAdverts.isEmpty();
    }

    /**
     * Queue advert to be the next one to appear.
     * If the task is not running, starts it.
     */
    public void queueAdvert(Advert advert) {
        loadedAdverts.addFirst(advert);
    }

    public void removeAdvert(Advert advert) {
        loadedAdverts.remove(advert);
    }

    private Advert getNext() {

        if (loadedAdverts.isEmpty()) return null;

        Advert advert;
        try {
            advert = loadedAdverts.pop();
        } catch (NoSuchElementException e) {
            return null;
        }

        // Check expiration
        if (advert.isExpired()) {
            plugin.getConsoleOutput().debug("Advert " + advert.getName() + " expired.");
            return getNext();
        }

        return advert;
    }

    @Override
    public void run() {

        if (loadedAdverts.isEmpty() && !queueAdverts()) {
            plugin.getConsoleOutput().debug("Could not fetch any adverts.");
            return;
        }

        Advert advert = getNext();

        if (advert == null) {
            plugin.getConsoleOutput().debug("There's no next advert.");
            return;
        }

        advert.send();
        plugin.getConsoleOutput().debug("Sent message for " + advert.getName() + " and updated queue.");

        plugin.getAdvertManager().getAdvertAccount(advert.getOwner()).removeInvalid();
    }

    public boolean isRunning() {
        return task != null;
    }
}