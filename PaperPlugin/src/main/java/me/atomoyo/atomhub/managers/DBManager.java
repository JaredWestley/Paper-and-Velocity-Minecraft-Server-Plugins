// src/main/java/me/atomoyo/atomhub/managers/MuteDBManager.java

package me.atomoyo.atomhub.managers;

import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DBManager {

    private final Connection connection;

    public DBManager(Connection connection) {
        this.connection = connection;
    }

    public boolean isNetworkMuted(UUID uuid) throws SQLException {
        String sql = "SELECT 1 FROM atomhub_mutes WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void setNetworkMuted(@NotNull UUID uuid, boolean muted) throws SQLException {
        if (muted) {
            String sql = "INSERT INTO atomhub_mutes (uuid) VALUES (?) ON DUPLICATE KEY UPDATE uuid=uuid";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
        } else {
            String sql = "DELETE FROM atomhub_mutes WHERE uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
        }
    }

    public Set<UUID> loadAllMuted() throws SQLException {
        Set<UUID> muted = new HashSet<>();
        String sql = "SELECT uuid FROM atomhub_mutes";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                muted.add(UUID.fromString(rs.getString("uuid")));
            }
        }
        return muted;
    }
}
