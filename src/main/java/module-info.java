module drone.dronepathplanning_aco {
    requires javafx.controls;
    requires javafx.fxml;


    opens drone.dronepathplanning_aco to javafx.fxml;
    exports drone.dronepathplanning_aco;
}