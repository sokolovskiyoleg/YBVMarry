package org.yabogvk.ybvmarry.command;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;

public class MarryBank implements SubCommand {
    private final YBVMarry plugin;

    public MarryBank(YBVMarry plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        long remaining = plugin.getCooldownManager().getRemaining("bank", player.getUniqueId());
        if (remaining > 0 && !player.hasPermission("ybvmarry.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", remaining));
            return;
        }

        Marriage m = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (m == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-married"));
            return;
        }

        Economy econ = plugin.getEconomy();
        if (econ == null) {
            player.sendMessage("§cОшибка: Vault/Economy не найден!");
            return;
        }

        if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("balance"))) {
            player.sendMessage(plugin.getConfigManager().getMessage("bank-balance", m.getBalance()));
            setBankCooldown(player);
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cИспользование: /marry bank <deposit/withdraw/balance> [сумма]");
            return;
        }

        String action = args[1].toLowerCase();
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
            return;
        }

        switch (action) {
            case "deposit":
                if (econ.has(player, amount)) {
                    econ.withdrawPlayer(player, amount);
                    m.addBalance(amount);

                    player.sendMessage(plugin.getConfigManager().getMessage("bank-deposit-success", amount));
                    setBankCooldown(player);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
                }
                break;

            case "withdraw":
                if (m.getBalance() >= amount) {
                    m.addBalance(-amount);
                    econ.depositPlayer(player, amount);

                    player.sendMessage(plugin.getConfigManager().getMessage("bank-withdraw-success", amount));
                    setBankCooldown(player);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("bank-insufficient"));
                }
                break;

            default:
                player.sendMessage("§cИспользование: /marry bank <deposit/withdraw> <сумма>");
                break;
        }
    }

    private void setBankCooldown(Player player) {
        int cooldownTime = plugin.getConfigManager().getInt("cooldowns.bank", 5);
        plugin.getCooldownManager().setCooldown("bank", player.getUniqueId(), cooldownTime);
    }

}