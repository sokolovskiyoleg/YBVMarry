package org.yabogvk.ybvmarry.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;
import org.yabogvk.ybvmarry.util.MessageUtil;

import java.util.UUID;

public class ChatListener implements Listener {
    private final YBVMarry plugin;

    public ChatListener(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.getChatToggledPlayers().contains(uuid)) {
            Marriage marriage = plugin.getMarriageManager().getMarriage(uuid);
            if (marriage == null) return;

            event.setCancelled(true);

            String format = plugin.getConfigManager().getRawMessage("chat-format");

            String pColor = marriage.getColor(); // HEX-код, например #FF0000
            String pSymbol = marriage.getSymbol(); // Символ, например ★

            String finalRaw = format
                    .replace("{0}", player.getName())
                    .replace("{1}", event.getMessage())
                    .replace("{SYMBOL}", pSymbol)
                    .replace("{COLOR}", pColor)
                    // Оставляем обратную совместимость, если вдруг в конфиге осталось сердечко
                    .replace("❤", pColor + pSymbol);

            String finalMessage = MessageUtil.color(finalRaw);

            player.sendMessage(finalMessage);

            Player partner = Bukkit.getPlayer(marriage.getPartnerOf(uuid));
            if (partner != null && partner.isOnline()) {
                partner.sendMessage(finalMessage);
            }
        }
    }
}