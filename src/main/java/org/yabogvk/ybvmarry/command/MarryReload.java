package org.yabogvk.ybvmarry.command;

import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;

public class MarryReload implements SubCommand {
    private final YBVMarry plugin;

    public MarryReload(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        plugin.getConfigManager().reload();
        player.sendMessage(plugin.getConfigManager().getMessage("reload-success"));
    }
}