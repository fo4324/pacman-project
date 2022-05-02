import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * A client for the PACMAN final project for Computational Problem Solving in the information domain II
 * @author F. Orlandini
 * @version 01052022
 * @ASSESSME.INTENSITY:LOW
 */

public class Game extends Application {

    // GUI Attributes
    private Group root;
    private Canvas canvas;
    private GraphicsContext gc;

    // Game Attributes
    private static final int WIDTH = 800;
    private static final int HEIGHT = WIDTH;
    private static final int ROWS = 80;
    private static final int COLUMNS = ROWS;
    private static final int SQUARE_SIZE = WIDTH / ROWS;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        stage.setTitle("PACMAN");

        root = new Group();

        canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);

        stage.setScene(new Scene(root));
        stage.show();

        gc = canvas.getGraphicsContext2D();
        drawScene();
        drawPacDots();
    }

    private void drawScene() {
        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < COLUMNS; j++) {
                gc.setFill(Color.BLACK);
                gc.fillRect(i * SQUARE_SIZE, j * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }

    private void drawPacDots() {
        int numOfDots = (int)((Math.random() * (50 - 25)) + 25);
        gc.setFill(Color.WHITE);
        for(int i = 0; i < numOfDots; i++) {
            int dotX = (int)(Math.random() * ROWS);
            int dotY = (int)(Math.random() * COLUMNS);
            gc.fillRect(dotX * SQUARE_SIZE, dotY * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
    }

}