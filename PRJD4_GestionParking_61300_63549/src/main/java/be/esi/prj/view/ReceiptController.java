package be.esi.prj.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class ReceiptController {
    @FXML
    private Label receiptLabel;

    /**
     * Appelé par CheckoutController pour injecter le texte.
     */
    public void initData(String receiptText) {
        receiptLabel.setText(receiptText);
    }

    @FXML
    public void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/MainView.fxml")
            );
            Stage stage = (Stage) receiptLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
