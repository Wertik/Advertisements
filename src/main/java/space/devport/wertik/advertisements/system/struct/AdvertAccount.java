package space.devport.wertik.advertisements.system.struct;

import lombok.Getter;
import org.bukkit.Bukkit;
import space.devport.wertik.advertisements.AdvertPlugin;
import space.devport.wertik.advertisements.bridge.RegionMarketBridge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class AdvertAccount {

    @Getter
    private final UUID owner;

    @Getter
    private final String lastKnownName;

    private final Set<Advert> adverts = new HashSet<>();

    public AdvertAccount(UUID owner) {
        this.owner = owner;
        this.lastKnownName = Bukkit.getOfflinePlayer(owner).getName();
    }

    public boolean removeInvalid() {
        for (Iterator<Advert> iterator = adverts.iterator(); iterator.hasNext(); ) {
            AdvertPlugin plugin = AdvertPlugin.getInstance();
            Advert advert = iterator.next();

            if (advert.isExpired() ||
                    (plugin.getConfig().getBoolean("require-arm-market", false) &&
                            !plugin.getBridge().hasMarket(owner))) {

                adverts.remove(advert);
                advert.sendExpireNotification();

                plugin.getAdvertManager().unScheduleAdvert(advert);
            }
        }
        return adverts.isEmpty();
    }

    public void removeAdvert(String advertName) {
        removeInvalid();
        Iterator<Advert> advertIterator = adverts.iterator();

        while (advertIterator.hasNext()) {
            Advert advert = advertIterator.next();

            if (advert.getName().equals(advertName)) {
                advertIterator.remove();
                advert.sendExpireNotification();

                AdvertPlugin.getInstance().getAdvertManager().unScheduleAdvert(advert);
            }
        }
    }

    public Set<Advert> getAdverts() {
        removeInvalid();
        return adverts;
    }

    public void addAdvert(Advert advert) {
        removeInvalid();
        adverts.add(advert);
    }

    public boolean hasAdvert(String name) {
        removeInvalid();
        return adverts.stream().anyMatch(a -> a.getName().equals(name));
    }

    public boolean hasAdverts() {
        removeInvalid();
        return !adverts.isEmpty();
    }

    public boolean hasMax() {
        removeInvalid();
        return adverts.size() >= AdvertPlugin.getInstance().getConfig().getInt("adverts.limit", 1);
    }
}