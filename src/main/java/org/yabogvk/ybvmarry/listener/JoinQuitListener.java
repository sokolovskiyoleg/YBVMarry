package org.yabogvk.ybvmarry.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;
import org.yabogvk.ybvmarry.util.MessageUtil;

public class JoinQuitListener implements Listener {
    private final YBVMarry plugin;

    public JoinQuitListener(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        handle(event.getPlayer(), "partner-join");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        handle(event.getPlayer(), "partner-quit");
    }

    private void handle(Player player, String configPath) {
        Marriage m = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (m == null || !m.isNotificationsEnabled()) return;

        Player partner = Bukkit.getPlayer(m.getPartnerOf(player.getUniqueId()));
        if (partner != null && partner.isOnline()) {
            String format = plugin.getConfigManager().getRawMessage(configPath);

            String raw = format
                    .replace("{0}", player.getName())
                    .replace("{SYMBOL}", m.getSymbol())
                    .replace("{COLOR}", m.getColor());

            partner.sendMessage(MessageUtil.color(raw));
        }
    }
}