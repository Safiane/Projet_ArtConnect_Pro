package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.model.Review;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC du DAO CommunityMember.
 */
public class JdbcCommunityMemberDao implements CommunityMemberDao {

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> list = new ArrayList<>();
        String sql = "SELECT id, name, email, birth_year, phone, city, membership_type FROM community_member";
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                CommunityMember m = mapRow(rs);
                m.setReviews(findReviewsByMemberId(conn, m));
                m.setBookings(findBookingsByMemberId(conn, m));
                m.setFavoriteDisciplines(findDisciplinesByMemberId(conn, m.getId()));
                list.add(m);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll community_member", e);
        }
        return list;
    }

    @Override
    public Optional<CommunityMember> findById(Long id) {
        String sql = "SELECT id, name, email, birth_year, phone, city, membership_type " +
                     "FROM community_member WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CommunityMember m = mapRow(rs);
                    m.setReviews(findReviewsByMemberId(conn, m));
                    m.setBookings(findBookingsByMemberId(conn, m));
                    m.setFavoriteDisciplines(findDisciplinesByMemberId(conn, m.getId()));
                    return Optional.of(m);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById community_member : " + id, e);
        }
        return Optional.empty();
    }

    // ---- helpers ----
    private CommunityMember mapRow(ResultSet rs) throws SQLException {
        CommunityMember m = new CommunityMember();
        m.setId(rs.getLong("id"));
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        m.setBirthYear(rs.getObject("birth_year", Integer.class));
        m.setPhone(rs.getString("phone"));
        m.setCity(rs.getString("city"));
        m.setMembershipType(rs.getString("membership_type"));
        return m;
    }

    private List<Review> findReviewsByMemberId(Connection conn, CommunityMember member) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.id, r.rating, r.comment, r.review_date, " +
                     "aw.id AS artwork_id, aw.title AS artwork_title " +
                     "FROM review r JOIN artwork aw ON r.artwork_id = aw.id " +
                     "WHERE r.member_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, member.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    Date d = rs.getDate("review_date");
                    if (d != null) r.setReviewDate(d.toLocalDate());
                    Artwork artwork = new Artwork();
                    artwork.setId(rs.getLong("artwork_id"));
                    artwork.setTitle(rs.getString("artwork_title"));
                    r.setArtwork(artwork);
                    r.setReviewer(member);
                    reviews.add(r);
                }
            }
        }
        return reviews;
    }

    private List<Booking> findBookingsByMemberId(Connection conn, CommunityMember member) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.id, b.booking_date, b.payment_status, " +
                     "w.id AS workshop_id, w.title AS workshop_title " +
                     "FROM booking b JOIN workshop w ON b.workshop_id = w.id " +
                     "WHERE b.member_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, member.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    b.setId(rs.getLong("id"));
                    Timestamp ts = rs.getTimestamp("booking_date");
                    if (ts != null) b.setBookingDate(ts.toLocalDateTime());
                    b.setPaymentStatus(rs.getString("payment_status"));
                    Workshop w = new Workshop();
                    w.setId(rs.getLong("workshop_id"));
                    w.setTitle(rs.getString("workshop_title"));
                    b.setWorkshop(w);
                    b.setMember(member);
                    bookings.add(b);
                }
            }
        }
        return bookings;
    }

    private List<Discipline> findDisciplinesByMemberId(Connection conn, Long memberId) throws SQLException {
        List<Discipline> disciplines = new ArrayList<>();
        String sql = "SELECT d.name FROM discipline d " +
                     "JOIN member_discipline md ON d.id = md.discipline_id " +
                     "WHERE md.member_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    disciplines.add(new Discipline(rs.getString("name")));
                }
            }
        }
        return disciplines;
    }
}
