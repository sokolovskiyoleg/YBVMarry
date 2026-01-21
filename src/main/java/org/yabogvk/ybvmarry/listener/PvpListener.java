package org.yabogvk.ybvmarry.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.yabogvk.ybvmarry.YBVMarry;

import java.util.UUID;

public class PvpListener implements Listener {
    private final YBVMarry plugin;

    public PvpListener(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim)) {
            return;
        }

        UUID attackerUUID = attacker.getUniqueId();
        UUID victimUUID = victim.getUniqueId();

        UUID partnerId = plugin.getMarriageManager().getPartner(attackerUUID);

        if (partnerId != null && partnerId.equals(victimUUID)) {
            if (!plugin.getMarriageManager().isPvpEnabled(attackerUUID)) {
                event.setCancelled(true);

                long remaining = plugin.getCooldownManager().getRemaining("pvp-msg", attackerUUID);
                if (remaining <= 0) {
                    attacker.sendMessage(plugin.getConfigManager().getMessage("pvp-blocked"));
                    plugin.getCooldownManager().setCooldown("pvp-msg", attackerUUID, 2);
                }
            }
        }
    }
}