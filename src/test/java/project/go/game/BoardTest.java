    package project.go.game;

    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;

    import java.util.List;

    import static org.junit.jupiter.api.Assertions.*;

    public class BoardTest {
        private Board board;

        @BeforeEach
        void freshBoard() {
            board = new Board();
        }

        @Test
        void PlacingTest() {
            Board board = new Board();
            board.setStone(1, 1, StoneColour.BLACK);
            board.setStone(2, 2, StoneColour.WHITE);

            String state = board.toCompactString();

            Board boardAfterConversion = new Board();
            boardAfterConversion.fromCompactString(state);

            assertEquals(StoneColour.BLACK, boardAfterConversion.getColourAt(1, 1));
            assertEquals(StoneColour.WHITE, boardAfterConversion.getColourAt(2, 2));
            assertEquals(StoneColour.EMPTY, boardAfterConversion.getColourAt(1, 2));
        }

        @Test
        void getNeigboursTest() {
            List<Point> neighbours = board.getNeighbours(2, 2);

            assertEquals(4, neighbours.size());
            assertTrue(neighbours.contains(new Point(2, 1)));
            assertTrue(neighbours.contains(new Point(2, 3)));
            assertTrue(neighbours.contains(new Point(1, 2)));
            assertTrue(neighbours.contains(new Point(3, 2)));
            assertFalse(neighbours.contains(new Point(3, 4)));
            assertFalse(neighbours.contains(new Point(7, 7)));
        }
    }
