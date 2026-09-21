package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC du DAO Gallery.
 */
public class JdbcGalleryDao implements GalleryDao {

    @Override
    public List<Gallery> findAll() {
        List<Gallery> list = new ArrayList<>();
        String sql = "SELECT id, name, address, owner_name, opening_hours, " +
                     "contact_phone, rating, website FROM gallery";
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Gallery g = mapRow(rs);
                g.setExhibitions(findExhibitionsByGalleryId(conn, g.getId()));
                list.add(g);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll gallery", e);
        }
        return list;
    }

    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = "SELECT id, name, address, owner_name, opening_hours, " +
                     "contact_phone, rating, website FROM gallery WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Gallery g = mapRow(rs);
                    g.setExhibitions(findExhibitionsByGalleryId(conn, g.getId()));
                    return Optional.of(g);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById gallery : " + id, e);
        }
        return Optional.empty();
    }

    // ---- helpers ----
    private Gallery mapRow(ResultSet rs) throws SQLException {
        Gallery g = new Gallery();
        g.setId(rs.getLong("id"));
        g.setName(rs.getString("name"));
        g.setAddress(rs.getString("address"));
        g.setOwnerName(rs.getString("owner_name"));
        g.setOpeningHours(rs.getString("opening_hours"));
        g.setContactPhone(rs.getString("contact_phone"));
        g.setRating(rs.getDouble("rating"));
        g.setWebsite(rs.getString("website"));
        return g;
    }

    private List<Exhibition> findExhibitionsByGalleryId(Connection conn, Long galleryId) throws SQLException {
        List<Exhibition> exhList = new ArrayList<>();
        String sql = "SELECT id, title, start_date, end_date, description, curator_name, theme " +
                     "FROM exhibition WHERE gallery_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, galleryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    e.setArtworks(findArtworksByExhibitionId(conn, e.getId()));
                    exhList.add(e);
                }
            }
        }
        return exhList;
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
}
