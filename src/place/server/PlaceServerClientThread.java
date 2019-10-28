package place.server;

import place.PlaceBoard;
import place.PlaceTile;
import place.network.NetworkServer;
import place.network.PlaceRequest;

import java.net.*;
import java.io.*;

public class PlaceServerClientThread extends Thread {
    private Socket socket = null;
    /**
     * The ObjectInputStream that will convert the serialized objects from the server
     */
    private ObjectInputStream in;

    /**
     * The ObjectInputStream that will convert the serialized objects from the server
     */
    private ObjectOutputStream out;

    private String username;

    private NetworkServer server;

    public PlaceServerClientThread(Socket socket, NetworkServer server) {
        super("PLACESERVERCLIENTTHREAD");
        this.server = server;
        this.socket = socket;
        username = null;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tileChanged(PlaceRequest<PlaceTile> tileChangereq) {
        try {
            out.writeUnshared(tileChangereq);
            out.flush();
        } catch (IOException e) {
            System.err.println("ERROR!");
        }
    }

    public String getUsername() { return username; }

    public Socket getSocket() {return socket;}

    public boolean stillRunning() {
        try {
            int x = socket.getInputStream().read();
            return true;
        } catch (IOException e) {
            server.logout(this);
            return false;
        }
    }


    public void run() {
        try {
                PlaceRequest<?> req = (PlaceRequest<?>) in.readUnshared();
                if (req.getType() == PlaceRequest.RequestType.LOGIN)
                     username = (String)req.getData();

                boolean connected = server.login(this);
                if (connected) {
                    String connectMsg = "Connection Successful.";
                    PlaceRequest<String> loginSuccess = new PlaceRequest<>
                                                    (PlaceRequest.RequestType.LOGIN_SUCCESS, connectMsg);
                    out.writeUnshared(loginSuccess);
                    out.flush();
                    PlaceRequest<PlaceBoard> boardReq = new PlaceRequest<>
                            (PlaceRequest.RequestType.BOARD, server.getBoard());
                    out.writeUnshared(boardReq);
                    out.flush();
                } else {
                    PlaceRequest<String> error = new PlaceRequest<>
                            (PlaceRequest.RequestType.ERROR, "Unable to login; username already exists");
                    out.writeUnshared(error);
                    out.flush();
                }
                boolean running = true;
                PlaceRequest<?> changeTileReq = (PlaceRequest<?>) in.readUnshared();
                while(running) {
                    if (changeTileReq != null) {
                        if (changeTileReq.getType() == PlaceRequest.RequestType.CHANGE_TILE) {
                            PlaceTile newTile = (PlaceTile) changeTileReq.getData();
                            server.changeTile(newTile);
                            changeTileReq = null;
                        }
                    }
                    else
                        changeTileReq = (PlaceRequest<?>) in.readUnshared();
                    }
            server.logout(this);
            socket.close();
            System.out.println(this + " has stopped");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
