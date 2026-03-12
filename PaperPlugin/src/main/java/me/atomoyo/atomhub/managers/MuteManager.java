package me.atomoyo.atomhub.managers;

import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MuteManager {

    private final Connection connection;
    private final String serverName; // Your server name

    public MuteManager(Connection connection, String serverName) {
        this.connection = connection;
        this.serverName = serverName;
    }

    public boolean isMuted(Player player) {
        return isMuted(player.getUniqueId());
    }

    public boolean isMuted(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM atomhub_mutes WHERE uuid = ? AND (server IS NULL OR server = ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, serverName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setNetworkMute(UUID uuid, boolean muted) {
        String sqlInsert = "INSERT INTO atomhub_mutes (uuid, server, muted_by, reason, mute_time) VALUES (?, NULL, 'Console', 'Network mute', ?)";
        String sqlDelete = "DELETE FROM atomhub_mutes WHERE uuid = ? AND server IS NULL";
        try (PreparedStatement ps = connection.prepareStatement(muted ? sqlInsert : sqlDelete)) {
            ps.setString(1, uuid.toString());
            if (muted) ps.setLong(2, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isNetworkMuted(UUID uuid) {
        String sql = "SELECT 1 FROM atomhub_mutes WHERE uuid = ? AND server IS NULL LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public Set<UUID> getMutedPlayers() {
        Set<UUID> muted = new HashSet<>();
        String sql = "SELECT uuid FROM atomhub_mutes WHERE server IS NULL OR server = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, serverName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) muted.add(UUID.fromString(rs.getString("uuid")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return muted;
    }
}
