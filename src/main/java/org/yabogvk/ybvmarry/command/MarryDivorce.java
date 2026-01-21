package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.manager.MarriageManager;

import java.util.UUID;

public class MarryDivorce implements SubCommand {
    private final YBVMarry plugin;

    public MarryDivorce(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        UUID playerId = player.getUniqueId();
        MarriageManager manager = plugin.getMarriageManager();

        if (!manager.isMarried(playerId)) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        UUID partnerUUID = manager.getPartner(playerId);
        String partnerName = (partnerUUID != null) ? Bukkit.getOfflinePlayer(partnerUUID).getName() : "Unknown";

        manager.divorce(playerId);

        player.sendMessage(plugin.getConfigManager().getMessage("divorce-success", partnerName));

        if (partnerUUID != null) {
            Player partner = Bukkit.getPlayer(partnerUUID);
            if (partner != null && partner.isOnline()) {
                partner.sendMessage(plugin.getConfigManager().getMessage("divorce-partner-notification", player.getName()));
            }
        }

        if (plugin.getConfigManager().getBoolean("settings.broadcast-divorce", false)) {
            String broadcastMsg = plugin.getConfigManager().getMessage("divorce-broadcast", player.getName(), partnerName);
            Bukkit.broadcastMessage(broadcastMsg);
        }
    }
}