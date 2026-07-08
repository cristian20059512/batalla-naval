module com.batallanaval {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    // necesario para que el FXMLLoader pueda inyectar el controlador por reflexion
    opens com.batallanaval to javafx.fxml;
    opens com.batallanaval.controller to javafx.fxml;

    exports com.batallanaval;
}
