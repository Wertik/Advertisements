package space.devport.wertik.advertisements.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.wertik.advertisements.AdvertPlugin;
import space.devport.wertik.advertisements.bridge.RegionMarketBridge;
import space.devport.wertik.advertisements.commands.AdvertSubCommand;
import space.devport.wertik.advertisements.system.struct.AdvertAccount;

public class BuySubCommand extends AdvertSubCommand {

    public BuySubCommand(AdvertPlugin plugin) {
        super(plugin, "buy");
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        boolean others = false;

        OfflinePlayer target;
        if (args.length > 1) {
            target = getPlugin().getCommandParser().parsePlayer(sender, args[1]);

            if (target == null) return CommandResult.FAILURE;
            others = true;
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        if (getPlugin().getConfig().getBoolean("require-arm-market", false)) {
            if (!RegionMarketBridge.getInstance().isHooked()) {
                language.sendPrefixed(sender, "Commands.ARM-Not-Hooked");
                return CommandResult.FAILURE;
            }

            if (!RegionMarketBridge.getInstance().hasMarket(target)) {
                language.sendPrefixed(sender, "Commands.Buy.No-Market");
                return CommandResult.FAILURE;
            }
        }

        AdvertAccount account = getPlugin().getAdvertManager().getAdvertAccount(target);

        if (account.hasMax()) {
            language.getPrefixed("Commands.Buy.Limit-Reached")
                    .replace("%amount%", account.getAdverts().size())
                    .replace("%max%", getPlugin().getConfig().getInt("adverts.limit", 1))
                    .send(sender);
            return CommandResult.FAILURE;
        }

        if (account.hasAdvert(args[0])) {
            language.getPrefixed("Commands.Buy.Duplicate-Name")
                    .replace("%name%", args[0])
                    .send(sender);
            return CommandResult.FAILURE;
        }

        if (!getPlugin().getAdvertManager().createAdvert(target, args[0])) {
            language.sendPrefixed(sender, "Commands.Buy.Could-Not-Create");
            return CommandResult.FAILURE;
        }

        language.getPrefixed(others ? "Commands.Buy.Done-Others" : "Commands.Buy.Done")
                .replace("%player%", target.getName())
                .replace("%name%", args[0])
                .send(sender);
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% buy <name> (player)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Buy an advert. Name it.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(1, 2);
    }
}