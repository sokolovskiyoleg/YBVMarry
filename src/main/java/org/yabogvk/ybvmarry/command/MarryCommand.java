package org.yabogvk.ybvmarry.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.yabogvk.ybvmarry.YBVMarry;

import java.util.*;

public class MarryCommand implements CommandExecutor, TabCompleter {

    private final YBVMarry plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public MarryCommand(YBVMarry plugin) {
        this.plugin = plugin;
        subCommands.put("send", new MarrySend(plugin));
        subCommands.put("accept", new MarryAccept(plugin));
        subCommands.put("divorce", new MarryDivorce(plugin));
        subCommands.put("reload", new MarryReload(plugin));
        subCommands.put("list", new MarryList(plugin));
        subCommands.put("tp", new MarryTp(plugin));
        subCommands.put("chat", new MarryChat(plugin));
        subCommands.put("pvp", new MarryPvp(plugin));
        subCommands.put("phrase", new MarryPhrase(plugin));
        subCommands.put("color", new MarryColor(plugin));
        subCommands.put("symbol", new MarrySymbol(plugin));
        subCommands.put("status", new MarryStatus(plugin));
        subCommands.put("sethome", new MarrySetHome(plugin));
        subCommands.put("home", new MarryHome(plugin));
        subCommands.put("kiss", new MarryKiss(plugin));
        subCommands.put("gift", new MarryGift(plugin));
        subCommands.put("bank", new MarryBank(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subName = args[0].toLowerCase();
        SubCommand sub = subCommands.get(subName);

        if (sub != null) {
            if (!hasPermission(player, subName)) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            sub.execute(player, args);
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("unknown-command"));
        }

        return true;
    }

    private boolean hasPermission(Player player, String sub) {
        if (sub.equals("reload")) {
            return player.hasPermission("ybvmarry.admin");
        }

        if (sub.equals("color")) return player.hasPermission("ybvmarry.color");
        if (sub.equals("symbol")) return player.hasPermission("ybvmarry.symbol");

        return switch (sub) {
            case "bank" -> player.hasPermission("ybvmarry.bank");
            case "home", "sethome" -> player.hasPermission("ybvmarry.home");
            case "kiss", "gift" -> player.hasPermission("ybvmarry.interact");
            default -> player.hasPermission("ybvmarry.user");
        };
    }

    private void sendHelp(Player player) {
        List<String> helpLines = plugin.getConfigManager().getColoredMessageList("help-menu");
        helpLines.forEach(player::sendMessage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> visibleSubs = subCommands.keySet().stream()
                    .filter(sub -> hasPermission(player, sub))
                    .toList();

            StringUtil.copyPartialMatches(args[0], visibleSubs, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();

            if (sub.equals("color") && player.hasPermission("ybvmarry.color")) {
                Set<String> colors = plugin.getConfigManager().getMarryColors().keySet();
                StringUtil.copyPartialMatches(args[1], colors, completions);
                return completions;
            }

            if (sub.equals("symbol") && player.hasPermission("ybvmarry.symbol")) {
                Set<String> symbols = plugin.getConfigManager().getMarrySymbols().keySet();
                return StringUtil.copyPartialMatches(args[1], symbols, completions);
            }

            if (sub.equals("bank") && player.hasPermission("ybvmarry.bank")) {
                return List.of("deposit", "withdraw", "balance").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .toList();
            }

            if (sub.equals("send")) return null;
        }

        return Collections.emptyList();
    }
}