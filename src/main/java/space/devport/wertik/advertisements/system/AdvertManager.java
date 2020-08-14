package space.devport.wertik.advertisements.system;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import space.devport.wertik.advertisements.AdvertPlugin;
import space.devport.wertik.advertisements.system.struct.Advert;
import space.devport.wertik.advertisements.system.struct.AdvertAccount;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@RequiredArgsConstructor
public class AdvertManager {

    private final AdvertPlugin plugin;

    private final Map<UUID, AdvertAccount> cache = new LinkedHashMap<>();

    private final Gson gson = new GsonBuilder()
            // .setPrettyPrinting()
            .create();

    @Getter
    private AutoSaveTask autoSave;

    @Getter
    private AdvertTask advertTask;

    public void startAutoSave() {
        if (plugin.getConfig().getBoolean("auto-save.enabled")) {
            autoSave = new AutoSaveTask();
            autoSave.load();
            autoSave.start();
        }
    }

    public void reloadAutoSave() {
        if (autoSave == null)
            startAutoSave();
        else
            autoSave.stop();

        if (autoSave == null) return;

        autoSave.load();
        autoSave.start();
    }

    public void startAdvertTask() {
        if (advertTask == null) {
            advertTask = new AdvertTask();
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

        Advert advert = new Advert(uniqueID, name);

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

    public void cancelAdvert(UUID uniqueID, String regionName) {
        if (!hasAccount(uniqueID)) return;
        AdvertAccount account = getAdvertAccount(uniqueID);
        account.removeAdvert(regionName);

        if (account.getAdverts().isEmpty())
            deleteAccount(uniqueID);
    }

    /**
     * Cancel an advert.
     */
    public void cancelAdvert(OfflinePlayer offlinePlayer, String regionName) {
        cancelAdvert(offlinePlayer.getUniqueId(), regionName);
    }

    /**
     * Remove empty Accounts.
     */
    public int purgeEmpty() {
        int count = 0;
        for (UUID uniqueID : cache.keySet()) {
            AdvertAccount advertAccount = cache.get(uniqueID);
            advertAccount.removeInvalid();
            if (advertAccount.getAdverts().isEmpty()) {
                cache.remove(uniqueID);
                count++;
            }
        }
        return count;
    }

    public void save() {
        plugin.getConsoleOutput().info("Purged " + purgeEmpty() + " empty account(s)...");

        final Map<UUID, AdvertAccount> finalCache = new HashMap<>(cache);

        plugin.getConsoleOutput().info("Saving " + finalCache.size() + " advert account(s)...");

        String output = gson.toJson(finalCache, new TypeToken<Map<UUID, AdvertAccount>>() {
        }.getType());

        plugin.getConsoleOutput().debug("JSON: " + output);

        Path path = Paths.get(plugin.getDataFolder().getPath() + "/data.json");

        try {
            Files.write(path, output.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        cache.clear();

        Path path = Paths.get(plugin.getDataFolder().getPath() + "/data.json");

        if (!Files.exists(path)) return;

        String input;
        try {
            input = String.join("", Files.readAllLines(path));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (Strings.isNullOrEmpty(input)) return;

        cache.putAll(gson.fromJson(input, new TypeToken<Map<UUID, AdvertAccount>>() {
        }.getType()));

        plugin.getConsoleOutput().info("Loaded " + cache.size() + " advert account(s) with " + collectAdverts().size() + " advert(s)...");
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