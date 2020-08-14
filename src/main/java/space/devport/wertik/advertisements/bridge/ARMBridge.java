package space.devport.wertik.advertisements.bridge;

import net.alex9849.arm.AdvancedRegionMarket;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class ARMBridge {

    private AdvancedRegionMarket regionMarket;

    public void build() {
        regionMarket = AdvancedRegionMarket.getInstance();
    }

    public boolean hasMarket(UUID uniqueID) {
        return !regionMarket.getRegionManager().getRegionsByOwner(uniqueID).isEmpty();
    }

    public boolean hasMarket(OfflinePlayer offlinePlayer) {
        return hasMarket(offlinePlayer.getUniqueId());
    }
}