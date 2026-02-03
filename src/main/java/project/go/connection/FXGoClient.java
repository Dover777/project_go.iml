package project.go.connection;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import project.go.game.Board;
import project.go.game.StoneColour;

import java.io.*;
import java.net.Socket;

public class FXGoClient extends Application {
    private static final int TILE_SIZE = 40;
    private static final int BOARD_SIZE = Board.SIZE;

    private PrintWriter out;
    private BufferedReader in;
    private StoneColour myColour;

    private final Board clientBoard = new Board();
    private final StackPane[][] tiles = new StackPane[BOARD_SIZE][BOARD_SIZE];
    private final Label statusLabel = new Label("Łączenie z serwerem...");
    private final Label colorLabel = new Label("Czekaj...");
    private final Button passButton = new Button("Pasuj");
    private final Button ffButton = new Button("Poddaj się (FF)");
    private boolean myTurn = false;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #C0C0C0;");

        GridPane boardGrid = createBoardGrid();
        root.setCenter(boardGrid);

        VBox controls = new VBox(15);
        controls.setPadding(new Insets(0, 0, 0, 20));
        controls.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("TWÓJ KOLOR:");
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
        colorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #000000;");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        passButton.setDisable(true);
        ffButton.setDisable(true);
        passButton.setMinWidth(120);
        ffButton.setMinWidth(120);

        passButton.setOnAction(e -> sendMove("pass"));
        ffButton.setOnAction(e -> sendMove("ff"));

        controls.getChildren().addAll(titleLabel, colorLabel, new Separator(), statusLabel, passButton, ffButton);
        root.setRight(controls);

        new Thread(this::setupNetwork).start();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Gra Go");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    private GridPane createBoardGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane tile = new StackPane();
                tile.setPrefSize(TILE_SIZE, TILE_SIZE);

                Line vLine = new Line(TILE_SIZE / 2.0, 0, TILE_SIZE / 2.0, TILE_SIZE);
                vLine.setStroke(Color.web("#404040"));
                Line hLine = new Line(0, TILE_SIZE / 2.0, TILE_SIZE, TILE_SIZE / 2.0);
                hLine.setStroke(Color.web("#404040"));

                tile.getChildren().addAll(vLine, hLine);

                final int r = row;
                final int c = col;
                tile.setOnMouseClicked(e -> handleTileClick(r, c));

                tiles[row][col] = tile;
                grid.add(tile, col, row);
            }
        }
        return grid;
    }

    private void handleTileClick(int row, int col) {
        if (myTurn) {
            sendMove((row + 1) + "," + (col + 1));
        }
    }

    private void sendMove(String move) {
        if (move.equals("pass") || move.equals("ff")) {
            out.println(move);
        }
        else {
            out.println("Ruch " + move);
        }
    }

    private void setupNetwork() {
        try {
            Socket socket = new Socket("localhost", 4444);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String welcome = in.readLine();
            if (welcome != null && welcome.startsWith("Witam")) {
                String[] parts = welcome.split(" ");
                myColour = StoneColour.valueOf(parts[1]);

                Platform.runLater(() -> {
                    colorLabel.setText(myColour.toString());
                    statusLabel.setText("Połączono");
                    ffButton.setDisable(false);
                    updateVisualBoard(parts[3]);
                });

                listenToServer();
            }
        }
        catch (IOException e) {
            Platform.runLater(() -> statusLabel.setText("Błąd serwera"));
        }
    }

    private void listenToServer() throws IOException {
        String response;
        while ((response = in.readLine()) != null) {
            final String res = response;
            Platform.runLater(() -> processResponse(res));
        }
    }

    private void processResponse(String response) {
        if (response.startsWith("Sukces")) {
            String[] parts = response.split(" ");
            updateVisualBoard(parts[2]);
        }
        else if (response.startsWith("Status Twoja_Kolej")) {
            myTurn = true;
            statusLabel.setText("Twoja Kolej");
            passButton.setDisable(false);
        }
        else if (response.startsWith("Status Czekaj")) {
            myTurn = false;
            statusLabel.setText("Ruch przeciwnika...");
            passButton.setDisable(true);
        }
        else if (response.startsWith("Błąd")) {
            showInfoAlert(response.substring(5));
        }
        else if (response.startsWith("Ukończono Mecz") || response.startsWith("Poddanie")) {
            statusLabel.setText("KONIEC GRY");
            showEndGameDialog(response);
        }
    }

    private void updateVisualBoard(String compactString) {
        clientBoard.fromCompactString(compactString);
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StoneColour color = clientBoard.getColourAt(row, col);
                drawStone(row, col, color);
            }
        }
    }

    private void drawStone(int row, int col, StoneColour color) {
        StackPane tile = tiles[row][col];
        tile.getChildren().removeIf(node -> node instanceof Circle);

        if (color != StoneColour.EMPTY) {
            Circle stone = new Circle(TILE_SIZE * 0.42);
            stone.setFill(color == StoneColour.BLACK ? Color.BLACK : Color.WHITE);
            stone.setStroke(Color.GRAY);
            tile.getChildren().add(stone);
        }
    }

    private void showInfoAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showEndGameDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Koniec meczu");
        alert.setHeaderText("Gra zakończona");

        if (msg.startsWith("Ukończono Mecz")) {
            String[] parts = msg.split(" ");
            if (parts.length >= 4) {
                String bPts = parts[2];
                String wPts = parts[3];
                String winner;

                double b = Double.parseDouble(bPts);
                double w = Double.parseDouble(wPts);

                if (b > w) winner = "CZARNY";
                else if (w > b) winner = "BIAŁY";
                else winner = "BRAK";

                alert.setContentText("WYNIKI:\n\nCzarny: " + bPts + " pkt\nBiały: " + wPts + " pkt\n\nZwycięzca: " + winner);
            } else {
                alert.setContentText(msg);
            }
        } else {
            alert.setContentText(msg);
        }

        alert.showAndWait();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}