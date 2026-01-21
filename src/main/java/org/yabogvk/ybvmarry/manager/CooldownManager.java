package org.yabogvk.ybvmarry.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // Структура: Название_Действия -> (UUID_Игрока -> Время_Окончания)
    private final Map<String, Map<UUID, Long>> cooldowns = new HashMap<>();

    /**
     * Установить кулдаун
     * @param action Название действия (например, "tp" или "phrase")
     * @param uuid UUID игрока
     * @param seconds Время в секундах
     */
    public void setCooldown(String action, UUID uuid, int seconds) {
        cooldowns.computeIfAbsent(action, k -> new HashMap<>())
                .put(uuid, System.currentTimeMillis() + (seconds * 1000L));
    }

    /**
     * Получить оставшееся время в секундах
     * @return 0, если кулдауна нет
     */
    public long getRemaining(String action, UUID uuid) {
        Map<UUID, Long> actionMap = cooldowns.get(action);
        if (actionMap == null || !actionMap.containsKey(uuid)) return 0;

        long timeLeft = (actionMap.get(uuid) - System.currentTimeMillis()) / 1000;
        if (timeLeft <= 0) {
            actionMap.remove(uuid);
            return 0;
        }
        return timeLeft;
    }

    public boolean hasCooldown(String action, UUID uuid) {
        return getRemaining(action, uuid) > 0;
    }
}