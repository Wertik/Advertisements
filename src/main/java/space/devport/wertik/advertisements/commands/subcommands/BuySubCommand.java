package space.devport.wertik.advertisements.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.SubCommand;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.utils.commands.struct.Preconditions;
import space.devport.wertik.advertisements.AdvertPlugin;
import space.devport.wertik.advertisements.commands.CommandUtils;
import space.devport.wertik.advertisements.system.struct.AdvertAccount;

public class BuySubCommand extends SubCommand {

    private final AdvertPlugin plugin;

    public BuySubCommand() {
        super("buy");
        this.plugin = AdvertPlugin.getInstance();
        this.preconditions = new Preconditions()
                .permissions("advertisements.buy");
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        boolean others = false;

        OfflinePlayer target;
        if (args.length > 1) {
            target = CommandUtils.getTargetPlayer(sender, args[1]);

            if (target == null) return CommandResult.FAILURE;
            others = true;
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        if (plugin.getConfig().getBoolean("require-arm-market", false)) {
            if (plugin.getBridge() == null) {
                language.sendPrefixed(sender, "Commands.ARM-Not-Hooked");
                return CommandResult.FAILURE;
            }

            if (!plugin.getBridge().hasMarket(target)) {
                language.sendPrefixed(sender, "Commands.Buy.No-Market");
                return CommandResult.FAILURE;
            }
        }

        AdvertAccount account = plugin.getAdvertManager().getAdvertAccount(target);

        if (account.hasMax()) {
            language.getPrefixed("Commands.Buy.Limit-Reached")
                    .replace("%amount%", account.getAdverts().size())
                    .replace("%max%", plugin.getConfig().getInt("adverts.limit", 1))
                    .send(sender);
            return CommandResult.FAILURE;
        }

        if (account.hasAdvert(args[0])) {
            language.getPrefixed("Commands.Buy.Duplicate-Name")
                    .replace("%name%", args[0])
                    .send(sender);
            return CommandResult.FAILURE;
        }

        if (!plugin.getAdvertManager().createAdvert(target, args[0])) {
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