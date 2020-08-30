package space.devport.wertik.advertisements.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import space.devport.wertik.advertisements.AdvertPlugin;

public class CommandParser {

    private final AdvertPlugin plugin;

    public CommandParser(AdvertPlugin plugin) {
        this.plugin = plugin;
    }

    public OfflinePlayer parsePlayer(CommandSender sender, String targetName) {
        OfflinePlayer offlinePlayer = Bukkit.getPlayer(targetName);

        if (offlinePlayer == null) {
            plugin.getLanguageManager().getPrefixed("Commands.Invalid-Player")
                    .replace("%param%", targetName)
                    .send(sender);
            return null;
        }
        return offlinePlayer;
    }
}