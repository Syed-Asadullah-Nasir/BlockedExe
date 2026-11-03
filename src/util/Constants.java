package util;

public final class Constants {

    private Constants() {
    }

    // Board and window
    public static final int BOARD_SIZE = 8;
    public static final int CELL_SIZE = 56;
    public static final int WINDOW_WIDTH = 600;
    public static final int WINDOW_HEIGHT = 800;

    // Number of piece slots in the tray
    public static final int TRAY_SIZE = 3;

    // Game modes (configurable)
    public static final int TIME_ATTACK_SECONDS = 180;
    public static final int TARGET_SCORE = 500;

    // Block colours: 0 means an empty cell
    public static final int EMPTY_CELL = 0;
    public static final int COLOR_BLUE = 1;
    public static final int COLOR_GREEN = 2;
    public static final int COLOR_ORANGE = 3;
    public static final int COLOR_PINK = 4;
    public static final int COLOR_PURPLE = 5;
    public static final int COLOR_RED = 6;
    public static final int COLOR_YELLOW = 7;
    public static final int COLOR_COUNT = 7;

    // Block image paths, indexed by colour (index 0 is unused)
    public static final String[] BLOCK_IMAGE_PATHS = {
            null,
            "images/blocks/blue.png",
            "images/blocks/green.png",
            "images/blocks/orange.png",
            "images/blocks/pink.png",
            "images/blocks/purple.png",
            "images/blocks/red.png",
            "images/blocks/yellow.png"
    };

    // Image paths (relative to the resources root)
    public static final String IMAGE_BACK_BUTTON = "images/back/button.png";
    public static final String IMAGE_BEST_STAR = "images/best/star.png";
    public static final String IMAGE_CLASSIC_BUTTON = "images/classic/button.png";
    public static final String IMAGE_EXIT_BUTTON = "images/exit/exit_button.png";
    public static final String IMAGE_GAME_OVER_CROSS = "images/game-over/cross.png";
    public static final String IMAGE_INSTRUCTIONS_BUTTON = "images/instructions/instructions_button.png";
    public static final String IMAGE_MENU_BUTTON = "images/menu/menu_button.png";
    public static final String IMAGE_MUTE_BUTTON = "images/mute/mute_button.png";
    public static final String IMAGE_PAUSE_BUTTON = "images/pause/pause_button.png";
    public static final String IMAGE_PLAY_ICON = "images/play/play_icon.png";
    public static final String IMAGE_RESTART_ICON = "images/restart/restart_icon.png";
    public static final String IMAGE_RESUME_BUTTON = "images/resume/resume_button.png";
    public static final String IMAGE_SCORE_BACKGROUND = "images/score/score_background.png";
    public static final String IMAGE_SLOT_BACKGROUND = "images/slot/slot_background.png";
    public static final String IMAGE_TIME_ATTACK_BUTTON = "images/time-attack/time-attack_button.png";
    public static final String IMAGE_WIN_CHECK = "images/win/win_check.png";

    // Font path
    public static final String FONT_PATH = "fonts/kenney_future_narrow.ttf";

    // Sound paths
    public static final String SOUND_CLEAR = "sounds/clear.wav";
    public static final String SOUND_INVALID = "sounds/invalid.wav";
    public static final String SOUND_PLACE = "sounds/place.wav";
    public static final String SOUND_SELECT = "sounds/select.wav";

    // Text files (relative to the project folder, not the classpath)
    public static final String BEST_SCORE_FILE = "data/bestscore.txt";
    public static final String LOG_FILE = "logs/game.log";
}