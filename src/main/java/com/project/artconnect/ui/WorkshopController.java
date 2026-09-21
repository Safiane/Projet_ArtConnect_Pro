package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ConnectionManager;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class WorkshopController {

    @FXML private TableView<Workshop>               workshopTable;
    @FXML private TableColumn<Workshop, String>     titleColumn;
    @FXML private TableColumn<Workshop, String>     instructorColumn;
    @FXML private TableColumn<Workshop, String>     dateColumn;
    @FXML private TableColumn<Workshop, Integer>    durationColumn;
    @FXML private TableColumn<Workshop, Double>     priceColumn;
    @FXML private TableColumn<Workshop, String>     levelColumn;
    @FXML private TableColumn<Workshop, Number>     spotsColumn;

    private final WorkshopService  workshopService  = ServiceProvider.getWorkshopService();
    private final CommunityService communityService = ServiceProvider.getCommunityService();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));

        durationColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDurationMinutes()).asObject());

        instructorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInstructor() != null
                        ? cellData.getValue().getInstructor().getName() : "Unknown"));

        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getDate();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "");
        });

        spotsColumn.setCellValueFactory(cellData -> {
            Workshop w = cellData.getValue();
            int booked = getBookedCount(w.getId());
            return new SimpleIntegerProperty(Math.max(0, w.getMaxParticipants() - booked));
        });

        refreshTable();
    }

    @FXML
    private void handleBook() {
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Sélectionne un atelier pour s'inscrire."); return; }

        int booked = getBookedCount(selected.getId());
        if (booked >= selected.getMaxParticipants()) {
            showError("Cet atelier est complet (" + selected.getMaxParticipants() + "/" + selected.getMaxParticipants() + " places).");
            return;
        }

        List<CommunityMember> members = communityService.getAllMembers();
        if (members.isEmpty()) { showError("Aucun membre disponible."); return; }

        ChoiceDialog<CommunityMember> memberDialog = new ChoiceDialog<>(members.get(0), members);
        memberDialog.setTitle("Réserver un atelier");
        memberDialog.setHeaderText("Atelier : " + selected.getTitle());
        memberDialog.setContentText("Sélectionner le membre :");

        Optional<CommunityMember> result = memberDialog.showAndWait();
        result.ifPresent(member -> {
            try {
                workshopService.bookWorkshop(selected, member);
                new Alert(Alert.AlertType.INFORMATION,
                        member.getName() + " inscrit(e) à \"" + selected.getTitle() + "\" avec succès !",
                        ButtonType.OK).showAndWait();
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de l'inscription : " + e.getMessage());
            }
        });
    }

    private void refreshTable() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }

    private int getBookedCount(Long workshopId) {
        if (workshopId == null) return 0;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM booking WHERE workshop_id = ?")) {
            ps.setLong(1, workshopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
