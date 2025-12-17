package project.go.connection;

import project.go.game.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MultiThread extends Thread {

    private static final Board GAME_BOARD;
    private static final Logic GAME_LOGIC;

    static {
        GAME_BOARD = new Board();
        GAME_LOGIC = new Logic(GAME_BOARD);
    }

    private static StoneColour currentPlayer = StoneColour.BLACK;
    private static int connectionCounter = 0;
    private static final List<MultiThread> CLIENT_THREADS = new ArrayList<>();

    private final StoneColour playerColour;
    private final Socket socket;
    private PrintWriter out;

    public MultiThread(Socket socket) {

        this.socket = socket;
        connectionCounter++;
        this.playerColour = (connectionCounter % 2 == 1) ? StoneColour.BLACK : StoneColour.WHITE;

        synchronized (CLIENT_THREADS) {
            CLIENT_THREADS.add(this);
        }
    }

    private void broadcastMove(int captured) {
        String boardState = GAME_BOARD.toCompactString();

        System.out.println("Aktualny stan planszy:\n" + GAME_BOARD.toDebugString());

        synchronized (CLIENT_THREADS) {
            for (MultiThread client : CLIENT_THREADS) {
                client.out.println("Sukces " + captured + " " + boardState);

                if (currentPlayer == client.playerColour) {
                    client.out.println("Status Twoja_Kolej");
                } else {
                    client.out.println("Status Czekaj");
                }
            }
        }
    }

    private synchronized void handleMove(String clientInput) {
        if (currentPlayer != playerColour) {
            out.println("Błąd poczekaj na swoją turę.");
            return;
        }

        try {
            String[] coords = clientInput.substring(5).trim().split(",");
            int x = Integer.parseInt(coords[0].trim()) - 1;
            int y = Integer.parseInt(coords[1].trim()) - 1;

            if (x < 0 || x >= Board.SIZE || y < 0 || y >= Board.SIZE) {
                out.println("Błąd Poza planszą! Zakres to 1-" + Board.SIZE);
                return;
            }

            PlacementResult result = GAME_LOGIC.placeStone(new Move(x, y, playerColour));
            if (result.success()) {
                currentPlayer = (playerColour == StoneColour.BLACK) ? StoneColour.WHITE : StoneColour.BLACK;
                broadcastMove(result.capturedCount());
            }
            else {
                out.println("Błąd " + result.message());
            }

        } catch (Exception e) {
            out.println("Błąd Nieprawidłowe współrzędne.");
        }
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Witam " + playerColour + " Plansza " + GAME_BOARD.toCompactString());

            while (true) {
                if (currentPlayer == playerColour) {
                    out.println("Status Twoja_Kolej");
                } else {
                    out.println("Status Czekaj");
                }

                String clientMessage = in.readLine();
                if (clientMessage == null) break;

                handleMove(clientMessage);
            }
        }
        catch (IOException ignored) {}
        finally {
            System.out.println("Zawodnik " + playerColour + " opuścił grę.");

            synchronized (CLIENT_THREADS) {
                CLIENT_THREADS.remove(this);
            }
            try {
                socket.close();
            }
            catch (IOException ignored) {}
        }
    }
}