package space.devport.wertik.advertisements.system;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import space.devport.utils.utility.json.GsonHelper;
import space.devport.wertik.advertisements.AdvertPlugin;
import space.devport.wertik.advertisements.system.struct.Advert;
import space.devport.wertik.advertisements.system.struct.AdvertAccount;

import java.util.*;

public class AdvertManager {

    private final AdvertPlugin plugin;

    private final Map<UUID, AdvertAccount> cache = new LinkedHashMap<>();

    private final GsonHelper gsonHelper = new GsonHelper();

    @Getter
    private AdvertTask advertTask;

    public AdvertManager(AdvertPlugin plugin) {
        this.plugin = plugin;
    }

    @Getter
    private BukkitTask autoSave;

    public void stopAutoSave() {
        if (autoSave == null)
            return;

        autoSave.cancel();
        this.autoSave = null;
    }

    public void startAutoSave() {
        stopAutoSave();

        if (plugin.getConfig().getBoolean("auto-save.enabled", false)) {
            long interval = plugin.getConfig().getInt("auto-save.interval", 300) * 20L;
            this.autoSave = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::save, interval, interval);
        }
    }

    public void startAdvertTask() {
        if (advertTask == null) {
            advertTask = new AdvertTask(plugin);
            advertTask.load();
        }

        if (!advertTask.isRunning())
            advertTask.start();
    }

    public void reloadAdvertTask() {
        advertTask.stop();
        advertTask.load();
        advertTask.start();
    }

    public boolean hasAccount(UUID uniqueID) {
        return cache.containsKey(uniqueID);
    }

    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return hasAccount(offlinePlayer.getUniqueId());
    }

    public boolean hasAdverts(UUID uniqueID) {
        return cache.containsKey(uniqueID) && !cache.get(uniqueID).getAdverts().isEmpty();
    }

    public boolean hasAdverts(OfflinePlayer offlinePlayer) {
        return hasAdverts(offlinePlayer.getUniqueId());
    }

    /**
     * Delete player account and dequeue adverts scheduled.
     */
    public void deleteAccount(UUID uniqueID) {
        AdvertAccount account = cache.getOrDefault(uniqueID, null);
        if (account == null) return;

        cache.remove(uniqueID);

        for (Advert advert : account.getAdverts()) {
            advertTask.removeAdvert(advert);
        }
    }

    /**
     * Get the advert account of a player.
     * If he doesn't have one, create one.
     */
    public AdvertAccount getAdvertAccount(UUID uniqueID) {
        if (!cache.containsKey(uniqueID)) {
            AdvertAccount advertAccount = new AdvertAccount(uniqueID);
            cache.put(uniqueID, advertAccount);
            return advertAccount;
        } else
            return cache.get(uniqueID);
    }

    public AdvertAccount getAdvertAccount(OfflinePlayer offlinePlayer) {
        return getAdvertAccount(offlinePlayer.getUniqueId());
    }

    @Nullable
    public Advert getAdvert(UUID uniqueID, String regionName) {
        if (!hasAccount(uniqueID)) return null;

        Set<Advert> adverts = cache.get(uniqueID).getAdverts();

        if (adverts.isEmpty()) return null;

        return adverts.stream()
                .filter(a -> a.getName().equals(regionName))
                .findAny().orElse(null);
    }

    @Nullable
    public Advert getAdvert(OfflinePlayer offlinePlayer, String regionName) {
        return getAdvert(offlinePlayer.getUniqueId(), regionName);
    }

    public Set<Advert> getAdverts(UUID uniqueID) {
        return getAdvertAccount(uniqueID).getAdverts();
    }

    public Set<Advert> getAdverts(OfflinePlayer offlinePlayer) {
        return getAdverts(offlinePlayer.getUniqueId());
    }

    /**
     * Create an advert for player.
     */
    public boolean createAdvert(UUID uniqueID, String name) {
        AdvertAccount advertAccount = getAdvertAccount(uniqueID);

        if (advertAccount.hasAdvert(name) || advertAccount.hasMax()) return false;

        long expirationTime = System.currentTimeMillis() + plugin.getConfig().getInt("adverts.expiration-time", 86400) * 1000L;

        Advert advert = new Advert(uniqueID, name, expirationTime);

        advertAccount.addAdvert(advert);
        advertTask.queueAdvert(advert);
        return true;
    }

    /**
     * Create an advert for player.
     */
    public boolean createAdvert(OfflinePlayer offlinePlayer, String name) {
        return createAdvert(offlinePlayer.getUniqueId(), name);
    }

    public void unScheduleAdvert(Advert advert) {
        if (advertTask == null) return;

        advertTask.removeAdvert(advert);
    }

    public void cancelAdvert(UUID uniqueID, String name) {
        if (!hasAccount(uniqueID)) return;
        AdvertAccount account = getAdvertAccount(uniqueID);
        account.removeAdvert(name);

        if (account.getAdverts().isEmpty())
            deleteAccount(uniqueID);
    }

    /**
     * Cancel an advert.
     */
    public void cancelAdvert(OfflinePlayer offlinePlayer, String name) {
        cancelAdvert(offlinePlayer.getUniqueId(), name);
    }

    /**
     * Remove empty Accounts.
     */
    public int purgeEmpty() {
        int count = 0;
        for (Iterator<UUID> iterator = cache.keySet().iterator(); iterator.hasNext(); ) {
            UUID uniqueID = iterator.next();

            AdvertAccount advertAccount = cache.get(uniqueID);

            if (advertAccount.removeInvalid()) {
                cache.remove(uniqueID);
                count++;
            }
        }
        return count;
    }

    public void save() {
        plugin.getConsoleOutput().info(String.format("Purged %d empty account(s)...", purgeEmpty()));

        final Map<UUID, AdvertAccount> finalCache = new HashMap<>(cache);

        gsonHelper.save(finalCache, plugin.getDataFolder() + "/data.json")
                .thenRunAsync(() -> plugin.getConsoleOutput().info(String.format("Saved %d user(s)...", finalCache.size())));
    }

    public void load() {
        gsonHelper.loadMapAsync(plugin.getDataFolder() + "/data.json", UUID.class, AdvertAccount.class).thenAcceptAsync(loaded -> {
            if (loaded == null)
                loaded = new HashMap<>();

            cache.clear();
            cache.putAll(loaded);

            plugin.getConsoleOutput().info(String.format("Loaded %d advert account(s) with %d advert(s)...", loaded.size(), collectAdverts().size()));
        }).exceptionally(e -> {
            plugin.getConsoleOutput().err("Could not load users: " + e.getMessage());
            e.printStackTrace();
            return null;
        });
    }

    /**
     * Collect all adverts.
     */
    public Set<Advert> collectAdverts() {
        Set<Advert> out = new HashSet<>();
        for (AdvertAccount account : cache.values()) {
            out.addAll(account.getAdverts());
        }
        return out;
    }

    public Map<UUID, AdvertAccount> getCache() {
        return Collections.unmodifiableMap(cache);
    }

    public Collection<AdvertAccount> getAccounts() {
        return Collections.unmodifiableCollection(cache.values());
    }
}