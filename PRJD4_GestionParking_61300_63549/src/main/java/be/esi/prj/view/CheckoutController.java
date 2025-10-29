package be.esi.prj.view;

import be.esi.prj.model.PlateScanner;
import be.esi.prj.model.ParkingTicket;
import be.esi.prj.model.ParkingFeeCalculator;
import be.esi.prj.model.dto.VehicleDto;
import be.esi.prj.model.repository.VehicleRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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
import java.util.function.Consumer;

public class CheckoutController {

    @FXML private Label imagePathLabel;
    @FXML private Label detectedPlateLabel;
    @FXML private Label entryTimeLabel;
    @FXML private Label plannedExitLabel;
    @FXML private TextField departureField;
    @FXML private Label feeLabel;

    private final PlateScanner plateScanner     = new PlateScanner();
    private final VehicleRepository repository  = new VehicleRepository();
    private final DateTimeFormatter FMT         = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private LocalDateTime plannedExitTime;
    private ParkingTicket currentTicket;
    private File selectedImageFile;
    @FXML
    public void initialize() {
        imagePathLabel.setOnDragOver((DragEvent evt) -> {
            if (evt.getDragboard().hasFiles()) {
                evt.acceptTransferModes(TransferMode.COPY);
            }
            evt.consume();
        });
        imagePathLabel.setOnDragDropped((DragEvent evt) -> {
            var db = evt.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File f = db.getFiles().get(0);
                selectedImageFile = f;
                imagePathLabel.setText(f.getName());
                success = true;
            }
            evt.setDropCompleted(success);
            evt.consume();
        });

    }


    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    /** Choisit une image **/
    @FXML
    public void chooseImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = chooser.showOpenDialog(getStage(event));
        if (file != null) {
            selectedImageFile = file;
            imagePathLabel.setText(file.getName());
        }
    }

    /** Scan OCR + affichage des dates de façon asynchrone **/
    @FXML
    public void scanTicket(ActionEvent event) {
        if (selectedImageFile == null) return;

        plateScanner.ocrAsync(
                selectedImageFile,
                rawResult -> {
                    String cleaned = rawResult.replaceAll("[^A-Z0-9\\-]", "").toUpperCase();
                    Platform.runLater(() -> {
                        detectedPlateLabel.setText(cleaned);
                        try {
                            currentTicket = repository.findTicketByPlate(cleaned)
                                    .orElseThrow(() -> new IllegalArgumentException("Aucun ticket pour cette plaque"));
                            entryTimeLabel.setText(currentTicket.entryTime().format(FMT));
                            LocalDateTime exit = currentTicket.exitTime();
                            if (exit != null) {
                                plannedExitTime = exit;
                                plannedExitLabel.setText(exit.format(FMT));
                            } else {
                                plannedExitTime = null;
                                plannedExitLabel.setText("");
                            }
                            departureField.setText(LocalDateTime.now().format(FMT));
                        } catch (Exception ex) {
                            detectedPlateLabel.setText("Erreur ticket : " + ex.getMessage());
                            currentTicket = null;
                        }
                    });
                },
                error -> Platform.runLater(() -> {
                    detectedPlateLabel.setText("Erreur OCR");
                    error.printStackTrace();
                })
        );
    }

    /** Calculer le tarif de manière asynchrone **/
    @FXML
    public void onCalculateFee(ActionEvent event) {
        if (currentTicket == null) {
            feeLabel.setText("Veuillez d'abord scanner un ticket.");
            return;
        }
        LocalDateTime actual;
        try {
            actual = LocalDateTime.parse(departureField.getText(), FMT);
        } catch (Exception e) {
            feeLabel.setText("Format date incorrect !");
            return;
        }

        LocalDateTime usedExit = (plannedExitTime != null) ? plannedExitTime : actual;
        ParkingFeeCalculator.calculateFeeAsync(
                currentTicket.entryTime(),
                usedExit,
                actual,
                fee -> Platform.runLater(() -> feeLabel.setText(
                        String.format("Montant à payer : %.2f€", fee)
                )),
                ex -> Platform.runLater(() -> feeLabel.setText("Erreur calcul tarif"))
        );

        VehicleDto updated = new VehicleDto(
                currentTicket.id(),
                currentTicket.plateNumber(),
                currentTicket.entryTime(),
                actual
        );
        repository.save(updated);
        currentTicket = new ParkingTicket(
                currentTicket.id(),
                currentTicket.plateNumber(),
                currentTicket.entryTime(),
                actual
        );
    }

    /** Valider la sortie et afficher le reçu en asynchrone **/
    @FXML
    public void onProcessExit(ActionEvent event) {
        if (currentTicket == null) return;

        LocalDateTime actual;
        try {
            actual = LocalDateTime.parse(departureField.getText(), FMT);
        } catch (Exception e) {
            feeLabel.setText("Format date incorrect !");
            return;
        }

        repository.delete(currentTicket.id());

        LocalDateTime usedExit = (plannedExitTime != null) ? plannedExitTime : actual;
        ParkingFeeCalculator.getExitSummaryAsync(
                currentTicket.entryTime(),
                usedExit,
                actual,
                summary -> Platform.runLater(() -> {
                    new Alert(Alert.AlertType.INFORMATION,
                            "Véhicule sorti le " + actual.format(FMT)
                    ).showAndWait();
                    MainController.goToReceipt(event, summary);
                    currentTicket = null;
                    selectedImageFile = null;
                }),
                ex -> Platform.runLater(() -> feeLabel.setText("Erreur sortie"))
        );
    }

    /** Retour au menu principal **/
    @FXML
    public void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
            getStage(event).setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
