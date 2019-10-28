package place.network;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.server.PlaceServerClientThread;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

public class NetworkServer {

    PlaceBoard board;

    /**
     * The ObjectInputStream that will convert the serialized objects from the server
     */
    private ObjectInputStream in;

    /**
     * The ObjectInputStream that will convert the serialized objects from the server
     */
    private ObjectOutputStream out;

    /**
     * A hashmap containing the current users.
     */
    private HashMap<String,PlaceServerClientThread> threads;

    private HashMap<String, Integer> numInts;

    /**
     * a counter that contains the number of users that have connected to the server.
     */
    private int numClientsConnected = 0;

    /**
     * A counter that contains the number of times a tile has been changed.
     */
    private int numInputs = 0;

    private HashMap<PlaceColor, Integer> ratios;

    public NetworkServer(int dim) {
        board = new PlaceBoard(dim);
        threads = new HashMap<> ();
        initRatios();
        numInts = new HashMap<>();
    }


    public void initRatios(){
        ratios = new HashMap<> ();
        ratios.put(PlaceColor.WHITE, 0);
        ratios.put(PlaceColor.BLACK, 0);
        ratios.put(PlaceColor.GRAY, 0);
        ratios.put(PlaceColor.SILVER, 0);
        ratios.put(PlaceColor.MAROON, 0);
        ratios.put(PlaceColor.RED, 0);
        ratios.put(PlaceColor.OLIVE, 0);
        ratios.put(PlaceColor.YELLOW, 0);
        ratios.put(PlaceColor.GREEN, 0);
        ratios.put(PlaceColor.LIME, 0);
        ratios.put(PlaceColor.TEAL, 0);
        ratios.put(PlaceColor.AQUA, 0);
        ratios.put(PlaceColor.NAVY, 0);
        ratios.put(PlaceColor.BLUE, 0);
        ratios.put(PlaceColor.PURPLE, 0);
        ratios.put(PlaceColor.FUCHSIA, 0);

    }

    /**
     * A method to be called from the network client to tell when a tile has been changed
     * @param tile
     */
    public synchronized void changeTile(PlaceTile tile) {
        board.setTile(tile);
        tileChanged(tile);
        System.out.println(board);
        ratios.put(tile.getColor(), ratios.get(tile.getColor()) + 1);
        numInputs++;
        numInts.put(tile.getOwner() ,numInts.get(tile.getOwner()) + 1);
        statistics();

    }

    public void statistics() {
        System.out.println("The Statistics of How Many Cells There are of Each Color:");
        System.out.println(ratios);
        System.out.println("The Statistics Regarding the Concentration of Each Color Among All Cells:");
        System.out.print("{");
        for(PlaceColor p : ratios.keySet())
        {
            int x = ratios.get(p);
            double conc = (double)x /numInputs;
            System.out.print(p + "=" + conc + ", ");

        }
        System.out.print("}");
        System.out.println();
        System.out.println("Current User # of Interaction Statistics:");
        System.out.println(numInts);

        int baseIntNum = 0;
        String best = "";
        for(String s : numInts.keySet())
        {
            int x = numInts.get(s);
            if (x > baseIntNum) {
                baseIntNum = x;
                best = s;
            }
        }
        System.out.println("The top contributor is :");
        System.out.println(best + " with " + baseIntNum + " tile contributions!");
    }

    /**
     * a method to be called when a user logs out from the server
     * @param thread
     */
    public synchronized void logout(PlaceServerClientThread thread){
        threads.remove(thread.getUsername());
        System.out.println(thread.getUsername() + " has logged out.");
    }

    /**
     * A method called from the server when a tile has been changed. this method will then notify all threads
     * that a change has been made
     * @param tile
     */
    public void tileChanged(PlaceTile tile) {
        Collection<PlaceServerClientThread> users = threads.values();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        Calendar calendar = Calendar.getInstance();
        System.out.println(dateFormat.format(calendar.getTime()) + " The tile " + tile.getRow() + ", " +
                            tile.getCol() + " has been changed to " + tile.getColor());
        System.out.println();

        PlaceRequest<PlaceTile> changedReq = new PlaceRequest<>(PlaceRequest.RequestType.TILE_CHANGED, tile);
        for (PlaceServerClientThread user : users) {
            user.tileChanged(changedReq);
        }
    }

    /**
     * The method that allows for each thread to login to the server
     * @param thread
     * @return
     */
    public synchronized boolean login(PlaceServerClientThread thread) {
        if (!threads.containsKey(thread.getUsername())) {
            threads.put(thread.getUsername(), thread);
            numInts.put(thread.getUsername(), 0);
            System.out.println(thread.getUsername() + " has connected. @ " + thread.getSocket());
            numClientsConnected++;
            return true;
        } else
            return false;

    }

    /**
     * A method that returns the server's version of the current board.
     * @return
     */
    public PlaceBoard getBoard() {return this.board;}


}
