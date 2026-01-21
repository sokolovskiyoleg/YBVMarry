package org.yabogvk.ybvmarry.data;

import org.bukkit.Location;
import java.util.UUID;

public class Marriage {
    private final UUID p1, p2;
    private boolean pvpEnabled;
    private String color;
    private String symbol;
    private boolean notificationsEnabled;
    private Location home;
    private double balance;

    private boolean dirty = false;

    public Marriage(UUID p1, UUID p2, boolean pvpEnabled, String color, String symbol, boolean notificationsEnabled, Location home, double balance) {
        this.p1 = p1;
        this.p2 = p2;
        this.pvpEnabled = pvpEnabled;
        this.color = color;
        this.symbol = symbol;
        this.notificationsEnabled = notificationsEnabled;
        this.home = home;
        this.balance = balance;
    }

    public UUID p1() { return p1; }
    public UUID p2() { return p2; }

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean enabled) {
        this.pvpEnabled = enabled;
        this.dirty = true;
    }

    public String getColor() { return color; }
    public void setColor(String color) {
        this.color = color;
        this.dirty = true;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
        this.dirty = true;
    }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
        this.dirty = true;
    }

    public Location getHome() { return home; }
    public void setHome(Location home) {
        this.home = home;
        this.dirty = true;
    }

    public double getBalance() { return balance; }
    public void setBalance(double balance) {
        this.balance = balance;
        this.dirty = true;
    }

    public void addBalance(double amount) {
        this.balance += amount;
        this.dirty = true;
    }

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }

    public UUID getPartnerOf(UUID uuid) {
        return p1.equals(uuid) ? p2 : p1;
    }
}