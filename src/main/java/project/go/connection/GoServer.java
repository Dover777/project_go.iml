package project.go.connection;

import java.io.*;
import java.net.*;

public class GoServer {

    private static final int PORT = 4444;
    public static boolean isRunning = true;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Go Server is listening on port " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                new MultiThread(clientSocket).start();
            }

        } catch (IOException ex) {
            System.err.println("Server exception: " + ex.getMessage());
        }
    }
}