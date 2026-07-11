package model;

public enum Difficulty {
    EASY(5),
    MEDIUM(10),
    HARD(15);

    private final int points;

    Difficulty(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
