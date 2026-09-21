package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC du DAO Exhibition.
 */
public class JdbcExhibitionDao implements ExhibitionDao {

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> list = new ArrayList<>();
        String sql = "SELECT e.id, e.title, e.start_date, e.end_date, e.description, " +
                     "e.curator_name, e.theme, " +
                     "g.id AS gallery_id, g.name AS gallery_name " +
                     "FROM exhibition e JOIN gallery g ON e.gallery_id = g.id";
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Exhibition e = mapRow(rs);
                e.setArtworks(findArtworksByExhibitionId(conn, e.getId()));
                list.add(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll exhibition", e);
        }
        return list;
    }

    @Override
    public void save(Exhibition exhibition) {
        String sql = "INSERT INTO exhibition (title, start_date, end_date, description, " +
                     "gallery_id, curator_name, theme) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, exhibition);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) exhibition.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save exhibition : " + exhibition.getTitle(), e);
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        String sql = "UPDATE exhibition SET start_date=?, end_date=?, description=?, " +
                     "gallery_id=?, curator_name=?, theme=? WHERE title=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Date sd = exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null;
            Date ed = exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null;
            ps.setDate(1, sd);
            ps.setDate(2, ed);
            ps.setString(3, exhibition.getDescription());
            ps.setObject(4, exhibition.getGallery() != null ? exhibition.getGallery().getId() : null);
            ps.setString(5, exhibition.getCuratorName());
            ps.setString(6, exhibition.getTheme());
            ps.setString(7, exhibition.getTitle());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update exhibition : " + exhibition.getTitle(), e);
        }
    }

    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM exhibition WHERE title = ?")) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete exhibition : " + title, e);
        }
    }

    // ---- helpers ----
    private Exhibition mapRow(ResultSet rs) throws SQLException {
        Exhibition e = new Exhibition();
        e.setId(rs.getLong("id"));
        e.setTitle(rs.getString("title"));
        Date sd = rs.getDate("start_date");
        Date ed = rs.getDate("end_date");
        if (sd != null) e.setStartDate(sd.toLocalDate());
        if (ed != null) e.setEndDate(ed.toLocalDate());
        e.setDescription(rs.getString("description"));
        e.setCuratorName(rs.getString("curator_name"));
        e.setTheme(rs.getString("theme"));
        Gallery g = new Gallery();
        g.setId(rs.getLong("gallery_id"));
        g.setName(rs.getString("gallery_name"));
        e.setGallery(g);
        return e;
    }

    private List<Artwork> findArtworksByExhibitionId(Connection conn, Long exhibitionId) throws SQLException {
        List<Artwork> artworks = new ArrayList<>();
        String sql = "SELECT aw.id, aw.title, aw.creation_year, aw.type, aw.price, aw.status, " +
                     "ar.id AS artist_id, ar.name AS artist_name " +
                     "FROM artwork aw " +
                     "JOIN exhibition_artwork ea ON aw.id = ea.artwork_id " +
                     "JOIN artist ar ON aw.artist_id = ar.id " +
                     "WHERE ea.exhibition_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, exhibitionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artwork a = new Artwork();
                    a.setId(rs.getLong("id"));
                    a.setTitle(rs.getString("title"));
                    a.setCreationYear(rs.getObject("creation_year", Integer.class));
                    a.setType(rs.getString("type"));
                    a.setPrice(rs.getDouble("price"));
                    String status = rs.getString("status");
                    if (status != null) a.setStatus(Artwork.Status.valueOf(status));
                    Artist artist = new Artist();
                    artist.setId(rs.getLong("artist_id"));
                    artist.setName(rs.getString("artist_name"));
                    a.setArtist(artist);
                    artworks.add(a);
                }
            }
        }
        return artworks;
    }

    private void setParams(PreparedStatement ps, Exhibition exhibition) throws SQLException {
        ps.setString(1, exhibition.getTitle());
        Date sd = exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null;
        Date ed = exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null;
        ps.setDate(2, sd);
        ps.setDate(3, ed);
        ps.setString(4, exhibition.getDescription());
        ps.setObject(5, exhibition.getGallery() != null ? exhibition.getGallery().getId() : null);
        ps.setString(6, exhibition.getCuratorName());
        ps.setString(7, exhibition.getTheme());
    }
}
