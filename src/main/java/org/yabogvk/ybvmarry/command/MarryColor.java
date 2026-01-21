package org.yabogvk.ybvmarry.command;

import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MarryColor implements SubCommand {
    private final YBVMarry plugin;
    private static final Pattern HEX_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6})$");
    private final Map<String, String> colorMap = new HashMap<>();
    private final String COOLDOWN_KEY = "color";

    public MarryColor(YBVMarry plugin) {
        this.plugin = plugin;
        colorMap.put("RED", "#FF5555");
        colorMap.put("GREEN", "#55FF55");
        colorMap.put("BLUE", "#5555FF");
        colorMap.put("YELLOW", "#FFFF55");
        colorMap.put("WHITE", "#FFFFFF");
        colorMap.put("AQUA", "#55FFFF");
        colorMap.put("PINK", "#FF69B4");
        colorMap.put("PURPLE", "#AA00AA");
        colorMap.put("GOLD", "#FFAA00");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("ybvmarry.color")) {
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
            player.sendMessage(plugin.getConfigManager().getMessage("usage-color"));
            return;
        }

        String input = args[1].toUpperCase();
        String finalHex;

        if (colorMap.containsKey(input)) {
            finalHex = colorMap.get(input);
        }
        else if (HEX_PATTERN.matcher(input).matches()) {
            finalHex = input;
        }
        else {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-color"));
            return;
        }
        plugin.getMarriageManager().setMarriageColor(player.getUniqueId(), finalHex);
        player.sendMessage(plugin.getConfigManager().getMessage("color-changed", finalHex));

        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.color", 60);
        plugin.getCooldownManager().setCooldown(COOLDOWN_KEY, player.getUniqueId(), cooldownTime);
    }
}