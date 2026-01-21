package org.yabogvk.ybvmarry.command;

import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;

public class MarryHome implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "home";

    public MarryHome(YBVMarry plugin) {
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

        if (marriage.getHome() == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("home-not-set"));
            return;
        }

        player.setFallDistance(0);
        player.teleport(marriage.getHome());
        player.sendMessage(plugin.getConfigManager().getMessage("home-teleported"));

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.home", 120);
        plugin.getCooldownManager().setCooldown(COOLDOWN_KEY, player.getUniqueId(), cooldownTime);
    }
}