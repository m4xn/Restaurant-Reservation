package com.bookstore.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Helper methods for showing styled JavaFX alerts.
 */
public class AlertUtil {

    private AlertUtil() {}

    /** Shows an information alert (no owner; may size oddly when the app is full screen). */
    public static void showInfo(String title, String message) {
        showInfo(null, title, message);
    }

    /** Shows an information alert owned by {@code owner} so it stays a compact dialog on top. */
    public static void showInfo(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert, owner);
        alert.showAndWait();
    }

    /** Shows an error alert. */
    public static void showError(String title, String message) {
        showError(null, title, message);
    }

    public static void showError(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert, owner);
        alert.showAndWait();
    }

    /** Shows a yes/no confirmation dialog. Returns true if the user clicks OK/Yes. */
    public static boolean showConfirmation(String title, String message) {
        return showConfirmation(null, title, message);
    }

    public static boolean showConfirmation(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert, owner);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /** Applies dark-theme CSS to alert dialogs. */
    private static void styleAlert(Alert alert, Window owner) {
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        } else {
            alert.initModality(Modality.APPLICATION_MODAL);
        }
        alert.getDialogPane().getStylesheets().add(
            AlertUtil.class.getResource("/com/bookstore/css/dark-theme.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("alert-dialog");
    }
}
