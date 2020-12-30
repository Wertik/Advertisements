package space.devport.wertik.advertisements.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.wertik.advertisements.AdvertPlugin;
import space.devport.wertik.advertisements.commands.AdvertSubCommand;
import space.devport.wertik.advertisements.system.struct.AdvertAccount;

public class CancelSubCommand extends AdvertSubCommand {

    public CancelSubCommand(AdvertPlugin plugin) {
        super(plugin, "cancel");
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        boolean others = false;

        OfflinePlayer target;
        if (args.length > 1) {
            target = plugin.getCommandParser().parsePlayer(sender, args[1]);

            if (target == null) return CommandResult.FAILURE;

            if (!sender.hasPermission("advertisements.cancel.others")) return CommandResult.NO_PERMISSION;

            others = true;
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        AdvertAccount account = plugin.getAdvertManager().getAdvertAccount(target);

        if (!account.hasAdvert(args[0])) {
            language.getPrefixed(others ? "Commands.None-Others" : "Commands.None")
                    .replace("%player%", target.getName())
                    .send(sender);
            return CommandResult.FAILURE;
        }

        account.removeAdvert(args[0]);
        language.getPrefixed("Commands.Cancel.Done")
                .replace("%player%", others ? target.getName() : language.get("Commands.You").color().toString())
                .replace("%name%", args[0])
                .send(sender);
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% cancel <name> (player)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Cancel an advert.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(1, 2);
    }
}