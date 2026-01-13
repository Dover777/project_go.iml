package project.go.game;

import java.util.*;

public class Logic {
    private final Board board;
    private String lastBoardState = "";
    private int passCount = 0;

    private int blackPrisoners = 0;
    private int whitePrisoners = 0;

    public Logic(Board board) {
        this.board = board;
    }

    public PlacementResult placeStone(Move move) {
        int x = move.getX();
        int y = move.getY();
        int size = board.getSize();
        StoneColour colour = move.getColour();

        if (x < 0 || x >= size || y < 0 || y >= size) {
                return PlacementResult.failure("Ruch poza planszą.");
        }
        if (board.getColourAt(x, y) != StoneColour.EMPTY) {
            return PlacementResult.failure("Pole jest już zajęte.");
        }
        String boardStateBefore = board.toCompactString();
        board.setStone(x, y, colour);

        int capturedCount = 0;
        StoneColour opponentColour = (colour == StoneColour.BLACK) ? StoneColour.WHITE : StoneColour.BLACK;

        for (Point neighbour : board.getNeighbours(x, y)) {
            if (board.getColourAt(neighbour.x(), neighbour.y()) == opponentColour) {
                Set<Point> group = findGroup(neighbour.x(), neighbour.y());

                if (countBreaths(group) == 0) {
                    int prisonersCount = removeGroup(group);
                    capturedCount += prisonersCount;

                    if (colour == StoneColour.BLACK) {
                        blackPrisoners += prisonersCount;
                    }
                    else {
                        whitePrisoners += prisonersCount;
                    }
                }
            }
        }

        if (capturedCount == 0) {
            Set<Point> group = findGroup(x, y);
            if (countBreaths(group) == 0) {
                board.setStone(x, y, StoneColour.EMPTY);
                return PlacementResult.failure("Ruch samobójczy");
            }
        }

        String boardStateAfter = board.toCompactString();
        if (boardStateAfter.equals(lastBoardState)) {
            board.fromCompactString(boardStateBefore);
            return  PlacementResult.failure("Nie możesz wykonać tego ruchu (zasada KO)");
        }

        lastBoardState = boardStateBefore;
        resetPassCount();
        return PlacementResult.success(capturedCount);
    }

    private Set<Point> findGroup(int x, int y) {
        if (this.board.getColourAt(x, y) == StoneColour.EMPTY) return Collections.emptySet();

        Set<Point> group = new HashSet<>();
        Queue<Point> queue = new LinkedList<>();
        StoneColour targetColour = this.board.getColourAt(x, y);

        Point startPoint = new Point(x, y);
        queue.add(startPoint);
        group.add(startPoint);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            for (Point neighbour : this.board.getNeighbours(current.x(), current.y())) {
                if (this.board.getColourAt(neighbour.x(), neighbour.y()) == targetColour && !group.contains(neighbour)) {
                    group.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }
        return group;
    }

    private int countBreaths(Set<Point> group) {
        Set<Point> breaths = new HashSet<>();
        for (Point stone : group) {
            for (Point neighbour : board.getNeighbours(stone.x(), stone.y())) {
                if (board.getColourAt(neighbour.x(), neighbour.y()) == StoneColour.EMPTY) {
                    breaths.add(neighbour);
                }
            }
        }
        return breaths.size();
    }

    private int removeGroup(Set<Point> group) {
        int count = 0;
        for (Point stone : group) {
            board.setStone(stone.x(), stone.y(), StoneColour.EMPTY);
            count++;
        }
        return count;
    }

    public GameScore setTerritory() {
        int blackTerritory = 0;
        int whiteTerritory = 0;
        int size = board.getSize();
        boolean [][]visited = new boolean[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.getColourAt(x, y) == StoneColour.EMPTY && !visited[x][y]) {
                    List<Point> area = new ArrayList<>();
                    StoneColour owner = findAreaOwner(x, y, visited, area);

                    if (owner == StoneColour.BLACK) {
                        blackTerritory += area.size();
                    }
                    else if (owner == StoneColour.WHITE) {
                        whiteTerritory += area.size();
                    }
                }
            }
        }

        return new GameScore(blackTerritory, whiteTerritory, blackPrisoners, whitePrisoners);
    }

    private StoneColour findAreaOwner(int startX, int startY, boolean[][] visited, List<Point> area) {
        Queue<Point> queue = new LinkedList<>();
        Point start = new Point(startX, startY);
        queue.add(start);
        visited[startX][startY] = true;
        area.add(start);

        Set<StoneColour> borders = new HashSet<>();
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            for (Point  neighbour : board.getNeighbours(current.x(), current.y())) {
                StoneColour colour = board.getColourAt(neighbour.x(), neighbour.y());
                if (colour == StoneColour.EMPTY) {
                    if (!visited[neighbour.x()][neighbour.y()]) {
                        visited[neighbour.x()][neighbour.y()] = true;
                        area.add(neighbour);
                        queue.add(neighbour);
                    }
                }
                else {
                    borders.add(colour);
                }
            }
        }

        if (borders.size() == 1) {
            return borders.iterator().next();
        }
        return StoneColour.EMPTY;
    }

    public boolean pass() {
        passCount++;
        return passCount >= 2;
    }

    private void resetPassCount(){
        passCount = 0;
    }

}
