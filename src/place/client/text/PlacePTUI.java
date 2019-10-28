package place.client.text;

import place.PlaceBoard;
import place.PlaceException;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;

import java.io.PrintWriter;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

public class PlacePTUI extends ConsoleApplication implements Observer {
    /**
     * local board for the place program
     */
    private PlaceBoard board;

    /**
     * Model for the application to update the local version from
     */
    private ClientModel model;

    /**
     * Connection to network interface to server
     */
    private NetworkClient serverConn;

    /**
     * What to read to see what user types
     */
    private Scanner userIn;

    /**
     * Where to send text that the user can see
     */
    private PrintWriter userOut;

    private String username;

    /**
     * Create the board model, create the network connection based on
     * command line parameters, and use the first message received to
     * allocate the board size the server is also using.
     */
    @Override
    public void init() {
        try {
            List< String > args = super.getArguments();

            // Get host info from command line
            String host = args.get( 0 );
            int port = Integer.parseInt( args.get( 1 ) );
            username = args.get(2);

            // Create the network connection.
            this.serverConn = new NetworkClient( host, port, username );

            model = serverConn.getModel();
            board = model.getBoard();
        }
        catch( PlaceException |
                ArrayIndexOutOfBoundsException |
                NumberFormatException e ) {
            System.out.println( e );
            throw new RuntimeException( e );
        }
    }


    /**
     * This method continues running until the game is over.
     * That method returns as soon as the setup is done.
     * This method waits for a notification from {@link #endGame()},
     * called indirectly from a model update from {@link NetworkClient}.
     *
     * @param userIn what to read to see what user types
     * @param userOut where to send messages so user can see them
     */
    @Override
    public synchronized void go( Scanner userIn, PrintWriter userOut ) {

        this.userIn = userIn;
        this.userOut = userOut;

        // Connect UI to model. Can't do it sooner because streams not set up.
        this.model.addObserver(this);
        // Manually force a display of all board state, since it's too late
        // to trigger update().

        this.refresh();
        Boolean running = true;
        while (running) {
            try {
                this.wait();
            }
            catch( InterruptedException ie ) {}
        }

    }

    /**
     * GUI is closing, so close the network connection. Server will
     * get the message.
     */
    @Override
    public void stop() {
        this.userIn.close();
        this.userOut.close();
        this.serverConn.close();
    }

    private synchronized void endGame() {
        this.notify();
    }

    /**
     * Update all GUI Nodes to match the state of the model.
     */
    private void refresh() {
            this.userOut.println(model.getBoard());
            userOut.println("Change tile? (row col color): ");
            this.userOut.flush();
            int row = this.userIn.nextInt();
            //System.out.print(row + " ");
            int col = this.userIn.nextInt();
            //System.out.print(col + " ");
            String color = this.userIn.next();
            userIn.nextLine();
            //System.out.print(color + " ");
            if (this.model.isValid(row, col, color)) {
                //this.userOut.println(this.userIn.nextLine());
                this.serverConn.changeTile(row, col, username, color);
            }
    }
    /**
     * Update the UI when the model calls notify.
     * Currently no information is passed as to what changed,
     * so everything is redone.
     *
     * @param t An Observable -- assumed to be the model.
     * @param o An Object -- not used.
     */
    @Override
    public void update( Observable t, Object o ) {
        assert t == this.model: "Update from non-model Observable";

        this.refresh();

    }


    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceClient host port username");
        }
        else {
            ConsoleApplication.launch(PlacePTUI.class, args);
        }
    }
}
