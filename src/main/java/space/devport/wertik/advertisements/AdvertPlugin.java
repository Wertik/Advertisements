package space.devport.wertik.advertisements;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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

    @Override
    public void onPluginEnable() {

        loadOptions();

        advertManager = new AdvertManager(this);
        advertManager.load();

        advertManager.startAutoSave();
        advertManager.startAdvertTask();

        new AdvertLanguage(this);

        setupRegionMarket();
        setupPlaceholders();

        this.commandParser = new CommandParser(this);

        addMainCommand(new AdvertsCommand()
                .addSubCommand(new ReloadSubCommand(this))
                .addSubCommand(new BuySubCommand(this))
                .addSubCommand(new CancelSubCommand(this))
                .addSubCommand(new InfoSubCommand(this)));
    }

    private void setupPlaceholders() {
        if (PlaceholderAPI.isRegistered("adverts") &&
                VersionUtil.compareVersions("2.10.9", getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion()) < 1 &&
                getPluginManager().getPlugin("PlaceholderAPI") != null) {

            PlaceholderExpansion expansion = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion("adverts");

            if (expansion != null) {
                expansion.unregister();
                consoleOutput.info("Unregistered old expansion version...");
            }
        }

        new AdvertExpansion(this).register();
        consoleOutput.info("Found PlaceholderAPI! &aRegistering expansion.");
    }

    private void setupRegionMarket() {
        if (getServer().getPluginManager().getPlugin("AdvancedRegionMarket") != null) {
            RegionMarketBridge.getInstance().hook();
            consoleOutput.info("Found &aAdvanced Region Market &7v&f" + getServer().getPluginManager().getPlugin("AdvancedRegionMarket").getDescription().getVersion());
        } else {
            if (RegionMarketBridge.getInstance().isHooked()) {
                consoleOutput.warn("Uninstalled ARM, disabling bridge.");
                RegionMarketBridge.getInstance().unHook();
                return;
            }

            if (getConfig().getBoolean("require-arm-market", false))
                consoleOutput.warn("Advanced Region Market has not been found.");
        }
    }

    private void loadOptions() {
        this.dateFormat = new SimpleDateFormat(getConfig().getString("formats.date-format", "d.M. HH:mm:ss"));
        this.durationFormat = getConfig().getString("formats.duration-format", "HH:mm:ss");
    }

    @Override
    public void onPluginDisable() {
        advertManager.getAdvertTask().stop();
        advertManager.getAutoSave().stop();

        advertManager.save();
    }

    @Override
    public void onReload() {
        setupRegionMarket();
        setupPlaceholders();

        loadOptions();

        advertManager.reloadAutoSave();
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