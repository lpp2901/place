package place.server;

import place.network.NetworkServer;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class PlaceServer {
    /**Checks whether a String can be converted to an int or not
     *
     * @param input -> String potentially containing a number
     * @return true if input can be converted to an integer
     */
    private boolean isInteger( String input ) {
        try {
            Integer.parseInt( input );
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }

    /**
     * A method that reads in each client, starts a clientthread for them, and then later sends them to the
     * NetworkServer
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        PlaceServer server = new PlaceServer();
        if (args.length != 2 || !server.isInteger(args[0])) {
            System.err.println("Usage: java PlaceServer <port number> DIM");
            System.exit(1);
        }
        Scanner in = new Scanner(System.in);
        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;
        int DIM = Integer.parseInt(args[1]);
        NetworkServer netServer = new NetworkServer(DIM);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {

                new PlaceServerClientThread(serverSocket.accept(), netServer).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}