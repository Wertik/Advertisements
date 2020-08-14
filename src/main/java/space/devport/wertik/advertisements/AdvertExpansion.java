package space.devport.wertik.advertisements;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.wertik.advertisements.system.struct.Advert;

import java.util.Date;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AdvertExpansion extends PlaceholderExpansion {

    private final AdvertPlugin plugin;

    /*
     * %adverts_hasmarket%
     *
     * %adverts_hasadverts%
     * %adverts_adverts%
     *
     * %adverts_adverts_count%
     * %adverts_hasadvert_<name>%
     *
     * %adverts_expire_<name>_<date/until>%
     */

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        if (player == null)
            return "";

        String[] arr = params.split("_");

        if (arr.length > 0)
            switch (arr[0].toLowerCase()) {
                case "hasmarket":
                    if (plugin.getBridge() == null) return "no_arm";
                    return plugin.getLanguageManager().get("Placeholders." + plugin.getBridge().hasMarket(player)).color().toString();
                case "hasadverts":
                    return plugin.getLanguageManager().get("Placeholders." + plugin.getAdvertManager().hasAdverts(player)).color().toString();
                case "adverts":
                    if (arr.length > 1)
                        if (arr[1].equalsIgnoreCase("count"))
                            return String.valueOf(plugin.getAdvertManager().getAdverts(player).size());
                        else break;

                    return plugin.getAdvertManager().getAdverts(player).stream().map(Advert::getName).collect(Collectors.joining(", "));
                case "hasadvert":
                    if (arr.length < 2) return "not_enough_args";
                    return plugin.getLanguageManager().get("Placeholders." + plugin.getAdvertManager().getAdvertAccount(player).hasAdvert(arr[1])).color().toString();
                case "expire":
                    if (arr.length < 3) return "not_enough_args";
                    Advert advert = plugin.getAdvertManager().getAdvert(player, arr[1]);

                    if (advert == null) return "no_advert";

                    if (arr[2].equalsIgnoreCase("date")) {
                        long time = advert.getExpirationTime();
                        Date date = new Date(time);
                        return plugin.getDateFormat().format(date);
                    } else if (arr[2].equalsIgnoreCase("until")) {
                        long until = advert.getExpirationTime() - System.currentTimeMillis();
                        return DurationFormatUtils.formatDuration(until, plugin.getDurationFormat());
                    }
            }

        return "invalid_params";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "adverts";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
