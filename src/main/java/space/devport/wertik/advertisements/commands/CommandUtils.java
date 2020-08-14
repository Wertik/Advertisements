package space.devport.wertik.advertisements.commands;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import space.devport.wertik.advertisements.AdvertPlugin;

@UtilityClass
public class CommandUtils {

    public OfflinePlayer getTargetPlayer(CommandSender sender, String targetName) {
        OfflinePlayer offlinePlayer = Bukkit.getPlayer(targetName);

        if (offlinePlayer == null) {
            AdvertPlugin.getInstance().getLanguageManager().getPrefixed("Commands.Invalid-Player")
                    .replace("%param%", targetName)
                    .send(sender);
            return null;
        }
        return offlinePlayer;
    }
}
