package drone.dronepathplanning_aco;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.Locale;

public class MainView extends Application {

    // Popup menu icons
    private final String CLEAR_LOG_ICON = "M8.5 6.5 H15.5 M10 4.5 H14 M7 6.5 L8 18.5 H16 L17 6.5 M10.5 9.5 V15.5 M13.5 9.5 V15.5";
    private final String COPY_LOG_ICON = "M8.5 8.5 H16.5 A1.5 1.5 0 0 1 18 10 V17 A1.5 1.5 0 0 1 16.5 18.5 H8.5 A1.5 1.5 0 0 1 7 17 V10 A1.5 1.5 0 0 1 8.5 8.5 Z M10 8 V7 A1.5 1.5 0 0 1 11.5 5.5 H18 A1.5 1.5 0 0 1 19.5 7 V13.5";
    private final String SAVE_LOG_ICON = "M4.5 5.5 L6.5 3.5 H17.5 A1.5 1.5 0 0 1 19 5 V19 A1.5 1.5 0 0 1 17.5 20.5 H6 A1.5 1.5 0 0 1 4.5 19 Z M7.5 3.8 V8.5 H15.5 V3.8 M8 20.2 V13.2 H16 V20.2 M14 5.4 V7";
    // BUTTON
    private final ToggleGroup toolGroup = new ToggleGroup();
    private final Button btnNew = initializeButton(SVGIcon.newEnv, 1.0, 1.0, "New Environment");// VISUALIZATION
    private final Button btnOpen = initializeButton(SVGIcon.openEnv, 1.0, 1.0, "Open Environment");// VISUALIZATION
    private final Button btnSave = initializeButton(SVGIcon.saveEnv, 1.0, 1.0, "Save Environment");// VISUALIZATION
    private final ToggleButton btnTransform = initializeToggleButton(SVGIcon.transform, 1.0, 1.0, "Transform");// VISUALIZATION
    private final ToggleButton btnWall = initializeToggleButton(SVGIcon.wall, 1.0, 1.0, "Wall");// VISUALIZATION
    private final ToggleButton btnDrawLine = initializeToggleButton(SVGIcon.drawLine, 1.0, 1.0, "Draw Line");// VISUALIZATION
    private final ToggleButton btnRemoveLine = initializeToggleButton(SVGIcon.removeLine, 1.0, 1.0, "Remove Line");// VISUALIZATION
    private final ToggleButton btnSetFinish = initializeToggleButton(SVGIcon.setFinish, 1.0, 1.0, "Set Finish");
    private final Button btnRun = initializeButton(SVGIcon.run, 1.0, 1.0, "Run");// VISUALIZATION
    private final Button btnPlayPause = initializeButton(SVGIcon.playPause, 1.0, 1.0, "Play - Pause");// VISUALIZATION
    private final Button btnReplay = initializeButton(SVGIcon.replay, 1.0, 1.0, "Replay");// VISUALIZATION
    private final int MAX_NUM_ROWS = 200;// GRID
    private final int MAX_NUM_COLS = 400;// GRID
    private final int cellSize = 10;// GRID
    private final int numRows = MAX_NUM_ROWS;// GRID
    private final int numCols = MAX_NUM_COLS;// GRID
    private final int[][] environment = new int[numRows][numCols];// GRID
    private Label statusLabel; // Status shown in the bottom bar
    private Label statelabel; // Current editor mode
    private FileChooser fileChooser = new FileChooser();// VISUALIZATION
    private String workspace = System.getProperty("user.home");
    private Stage primaryStage = null;
    // VISUALIZATION
    private ProgressIndicator progressIndicator;// VISUALIZATION
    private CanvasView canvas = null;
    private TextArea logArea;
    private String currentMapName = "Untitled (in-memory environment)";

    // Create button
    private Button initializeButton(String svgPathData, double scaleX, double scaleY, String tooltip) {
        SVGPath path = new SVGPath();
        path.setContent(svgPathData);
        path.setFill(Color.valueOf("#393E46"));
        path.setStroke(null);
        path.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        path.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        path.setScaleX(scaleX);
        path.setScaleY(scaleY);
        Button btn = new Button();
        btn.setGraphic(path);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: transparent;");
        btn.setOnMouseEntered(e -> path.setFill(Color.valueOf("#0096FF")));
        btn.setOnMouseExited(e -> path.setFill(Color.valueOf("#393E46")));
        return btn;
    }

    // Create ToggleButton
    private ToggleButton initializeToggleButton(String svgPathData, double scaleX, double scaleY, String tooltip) {
        SVGPath path = new SVGPath();
        path.setContent(svgPathData);
        path.setFill(Color.valueOf("#393E46"));
        path.setStroke(null);
        path.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        path.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        path.setScaleX(scaleX);
        path.setScaleY(scaleY);

        ToggleButton btn = new ToggleButton();
        btn.setGraphic(path);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: transparent;");

        // Hover
        btn.setOnMouseEntered(e -> {
            if (!btn.isSelected()) {
                path.setFill(Color.valueOf("#393E46"));
                path.setStroke(null);
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.isSelected()) {
                path.setFill(Color.valueOf("#393E46"));
                path.setStroke(null);
            }
        });

        // Selected state
        btn.selectedProperty().addListener((obs, oldVal, isSelected) -> {
            if (isSelected) {
                path.setFill(Color.valueOf("#0096FF")); // warna aktif
            } else {
                path.setFill(Color.valueOf("#393E46")); // kembali normal
            }
        });

        return btn;
    }

    private void initializeToggleGroup() {
        btnTransform.setToggleGroup(toolGroup);
        btnWall.setToggleGroup(toolGroup);
        btnDrawLine.setToggleGroup(toolGroup);
        btnRemoveLine.setToggleGroup(toolGroup);
        btnSetFinish.setToggleGroup(toolGroup);

        // default tool
        btnTransform.setSelected(true);

        // aksi untuk tombol di toggle group
        toolGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
                return;
            }
            if (newToggle == btnTransform) {
                canvas.setFreeTransform();
            } else if (newToggle == btnWall) {
                canvas.setState(State.OBSTACLE);
            } else if (newToggle == btnDrawLine) {
                canvas.setState(State.LINE);
            } else if (newToggle == btnSetFinish) {
                canvas.setState(State.SET_FINISH);
            } else if (newToggle == btnRemoveLine) {
                canvas.setState(State.REMOVE_LINE);
            }
            checkState();
        });
    }

    // Create a small outlined SVG icon for popup/context menu items
    private javafx.scene.Node createMenuIcon(String svgPathData) {
        SVGPath path = new SVGPath();
        path.setContent(svgPathData);
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.valueOf("#393E46"));
        path.setStrokeWidth(1.45);
        path.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        path.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        path.setScaleX(0.90);
        path.setScaleY(0.90);

        StackPane wrapper = new StackPane(path);
        wrapper.setMinSize(18, 18);
        wrapper.setPrefSize(18, 18);
        wrapper.setMaxSize(18, 18);
        return wrapper;
    }

    private CanvasView getCanvas() {
        if (canvas == null) {
            canvas = new CanvasView(environment, cellSize);
            canvas.setOnMousePressed(this::handleMousePressed);
            canvas.setOnMouseDragged(this::handleMouseDragged);
            canvas.setOnScroll(this::handleMouseScroll);
            canvas.setOnMouseClicked(this::handleMouseClicked);
            canvas.setOnMouseReleased(this::handleMouseReleased);
            canvas.setOnMouseMoved(this::handleMouseMoved);
        }
        return canvas;
    }

    private void handleMouseMoved(MouseEvent mouseEvent) {
        canvas.handleMouseMoved(mouseEvent);
    }

    private void handleMouseReleased(MouseEvent mouseEvent) {
        canvas.handleMouseReleased(mouseEvent);
    }

    private void handleMouseClicked(MouseEvent mouseEvent) {canvas.handleMouseClicked(mouseEvent); }

    private void handleMouseScroll(ScrollEvent scrollEvent) {
        canvas.handleMouseScroll(scrollEvent);
    }

    private void handleMouseDragged(MouseEvent mouseEvent) {
        canvas.handleMouseDragged(mouseEvent);
    }

    private void handleMousePressed(MouseEvent mouseEvent) {
        canvas.handleMousePressed(mouseEvent);
    }

    private void handleButtonOpen(ActionEvent actionEvent) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open Environment");

        // Set initial directory "Documents"
        File documentsDir = new File(workspace, "Documents");
        if (documentsDir.exists()) {
            fileChooser.setInitialDirectory(documentsDir);
        }

        // Opsional: filter jenis file
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Data", "*.sc"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            // System.out.println("Selected File: " + selectedFile.getAbsolutePath());
            try {
                canvas.readFromFile(selectedFile);
                currentMapName = selectedFile.getName();
                clearSimulationLog();
                logArea.appendText("Environment loaded: " + currentMapName + "\n");
                statusLabel.setText("Opened: " + selectedFile.getName());
                checkState();
            } catch (RuntimeException ex) {
                showFileError("Open failed", ex.getMessage());
            }
        } else {
            // System.out.println("Tidak ada file dipilih.");
        }
    }


    private void handleButtonSave(ActionEvent actionEvent) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Save Environment");

        // Set initial directory "Documents"
        File documentsDir = new File(System.getProperty("user.home"), "Documents");
        if (documentsDir.exists()) {
            fileChooser.setInitialDirectory(documentsDir);
        }

        // Opsional: filter jenis file
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Data", "*.sc"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showSaveDialog(primaryStage);
        if (selectedFile != null) {
            // System.out.println("Selected File: " + selectedFile.getAbsolutePath());
            try {
                if (!selectedFile.getName().toLowerCase().endsWith(".sc"))
                    selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".sc");
                canvas.writeToFile(selectedFile);
                currentMapName = selectedFile.getName();
                statusLabel.setText("Saved: " + selectedFile.getName());
                checkState();
            } catch (RuntimeException ex) {
                showFileError("Save failed", ex.getMessage());
            }
        } else {
            // System.out.println("Tidak ada file dipilih.");
        }
    }

    private void handleButtonRun(ActionEvent actionEvent) {
        canvas.setState(State.DEFAULT);
        checkState();
        clearSimulationLog();
        logArea.appendText("Running Greedy Best-First Search...\n");
        try {
            canvas.run();
            logArea.setText(buildSimulationReport());
            logArea.positionCaret(0);
            statusLabel.setText(canvas.hasReplay() ? "Greedy path calculated — replay playing" : "Greedy path planning completed — " + canvas.getLastPathPlanning().getTerminationReason());
        } catch (RuntimeException ex) {
            logArea.appendText("SIMULATION ERROR: " + ex.getMessage() + "\n");
            showFileError("Simulation failed", ex.getMessage());
        }
    }

    private void handleButtonPlayPause(ActionEvent actionEvent) {
        canvas.setState(State.DEFAULT);
        checkState();
        if (!canvas.hasReplay()) {
            statusLabel.setText("Run the simulation first.");
            return;
        }
        canvas.togglePlayPause();
        statusLabel.setText(canvas.isPlaybackPlaying() ? "Playing" : "Paused");
    }

    private void handleButtonReplay(ActionEvent actionEvent) {
        canvas.setState(State.DEFAULT);
        checkState();
        if (!canvas.hasReplay()) {
            statusLabel.setText("Run the simulation first.");
            return;
        }
        canvas.replay();
        statusLabel.setText("Replay from timestep 0");
    }

    private void showFileError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
        statusLabel.setText(title);
    }

    private void handleButtonResetEnvironment(ActionEvent actionEvent) {
        canvas.resetEnvironment();
        currentMapName = "Untitled (in-memory environment)";
        clearSimulationLog();
        logArea.appendText("Environment reset. Ready for a new simulation.\n");
        checkState();
    }

    private void copySimulationLog() {
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(logArea.getText());
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
    }

    private void saveSimulationLog() {
        FileChooser logFileChooser = new FileChooser();
        logFileChooser.setTitle("Save Drone Path Planning Log");
        logFileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt")
        );

        String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        );
        logFileChooser.setInitialFileName("DronePathPlanningLog" + timestamp + ".txt");

        File documentsDir = new File(System.getProperty("user.home"), "Documents");
        if (documentsDir.isDirectory()) {
            logFileChooser.setInitialDirectory(documentsDir);
        }

        File selectedFile = logFileChooser.showSaveDialog(primaryStage);
        if (selectedFile == null) return;

        if (!selectedFile.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".txt");
        }

        try {
            java.nio.file.Files.writeString(
                    selectedFile.toPath(),
                    logArea.getText(),
                    java.nio.charset.StandardCharsets.UTF_8
            );
            statusLabel.setText("Log saved: " + selectedFile.getName());
        } catch (java.io.IOException ex) {
            showFileError("Save Log failed", ex.getMessage());
        }
    }

    private void clearSimulationLog() {
        if (logArea != null) logArea.clear();
    }


    // Presentation-only report: reads existing getters and the stored replay paths.
    private String buildSimulationReport() {
        DronePathPlanning result = canvas.getLastPathPlanning();
        if (result == null) return "No path planning session available.\n";
        return "DRONE PATH PLANNING - GREEDY BEST-FIRST SEARCH\n"
                + "Map file : " + currentMapName + "\n"
                + "Grid size: " + canvas.getGridRows() + " x " + canvas.getGridCols() + " cells\n"
                + "Start    : " + result.getStart() + " [zero-based]\n"
                + "Finish   : " + result.getFinish() + " [zero-based]\n"
                + "Planner  : Greedy Best-First Search (8-connected)\n"
                + "Status   : " + result.getTerminationReason() + "\n"
                + "Path cost: " + (result.isSuccess() ? String.format(Locale.ROOT, "%.4f", result.getPathCost()) : "N/A") + "\n"
                + "Steps    : " + result.getSteps() + "\n"
                + "Expanded : " + result.getExpandedNodes() + "\n"
                + "Time (ms): " + String.format(Locale.ROOT, "%.3f", result.getElapsedNanos() / 1_000_000.0) + "\n";
    }

    private void checkState() {
        if (canvas.getState().equals(State.DEFAULT)) {
            statelabel.setText("STATE: " + "FREE TRANSFORM" + "\t-\t");
            btnTransform.setSelected(true);
        } else if (canvas.getState().equals(State.OBSTACLE)) {
            statelabel.setText("STATE: " + "DRAW OBSTACLE" + "\t-\t");
            btnWall.setSelected(true);
        } else if (canvas.getState().equals(State.LINE)) {
            statelabel.setText("STATE: " + "DRAW LINE" + "\t-\t");
            btnDrawLine.setSelected(true);
        } else if (canvas.getState().equals(State.SET_FINISH)) {
            statelabel.setText("STATE: SET FINISH\t-\t");
            btnSetFinish.setSelected(true);
        } else if (canvas.getState().equals(State.REMOVE_LINE)) {
            statelabel.setText("STATE: " + "REMOVE LINE" + "\t-\t");
            btnRemoveLine.setSelected(true);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // === Sidebar ===
        VBox sidebar = new VBox(2);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(4));

        // button
        initializeToggleGroup();
        // set on action for buttons
        btnOpen.setOnAction(this::handleButtonOpen);
        btnSave.setOnAction(this::handleButtonSave);
        btnRun.setOnAction(this::handleButtonRun);
        btnPlayPause.setOnAction(this::handleButtonPlayPause);
        btnReplay.setOnAction(this::handleButtonReplay);
        btnNew.setOnAction(this::handleButtonResetEnvironment);
        VBox topButtons = new VBox(2, btnNew, btnOpen, btnTransform, btnWall, btnDrawLine, btnRemoveLine, btnSetFinish, btnSave, btnRun, btnPlayPause, btnReplay);
        topButtons.setAlignment(Pos.TOP_CENTER);

        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setVisible(false);

        VBox bottomButtons = new VBox(2, progressIndicator);
        bottomButtons.setAlignment(Pos.BOTTOM_CENTER);
        VBox.setVgrow(topButtons, Priority.ALWAYS);
        sidebar.getChildren().addAll(topButtons, bottomButtons);

        // === Center Pane ===
        BorderPane centerPane = new BorderPane();
        centerPane.getStyleClass().add("center-pane");
        StackPane canvasPane = new StackPane();

        getCanvas();
        canvas.render();
        canvasPane.getChildren().add(canvas);

        // Scroll Pane
        ScrollPane scrollPane = new ScrollPane(canvasPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setPrefHeight(900);

        // === Log Area ===

        // === Log Area ===
        logArea = new TextArea();
        logArea.setPrefRowCount(3);
        logArea.setWrapText(false);
        logArea.setEditable(false);
        logArea.setBorder(null);
        logArea.setText("Drone Path Planning in Grid-Based Environments\n");

        MenuItem copyLogItem = new MenuItem("Copy All", createMenuIcon(COPY_LOG_ICON));
        copyLogItem.setOnAction(e -> copySimulationLog());
        MenuItem saveLogItem = new MenuItem("Save Log", createMenuIcon(SAVE_LOG_ICON));
        saveLogItem.setOnAction(e -> saveSimulationLog());
        MenuItem clearLogItem = new MenuItem("Clear Log", createMenuIcon(CLEAR_LOG_ICON));
        clearLogItem.setOnAction(e -> clearSimulationLog());
        logArea.setContextMenu(new ContextMenu(copyLogItem, saveLogItem, clearLogItem));


        // === Status Bar ===
        statusLabel = new Label("Ready.");
        statusLabel.getStyleClass().add("status-label");
        statelabel = new Label("STATE: " + canvas.getState().toString() + "\t-\t");
        statelabel.getStyleClass().add("status-label");
        HBox statusBar = new HBox(statelabel, statusLabel);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5));

        // SplitPane vertikal
        SplitPane splitPane = new SplitPane(scrollPane, logArea);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.90);

        // === Bottom (log + status) ===
        VBox bottomBox = new VBox(statusBar);

        centerPane.setCenter(splitPane);
        centerPane.setBottom(bottomBox);

        // === Layout ===
        BorderPane root = new BorderPane();
        //root.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(centerPane);
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(CSS.style);

        /* STRART the ENGINE */
        AnimationTimer engine = new AnimationTimer() {
            @Override
            public void handle(long l) {
                // UPDATE
                canvas.update();

                // RENDER
                canvas.render();
            }
        };
        engine.start();

        // stage
        primaryStage.setTitle("Informatics Department - Engineering Faculty - Universitas Sulawesi Barat");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
    }
}