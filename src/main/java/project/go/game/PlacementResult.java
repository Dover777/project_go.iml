package project.go.game;

public record PlacementResult(boolean success, int capturedCount, String message) {

    public static PlacementResult success(int capturedCount) {
        return new PlacementResult(true, capturedCount, "Ruch poprawny.");
    }

    public static PlacementResult failure(String message) {
        return new PlacementResult(false, 0, message);
    }
}