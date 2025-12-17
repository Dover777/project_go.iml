package project.go.game;

import java.util.*;

public class Board {
    public static final int SIZE = 9;
    private final StoneColour[][] grid;

    public Board() {
        this.grid = new StoneColour[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(grid[i], StoneColour.EMPTY);
        }
    }

    public int getSize() {
        return SIZE;
    }

    public StoneColour getColourAt(int x, int y) {
        if (!isValid(x, y)) return StoneColour.EMPTY;
        return grid[x][y];
    }

    public void setStone(int x, int y, StoneColour colour) {
        if (isValid(x, y)) {
            grid[x][y] = colour;
        }
    }


    private char getSymbol(int x, int y) {
        return switch (grid[x][y]) {
            case BLACK -> 'B';
            case WHITE -> 'W';
            case EMPTY -> 'O';
        };
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    public List<Point> getNeighbours(int x, int y) {
        List<Point> neighbours = new ArrayList<>();
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (isValid(nx, ny)) {
                neighbours.add(new Point(nx, ny));
            }
        }
        return neighbours;
    }

    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                sb.append(getSymbol(x, y)).append(',');
            }
        }
        if (!sb.isEmpty()) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int y = 0; y < SIZE; y++) sb.append(y + 1).append(" ");
        sb.append("\n");

        for (int x = 0; x < SIZE; x++) {
            sb.append(String.format("%2d ", x + 1));
            for (int y = 0; y < SIZE; y++) {
                sb.append(getSymbol(x, y)).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void fromCompactString(String compactString) {
        String[] parts = compactString.split(",");
        if (parts.length != SIZE * SIZE) {
            throw new IllegalArgumentException("Nieprawidłowa długość stringa.");
        }

        int index = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                switch (parts[index]) {
                    case "B":
                        grid[x][y] = StoneColour.BLACK;
                        break;
                    case "W":
                        grid[x][y] = StoneColour.WHITE;
                        break;
                    case "O":
                    default:
                        grid[x][y] = StoneColour.EMPTY;
                        break;
                }
                index++;
            }
        }
    }
}