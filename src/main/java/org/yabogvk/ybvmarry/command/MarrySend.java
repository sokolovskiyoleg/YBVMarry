package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.manager.MarriageManager;
import org.yabogvk.ybvmarry.manager.CooldownManager;

import java.util.UUID;

public class MarrySend implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "send";

    public MarrySend(YBVMarry plugin) {
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

        long remaining = cm.getRemaining(COOLDOWN_KEY, playerId);
        if (remaining > 0 && !player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remaining));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage-send"));
            return;
        }

        MarriageManager manager = plugin.getMarriageManager();

        if (manager.isMarried(playerId)) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-married"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        if (target.getUniqueId().equals(playerId)) {
            player.sendMessage(plugin.getConfigManager().getMessage("marry-self"));
            return;
        }

        if (manager.isMarried(target.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("target-already-married"));
            return;
        }

        manager.sendProposal(playerId, target.getUniqueId());

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.send",
                plugin.getConfigManager().getInt("settings.send-cooldown", 30));
        cm.setCooldown(COOLDOWN_KEY, playerId, cooldownTime);

        player.sendMessage(plugin.getConfigManager().getMessage("proposal-sent", target.getName()));
        target.sendMessage(plugin.getConfigManager().getMessage("proposal-received", player.getName()));
    }
}