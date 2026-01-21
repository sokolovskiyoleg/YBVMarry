package org.yabogvk.ybvmarry.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class MessageUtil {

    private final Pattern HEX_PATTERN = Pattern.compile("(&?#)([A-Fa-f0-9]{6})");

    private final int SERVER_VERSION = Bukkit.getServer().getClass().getPackage().getName().contains("1.16") ||
            Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]) >= 16 ? 16 : 1;

    public String color(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        if (SERVER_VERSION >= 16) {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuilder sb = new StringBuilder();

            while (matcher.find()) {
                String hex = matcher.group(2);
                StringBuilder replacement = new StringBuilder("§x");
                for (char c : hex.toCharArray()) {
                    replacement.append('§').append(c);
                }
                matcher.appendReplacement(sb, replacement.toString());
            }
            message = matcher.appendTail(sb).toString();
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}