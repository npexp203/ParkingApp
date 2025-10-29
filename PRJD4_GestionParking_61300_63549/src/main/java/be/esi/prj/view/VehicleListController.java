package be.esi.prj.view;

import be.esi.prj.model.dto.VehicleDto;
import be.esi.prj.model.repository.VehicleRepository;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VehicleListController {

    @FXML
    private TableView<VehicleDto> vehicleTable;
    @FXML
    private TableColumn<VehicleDto, String> plateColumn;
    @FXML
    private TableColumn<VehicleDto, String> timeColumn;
    @FXML
    private TableColumn<VehicleDto, String> exitColumn;

    private final VehicleRepository vehicleRepo = new VehicleRepository();
    private final ObservableList<VehicleDto> vehicleData = FXCollections.observableArrayList();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        plateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().plateNumber())
        );

        timeColumn.setCellValueFactory(cellData -> {
            String text = cellData.getValue().entryTime().format(FORMATTER);
            return new SimpleStringProperty(text);
        });

        exitColumn.setCellValueFactory(cellData -> {
            LocalDateTime exit = cellData.getValue().exitTime();
            String text = exit != null ? exit.format(FORMATTER) : "";
            return new SimpleStringProperty(text);
        });

        vehicleTable.setItems(vehicleData);

        refreshVehicleData();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> refreshVehicleData()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void refreshVehicleData() {
        Thread fetchThread = new Thread(() -> {
            List<VehicleDto> allVehicles = vehicleRepo.findAll();

            System.out.println("Total véhicules dans le cache : " + allVehicles.size());
            allVehicles.forEach(v -> System.out.println(
                    "- " + v.plateNumber() + " | Entry: " + v.entryTime() + " | Exit: " + v.exitTime()
            ));

            Platform.runLater(() -> vehicleData.setAll(allVehicles));
        });

        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    @FXML
    public void goBack(ActionEvent event) {
        switchScene(event, "fxml/MainView.fxml");
    }

    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = getStage(event);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue : " + fxmlFile);
            e.printStackTrace();
        }
    }
}
