package com.bookstore.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Helper methods for showing styled JavaFX alerts.
 */
public class AlertUtil {

    private AlertUtil() {}

    /** Shows an information alert. */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /** Shows an error alert. */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /** Shows a yes/no confirmation dialog. Returns true if the user clicks OK/Yes. */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /** Applies dark-theme CSS to alert dialogs. */
    private static void styleAlert(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert.getDialogPane().getStylesheets().add(
            AlertUtil.class.getResource("/com/bookstore/css/dark-theme.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("alert-dialog");
    }
}
