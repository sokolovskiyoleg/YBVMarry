package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.manager.CooldownManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MarryPhrase implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "phrase";

    public MarryPhrase(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("ybvmarry.interact")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        UUID playerId = player.getUniqueId();
        CooldownManager cm = plugin.getCooldownManager();

        UUID partnerUUID = plugin.getMarriageManager().getPartner(playerId);
        if (partnerUUID == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        long remaining = cm.getRemaining(COOLDOWN_KEY, playerId);
        if (remaining > 0 && !player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remaining));
            return;
        }

        Player partner = Bukkit.getPlayer(partnerUUID);
        if (partner == null || !partner.isOnline()) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        List<String> phrases = plugin.getConfigManager().getPhrases();
        if (phrases.isEmpty()) {
            player.sendMessage("§cОшибка: Список фраз в конфиге пуст!");
            return;
        }

        String randomPhrase = phrases.get(ThreadLocalRandom.current().nextInt(phrases.size()));

        player.sendMessage(plugin.getConfigManager().getMessage("phrase-sent", randomPhrase));
        partner.sendMessage(plugin.getConfigManager().getMessage("phrase-received", player.getName(), randomPhrase));

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.phrase",
                plugin.getConfigManager().getInt("settings.phrase-cooldown", 60));

        cm.setCooldown(COOLDOWN_KEY, playerId, cooldownTime);
    }
}