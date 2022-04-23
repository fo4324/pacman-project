import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * A server for the PACMAN final Project for Computational Problem Solving in the information domain II
 * @author F. Orlandini
 * @version 14042022
 * @ASSESSME.INTENSITY:LOW
 */

public class Server extends Application {
    
    // Server Attributes
    private final int SERVER_PORT = 36912;
    private Thread server;
    private ServerSocket serverSocket = null;
    private ArrayList<ClientHandler> clients = new ArrayList<>();

    // GUI Attributes
    private VBox root = new VBox(10);
    private Button btStart = new Button("Start");
    private TextArea taLog = new TextArea();
    private TextArea taClients = new TextArea();

    // ** MAIN **
    public static final void main(String[] args) {
        launch(args);
    }

    // Launching the Application
    public void start(Stage stage) {
        // Application Title
        stage.setTitle("PACMAN Server");

        // Handling the window close
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.exit(0);
            }
        });

        // GUI Layout
        root.getChildren().addAll(btStart, new Label("Connected clients:"), taClients, new Label("Server log:"), taLog);
        root.setAlignment(Pos.CENTER);

        taClients.setPrefHeight(150);
        taClients.setEditable(false);

        taLog.setPrefHeight(225);
        taLog.setEditable(false);

        stage.setResizable(false);
        stage.setX(0);
        stage.setY(0);

        // Window Icon
        stage.getIcons().add(new Image("logo.png"));

        // Showing the Application
        stage.setScene(new Scene(root, 500, 500));
        stage.show();

        // Button Actions
        btStart.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent ae) {
                server = new Thread() {
                    public void run() {
                        startServer();
                    }
                };
                server.start();
            }
        });

    }

    // Method for starting the server
    public void startServer() {
        btStart.setDisable(true);
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            taLog.appendText("Server started.\n");
    
            while(true) {
                Socket clientSocket = serverSocket.accept();
                Thread client = new ClientHandler(clientSocket);
                taLog.appendText(clientSocket.getInetAddress() + " connected.\n");
                client.start();
            }
        } catch(IOException ioe) {
            taLog.appendText("ERROR - Cannot start server!\n");
            ioe.printStackTrace();
        }
    }

    // Client Thread class
    class ClientHandler extends Thread {

        // Client Thread Attributes
        private Socket clientSocket;
        private Scanner in;
        private PrintWriter out;
        private String name;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            clients.add(this);
        }

        public void run() {
            try {
                this.in = new Scanner(this.clientSocket.getInputStream());
                this.out = new PrintWriter(this.clientSocket.getOutputStream());
                while(this.clientSocket.isConnected()) {
                    if(this.clientSocket.isClosed()) {
                        break;
                    } else {
                        String[] cmd = in.nextLine().split("@");
                        switch(cmd[0]) {
                            case "LOGIN":
                                doLogin(cmd[1]);
                                break;
                            case "LOGOUT":
                                doLogout();
                                break;
                            case "MESSAGE":
                                doMessage(cmd[1]);
                                break;
                        }
                    }
                }
            } catch(IOException ioe) {
                taLog.appendText("ERROR - Cannot open input and output streams for " + this.clientSocket.getInetAddress() + "\n");
                ioe.printStackTrace();
            }
        }

        // Method for client login
        public void doLogin(String text) {
            this.name = text;
            taLog.appendText(this.clientSocket.getInetAddress() + " logged in as: " + this.name + "\n");
            flushToAll("MESSAGE@" + this.name + " logged in");
            listClients();
        }

        // Method for client logout
        public void doLogout() {
            clients.remove(this);
            String line = this.name + " logged out";
            flushToAll("MESSAGE@" + line);
            taLog.appendText(line + "\n");
            listClients();
            this.in.close();
            this.out.close();
            try {
                this.clientSocket.close();
            } catch (IOException ioe) {
                taLog.appendText("ERROR - Cannot close " + this.clientSocket.getInetAddress() + "\n");
                ioe.printStackTrace();
            }
        }

        // Method for chatting between clients and the server
        public void doMessage(String message) {
            taLog.appendText(this.name + ": " + message + "\n");
            flushToAll("MESSAGE@" + this.name + ": " + message);
        }

        // Method for flushing to all connected clients
        public void flushToAll(String line) {
            new Thread(new Runnable() {
                public void run() {
                    for(ClientHandler client : clients) {
                        client.out.println(line);
                        client.out.flush();
                    }
                }
            }).start();
        }

        // Method for maintaining the connected client list
        public void listClients() {
            new Thread(new Runnable() {
                public void run() {
                    taClients.clear();
                    for(ClientHandler client : clients) {
                        for(int i = 0; i < clients.size(); i++) {
                            client.out.println("ADD@" + clients.get(i).name);
                            client.out.flush();
                        }
                        taClients.appendText(client.name + "\n");
                    }
                }
            }).start();
        }

    }

}