package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;

import java.util.Map;
import java.util.UUID;

public class MarryGift implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "gift";

    public MarryGift(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("ybvmarry.interact")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        long remainingCooldown = plugin.getCooldownManager().getRemaining(COOLDOWN_KEY, player.getUniqueId());
        if (remainingCooldown > 0 && !player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remainingCooldown));
            return;
        }

        UUID playerId = player.getUniqueId();
        Marriage marriage = plugin.getMarriageManager().getMarriage(playerId);

        if (marriage == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        UUID partnerUUID = marriage.getPartnerOf(playerId);
        Player partner = Bukkit.getPlayer(partnerUUID);

        if (partner == null || !partner.isOnline()) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(plugin.getConfigManager().getMessage("gift-no-item"));
            return;
        }

        Map<Integer, ItemStack> remaining = partner.getInventory().addItem(itemInHand.clone());
        if (!remaining.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("gift-inventory-full"));
            return;
        }

        String itemName = itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName()
                ? itemInHand.getItemMeta().getDisplayName()
                : itemInHand.getType().name().replace("_", " ");

        player.getInventory().setItemInMainHand(null);

        player.sendMessage(plugin.getConfigManager().getMessage("gift-sent", itemName));
        partner.sendMessage(plugin.getConfigManager().getMessage("gift-received", player.getName(), itemName));

        partner.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, partner.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.gift", 30);
        plugin.getCooldownManager().setCooldown(COOLDOWN_KEY, player.getUniqueId(), cooldownTime);
    }
}