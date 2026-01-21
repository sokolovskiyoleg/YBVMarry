package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;
import org.yabogvk.ybvmarry.manager.CooldownManager;
import org.yabogvk.ybvmarry.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarryList implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "list";

    public MarryList(YBVMarry plugin) {
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

        List<Marriage> allMarriages = new ArrayList<>(plugin.getMarriageManager().getAllMarriages());

        if (allMarriages.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("list-empty"));
            return;
        }

        int itemsPerPage = plugin.getConfigManager().getInt("settings.list-items-per-page", 10);
        int totalPages = (int) Math.ceil((double) allMarriages.size() / itemsPerPage);

        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) { page = 1; }
        }

        if (page < 1 || page > totalPages) {
            player.sendMessage(plugin.getConfigManager().getMessage("list-invalid-page", totalPages));
            return;
        }

        player.sendMessage(plugin.getConfigManager().getMessage("list-header", page, totalPages));

        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allMarriages.size());
        String format = plugin.getConfigManager().getRawMessage("list-format");

        for (int i = start; i < end; i++) {
            Marriage m = allMarriages.get(i);
            String pColor = m.getColor();
            String pSymbol = (m.getSymbol() != null) ? m.getSymbol() : "❤";

            String entry = format
                    .replace("{0}", getName(m.p1()))
                    .replace("{1}", getName(m.p2()))
                    .replace("{SYMBOL}", pSymbol)
                    .replace("{COLOR}", pColor);

            player.sendMessage(MessageUtil.color(entry));
        }

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.list", 10);
        cm.setCooldown(COOLDOWN_KEY, playerId, cooldownTime);
    }

    private String getName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        String name = op.getName();
        return (name != null) ? name : "Unknown";
    }
}