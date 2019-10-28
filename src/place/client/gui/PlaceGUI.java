package place.client.gui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PlaceGUI extends Application implements Observer {
    /**
     * DIM OF THE BOARD
     */
    int DIM;

    /**
     * The PlaceBoard that the GUI is based off of
     */
    private PlaceBoard board;

    /**
     * The ToggleButtons that allow for the user to select a color.
     */
    private ToggleButton[] buttons = new ToggleButton[PlaceColor.TOTAL_COLORS];

    /**
     * The clickable rectangles that store the PlaceTiles
     */
    private Rectangle[][] colorCells;

    /**
     * The model that the board is based off of
     */
    private ClientModel model;

    /**
     * The GridPane that contains all the rectangles
     */
    private GridPane center;

    /**
     * The array of PlaceColors used to color the rectangles
     */
    private PlaceColor[] colors = PlaceColor.values();

    /**
     * The NetworkClient that communicates with the server.
     */
    private NetworkClient serverConn;

    /**
     * The username used to connect to the server.
     */
    String username;
    /**
     * The color that the user is currently using
     */
    private PlaceColor colorPlace = PlaceColor.BLACK;

    /**
     * The current color
     */
    private int COLOR = -1;

    /**
     * A switch used to disable the GUI and keep the user from spamming the server.
     */
    boolean disable = false;


    /**
     * initializes the GUI and connects to the server.
      * @throws PlaceException
     */
    public void init() throws PlaceException{
        try {
            List<String> args = getParameters().getRaw();

            // Get host info from command line
            String host = args.get( 0 );
            int port = Integer.parseInt( args.get( 1 ) );
            username = args.get(2);

            // Create the network connection.
            this.serverConn = new NetworkClient( host, port, username );

            model = serverConn.getModel();
            board = model.getBoard();
            DIM = board.DIM;
        }
        catch( PlaceException |
                ArrayIndexOutOfBoundsException |
                NumberFormatException e ) {
            System.out.println( e );
            throw new RuntimeException( e );
        }

    }

    /**
     *Starts the program by creating the stage and telling the processor that this program will observe
     * ClientModel
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        model.addObserver(this);

        //creates the mainPane and its size
        BorderPane mainPane = new BorderPane();
        mainPane.setPrefSize(500, 540);

        center = new GridPane();
        center.setPrefSize(500, 50);

        GridPane bottomPane = new GridPane();
        ToggleGroup tg = new ToggleGroup();

        //creates the colors to choose from
        for(int i = 0; i < PlaceColor.TOTAL_COLORS; i++){
            ToggleButton button = new ToggleButton();

            button.setStyle("-fx-background-color: " + colors[i].getName());
            button.setPrefWidth(500/PlaceColor.TOTAL_COLORS);
            button.setPrefHeight(30);
            int num = i;
            if (i < 10)
                button.setText(Integer.toString(i));
            else
            {
                switch (i){
                    case 10: button.setText("A");
                            break;
                    case 11: button.setText("B");
                             break;
                    case 12: button.setText("C");
                             break;
                    case 13: button.setText("D");
                             break;
                    case 14: button.setText("E");
                             break;
                    case 15: button.setText("F");
                }
            }
            button.setOnAction(e -> {
                colorClick(colors[num].getName(), button);
            });

            bottomPane.add(button, i, DIM + 1);
            buttons[i] = button;
            button.setToggleGroup(tg);
        }
        colorCells = new Rectangle[DIM][DIM];
        for(int r = 0; r < DIM; r++){
            for(int c = 0; c < DIM; c++){
                Rectangle tileDis = new Rectangle();
                tileDis.setWidth(500/DIM);
                tileDis.setHeight(500/DIM);
                colorCells[r][c] = tileDis;
                PlaceTile tile = board.getTile(c, r);
                updateColor(tile, tileDis);
                center.add(tileDis, r, c);
                Tooltip t = new Tooltip(tooltipCreator(tile));
                Tooltip.install(tileDis, t);
                tileDis.setOnMouseClicked(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent t) {
                        centerClick(tileDis);
                    }
                });
            }
        }


        mainPane.setBottom(bottomPane);
        mainPane.setCenter(center);

        Scene scene = new Scene(mainPane);
        primaryStage.setTitle("Place");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    /**
     * A helper method that will select which color will fill some rectangle, b.
     * @param color
     * @param b
     */
    public void determineColor (PlaceColor color, Rectangle b) {
        switch (color) {
            case BLACK:
                b.setFill(Color.BLACK);
                break;
            case GRAY:
                b.setFill(Color.GRAY);
                break;
            case SILVER:
                b.setFill(Color.SILVER);
                break;
            case WHITE:
                b.setFill(Color.WHITE);
                break;
            case MAROON:
                b.setFill(Color.MAROON);
                break;
            case RED:
                b.setFill(Color.RED);
                break;
            case OLIVE:
                b.setFill(Color.OLIVE);
                break;
            case YELLOW:
                b.setFill(Color.YELLOW);
                break;
            case GREEN:
                b.setFill(Color.GREEN);
                break;
            case LIME:
                b.setFill(Color.LIME);
                break;
            case TEAL:
                b.setFill(Color.TEAL);
                break;
            case AQUA:
                b.setFill(Color.AQUA);
                break;
            case NAVY:
                b.setFill(Color.NAVY);
                break;
            case BLUE:
                b.setFill(Color.BLUE);
                break;
            case PURPLE:
                b.setFill(Color.PURPLE);
                break;
            case FUCHSIA:
                b.setFill(Color.FUCHSIA);
                break;
        }
    }

    /**
     * The method used to click on the rectangles, and send the request to the server.
     * @param b
     */
    public void centerClick(Rectangle b){
        if(!disable){

            String s = "" + colorPlace;
            serverConn.changeTile(center.getRowIndex(b), center.getColumnIndex(b), username,
                    s);
            disableGUI();
        }
    }


    /**
     * A helper method that creates the tooltips for each rectangle
     * @param tile
     * @return the string to be used in the tooltip containing the coordinate, the time, and the creator
     */
    private String tooltipCreator(PlaceTile tile) {
        String s = "(";
        int row = tile.getRow();
        int col = tile.getCol();
        s += row + ", " + col + ") \n";
        s += tile.getOwner() + "\n";
        s += DateFormat.getDateInstance().format(tile.getTime()) + "\n";
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("EST"));
        s += formatter.format(tile.getTime());
        return s;
    }

    /**
     * A helper method that will disable the GUI upon one click to prevent spamming the server.
     */
    public void disableGUI (){
        disable = true;
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        disable = false;
                    }
                },
                1000
        );
    }

    /**
     * A click method that allows the user to choose the current color based on the toggle buttons
     * @param colorName
     * @param button
     */
    public void colorClick(String colorName, ToggleButton button){
        String color = colorName;
        for(int i = 0; i < PlaceColor.TOTAL_COLORS; i++){
            buttons[i].setPrefHeight(30);
        }
        switch( color ){
            case("black"):
                COLOR = 0;
                colorPlace = colors[0];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("gray"):
                COLOR = 1;
                colorPlace = colors[1];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("silver"):
                COLOR = 2;
                colorPlace = colors[2];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("white"):
                COLOR = 3;
                colorPlace = colors[3];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("maroon"):
                COLOR = 4;
                colorPlace = colors[4];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("red"):
                COLOR = 5;
                colorPlace = colors[5];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("olive"):
                COLOR = 6;
                colorPlace = colors[6];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("yellow"):
                COLOR = 7;
                colorPlace = colors[7];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("green"):
                COLOR = 8;
                colorPlace = colors[8];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("lime"):
                COLOR = 9;
                colorPlace = colors[9];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("teal"):
                COLOR = 10;
                colorPlace = colors[10];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("aqua"):
                COLOR = 11;
                colorPlace = colors[11];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("navy"):
                COLOR = 12;
                colorPlace = colors[12];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("blue"):
                COLOR = 13;
                colorPlace = colors[13];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("purple"):
                COLOR = 14;
                colorPlace = colors[14];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
            case("fuchsia"):
                COLOR = 15;
                colorPlace = colors[15];
                System.out.println(colorPlace.getName());
                button.setPrefHeight(40);
                break;
        }

    }

    /**
     * Updates the colors of the rectangles based on the model, given the tile sent in from the server
     * @param tile
     * @param b
     */
    private void updateColor(PlaceTile tile, Rectangle b ){
       PlaceColor color = tile.getColor();
       determineColor(color, b);
        Tooltip t = new Tooltip(tooltipCreator(tile));
        Tooltip.install(b, t);
       b.setStrokeType(StrokeType.INSIDE);
    }

    /**
     * Refreshes each tile to reflect the model
     */
    private void refresh() {
        for (int x =0 ; x < DIM; x++) {
            for (int y =0 ; y < DIM; y++) {
                PlaceTile tile = board.getTile(x, y);
                updateColor(tile, colorCells[y][x]);
            }
        }

    }

    /**
     * An update method to be called whenever the model is updated
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        assert o.equals(this.model);
        javafx.application.Platform.runLater(this::refresh);

    }

    /**
     * A main method that checks the number of arguments.
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceGUI host port username");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }
}
