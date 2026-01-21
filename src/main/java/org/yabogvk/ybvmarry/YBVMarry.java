package org.yabogvk.ybvmarry;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.yabogvk.ybvmarry.command.MarryCommand;
import org.yabogvk.ybvmarry.listener.ChatListener;
import org.yabogvk.ybvmarry.listener.JoinQuitListener;
import org.yabogvk.ybvmarry.listener.PvpListener;
import org.yabogvk.ybvmarry.manager.ConfigManager;
import org.yabogvk.ybvmarry.manager.CooldownManager;
import org.yabogvk.ybvmarry.manager.Database;
import org.yabogvk.ybvmarry.manager.MarriageManager;
import org.yabogvk.ybvmarry.util.MarryExpansion;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class YBVMarry extends JavaPlugin {
    private Database database;
    private MarriageManager marriageManager;
    private CooldownManager cooldownManager;
    private ConfigManager configManager;
    private Economy econ = null;

    private BukkitTask saveTask;
    private final Set<UUID> chatToggledPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);

        this.database = new Database(this, this.configManager);

        try {
            database.connect();
        } catch (SQLException e) {
            getLogger().severe("Не удалось подключиться к БД! Ошибка: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        this.marriageManager = new MarriageManager(this, database);
        this.cooldownManager = new CooldownManager();

        if (!setupEconomy()) {
            getLogger().warning("Vault не найден или экономика не настроена. Банк отключен!");
        } else {
            getLogger().info("Экономика Vault успешно подключена.");
        }

        startAutoSave();

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PvpListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

        MarryCommand marryCommand = new MarryCommand(this);
        getCommand("marry").setExecutor(marryCommand);
        getCommand("marry").setTabCompleter(marryCommand);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MarryExpansion(this).register();
        }

        getLogger().info("Плагин успешно запущен!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void startAutoSave() {
        saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (marriageManager != null) {
                marriageManager.saveAllToDatabase();
            }
        }, 6000L, 6000L);
    }

    @Override
    public void onDisable() {
        if (saveTask != null) saveTask.cancel();

        if (marriageManager != null) {
            getLogger().info("Финальное сохранение данных...");
            marriageManager.saveAllToDatabase();
        }

        if (database != null) {
            database.close();
        }
        getLogger().info("Плагин выключен.");
    }

    public Economy getEconomy() { return econ; }
    public MarriageManager getMarriageManager() { return marriageManager; }
    public Database getDatabase() { return database; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public Set<UUID> getChatToggledPlayers() { return chatToggledPlayers; }
}