package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.manager.MarriageManager;
import org.yabogvk.ybvmarry.manager.CooldownManager;

import java.util.UUID;

public class MarryPvp implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "pvp";

    public MarryPvp(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("ybvmarry.user")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        UUID playerId = player.getUniqueId();
        CooldownManager cm = plugin.getCooldownManager();
        MarriageManager manager = plugin.getMarriageManager();

        if (!manager.isMarried(playerId)) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        long remaining = cm.getRemaining(COOLDOWN_KEY, playerId);
        if (remaining > 0 && !player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remaining));
            return;
        }

        manager.togglePvp(playerId);
        boolean newState = manager.isPvpEnabled(playerId);

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.pvp",
                plugin.getConfigManager().getInt("settings.pvp-cooldown", 10));
        cm.setCooldown(COOLDOWN_KEY, playerId, cooldownTime);

        String messagePath = newState ? "pvp-enabled" : "pvp-disabled";
        String notification = plugin.getConfigManager().getMessage(messagePath);

        player.sendMessage(notification);

        UUID partnerUUID = manager.getPartner(playerId);
        if (partnerUUID != null) {
            Player partner = Bukkit.getPlayer(partnerUUID);
            if (partner != null && partner.isOnline()) {
                partner.sendMessage(notification);
            }
        }
    }
}