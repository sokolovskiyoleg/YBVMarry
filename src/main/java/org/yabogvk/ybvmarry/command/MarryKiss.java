package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;
import org.yabogvk.ybvmarry.manager.CooldownManager;

import java.util.UUID;

public class MarryKiss implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "kiss";

    public MarryKiss(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("ybvmarry.interact")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        UUID playerId = player.getUniqueId();
        Marriage marriage = plugin.getMarriageManager().getMarriage(playerId);

        if (marriage == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        CooldownManager cm = plugin.getCooldownManager();
        long remaining = cm.getRemaining(COOLDOWN_KEY, playerId);
        if (remaining > 0 && !player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remaining));
            return;
        }

        UUID partnerUUID = marriage.getPartnerOf(playerId);
        Player partner = Bukkit.getPlayer(partnerUUID);

        if (partner == null || !partner.isOnline()) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        if (player.getWorld() != partner.getWorld() || player.getLocation().distance(partner.getLocation()) > 3) {
            player.sendMessage(plugin.getConfigManager().getMessage("kiss-too-far"));
            return;
        }

        spawnKissParticles(player);
        spawnKissParticles(partner);

        player.sendMessage(plugin.getConfigManager().getMessage("kiss-sender", partner.getName()));
        partner.sendMessage(plugin.getConfigManager().getMessage("kiss-receiver", player.getName()));

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.kiss", 10);
        cm.setCooldown(COOLDOWN_KEY, playerId, cooldownTime);
    }

    private void spawnKissParticles(Player p) {
        p.getWorld().spawnParticle(
                Particle.HEART,
                p.getLocation().add(0, 1.8, 0),
                5, 0.3, 0.3, 0.3, 0.1
        );
    }
}