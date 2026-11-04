package audio;

import util.Constants;

// The four sound effects of the game. Using an enum instead of text means a typo
// like play("palce") cannot happen: the compiler checks the name.
public enum SoundEffect {
    SELECT(Constants.SOUND_SELECT),     // a piece is picked up
    PLACE(Constants.SOUND_PLACE),       // a piece is placed
    CLEAR(Constants.SOUND_CLEAR),       // one or more lines are cleared
    INVALID(Constants.SOUND_INVALID);   // a rejected move, and also game over

    private final String path;

    SoundEffect(String path) {
        this.path = path;
    }

    // Path of the WAV file, relative to the resources root.
    public String getPath() {
        return path;
    }
}
