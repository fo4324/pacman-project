import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * A client for the PACMAN final project for Computational Problem Solving in the information domain II
 * @author F. Orlandini
 * @version 20042022
 * @ASSESSME.INTENSITY:LOW
 */

public class Client extends Application {

    // Client Attributes
    private final int SERVER_PORT = 36912;
    private Scanner in = null;
    private PrintWriter out = null;
    private String name = "";
    private Socket socket = null;

    // GUI Attributes
    private VBox root = new VBox(10);
    private TextField tfInput = new TextField();
    private TextField tfConnect = new TextField();
    private Button btSend = new Button("Send");
    private Button btConnect = new Button("Connect");
    private TextArea taLog = new TextArea();
    private TextArea taClients = new TextArea();
    private HBox hbConnect = new HBox(10);
    private HBox hbInput = new HBox(10);

    // ** MAIN **
    public static void main(String[] args) {
        launch(args);
    }

    // Launching the Application
    public void start(Stage stage) {
        // Application title
        stage.setTitle("PACMAN Client");

        // Handling the window close
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                if(socket != null) {
                    out.println("LOGOUT@");
                    out.flush();
                }
                System.exit(0);
            }
        });

        // GUI Layout
        hbInput.getChildren().addAll(tfInput, btSend);
        hbInput.setAlignment(Pos.CENTER);

        hbConnect.getChildren().addAll(tfConnect, btConnect);
        hbConnect.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(hbConnect, taClients, taLog, hbInput);

        taClients.setEditable(false);
        taClients.setPrefHeight(150);

        taLog.setEditable(false);
        taLog.setPrefHeight(250);

        stage.setX(500);
        stage.setY(0);
        stage.setResizable(false);

        // Window Icon
        stage.getIcons().add(new Image("logo.png"));

        // Showing the Application
        stage.setScene(new Scene(root, 500, 500));
        stage.show();

        // Button Actions
        btConnect.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent ae) {
                doConnect();
            }
        });

        btSend.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent ae) {
                doSend();
            }
        });
    }

    // Method for connecting to the server
    public void doConnect() {
        if(!tfConnect.getText().equals("")) {
            try {
                socket = new Socket(tfConnect.getText(), SERVER_PORT);
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream());

                // A Text Input Dialog for the username
                TextInputDialog loginDialog = new TextInputDialog();
                loginDialog.setHeaderText("Enter your username: ");
                loginDialog.showAndWait();

                name = loginDialog.getEditor().getText();
                out.println("LOGIN@" + name);
                out.flush();
                taLog.appendText("Connected to the server as " + name + "\n");
                btConnect.setDisable(true);
                tfConnect.setDisable(true);
                receiveMessage();
            } catch(IOException ioe) {
                taLog.appendText("ERROR - Cannot connect to the server.\n");
            }
        } else {
            taLog.appendText("Please enter a valid server address.\n");
        }
    }

    // Method for receiving commands and messages from the server
    public void receiveMessage() {
        new Thread(new Runnable() {
            public void run() {
                while(in.hasNextLine()) {
                    String line = in.nextLine();
                    String[] cmd = line.split("@");
                    switch(cmd[0]) {
                        case "ADD":
                            taClients.clear();
                            doAdd(cmd[1]);
                            break;
                        case "MESSAGE":
                            doReceive(cmd[1]);
                            break;
                    }
                }
            }
        }).start();
    }

    // Method for sending messages to the server
    public void doSend() {
        String text = tfInput.getText();
        if(!text.equals("")) {
            out.println("MESSAGE@" + text);
            out.flush();
        }
        tfInput.clear();
    }

    // Methods for listing all the connected clients
    public void doAdd(String name) {
        new Thread(new Runnable() {
            public void run() {
                taClients.appendText(name + "\n");
            }
        }).start();
    }

    // Method for receiving a message
    public void doReceive(String message) {
        taLog.appendText(message + "\n");
    }

}