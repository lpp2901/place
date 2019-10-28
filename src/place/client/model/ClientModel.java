package place.client.model;

import place.PlaceBoard;
import place.PlaceTile;

import java.util.Observable;

public class ClientModel extends Observable {
    /**
     * A board that contains the current server board
     */
    private PlaceBoard board;

    /**
     * An indicator that this model is still going
     */
    private boolean running;

    public ClientModel(PlaceBoard board) {
        this.board = board;
        running = true;
    }

    /**
     * A helper method that checks whether or not the given inputs from the user are valid
     * @param row
     * @param col
     * @param color
     * @return
     */
    public boolean isValid(int row, int col, String color) {
        if (row >= board.DIM)
            return false;
        else if (col >= board.DIM)
            return false;
        return (color.equals("0") || color.equals("1") || color.equals("2") || color.equals("3") || color.equals("4")
                    || color.equals("5") || color.equals("6") || color.equals("7") || color.equals("8")
                    || color.equals("9") || color.equals("A") || color.equals("B") || color.equals("C")
                    || color.equals("D") || color.equals("E") || color.equals("F"));
    }

    /**
     * Returns the board.
     * @return
     */
    public PlaceBoard getBoard() {
        return board;
    }

    /**
     * Get a tile on the board
     *
     * @param row row
     * @param col column
     * @rit.pre row and column constitute a valid board coordinate
     * @return the tile
     */
    public PlaceTile getTile(int row, int col){
        return board.getTile(row, col);
    }

    /**
     * Change a tile in the board.
     *
     * @param tile the new tile
     * @rit.pre row and column constitute a valid board coordinate
     */
    public void setTile(PlaceTile tile) {
        board.setTile(tile);
        super.setChanged();
        super.notifyObservers();
    }

    /**
     * Return the state of the model
     * @return
     */
    public boolean getStatus() {return running;}

    /**
     * Tells whether the coordinates of the tile are valid or not
     * @param tile the tile
     * @return are the coordinates within the dimensions of the board?
     */
    public boolean isValid(PlaceTile tile) {
        return tile.getRow() >=0 &&
                tile.getRow() < board.DIM &&
                tile.getCol() >= 0 &&
                tile.getCol() < board.DIM;
    }
}
