package space.devport.wertik.advertisements.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.utils.text.message.Message;
import space.devport.wertik.advertisements.AdvertPlugin;
import space.devport.wertik.advertisements.commands.AdvertSubCommand;
import space.devport.wertik.advertisements.system.struct.Advert;
import space.devport.wertik.advertisements.system.struct.AdvertAccount;

public class InfoSubCommand extends AdvertSubCommand {

    public InfoSubCommand(AdvertPlugin plugin) {
        super(plugin, "info");
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        boolean others = false;

        OfflinePlayer target;
        if (args.length > 0) {
            target = plugin.getCommandParser().parsePlayer(sender, args[0]);

            if (target == null) return CommandResult.FAILURE;

            if (!sender.hasPermission("advertisements.info.others")) return CommandResult.NO_PERMISSION;

            others = true;
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        AdvertAccount account = plugin.getAdvertManager().getAdvertAccount(target);

        account.removeInvalid();

        if (!account.hasAdverts()) {
            language.getPrefixed(others ? "Commands.None-Others" : "Commands.None")
                    .replace("&f%player%", target.getName())
                    .send(sender);
            return CommandResult.FAILURE;
        }

        Message header = language.get("Commands.Info.Header");

        for (Advert advert : account.getAdverts()) {
            header.append(language.get("Commands.Info.Advert-Line")
                    .replace("%name%", advert.getName())
                    .replace("%expire%", advert.getExpirationDate())
                    .replace("%until%", advert.getUntilExpiration())
                    .color()
                    .toString());
        }

        header.append(language.get("Commands.Info.Footer"))
                .replace("%player%", others ? target.getName() : language.get("Commands.You").color().toString())
                .send(sender);
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% info (player)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Display info about players adverts.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(0, 1);
    }
}