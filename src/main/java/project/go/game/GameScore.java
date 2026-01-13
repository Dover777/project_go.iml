package project.go.game;

public record GameScore(int blackTerritory, int whiteTerritory, int blackPrisoners, int whitePrisoners) {
    public int getTotalBlackScore() {
        return blackTerritory + blackPrisoners;
    }

    public int getTotalWhiteScore() {
        return whiteTerritory + whitePrisoners;
    }
}
