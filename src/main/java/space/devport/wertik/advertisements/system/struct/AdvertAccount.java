package space.devport.wertik.advertisements.system.struct;

import lombok.Getter;
import org.bukkit.Bukkit;
import space.devport.wertik.advertisements.AdvertPlugin;

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

    public void removeInvalid() {
        Iterator<Advert> advertIterator = adverts.iterator();
        while (advertIterator.hasNext()) {
            Advert advert = advertIterator.next();

            if (advert.isExpired() || (AdvertPlugin.getInstance().getBridge() != null && !AdvertPlugin.getInstance().getBridge().hasMarket(owner))) {
                advertIterator.remove();
                advert.sendExpireNotification();

                AdvertPlugin.getInstance().getAdvertManager().getAdvertTask().removeAdvert(advert);
            }
        }
    }

    public void removeAdvert(String advertName) {
        Iterator<Advert> advertIterator = adverts.iterator();
        while (advertIterator.hasNext()) {
            Advert advert = advertIterator.next();

            if (advert.getName().equals(advertName)) {
                advertIterator.remove();
                advert.sendExpireNotification();

                AdvertPlugin.getInstance().getAdvertManager().getAdvertTask().removeAdvert(advert);
            }
        }
    }

    public Set<Advert> getAdverts() {
        removeInvalid();
        return adverts;
    }

    public void addAdvert(Advert advert) {
        adverts.add(advert);
    }

    public boolean hasAdvert(String name) {
        return adverts.stream().anyMatch(a -> a.getName().equals(name));
    }

    public boolean hasAdverts() {
        return !adverts.isEmpty();
    }

    public boolean hasMax() {
        return adverts.size() >= AdvertPlugin.getInstance().getConfig().getInt("adverts.limit", 1);
    }
}