package org.yabogvk.ybvmarry.command;

import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import java.util.Map;

public class MarrySymbol implements SubCommand {
    private final YBVMarry plugin;
    private final String COOLDOWN_KEY = "symbol";

    public MarrySymbol(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("ybvmarry.symbol")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        long remaining = plugin.getCooldownManager().getRemaining(COOLDOWN_KEY, player.getUniqueId());
        if (remaining > 0 && !player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remaining));
            return;
        }

        if (!plugin.getMarriageManager().isMarried(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage-symbol"));
            return;
        }

        String input = args[1].toLowerCase();
        Map<String, String> allowedSymbols = plugin.getConfigManager().getMarrySymbols();

        if (!allowedSymbols.containsKey(input)) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-symbol"));
            return;
        }

        String finalSymbol = allowedSymbols.get(input);
        plugin.getMarriageManager().setMarriageSymbol(player.getUniqueId(), finalSymbol);

        player.sendMessage(plugin.getConfigManager().getMessage("symbol-changed", finalSymbol));

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.symbol", 60);
        plugin.getCooldownManager().setCooldown(COOLDOWN_KEY, player.getUniqueId(), cooldownTime);
    }
}