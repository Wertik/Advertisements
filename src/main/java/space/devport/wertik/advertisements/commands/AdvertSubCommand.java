package space.devport.wertik.advertisements.commands;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import space.devport.utils.commands.SubCommand;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.wertik.advertisements.AdvertPlugin;

public abstract class AdvertSubCommand extends SubCommand {

    @Getter
    private final AdvertPlugin plugin;

    public AdvertSubCommand(AdvertPlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
        setPermissions();
    }

    @Override
    public abstract @Nullable String getDefaultUsage();

    @Override
    public abstract @Nullable String getDefaultDescription();

    @Override
    public abstract @Nullable ArgumentRange getRange();
}