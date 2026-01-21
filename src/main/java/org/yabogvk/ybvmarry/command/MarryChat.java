package org.yabogvk.ybvmarry.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;
import org.yabogvk.ybvmarry.util.MessageUtil;

import java.util.UUID;

public class MarryChat implements SubCommand {
    private final YBVMarry plugin;

    public MarryChat(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        UUID playerId = player.getUniqueId();

        Marriage marriage = plugin.getMarriageManager().getMarriage(playerId);

        if (marriage == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        if (args.length == 1) {
            if (plugin.getChatToggledPlayers().contains(playerId)) {
                plugin.getChatToggledPlayers().remove(playerId);
                player.sendMessage(plugin.getConfigManager().getMessage("chat-toggle-off"));
            } else {
                plugin.getChatToggledPlayers().add(playerId);
                player.sendMessage(plugin.getConfigManager().getMessage("chat-toggle-on"));
            }
            return;
        }

        String msgContent = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        UUID partnerUUID = marriage.getPartnerOf(playerId);
        Player partner = Bukkit.getPlayer(partnerUUID);

        String format = plugin.getConfigManager().getRawMessage("chat-format");

        String pColor = marriage.getColor();
        String pSymbol = marriage.getSymbol();

        String finalRaw = format
                .replace("{0}", player.getName())
                .replace("{1}", msgContent)
                .replace("{SYMBOL}", pSymbol)
                .replace("{COLOR}", pColor);

        String finalMessage = MessageUtil.color(finalRaw);

        player.sendMessage(finalMessage);

        if (partner != null && partner.isOnline()) {
            partner.sendMessage(finalMessage);
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("chat-no-partner"));
        }
    }
}
