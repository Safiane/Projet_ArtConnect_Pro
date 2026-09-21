package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC du DAO Workshop.
 */
public class JdbcWorkshopDao implements WorkshopDao {

    @Override
    public List<Workshop> findAll() {
        List<Workshop> list = new ArrayList<>();
        String sql = "SELECT w.id, w.title, w.date_time, w.duration_minutes, w.max_participants, " +
                     "w.price, w.location, w.description, w.level, " +
                     "a.id AS artist_id, a.name AS artist_name " +
                     "FROM workshop w JOIN artist a ON w.instructor_id = a.id";
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll workshop", e);
        }
        return list;
    }

    @Override
    public Optional<Workshop> findById(Long id) {
        String sql = "SELECT w.id, w.title, w.date_time, w.duration_minutes, w.max_participants, " +
                     "w.price, w.location, w.description, w.level, " +
                     "a.id AS artist_id, a.name AS artist_name " +
                     "FROM workshop w JOIN artist a ON w.instructor_id = a.id WHERE w.id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById workshop : " + id, e);
        }
        return Optional.empty();
    }

    // ---- helper ----
    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop w = new Workshop();
        w.setId(rs.getLong("id"));
        w.setTitle(rs.getString("title"));
        Timestamp ts = rs.getTimestamp("date_time");
        if (ts != null) w.setDate(ts.toLocalDateTime());
        w.setDurationMinutes(rs.getInt("duration_minutes"));
        w.setMaxParticipants(rs.getInt("max_participants"));
        w.setPrice(rs.getDouble("price"));
        w.setLocation(rs.getString("location"));
        w.setDescription(rs.getString("description"));
        w.setLevel(rs.getString("level"));
        Artist instructor = new Artist();
        instructor.setId(rs.getLong("artist_id"));
        instructor.setName(rs.getString("artist_name"));
        w.setInstructor(instructor);
        return w;
    }
}
