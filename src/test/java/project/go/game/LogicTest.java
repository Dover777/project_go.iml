package project.go.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LogicTest {
    private Board board;
    private Logic logic;

    @BeforeEach
    void freshBoard() {
        board = new Board();
        logic = new Logic(board);
    }

    @Test
    void CaptureTest() {
        board.setStone(1, 2, StoneColour.BLACK);
        board.setStone(2, 1, StoneColour.BLACK);
        board.setStone(2, 3, StoneColour.BLACK);
        board.setStone(2, 2, StoneColour.WHITE);

        PlacementResult result = logic.placeStone(new Move(3, 2, StoneColour.BLACK));

        assertTrue(result.success());
        assertEquals(1, result.capturedCount());
        assertEquals(StoneColour.EMPTY, board.getColourAt(2,2));
    }

    @Test
    void SuicideRuleTest() {
        board.setStone(1, 2, StoneColour.BLACK);
        board.setStone(2, 1, StoneColour.BLACK);
        board.setStone(2, 3, StoneColour.BLACK);
        board.setStone(3, 2, StoneColour.BLACK);

        PlacementResult result = logic.placeStone(new Move(2, 2, StoneColour.WHITE));

        assertFalse(result.success());
        assertEquals("Ruch samobójczy", result.message());
        assertEquals(StoneColour.EMPTY, board.getColourAt(2,2));
    }

    @Test
    void KoRuleTest() {
        board.setStone(1, 2, StoneColour.BLACK);
        board.setStone(2, 1, StoneColour.BLACK);
        board.setStone(3, 2, StoneColour.BLACK);

        board.setStone(1, 3, StoneColour.WHITE);
        board.setStone(2, 2, StoneColour.WHITE);
        board.setStone(3, 3, StoneColour.WHITE);
        board.setStone(2, 4, StoneColour.WHITE);

        logic.placeStone(new Move(2, 3, StoneColour.BLACK));
        PlacementResult result = logic.placeStone(new Move(2, 2, StoneColour.WHITE));

        assertFalse(result.success());
        assertEquals("Nie możesz wykonać tego ruchu (zasada KO)", result.message());
        assertEquals(StoneColour.EMPTY, board.getColourAt(2,2));
    }

    @Test
    void passEndsGameTest() {
        boolean firstPass = logic.pass();
        assertFalse(firstPass);

        boolean secondPass = logic.pass();
        assertTrue(secondPass);
    }

    @Test
    void moveResetsPassCountTest() {
        logic.pass(); // Pierwszy pas
        logic.placeStone(new Move(1, 1, StoneColour.BLACK));

        boolean afterMove = logic.pass();
        assertFalse(afterMove);
    }

}

