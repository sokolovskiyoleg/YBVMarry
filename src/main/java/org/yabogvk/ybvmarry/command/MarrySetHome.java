package org.yabogvk.ybvmarry.command;

import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;

public class MarrySetHome implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "sethome";

    public MarrySetHome(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("ybvmarry.home")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        long remaining = plugin.getCooldownManager().getRemaining(COOLDOWN_KEY, player.getUniqueId());
        if (remaining > 0 && !player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remaining));
            return;
        }

        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());

        if (marriage == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        plugin.getMarriageManager().setHome(player.getUniqueId(), player.getLocation());
        player.sendMessage(plugin.getConfigManager().getMessage("home-set"));

        org.bukkit.entity.Player partner = org.bukkit.Bukkit.getPlayer(marriage.getPartnerOf(player.getUniqueId()));
        if (partner != null && partner.isOnline()) {
            partner.sendMessage(plugin.getConfigManager().getMessage("home-set-partner", player.getName()));
        }

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.sethome", 300);
        plugin.getCooldownManager().setCooldown(COOLDOWN_KEY, player.getUniqueId(), cooldownTime);
    }
}