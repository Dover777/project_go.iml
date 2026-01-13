    package project.go.connection;

    import java.net.*;
    import java.io.*;
    import project.go.game.StoneColour;
    import project.go.game.Board;

    public class GoClient {

        private static void displayBoard(Board board, String compactString, StoneColour myColour) {
            board.fromCompactString(compactString);
            System.out.println("\nAktualny stan planszy (Twoj kolor: " + myColour + ")");
            System.out.println(board.toDebugString());
        }

        private static void processServerResponse(String response, Board board, StoneColour myColour) {
            if (response.startsWith("Sukces")) {
                String[] parts = response.split(" ");
                if (parts.length < 2) return;

                int captured = Integer.parseInt(parts[1]);
                String newBoardState = parts[2];

                if (captured == -1){
                    System.out.println("Sukces! Ruch spasowany.");
                }
                else if (captured == 1) {
                    System.out.println("Sukces! Zdobyłeś " + captured + " kamień.");
                }
                else if (captured > 2 && captured < 5){
                    System.out.println("Sukces! Zdobyłeś " + captured + " kamienie.");
                }
                else if (captured > 4){
                    System.out.println("Sukces! Zdobyłeś " + captured + " kamieni.");
                }
                else {
                    System.out.println("Sukces! Ruch postawiony.");
                }
                displayBoard(board, newBoardState, myColour);

            }
            else if (response.startsWith("Błąd")) {
                System.err.println("Błąd: " + response.substring(5));
            }
            else if (response.startsWith("Koniec")) {
                System.out.println("Serwer zakończył połączenie.");
            }
            else if (response.startsWith("Ukończono Mecz")) {
                String[] score = response.split(" ");
                if (score.length >= 4) {
                    String blackPoints = score[2];
                    String whitePoints = score[3];

                    System.out.println("\nUkończono Mecz");
                    System.out.println("Punkty Czarnego: " + blackPoints);
                    System.out.println("Punkty Białego: " + whitePoints);

                    int bPoints = Integer.parseInt(blackPoints);
                    int wPoints = Integer.parseInt(whitePoints);
                    if (bPoints > wPoints) {
                        System.out.println("Wygrał Czarny.");
                    }
                    else if (bPoints < wPoints) {
                        System.out.println("Wygrał Biały.");
                    }
                    else {
                        System.out.print("Remis.\n");
                    }
                }
            }
            else if (response.startsWith("Poddanie ")){
                String[] score = response.split(" ");
                if (score.length >= 3) {
                    String loser = score[1];
                    String winner = score[2];
                    System.out.println("\n" + loser + " poddał partię. " + winner + " wygrał.");
                }
            }
            else if (response.contains(",") && response.length() > 5) {
                displayBoard(board, response.split(" ")[0], myColour);
            }
        }

        public static void main(String[] args) {
            try (
                    Socket socket = new Socket("localhost", 4444);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))
            ) {
                StoneColour myColour;
                String serverResponse;
                String clientInput;

                serverResponse = in.readLine();
                if (serverResponse == null || !serverResponse.startsWith("Witam")) {
                    System.out.println("Błąd inicjalizacji serwera.");
                    return;
                }

                String[] initParts = serverResponse.split(" ");
                myColour = StoneColour.valueOf(initParts[1]);
                Board clientBoard = new Board();
                displayBoard(clientBoard, initParts[3], myColour);

                System.out.println("Witam");
                System.out.println("Przyznany kolor: " + myColour);

                System.out.println("Oczekiwanie na przeciwnika...");

                while (true) {
                    serverResponse = in.readLine();
                    if (serverResponse == null) break;

                    processServerResponse(serverResponse, clientBoard, myColour);
                    if (serverResponse.startsWith("Status Koniec") ||
                            serverResponse.startsWith("Ukończono Mecz") ||
                            serverResponse.startsWith("Poddanie ")) {

                        System.out.println("Aplikacja zostanie zamknięta.");
                        break;
                    }

                    if (serverResponse.startsWith("Status Twoja_Kolej")) {
                        System.out.println("\nTwoja kolej (" + myColour + ")");
                        System.out.print("Wpisz ruch: x,y lub 'pass' ewentualnie 'ff' aby się poddać: ");
                        clientInput = consoleReader.readLine();
                        if (clientInput == null) break;

                        String trimmedInput = clientInput.trim();
                        if (clientInput.equalsIgnoreCase("pass")) {
                            out.println("pass");
                        }
                        else if (clientInput.equalsIgnoreCase("ff")) {
                            out.println("ff");
                        }
                        else {
                            out.println("Ruch " + trimmedInput);
                        }
                    }
                    else if (serverResponse.startsWith("Status Czekaj")) {
                        System.out.println("Oczekiwanie na ruch przeciwnika...");
                    }
                }

            }
            catch (UnknownHostException ex) {
                System.err.println("Serwer nie znaleziony: " + ex.getMessage());
            }
            catch (IOException ex) {
                System.err.println("I/O error: " + ex.getMessage());
            }
        }
    }