package project.go.game;

public class Move {
    private final Point point;
    private final StoneColour colour;

    public Move(int x, int y, StoneColour colour) {
        if (colour == StoneColour.EMPTY) {
            throw new IllegalArgumentException("Ruch może zostać wykonany jedynie kolorem czarnym albo białym");
        }
        this.point = new Point(x, y);
        this.colour = colour;
    }

    public int getX() { return point.x(); }
    public int getY() { return point.y(); }

    public StoneColour getColour() {
        return colour;
    }
}