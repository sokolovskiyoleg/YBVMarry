package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;

import java.util.UUID;

public class MarryStatus implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "status";

    public MarryStatus(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("ybvmarry.user")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        UUID playerId = player.getUniqueId();
        long remaining = plugin.getCooldownManager().getRemaining(COOLDOWN_KEY, playerId);
        if (remaining > 0 && !player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remaining));
            return;
        }

        Marriage m = plugin.getMarriageManager().getMarriage(playerId);
        if (m == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        boolean newState = !m.isNotificationsEnabled();
        m.setNotificationsEnabled(newState);

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.status", 30);
        plugin.getCooldownManager().setCooldown(COOLDOWN_KEY, playerId, cooldownTime);

        String msgPath = newState ? "status-enabled" : "status-disabled";
        String message = plugin.getConfigManager().getMessage(msgPath);

        player.sendMessage(message);

        UUID partnerUUID = m.getPartnerOf(playerId);
        Player partner = Bukkit.getPlayer(partnerUUID);

        if (partner != null && partner.isOnline()) {
            partner.sendMessage(message);
        }
    }
}