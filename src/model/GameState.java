package model;

public enum GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,  // Classic mode: no piece fits
    WON,        // Time Attack: target score reached
    FAILED      // Time Attack: time ran out or no piece fits
}
