package project.go.connection;

import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import project.go.game.Board;
import project.go.game.StoneColour;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class GoBot extends Application {
    private final Board botBoard = new Board();
    private final Random random = new Random();

    private PrintWriter out;
    private BufferedReader in;

    private int attempts = 0;

    @Override
    public void start(Stage primaryStage) {
        new Thread(this::setupNetwork).start();
    }


    private void setupNetwork() {
        try {
            Socket socket = new Socket("localhost", 4444);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String welcome = in.readLine();
            if (welcome != null && welcome.startsWith("Witam")) {
                String[] parts = welcome.split(" ");

                StoneColour myColour = StoneColour.valueOf(parts[1]);
                System.out.println("GoBot uruchomiony jako " + myColour);

                botBoard.fromCompactString(parts[3]);

                listenToServer();
            }
        }
        catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        }
    }

    private void listenToServer() throws IOException {
        String response;

        while ((response = in.readLine()) != null) {
            processResponse(response);

            if (response.startsWith("Status Koniec") || response.startsWith("Ukończono Mecz")) {
                break;
            }
        }

        System.out.println("Bot kończy pracę.");
        System.exit(0);
    }

    private void processResponse(String response) {
        if (response.startsWith("Sukces")) {
            String[] parts = response.split(" ");

            if (parts.length >= 3) {
                botBoard.fromCompactString(parts[2]);
            }

            if (attempts > 0) {
                System.out.println("Bot wykonał ruch pomyślnie.");
                attempts = 0;
            }
        }
        else if (response.startsWith("Status Twoja_Kolej")) {
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException ignored) {}

            makeRandomMove();
        }
    }

    private void makeRandomMove(){
        int size = botBoard.getSize();
        double maxAttempts = size * size * Math.log(size * size); // n^2 * ln(n^2)

        int x = -1, y = -1;

        while (x == - 1 || botBoard.getColourAt(x, y) != StoneColour.EMPTY) {
            x = random.nextInt(size);
            y = random.nextInt(size);
            attempts++;

            if (attempts > maxAttempts) {
                System.out.println("Bot pasuje.");
                out.println("pass");
                attempts = 0;
                return;
            }

        }
        System.out.println("Próba " + attempts + " Bot próbuje wykonać ruch na: " + (x + 1) + "," + (y + 1));
        out.println("Ruch " + (x + 1) + "," + (y + 1));
    }

    public static void main(String[] args) {
        launch(args);
    }
}