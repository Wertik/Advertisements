package space.devport.wertik.advertisements.bridge;

import net.alex9849.arm.AdvancedRegionMarket;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class RegionMarketBridge {

    private static RegionMarketBridge instance;

    private AdvancedRegionMarket regionMarket;

    private RegionMarketBridge() {
    }

    public static RegionMarketBridge getInstance() {
        if (instance == null) instance = new RegionMarketBridge();
        return instance;
    }

    public boolean isHooked() {
        return this.regionMarket != null;
    }

    /**
     * Build the bridge.
     */
    public void hook() {
        this.regionMarket = AdvancedRegionMarket.getPlugin(AdvancedRegionMarket.class);
    }

    public void unHook() {
        this.regionMarket = null;
    }

    public boolean hasMarket(UUID uniqueID) {
        return isHooked() && !regionMarket.getRegionManager().getRegionsByOwner(uniqueID).isEmpty();
    }

    public boolean hasMarket(OfflinePlayer offlinePlayer) {
        return hasMarket(offlinePlayer.getUniqueId());
    }
}