package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.stream.Collectors;

public class ArtworkController {

    @FXML private TableView<Artwork>              artworkTable;
    @FXML private TableColumn<Artwork, String>    titleColumn;
    @FXML private TableColumn<Artwork, String>    artistColumn;
    @FXML private TableColumn<Artwork, String>    typeColumn;
    @FXML private TableColumn<Artwork, Integer>   yearColumn;
    @FXML private TableColumn<Artwork, Double>    priceColumn;
    @FXML private TableColumn<Artwork, String>    statusColumn;
    @FXML private TableColumn<Artwork, String>    tagsColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService  artistService  = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("creationYear"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null
                        ? cellData.getValue().getArtist().getName() : "Unknown"));

        tagsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getTags().stream()
                        .map(ArtworkTag::getName)
                        .collect(Collectors.joining(", "))));

        refreshTable();
    }

    @FXML
    private void handleAdd() {
        showArtworkDialog(null).ifPresent(artwork -> {
            try {
                artworkService.createArtwork(artwork);
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de l'ajout : " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Sélectionne une œuvre à modifier."); return; }
        showArtworkDialog(selected).ifPresent(artwork -> {
            try {
                artworkService.updateArtwork(artwork);
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de la modification : " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Sélectionne une œuvre à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'œuvre \"" + selected.getTitle() + "\" ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            try {
                artworkService.deleteArtwork(selected.getTitle());
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de la suppression : " + e.getMessage());
            }
        });
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    private java.util.Optional<Artwork> showArtworkDialog(Artwork existing) {
        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter une œuvre" : "Modifier l'œuvre");
        dialog.setHeaderText(existing == null ? "Remplir les informations de la nouvelle œuvre" : "Modifier : " + existing.getTitle());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField titleField  = new TextField(existing != null ? existing.getTitle() : "");
        TextField typeField   = new TextField(existing != null ? nvl(existing.getType()) : "");
        TextField medField    = new TextField(existing != null ? nvl(existing.getMedium()) : "");
        TextField dimField    = new TextField(existing != null ? nvl(existing.getDimensions()) : "");
        TextField descField   = new TextField(existing != null ? nvl(existing.getDescription()) : "");
        TextField priceField  = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "0.0");
        TextField yearField   = new TextField(existing != null && existing.getCreationYear() != null
                ? existing.getCreationYear().toString() : "");

        ComboBox<Artwork.Status> statusBox = new ComboBox<>();
        statusBox.setItems(FXCollections.observableArrayList(Artwork.Status.values()));
        statusBox.setValue(existing != null && existing.getStatus() != null
                ? existing.getStatus() : Artwork.Status.FOR_SALE);

        ComboBox<Artist> artistBox = new ComboBox<>();
        artistBox.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        if (existing != null && existing.getArtist() != null) {
            artistService.getArtistByName(existing.getArtist().getName())
                    .ifPresent(artistBox::setValue);
        }

        titleField.setPrefWidth(250);
        if (existing != null) titleField.setEditable(false);

        grid.add(new Label("Titre *:"),      0, 0); grid.add(titleField,  1, 0);
        grid.add(new Label("Type :"),        0, 1); grid.add(typeField,   1, 1);
        grid.add(new Label("Medium :"),      0, 2); grid.add(medField,    1, 2);
        grid.add(new Label("Dimensions :"),  0, 3); grid.add(dimField,    1, 3);
        grid.add(new Label("Description :"), 0, 4); grid.add(descField,   1, 4);
        grid.add(new Label("Prix (€) :"),    0, 5); grid.add(priceField,  1, 5);
        grid.add(new Label("Année :"),       0, 6); grid.add(yearField,   1, 6);
        grid.add(new Label("Statut :"),      0, 7); grid.add(statusBox,   1, 7);
        grid.add(new Label("Artiste :"),     0, 8); grid.add(artistBox,   1, 8);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        titleField.textProperty().addListener((obs, o, n) ->
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(n.trim().isEmpty()));
        if (existing != null)
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            Artwork a = existing != null ? existing : new Artwork();
            if (existing == null) a.setTitle(titleField.getText().trim());
            a.setType(typeField.getText().trim());
            a.setMedium(medField.getText().trim());
            a.setDimensions(dimField.getText().trim());
            a.setDescription(descField.getText().trim());
            a.setStatus(statusBox.getValue());
            a.setArtist(artistBox.getValue());
            try { a.setPrice(Double.parseDouble(priceField.getText().trim())); }
            catch (NumberFormatException ignored) { a.setPrice(0); }
            try { a.setCreationYear(Integer.parseInt(yearField.getText().trim())); }
            catch (NumberFormatException ignored) { a.setCreationYear(null); }
            return a;
        });

        return dialog.showAndWait();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
