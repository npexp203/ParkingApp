package be.esi.prj.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class MainController {

    private static Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = getStage(event);
            stage.setScene(new Scene(root));
            stage.setWidth(640);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue : " + fxmlFile);
            e.printStackTrace();
        }
    }

    @FXML
    private void goToScan(ActionEvent event) {
        switchScene(event, "fxml/ScanTicketView.fxml");
    }

    @FXML
    private void goToVehicleList(ActionEvent event) {
        switchScene(event, "fxml/VehicleListView.fxml");
    }

    @FXML
    private void goToCheckout(ActionEvent event) {
        switchScene(event, "fxml/CheckoutView.fxml");
    }
    /**
     * Charge la page de reçu en passant le texte du reçu.
     */
    public static void goToReceipt(ActionEvent event, String receiptText) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("/fxml/ReceiptView.fxml")
            );
            Parent root = loader.load();

            ReceiptController rc = loader.getController();
            rc.initData(receiptText);

            Stage stage = getStage(event);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Impossible de charger ReceiptView.fxml");
            e.printStackTrace();
        }
    }

    @FXML
    private void exitApp(ActionEvent event) {
        getStage(event).close();
    }
}
