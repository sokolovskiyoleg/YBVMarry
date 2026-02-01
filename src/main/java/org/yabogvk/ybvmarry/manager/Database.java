package org.yabogvk.ybvmarry.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.yabogvk.ybvmarry.YBVMarry;
import org.yabogvk.ybvmarry.data.Marriage;

import java.sql.*;
import java.util.*;

public class Database {
    private final YBVMarry plugin;
    private final ConfigManager config;
    private Connection connection;

    public Database(YBVMarry plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) return;

        String type = config.getString("database.type", "sqlite").toLowerCase();

        plugin.getLogger().info("[Database] Попытка подключения. Тип: " + type);

        if (type.equals("mysql")) {
            String host = config.getString("database.mysql.host", "localhost");
            int port = config.getInt("database.mysql.port", 3306);
            String dbName = config.getString("database.mysql.database", "marry_db");
            String user = config.getString("database.mysql.username", "root");
            String pass = config.getString("database.mysql.password", "");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName +
                    "?useSSL=false&characterEncoding=utf8&allowPublicKeyRetrieval=true&autoReconnect=true";

            connection = DriverManager.getConnection(url, user, pass);
            plugin.getLogger().info("[Database] Соединение с MySQL установлено!");
        } else {
            String path = plugin.getDataFolder().getAbsolutePath() + "/database.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            plugin.getLogger().info("[Database] Используется SQLite.");
        }

        createTables();
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS marriages (" +
                "player1 VARCHAR(36) NOT NULL," +
                "player2 VARCHAR(36) NOT NULL," +
                "pvp_enabled BOOLEAN DEFAULT 0," +
                "color VARCHAR(7)," +
                "symbol VARCHAR(10)," +
                "notifications BOOLEAN DEFAULT 1," +
                "home_world VARCHAR(64)," +
                "home_x DOUBLE," +
                "home_y DOUBLE," +
                "home_z DOUBLE," +
                "balance DOUBLE DEFAULT 0.0," +
                "PRIMARY KEY (player1, player2)" +
                ")";

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public void saveMarriagesBatch(List<Marriage> marriages) {
        String sql = "REPLACE INTO marriages " +
                "(player1, player2, pvp_enabled, color, symbol, notifications, " +
                "home_world, home_x, home_y, home_z, balance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Marriage m : marriages) {
                    UUID first = m.p1().compareTo(m.p2()) < 0 ? m.p1() : m.p2();
                    UUID second = m.p1().compareTo(m.p2()) < 0 ? m.p2() : m.p1();

                    ps.setString(1, first.toString());
                    ps.setString(2, second.toString());
                    ps.setBoolean(3, m.isPvpEnabled());
                    ps.setString(4, m.getColor());
                    ps.setString(5, m.getSymbol());
                    ps.setBoolean(6, m.isNotificationsEnabled());

                    if (m.getHome() != null) {
                        ps.setString(7, m.getHome().getWorld().getName());
                        ps.setDouble(8, Math.round(m.getHome().getX() * 10.0) / 10.0);
                        ps.setDouble(9, Math.round(m.getHome().getY() * 10.0) / 10.0);
                        ps.setDouble(10, Math.round(m.getHome().getZ() * 10.0) / 10.0);
                    } else {
                        ps.setNull(7, Types.VARCHAR);
                        ps.setNull(8, Types.DOUBLE);
                        ps.setNull(9, Types.DOUBLE);
                        ps.setNull(10, Types.DOUBLE);
                    }

                    ps.setDouble(11, m.getBalance());
                    ps.addBatch();
                }
                ps.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Marriage> loadMarriages() {
        List<Marriage> list = new ArrayList<>();
        String sql = "SELECT * FROM marriages";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                UUID p1 = UUID.fromString(rs.getString("player1"));
                UUID p2 = UUID.fromString(rs.getString("player2"));
                boolean pvp = rs.getBoolean("pvp_enabled");
                String color = rs.getString("color");
                String symbol = rs.getString("symbol");
                boolean notifications = rs.getBoolean("notifications");
                double balance = rs.getDouble("balance");

                Location home = null;
                String worldName = rs.getString("home_world");
                if (worldName != null) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        home = new Location(world, rs.getDouble("home_x"), rs.getDouble("home_y"), rs.getDouble("home_z"));
                    }
                }
                list.add(new Marriage(p1, p2, pvp, color, symbol, notifications, home, balance));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void deleteMarriagesBatch(Collection<Marriage> marriages) {
        if (marriages.isEmpty()) return;

        String sql = "DELETE FROM marriages WHERE player1 = ? AND player2 = ?";
        try {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Marriage m : marriages) {
                    UUID first = m.p1().compareTo(m.p2()) < 0 ? m.p1() : m.p2();
                    UUID second = m.p1().compareTo(m.p2()) < 0 ? m.p2() : m.p1();
                    ps.setString(1, first.toString());
                    ps.setString(2, second.toString());
                    ps.addBatch();
                }
                ps.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}