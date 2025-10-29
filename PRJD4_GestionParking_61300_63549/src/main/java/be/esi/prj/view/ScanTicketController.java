package be.esi.prj.view;

import be.esi.prj.model.PlateScanner;
import be.esi.prj.model.dto.VehicleDto;
import be.esi.prj.model.repository.VehicleRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur pour scanner et enregistrer un ticket via OCR.
 * Supporte le drag & drop d'image directement sur le champ de plaque.
 */
public class ScanTicketController {

    @FXML
    private TextField plateNumberField;

    @FXML
    private TextField entryTimeField;

    @FXML
    private TextField exitTimeField;

    private final PlateScanner plateScanner  = new PlateScanner();
    private final VehicleRepository repository = new VehicleRepository();
    private File selectedImageFile;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    /** Initialise le contrôleur, configure drag & drop **/
    @FXML
    public void initialize() {
        plateNumberField.setEditable(false);
        entryTimeField.setEditable(false);
        exitTimeField.setEditable(true);

        // Drag & Drop sur le champ plaque
        plateNumberField.setOnDragOver((DragEvent evt) -> {
            if (evt.getDragboard().hasFiles()) evt.acceptTransferModes(TransferMode.COPY);
            evt.consume();
        });
        plateNumberField.setOnDragDropped((DragEvent evt) -> {
            var db = evt.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                selectedImageFile = db.getFiles().get(0);
                success = true;
                // Lancer automatiquement le scan
                scanTicket(null);
            }
            evt.setDropCompleted(success);
            evt.consume();
        });
    }

    /** Retour à l'écran principal **/
    @FXML
    public void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
            getStage(event).setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Ouvre le dialogue de sélection d'image **/
    @FXML
    public void chooseImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = chooser.showOpenDialog(getStage(event));
    }

    /** Lance le scan OCR et affiche les résultats **/
    @FXML
    public void scanTicket(ActionEvent event) {
        if (selectedImageFile == null) {
            System.out.println("Aucune image sélectionnée !");
            return;
        }
        plateScanner.ocrAsync(
                selectedImageFile,
                raw -> {
                    String cleaned = raw.replaceAll("[^A-Z0-9\\-]", "").toUpperCase();
                    Platform.runLater(() -> {
                        plateNumberField.setText(cleaned);
                        entryTimeField.setText(LocalDateTime.now().format(FORMATTER));
                        exitTimeField.setText(LocalDateTime.now().format(FORMATTER));
                    });
                },
                err -> {
                    Platform.runLater(() -> {
                        plateNumberField.clear();
                        entryTimeField.clear();
                        exitTimeField.clear();
                    });
                    err.printStackTrace();
                }
        );
    }

    /** Enregistre le ticket en base **/
    @FXML
    public void saveEntry(ActionEvent event) {
        String plate = plateNumberField.getText().trim();
        String inStr = entryTimeField.getText().trim();
        String outStr= exitTimeField.getText().trim();
        if (plate.isEmpty() || inStr.isEmpty() || outStr.isEmpty()) {
            System.out.println("Champs vides !");
            return;
        }
        try {
            boolean exists = repository.findAll().stream()
                    .anyMatch(v -> v.plateNumber().equalsIgnoreCase(plate));
            if (exists) {
                new Alert(Alert.AlertType.ERROR,
                        "Ce véhicule est déjà présent dans le parking.").showAndWait();
                return;
            }
            LocalDateTime in = LocalDateTime.parse(inStr, FORMATTER);
            LocalDateTime out= LocalDateTime.parse(outStr, FORMATTER);
            VehicleDto vehicle = new VehicleDto(
                    repository.getMaxVehicleId()+1,
                    plate, in, out
            );
            int id = repository.save(vehicle);
            if (id != -1) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Véhicule enregistré avec succès !").showAndWait();
                plateNumberField.clear();
                entryTimeField.clear();
                exitTimeField.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur lors de la sauvegarde : " + e.getMessage()).showAndWait();
        }
    }
}
