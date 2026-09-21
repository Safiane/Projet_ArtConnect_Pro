package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC du DAO Artist.
 */
public class JdbcArtistDao implements ArtistDao {

    @Override
    public List<Artist> findAll() {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT id, name, bio, birth_year, contact_email, phone, city, " +
                     "website, social_media, is_active FROM artist";
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Artist a = mapRow(rs);
                a.setDisciplines(findDisciplinesByArtistId(conn, a.getId()));
                artists.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll artist", e);
        }
        return artists;
    }

    @Override
    public void save(Artist artist) {
        String sql = "INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, " +
                     "website, social_media, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setArtistParams(ps, artist);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) artist.setId(keys.getLong(1));
            }
            saveDisciplines(conn, artist);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save artist : " + artist.getName(), e);
        }
    }

    @Override
    public void update(Artist artist) {
        String sql = "UPDATE artist SET bio=?, birth_year=?, contact_email=?, phone=?, city=?, " +
                     "website=?, social_media=?, is_active=? WHERE name=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artist.getBio());
            ps.setObject(2, artist.getBirthYear());
            ps.setString(3, artist.getContactEmail());
            ps.setString(4, artist.getPhone());
            ps.setString(5, artist.getCity());
            ps.setString(6, artist.getWebsite());
            ps.setString(7, artist.getSocialMedia());
            ps.setBoolean(8, artist.isActive());
            ps.setString(9, artist.getName());
            ps.executeUpdate();
            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM artist_discipline WHERE artist_id = ?")) {
                del.setLong(1, artist.getId());
                del.executeUpdate();
            }
            saveDisciplines(conn, artist);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update artist : " + artist.getName(), e);
        }
    }

    @Override
    public void delete(String artistName) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM artist WHERE name = ?")) {
            ps.setString(1, artistName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete artist : " + artistName, e);
        }
    }

    @Override
    public List<Artist> findByCity(String city) {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT id, name, bio, birth_year, contact_email, phone, city, " +
                     "website, social_media, is_active FROM artist WHERE city = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, city);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artist a = mapRow(rs);
                    a.setDisciplines(findDisciplinesByArtistId(conn, a.getId()));
                    artists.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByCity : " + city, e);
        }
        return artists;
    }

    // ---- helpers ----
    private Artist mapRow(ResultSet rs) throws SQLException {
        Artist a = new Artist();
        a.setId(rs.getLong("id"));
        a.setName(rs.getString("name"));
        a.setBio(rs.getString("bio"));
        a.setBirthYear(rs.getObject("birth_year", Integer.class));
        a.setContactEmail(rs.getString("contact_email"));
        a.setPhone(rs.getString("phone"));
        a.setCity(rs.getString("city"));
        a.setWebsite(rs.getString("website"));
        a.setSocialMedia(rs.getString("social_media"));
        a.setActive(rs.getBoolean("is_active"));
        return a;
    }

    private void setArtistParams(PreparedStatement ps, Artist artist) throws SQLException {
        ps.setString(1, artist.getName());
        ps.setString(2, artist.getBio());
        ps.setObject(3, artist.getBirthYear());
        ps.setString(4, artist.getContactEmail());
        ps.setString(5, artist.getPhone());
        ps.setString(6, artist.getCity());
        ps.setString(7, artist.getWebsite());
        ps.setString(8, artist.getSocialMedia());
        ps.setBoolean(9, artist.isActive());
    }

    private List<Discipline> findDisciplinesByArtistId(Connection conn, Long artistId) throws SQLException {
        List<Discipline> list = new ArrayList<>();
        String sql = "SELECT d.name FROM discipline d " +
                     "JOIN artist_discipline ad ON d.id = ad.discipline_id WHERE ad.artist_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, artistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Discipline(rs.getString("name")));
            }
        }
        return list;
    }

    private void saveDisciplines(Connection conn, Artist artist) throws SQLException {
        if (artist.getId() == null || artist.getDisciplines() == null) return;
        String sql = "INSERT IGNORE INTO artist_discipline (artist_id, discipline_id) " +
                     "SELECT ?, id FROM discipline WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Discipline d : artist.getDisciplines()) {
                ps.setLong(1, artist.getId());
                ps.setString(2, d.getName());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
