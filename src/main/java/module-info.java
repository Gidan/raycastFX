module dev.gidan.raycastfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;


    opens dev.gidan.raycastfx to javafx.fxml;
    exports dev.gidan.raycastfx;
    exports dev.gidan.raycastfx.util;
    exports dev.gidan.raycastfx.prefabs;
    opens dev.gidan.raycastfx.prefabs to javafx.fxml;
}