package space.devport.wertik.advertisements;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import space.devport.utils.DevportPlugin;
import space.devport.utils.UsageFlag;
import space.devport.utils.utility.VersionUtil;
import space.devport.wertik.advertisements.bridge.RegionMarketBridge;
import space.devport.wertik.advertisements.commands.AdvertsCommand;
import space.devport.wertik.advertisements.commands.CommandParser;
import space.devport.wertik.advertisements.commands.subcommands.BuySubCommand;
import space.devport.wertik.advertisements.commands.subcommands.CancelSubCommand;
import space.devport.wertik.advertisements.commands.subcommands.InfoSubCommand;
import space.devport.wertik.advertisements.commands.subcommands.ReloadSubCommand;
import space.devport.wertik.advertisements.system.AdvertManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class AdvertPlugin extends DevportPlugin {

    @Getter
    private AdvertManager advertManager;

    @Getter
    private DateFormat dateFormat;
    @Getter
    private String durationFormat;

    @Getter
    private CommandParser commandParser;

    @Getter
    private AdvertExpansion expansion;

    @Getter
    private final RegionMarketBridge bridge = new RegionMarketBridge();

    @Override
    public void onPluginEnable() {
        loadOptions();

        advertManager = new AdvertManager(this);
        advertManager.load();

        new AdvertLanguage(this);

        registerRegionMarket();
        registerPlaceholders();

        this.commandParser = new CommandParser(this);

        addMainCommand(new AdvertsCommand()
                .addSubCommand(new ReloadSubCommand(this))
                .addSubCommand(new BuySubCommand(this))
                .addSubCommand(new CancelSubCommand(this))
                .addSubCommand(new InfoSubCommand(this)));

        advertManager.startAutoSave();
        advertManager.startAdvertTask();
    }

    private void unregisterPlaceholders() {
        Plugin papiPlugin = getPluginManager().getPlugin("PlaceholderAPI");
        if (papiPlugin != null &&
                expansion != null &&
                expansion.isRegistered() &&
                VersionUtil.compareVersions("2.10.9", papiPlugin.getDescription().getVersion()) < 1) {

            expansion.unregister();
            consoleOutput.info("Unregistered old expansion...");
        }
    }

    private void registerPlaceholders() {
        unregisterPlaceholders();

        if (expansion == null)
            this.expansion = new AdvertExpansion(this);

        expansion.register();
        consoleOutput.info("Found PlaceholderAPI! &aRegistering expansion.");
    }

    private void registerRegionMarket() {
        Plugin armPlugin = getServer().getPluginManager().getPlugin("AdvancedRegionMarket");
        if (armPlugin != null) {
            bridge.hook();
            consoleOutput.info("Found &aAdvanced Region Market &7v&f" + armPlugin.getDescription().getVersion());
        } else {
            if (bridge.isHooked()) {
                consoleOutput.warn("Uninstalled ARM, disabling bridge.");
                bridge.unHook();
                return;
            }

            if (getConfig().getBoolean("require-arm-market", false))
                consoleOutput.warn("Advanced Region Market has not been found.");
        }
    }

    private void loadOptions() {
        this.dateFormat = new SimpleDateFormat(getConfiguration().getString("formats.date-format", "d.M. HH:mm:ss"));
        this.durationFormat = getConfig().getString("formats.duration-format", "HH:mm:ss");
    }

    @Override
    public void onPluginDisable() {
        advertManager.getAdvertTask().stop();
        advertManager.stopAutoSave();

        advertManager.save();
    }

    @Override
    public void onReload() {
        registerRegionMarket();
        registerPlaceholders();

        loadOptions();

        advertManager.startAutoSave();
        advertManager.reloadAdvertTask();
    }

    @Override
    public UsageFlag[] usageFlags() {
        return new UsageFlag[]{UsageFlag.CONFIGURATION, UsageFlag.COMMANDS, UsageFlag.LANGUAGE};
    }

    public static AdvertPlugin getInstance() {
        return getPlugin(AdvertPlugin.class);
    }
}