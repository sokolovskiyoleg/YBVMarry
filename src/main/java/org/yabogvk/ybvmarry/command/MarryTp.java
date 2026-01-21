package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.manager.CooldownManager;

import java.util.UUID;

public class MarryTp implements SubCommand {
    private final YBVMarry plugin;

    public MarryTp(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        UUID uuid = player.getUniqueId();
        CooldownManager cm = plugin.getCooldownManager();

        long remaining = cm.getRemaining("tp", uuid);
        if (remaining > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remaining));
            return;
        }

        UUID partnerUUID = plugin.getMarriageManager().getPartner(uuid);
        if (partnerUUID == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        Player partner = Bukkit.getPlayer(partnerUUID);
        if (partner == null || !partner.isOnline()) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        int delaySeconds = plugin.getConfigManager().getInt("settings.tp-delay", 3);
        player.sendMessage(plugin.getConfigManager().getMessage("tp-started", delaySeconds));

        Location startLoc = player.getLocation();

        new BukkitRunnable() {
            int secondsRemaining = delaySeconds;

            @Override
            public void run() {
                if (!player.isOnline() || !partner.isOnline()) {
                    this.cancel();
                    return;
                }

                if (player.getLocation().distanceSquared(startLoc) > 0.1) {
                    player.sendMessage(plugin.getConfigManager().getMessage("tp-cancelled"));
                    this.cancel();
                    return;
                }

                secondsRemaining--;

                if (secondsRemaining <= 0) {
                    player.setFallDistance(0);
                    player.teleport(partner.getLocation());
                    player.sendMessage(plugin.getConfigManager().getMessage("tp-success"));

                    int cooldownTime = plugin.getConfigManager().getInt("settings.tp-cooldown", 300);
                    cm.setCooldown("tp", uuid, cooldownTime);

                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}