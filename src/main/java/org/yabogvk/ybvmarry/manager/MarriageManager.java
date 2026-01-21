package org.yabogvk.ybvmarry.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;
import org.yabogvk.ybvmarry.manager.Database;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MarriageManager {
    private final YBVMarry plugin;
    private final Database db;

    private final Map<UUID, Marriage> marriages = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> proposals = new ConcurrentHashMap<>();

    public MarriageManager(YBVMarry plugin, Database db) {
        this.plugin = plugin;
        this.db = db;
        loadAll();
    }

    private void loadAll() {
        marriages.clear();
        for (Marriage m : db.loadMarriages()) {
            marriages.put(m.p1(), m);
            marriages.put(m.p2(), m);
        }
    }

    public void saveAllToDatabase() {
        List<Marriage> toSave = marriages.values().stream()
                .filter(Marriage::isDirty)
                .distinct()
                .toList();

        if (toSave.isEmpty()) return;

        db.saveMarriagesBatch(toSave);
        toSave.forEach(m -> m.setDirty(false));
    }

    public void acceptProposal(UUID p1, UUID p2) {
        boolean defaultPvp = plugin.getConfigManager().getBoolean("settings.default-pvp-allowed", false);
        String defaultColor = plugin.getConfigManager().getRawMessage("default-color");
        String defaultSymbol = plugin.getConfigManager().getMarriageSymbol();

        Marriage marriage = new Marriage(p1, p2, defaultPvp, defaultColor, defaultSymbol, true, null, 0.0);

        marriage.setDirty(true);

        marriages.put(p1, marriage);
        marriages.put(p2, marriage);

        proposals.remove(p1);
        proposals.remove(p2);
    }

    public void setHome(UUID playerUUID, Location loc) {
        Marriage m = marriages.get(playerUUID);
        if (m != null) {
            m.setHome(loc);
        }
    }

    public void setMarriageColor(UUID playerUUID, String hexColor) {
        Marriage m = marriages.get(playerUUID);
        if (m != null) m.setColor(hexColor);
    }

    public void setMarriageSymbol(UUID playerUUID, String symbol) {
        Marriage m = marriages.get(playerUUID);
        if (m != null) m.setSymbol(symbol);
    }

    public void updateBalance(UUID playerUUID, double newBalance) {
        Marriage m = marriages.get(playerUUID);
        if (m != null) m.setBalance(newBalance);
    }

    public void togglePvp(UUID playerId) {
        Marriage m = marriages.get(playerId);
        if (m != null) m.setPvpEnabled(!m.isPvpEnabled());
    }

    public void divorce(UUID player) {
        Marriage m = marriages.remove(player);
        if (m != null) {
            UUID partner = m.getPartnerOf(player);
            marriages.remove(partner);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                db.deleteMarriage(m.p1(), m.p2());
            });
        }
    }

    public void sendProposal(UUID from, UUID to) { proposals.put(to, from); }
    public UUID getProposer(UUID to) { return proposals.get(to); }
    public void removeProposal(UUID receiverUUID) { proposals.remove(receiverUUID); }
    public Marriage getMarriage(UUID playerUUID) { return marriages.get(playerUUID); }
    public boolean isMarried(UUID player) { return marriages.containsKey(player); }

    public UUID getPartner(UUID player) {
        Marriage m = marriages.get(player);
        return (m != null) ? m.getPartnerOf(player) : null;
    }

    public Collection<Marriage> getAllMarriages() {
        return marriages.values().stream().distinct().toList();
    }

    public boolean isPvpEnabled(UUID playerId) {
        Marriage m = marriages.get(playerId);
        return m != null && m.isPvpEnabled();
    }
}