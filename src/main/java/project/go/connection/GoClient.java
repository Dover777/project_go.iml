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

                if (captured == 1) {
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
                System.err.println("Ruch Błąd: " + response.substring(5));
            }
            else if (response.startsWith("Koniec")) {
                System.out.println("Serwer zakończył połączenie.");
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

                    if (serverResponse.startsWith("Status Twoja_Kolej")) {
                        System.out.println("\nTwoja kolej (" + myColour + ")");
                        System.out.print("Wpisz ruch: x,y: ");
                        clientInput = consoleReader.readLine();
                        if (clientInput == null) break;

                        String commandToSend = "MOVE " + clientInput.trim();
                        out.println(commandToSend);
                    }
                    else if (serverResponse.startsWith("Status Czekaj")) {
                        System.out.println("Oczekiwanie na ruch przeciwnika...");
                    }
                    else {
                        processServerResponse(serverResponse, clientBoard, myColour);
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