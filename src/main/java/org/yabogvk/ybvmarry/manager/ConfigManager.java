package org.yabogvk.ybvmarry.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.util.MessageUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {
    private final YBVMarry plugin;
    private FileConfiguration settings;
    private FileConfiguration messages;

    private final Map<String, String> defaultMessages = new HashMap<>();

    public ConfigManager(YBVMarry plugin) {
        this.plugin = plugin;
        setupDefaultMessages();
        reload();
    }

    private void setupDefaultMessages() {
        defaultMessages.put("prefix", "&d&lYBVMarry &8» ");
        defaultMessages.put("no-permission", "&cУ вас нет прав!");
        defaultMessages.put("already-married", "&cВы уже женаты!");
        defaultMessages.put("not-married", "&cВы не состоите в браке.");
        defaultMessages.put("player-not-found", "&cИгрок не найден.");
        defaultMessages.put("reload-success", "&aКонфигурация успешно перезагружена!");
        defaultMessages.put("unknown-command", "&cНеизвестная команда.");
        defaultMessages.put("tp-started", "&eТелепортация начнется через &f{0} сек&e. Не двигайтесь!");
        defaultMessages.put("tp-cancelled", "&cТелепортация отменена: вы сдвинулись.");
        defaultMessages.put("tp-success", "&aВы успешно телепортировались к партнеру!");
        defaultMessages.put("usage-send", "&cИспользование: /marry send <ник>");
        defaultMessages.put("marry-self", "&cВы не можете жениться на самом себе!");
        defaultMessages.put("target-already-married", "&cЭтот игрок уже состоит в браке.");
        defaultMessages.put("proposal-sent", "&eВы отправили предложение руки и сердца игроку &f{0}&e.");
        defaultMessages.put("proposal-received", "&d{0} &eпредлагает вам пожениться! Введите &f/marry accept&e, чтобы согласиться.");
        defaultMessages.put("pvp-enabled", "&aPvP в паре теперь включено! Вы можете наносить урон друг другу.");
        defaultMessages.put("pvp-disabled", "&cPvP в паре теперь выключено! Вы защищены от ударов партнера.");
        defaultMessages.put("phrase-sent", "&dВы отправили партнеру фразу: &f{0}");
        defaultMessages.put("phrase-received", "&dПартнер шепчет вам: &f{0}");
        defaultMessages.put("list-empty", "&cНа сервере пока нет женатых пар.");
        defaultMessages.put("list-invalid-page", "&cТакой страницы не существует! Всего страниц: &f{0}");
        defaultMessages.put("list-header", "&d&lСписок женатых пар &7(Страница {0} из {1})&8:");
        defaultMessages.put("list-format", "&8- &f{0} &d❤ &f{1}");
        defaultMessages.put("list-footer", "&d&m---------------------------------------");
        defaultMessages.put("divorce-success", "&6Вы официально развелись с &f{0}&6. Теперь вы свободны!");
        defaultMessages.put("divorce-partner-notification", "&cВаш партнер &f{0} &cрасторг брак с вами.");
        defaultMessages.put("divorce-broadcast", "&8[&d❤&8] &f{0} &7и &f{1} &7больше не пара. Печально...");
        defaultMessages.put("chat-toggle-on", "&aРежим семейного чата включен! Теперь ваши сообщения видны только партнеру.");
        defaultMessages.put("chat-toggle-off", "&cРежим семейного чата выключен. Вы вернулись в общий чат.");
        defaultMessages.put("chat-no-partner", "&7(Ваш партнер сейчас не в сети, но сообщение отправлено в пустоту...)");
        defaultMessages.put("chat-format", "&8[&d❤ Чат&8] &f{0}: &d{1}");
        defaultMessages.put("pvp-blocked", "&cВы не можете бить свою половинку! Включите PvP: &f/marry pvp");
        defaultMessages.put("usage-color", "&cИспользование: /marry color <#HEX-код>");
        defaultMessages.put("invalid-color", "&cНеверный формат цвета! Используйте HEX (например: #FF5555).");
        defaultMessages.put("color-changed", "&aЦвет вашего брака успешно изменен на {0}!");
        defaultMessages.put("default-color", "#FF69B4");
        defaultMessages.put("status-enabled", "&aУведомления о входе/выходе партнера включены!");
        defaultMessages.put("status-disabled", "&cУведомления о входе/выходе партнера выключены.");
        defaultMessages.put("usage-status", "&cИспользование: /marry status");
        defaultMessages.put("cooldown", "&c&l✘ &7Подождите еще &f{0} &7сек. перед повторным использованием!");
    }

    public void reload() {
        File settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!settingsFile.exists()) plugin.saveResource("settings.yml", false);
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);

        settings = YamlConfiguration.loadConfiguration(settingsFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path, Object... args) {
        String msg = messages.getString(path, defaultMessages.getOrDefault(path, path));

        String prefix = messages.getString("prefix", defaultMessages.get("prefix"));
        String raw = prefix + msg;

        for (int i = 0; i < args.length; i++) {
            raw = raw.replace("{" + i + "}", String.valueOf(args[i]));
        }

        return MessageUtil.color(raw);
    }

    public List<String> getColoredMessageList(String path) {
        List<String> list = messages.getStringList(path);
        if (list.isEmpty() && path.equals("help-menu")) {
            return List.of(
                    "&d&lYBVMarry &7| &fПомощь:",
                    "&d/marry send <ник> &7- предложить брак",
                    "&d/marry tp &7- телепорт к партнеру"
            ).stream().map(MessageUtil::color).collect(Collectors.toList());
        }
        return list.stream().map(MessageUtil::color).collect(Collectors.toList());
    }

    public Map<String, String> getMarryColors() {
        Map<String, String> colors = new HashMap<>();
        if (settings.isConfigurationSection("marry-colors")) {
            for (String key : settings.getConfigurationSection("marry-colors").getKeys(false)) {
                colors.put(key.toUpperCase(), settings.getString("marry-colors." + key));
            }
        }
        return colors;
    }

    public int getInt(String path, int def) {
        return settings.getInt(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return settings.getBoolean(path, def);
    }

    public List<String> getPhrases() {
        List<String> phrases = settings.getStringList("phrases");
        if (phrases.isEmpty()) {
            return List.of("Я тебя люблю!", "Ты — мое всё!");
        }
        return phrases;
    }

    public String getRawMessage(String path) {
        return messages.getString(path, defaultMessages.getOrDefault(path, path));
    }

    /**
     * Получает карту разрешенных символов из settings.yml
     * Ключ — название (для команды), Значение — сам символ
     */
    public Map<String, String> getMarrySymbols() {
        Map<String, String> symbols = new HashMap<>();

        if (settings.isConfigurationSection("marry-symbols")) {
            for (String key : settings.getConfigurationSection("marry-symbols").getKeys(false)) {
                String symbol = settings.getString("marry-symbols." + key);
                if (symbol != null) {
                    symbols.put(key.toLowerCase(), symbol);
                }
            }
        }

        if (symbols.isEmpty()) {
            symbols.put("heart", "❤");
        }

        return symbols;
    }

    public String getString(String path, String def) {
        return settings.getString(path, def);
    }

    public String getMarriageSymbol() {
        return settings.getString("settings.marriage-symbol", "❤");
    }
}