package space.devport.wertik.advertisements.system.struct;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import space.devport.utils.text.message.Message;
import space.devport.wertik.advertisements.AdvertPlugin;

import java.util.Date;
import java.util.UUID;

public class Advert {

    @Getter
    private final UUID owner;

    @Getter
    private final String name;

    @Getter
    @Setter
    private long expirationTime;

    public Advert(UUID owner, String name) {
        this.owner = owner;
        this.name = name;
        this.expirationTime = System.currentTimeMillis() + (AdvertPlugin.getInstance().getConfig().getInt("adverts.expiration-time", 86400) * 1000);
    }

    public Advert(UUID owner, String name, long expirationTime) {
        this.owner = owner;
        this.name = name;
        this.expirationTime = expirationTime;
    }

    /**
     * Send expire message.
     */
    public void sendExpireNotification() {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);

        if (!offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) return;

        AdvertPlugin.getInstance().getLanguageManager().getPrefixed("Advert-Expired")
                .replace("%name%", name)
                .send(offlinePlayer.getPlayer());
    }

    public boolean isExpired() {
        return expirationTime <= System.currentTimeMillis();
    }

    /**
     * Send the advert message to all online players.
     */
    public void send() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player);
        }
    }

    /**
     * Send the advert message to a player.
     */
    public void send(Player player) {
        compose().replace("%player%", player.getName())
                .send(player);
    }

    /**
     * Compose the advert message.
     */
    public Message compose() {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(this.owner);

        Message template = AdvertPlugin.getInstance().getLanguageManager().get("Advert-Message");

        return template.replace("%owner%", owner.getName());
    }

    /**
     * Formatted string of date of expiration.
     */
    public String getExpirationDate() {
        return AdvertPlugin.getInstance().getDateFormat().format(new Date(expirationTime));
    }

    /**
     * Formatted string of time until expiration.
     */
    public String getUntilExpiration() {
        return DurationFormatUtils.formatDuration(expirationTime - System.currentTimeMillis(), AdvertPlugin.getInstance().getDurationFormat());
    }
}