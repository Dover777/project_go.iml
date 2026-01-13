package project.go.game;

import java.util.*;

public class Logic {
    private final Board board;
    private String lastBoardState = "";

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
                    capturedCount += removeGroup(group);
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
}