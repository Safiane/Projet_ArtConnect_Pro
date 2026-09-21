package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC du DAO Artwork.
 */
public class JdbcArtworkDao implements ArtworkDao {

    @Override
    public List<Artwork> findAll() {
        List<Artwork> list = new ArrayList<>();
        String sql = "SELECT aw.id, aw.title, aw.creation_year, aw.type, aw.medium, " +
                     "aw.dimensions, aw.description, aw.price, aw.status, " +
                     "ar.id AS artist_id, ar.name AS artist_name " +
                     "FROM artwork aw JOIN artist ar ON aw.artist_id = ar.id";
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Artwork a = mapRow(rs);
                a.setTags(findTagsByArtworkId(conn, a.getId()));
                list.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll artwork", e);
        }
        return list;
    }

    @Override
    public void save(Artwork artwork) {
        String sql = "INSERT INTO artwork (title, creation_year, type, medium, dimensions, " +
                     "description, price, status, artist_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, artwork);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) artwork.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save artwork : " + artwork.getTitle(), e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        String sql = "UPDATE artwork SET creation_year=?, type=?, medium=?, dimensions=?, " +
                     "description=?, price=?, status=?, artist_id=? WHERE title=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, artwork.getCreationYear());
            ps.setString(2, artwork.getType());
            ps.setString(3, artwork.getMedium());
            ps.setString(4, artwork.getDimensions());
            ps.setString(5, artwork.getDescription());
            ps.setDouble(6, artwork.getPrice());
            ps.setString(7, artwork.getStatus() != null ? artwork.getStatus().name() : "FOR_SALE");
            ps.setObject(8, artwork.getArtist() != null ? artwork.getArtist().getId() : null);
            ps.setString(9, artwork.getTitle());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update artwork : " + artwork.getTitle(), e);
        }
    }

    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM artwork WHERE title = ?")) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete artwork : " + title, e);
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> list = new ArrayList<>();
        String sql = "SELECT aw.id, aw.title, aw.creation_year, aw.type, aw.medium, " +
                     "aw.dimensions, aw.description, aw.price, aw.status, " +
                     "ar.id AS artist_id, ar.name AS artist_name " +
                     "FROM artwork aw JOIN artist ar ON aw.artist_id = ar.id WHERE ar.name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artwork a = mapRow(rs);
                    a.setTags(findTagsByArtworkId(conn, a.getId()));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByArtistName : " + artistName, e);
        }
        return list;
    }

    // ---- helpers ----
    private List<ArtworkTag> findTagsByArtworkId(Connection conn, Long artworkId) throws SQLException {
        List<ArtworkTag> tags = new ArrayList<>();
        String sql = "SELECT name FROM artwork_tag WHERE artwork_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, artworkId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tags.add(new ArtworkTag(rs.getString("name")));
                }
            }
        }
        return tags;
    }

    private Artwork mapRow(ResultSet rs) throws SQLException {
        Artwork a = new Artwork();
        a.setId(rs.getLong("id"));
        a.setTitle(rs.getString("title"));
        a.setCreationYear(rs.getObject("creation_year", Integer.class));
        a.setType(rs.getString("type"));
        a.setMedium(rs.getString("medium"));
        a.setDimensions(rs.getString("dimensions"));
        a.setDescription(rs.getString("description"));
        a.setPrice(rs.getDouble("price"));
        String status = rs.getString("status");
        if (status != null) a.setStatus(Artwork.Status.valueOf(status));
        Artist artist = new Artist();
        artist.setId(rs.getLong("artist_id"));
        artist.setName(rs.getString("artist_name"));
        a.setArtist(artist);
        return a;
    }

    private void setParams(PreparedStatement ps, Artwork artwork) throws SQLException {
        ps.setString(1, artwork.getTitle());
        ps.setObject(2, artwork.getCreationYear());
        ps.setString(3, artwork.getType());
        ps.setString(4, artwork.getMedium());
        ps.setString(5, artwork.getDimensions());
        ps.setString(6, artwork.getDescription());
        ps.setDouble(7, artwork.getPrice());
        ps.setString(8, artwork.getStatus() != null ? artwork.getStatus().name() : "FOR_SALE");
        ps.setObject(9, artwork.getArtist() != null ? artwork.getArtist().getId() : null);
    }
}
