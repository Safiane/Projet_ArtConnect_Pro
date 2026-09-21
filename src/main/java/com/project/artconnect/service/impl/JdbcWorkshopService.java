package com.project.artconnect.service.impl;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.persistence.JdbcWorkshopDao;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC du service Workshop.
 * Remplace InMemoryWorkshopService.
 */
public class JdbcWorkshopService implements WorkshopService {

    private final JdbcWorkshopDao workshopDao = new JdbcWorkshopDao();

    @Override
    public List<Workshop> getAllWorkshops() {
        return workshopDao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        return workshopDao.findAll().stream()
                .filter(w -> w.getTitle().equals(title))
                .findFirst();
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || member == null
                || workshop.getId() == null || member.getId() == null) return;

        String sql = "INSERT INTO booking (workshop_id, member_id, booking_date, payment_status) " +
                     "VALUES (?, ?, NOW(), 'PENDING')";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, workshop.getId());
            ps.setLong(2, member.getId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Booking b = new Booking(workshop, member);
                    b.setId(keys.getLong(1));
                    member.addBooking(b);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur bookWorkshop", e);
        }
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null) return List.of();
        return member.getBookings();
    }
}
