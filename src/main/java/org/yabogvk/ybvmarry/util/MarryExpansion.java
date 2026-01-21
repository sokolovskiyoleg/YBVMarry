package org.yabogvk.ybvmarry.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;

public class MarryExpansion extends PlaceholderExpansion {
    private final YBVMarry plugin;

    public MarryExpansion(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ybvmarry";
    }

    @Override
    public @NotNull String getAuthor() {
        return "yabogvk";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        Marriage m = plugin.getMarriageManager().getMarriage(player.getUniqueId());

        if (m == null) {
            if (params.equals("partner")) return "Никого нет";
            if (params.equals("balance")) return "0";
            if (params.equals("status")) return "—";
            return "";
        }

        switch (params.toLowerCase()) {
            case "partner":
                OfflinePlayer partner = Bukkit.getOfflinePlayer(m.getPartnerOf(player.getUniqueId()));
                return partner.getName() != null ? partner.getName() : "Неизвестно";

            case "balance":
                return String.valueOf((int) m.getBalance());

            case "status":
                Player onlinePartner = Bukkit.getPlayer(m.getPartnerOf(player.getUniqueId()));
                return (onlinePartner != null && onlinePartner.isOnline()) ? "&aВ сети" : "&cОффлайн";

            case "symbol":
                return m.getSymbol();

            case "color":
                return m.getColor();
        }

        return null;
    }
}