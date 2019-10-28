package place.client.network;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.client.model.ClientModel;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;



/**
 * The client side network interface to a Reversi game server.
 * Each of the two players in a game gets its own connection to the server.
 * This class represents the controller part of a model-view-controller
 * triumvirate, in that part of its purpose is to forward user actions
 * to the remote server.
 *
 * @author Robert St Jacques @ RIT SE
 * @author Sean Strout @ RIT CS
 * @author James Heliotis @ RIT CS
 */
public class NetworkClient {

    /**
     * The socket used to communicate with the place server.
     */
    private Socket sock;

    /**
     * The scanner used to read requests from the place server.
     */
    private Scanner networkIn;

    /**
     * The ObjectInputStream that will convert the serialized objects from the server
     */
    private ObjectInputStream in;

    /**
     * The ObjectInputStream that will convert the serialized objects from the server
     */
    private ObjectOutputStream out;

    /**
     * The PrintStream used to write responses to the place server.
     */
    private PrintStream networkOut;

    /**
     * The PlaceBoard used to keep track of the state of the game.
     */
    private PlaceBoard game;

    /**
     * The PlaceBoard used to keep track of the state of the game.
     */
    private ClientModel model;

    /**
     * Sentinel used to control the main game loop.
     */
    private boolean go;

    /**
     * Username used to connect to the server
     */
    private String username;

    /**
     * Accessor that takes multithreaded access into account
     *
     * @return whether it ok to continue or not
     */
    private synchronized boolean goodToGo() {
        return this.go;
    }

    /**
     * Multithread-safe mutator
     */
    private synchronized void stop() {
        this.go = false;
    }

    /**
     * Hook up with a Reversi game server already running and waiting for
     * two players to connect. Because of the nature of the server
     * protocol, this constructor actually blocks waiting for the first
     * message from the server that tells it how big the board will be.
     * Afterwards a thread that listens for server messages and forwards
     * them to the game object is started.
     *
     * @param hostname the name of the host running the server program
     * @param port     the port of the server socket on which the server is
     *                 listening
     * @param username the username of the user trying to connect
     *                 must be updated upon receiving server messages
     * @throws PlaceException If there is a problem opening the connection
     */
    public NetworkClient( String hostname, int port, String username)
            throws PlaceException {
        try {
            this.sock = new Socket(hostname, port);
            this.username = username;
            out = new ObjectOutputStream(sock.getOutputStream());
            out.flush();
            in = new ObjectInputStream(sock.getInputStream());


            connect();

            // Run rest of client in separate thread.
            // This threads stops on its own at the end of the game and
            // does not need to rendez-vous with other software components.
            Thread netThread = new Thread( () -> this.run() );
            netThread.start();
            this.go = true;
        }
        catch( IOException e ) {
            throw new PlaceException( e );
        }
    }


    /**
     * Called by the constructor to set up the game board for this player now
     * that the server has sent the board with the
     * PlaceRequest.BOARD request.
     *
     * @throws PlaceRequest if the dimensions are invalid
     */
    public void connect() throws PlaceException {
        PlaceBoard board;
        try {
            PlaceRequest<String> loginReq = new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username);
            out.writeUnshared(loginReq);
            out.flush();
            PlaceRequest<?> req = (PlaceRequest<?>) in.readUnshared();
                while (req.getType() == null) {
                    req = (PlaceRequest<?>) in.readUnshared();
                }

                if (req.getType() == PlaceRequest.RequestType.LOGIN_SUCCESS) {
                    System.out.println(req.getData());
                    PlaceRequest<?> boardReq = (PlaceRequest<?>) in.readUnshared();
                    if (boardReq.getType() == PlaceRequest.RequestType.BOARD) {
                         board = (PlaceBoard) boardReq.getData();
                         model = new ClientModel(board);
                    }
                }
                else {
                    if (req.getType() == PlaceRequest.RequestType.ERROR) {
                        System.out.println((String)req.getData());
                        System.exit(1);
                    }
                }
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR!");
        } catch (IOException e) {
            System.err.println("ERROR!");
        }
    }

    /**
     * A move has been made by one of the players
     *
     * @param tile string from the server's message that
     *                  contains the row, then column where the
     *                  player made the move
     */
    public void moveMade(PlaceTile tile) {
        model.setTile(tile);
        System.out.println(model);
    }

    /**
     * Returns the model
     * @return
     */
    public ClientModel getModel() {return model;}


    /**
     * Called when the server sends a message saying that
     * gameplay is damaged. Ends the game.
     *
     * @param arguments The error message sent from the reversi.server.
     */
    public void error( String arguments ) {
        System.out.println( "Fatal error: " + arguments );
        this.stop();
    }

    /**
     * This method should be called at the end of the game to
     * close the client connection.
     */
    public void close() {
        try {
            this.sock.close();
        }
        catch( IOException ioe ) {
            // squash
            ioe.printStackTrace();
        }
    }


    /**
     * UI wants to send a new move to the server.
     *
     * @param row the row
     * @param col the column
     */
    public void changeTile( int row, int col, String username, String color ) {
        PlaceColor tileColor;
        switch (color) {
            case "0" : tileColor = PlaceColor.BLACK;
                       break;
            case "1" : tileColor = PlaceColor.GRAY;
                       break;
            case "2" : tileColor = PlaceColor.SILVER;
                       break;
            case "3" : tileColor = PlaceColor.WHITE;
                       break;
            case "4" : tileColor = PlaceColor.MAROON;
                       break;
            case "5" : tileColor = PlaceColor.RED;
                       break;
            case "6" : tileColor = PlaceColor.OLIVE;
                       break;
            case "7" : tileColor = PlaceColor.YELLOW;
                       break;
            case "8" : tileColor = PlaceColor.GREEN;
                       break;
            case "9" : tileColor = PlaceColor.LIME;
                       break;
            case "A" : tileColor = PlaceColor.TEAL;
                       break;
            case "B" : tileColor = PlaceColor.AQUA;
                       break;
            case "C" : tileColor = PlaceColor.NAVY;
                       break;
            case "D" : tileColor = PlaceColor.BLUE;
                       break;
            case "E" : tileColor = PlaceColor.PURPLE;
                       break;
            case "F" : tileColor = PlaceColor.FUCHSIA;
                       break;
            default  : tileColor = PlaceColor.WHITE;
                       break;
        }

        PlaceTile tile = new PlaceTile(row, col, username, tileColor, System.currentTimeMillis());
        PlaceRequest<PlaceTile> newTileReq = new PlaceRequest<>
                (PlaceRequest.RequestType.CHANGE_TILE, tile);
        try {
            out.writeUnshared(newTileReq);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the main client loop. Intended to be started as a separate
     * thread internally. This method is made private so that no one
     * outside will call it or try to start a thread on it.
     */
    private void run() {

        while ( this.goodToGo() ) {
            try {
                PlaceRequest<?> newReq = (PlaceRequest<?>) in.readUnshared();
                switch ( newReq.getType() ) {
                    case ERROR:
                        break;
                    case TILE_CHANGED:
                        PlaceTile tile = (PlaceTile) newReq.getData();
                        moveMade(tile);
                        break;
                    default:
                        System.err
                                .println( "Unrecognized request: " );
                        this.stop();
                        break;
                }
            }
            catch( NoSuchElementException nse ) {
                // Looks like the connection shut down.
                this.error( "Lost connection to server." );
                this.stop();
            }
            catch( Exception e ) {
                this.error( e.getMessage() + '?' );
                this.stop();
            }
        }
        this.close();
    }

}
