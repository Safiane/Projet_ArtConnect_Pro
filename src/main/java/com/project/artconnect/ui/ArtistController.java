package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.stream.Collectors;

public class ArtistController {

    @FXML private TextField              searchField;
    @FXML private ComboBox<Discipline>   disciplineFilter;
    @FXML private TableView<Artist>      artistTable;
    @FXML private TableColumn<Artist, String>  nameColumn;
    @FXML private TableColumn<Artist, String>  cityColumn;
    @FXML private TableColumn<Artist, String>  emailColumn;
    @FXML private TableColumn<Artist, Integer> yearColumn;
    @FXML private TableColumn<Artist, String>  disciplinesColumn;
    @FXML private TableColumn<Artist, String> activeColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplinesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDisciplines().stream()
                        .map(Discipline::getName)
                        .collect(Collectors.joining(", "))));

        activeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "✔" : "✘"));

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(
                artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        showArtistDialog(null).ifPresent(artist -> {
            try {
                artist.setActive(true);
                artistService.createArtist(artist);
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de l'ajout : " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Sélectionne un artiste à modifier."); return; }
        showArtistDialog(selected).ifPresent(artist -> {
            try {
                artistService.updateArtist(artist);
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de la modification : " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Sélectionne un artiste à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'artiste \"" + selected.getName() + "\" ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            try {
                artistService.deleteArtist(selected.getName());
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de la suppression : " + e.getMessage());
            }
        });
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private java.util.Optional<Artist> showArtistDialog(Artist existing) {
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter un artiste" : "Modifier l'artiste");
        dialog.setHeaderText(existing == null ? "Remplir les informations du nouvel artiste" : "Modifier les informations de " + existing.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField nameField  = new TextField(existing != null ? existing.getName() : "");
        TextField cityField  = new TextField(existing != null ? nvl(existing.getCity()) : "");
        TextField emailField = new TextField(existing != null ? nvl(existing.getContactEmail()) : "");
        TextField phoneField = new TextField(existing != null ? nvl(existing.getPhone()) : "");
        TextField yearField  = new TextField(existing != null && existing.getBirthYear() != null
                ? existing.getBirthYear().toString() : "");
        TextField bioField   = new TextField(existing != null ? nvl(existing.getBio()) : "");
        TextField webField   = new TextField(existing != null ? nvl(existing.getWebsite()) : "");
        CheckBox  activeBox  = new CheckBox();
        activeBox.setSelected(existing == null || existing.isActive());

        nameField.setPrefWidth(250);
        bioField.setPrefWidth(250);

        if (existing != null) nameField.setEditable(false);

        grid.add(new Label("Nom *:"),       0, 0); grid.add(nameField,  1, 0);
        grid.add(new Label("Ville :"),      0, 1); grid.add(cityField,  1, 1);
        grid.add(new Label("Email :"),      0, 2); grid.add(emailField, 1, 2);
        grid.add(new Label("Téléphone :"),  0, 3); grid.add(phoneField, 1, 3);
        grid.add(new Label("Naissance :"),  0, 4); grid.add(yearField,  1, 4);
        grid.add(new Label("Bio :"),        0, 5); grid.add(bioField,   1, 5);
        grid.add(new Label("Site web :"),   0, 6); grid.add(webField,   1, 6);
        grid.add(new Label("Actif :"),      0, 7); grid.add(activeBox,  1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        nameField.textProperty().addListener((obs, o, n) ->
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(n.trim().isEmpty()));
        if (existing != null)
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            Artist a = existing != null ? existing : new Artist();
            if (existing == null) a.setName(nameField.getText().trim());
            a.setCity(cityField.getText().trim());
            a.setContactEmail(emailField.getText().trim());
            a.setPhone(phoneField.getText().trim());
            a.setWebsite(webField.getText().trim());
            a.setBio(bioField.getText().trim());
            a.setActive(activeBox.isSelected());
            try { a.setBirthYear(Integer.parseInt(yearField.getText().trim())); }
            catch (NumberFormatException ignored) { a.setBirthYear(null); }
            return a;
        });

        return dialog.showAndWait();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
