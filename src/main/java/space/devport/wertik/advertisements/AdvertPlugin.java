package space.devport.wertik.advertisements;

import lombok.Getter;
import space.devport.utils.DevportPlugin;
import space.devport.wertik.advertisements.bridge.ARMBridge;
import space.devport.wertik.advertisements.commands.AdvertsCommand;
import space.devport.wertik.advertisements.commands.subcommands.BuySubCommand;
import space.devport.wertik.advertisements.commands.subcommands.CancelSubCommand;
import space.devport.wertik.advertisements.commands.subcommands.InfoSubCommand;
import space.devport.wertik.advertisements.commands.subcommands.ReloadSubCommand;
import space.devport.wertik.advertisements.system.AdvertManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class AdvertPlugin extends DevportPlugin {

    @Getter
    private static AdvertPlugin instance;

    @Getter
    private AdvertManager advertManager;

    @Getter
    private ARMBridge bridge;

    @Getter
    private DateFormat dateFormat;
    @Getter
    private String durationFormat;

    @Override
    public void onPluginEnable() {
        instance = this;

        loadOptions();

        advertManager = new AdvertManager(this);
        advertManager.load();

        advertManager.startAutoSave();
        advertManager.startAdvertTask();

        new AdvertLanguage();

        setupARM();
        setupPAPI();

        addMainCommand(new AdvertsCommand()
                .addSubCommand(new ReloadSubCommand())
                .addSubCommand(new BuySubCommand())
                .addSubCommand(new CancelSubCommand())
                .addSubCommand(new InfoSubCommand()));
    }

    private void setupPAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AdvertExpansion(this).register();
            consoleOutput.info("Found PlaceholderAPI! &aRegistering expansion.");
        }
    }

    private void setupARM() {
        if (getServer().getPluginManager().getPlugin("AdvancedRegionMarket") != null) {
            if (bridge != null) return;
            consoleOutput.info("Found &aAdvanced Region Market &7v&f" + getServer().getPluginManager().getPlugin("AdvancedRegionMarket").getDescription().getVersion());
            bridge = new ARMBridge();
            bridge.build();
        } else {
            if (bridge != null) {
                consoleOutput.warn("Uninstalled ARM, disabling bridge.");
                bridge = null;
                return;
            }

            if (getConfig().getBoolean("require-arm-market", false))
                consoleOutput.warn("Advanced Region Market has not been found.");
        }
    }

    private void loadOptions() {
        this.dateFormat = new SimpleDateFormat(getConfig().getString("formats.date-format", "d.M. H:m:s"));
        this.durationFormat = getConfig().getString("formats.duration-format", "H:m:s");
    }

    @Override
    public void onPluginDisable() {
        advertManager.save();
    }

    @Override
    public void onReload() {
        setupARM();
        setupPAPI();

        loadOptions();

        advertManager.reloadAutoSave();
        advertManager.reloadAdvertTask();
    }

    @Override
    public boolean useLanguage() {
        return true;
    }

    @Override
    public boolean useHolograms() {
        return false;
    }

    @Override
    public boolean useMenus() {
        return false;
    }
}
