# BLOCKED.exe – The Block Burst Challenge

A desktop puzzle game inspired by Block Blast, written in Java Swing for the course
**Software Construction and Development (SE3005), Assignment 02, Fall 2026**.

Place pieces from a tray of three onto an 8 x 8 board. Fill a complete row or column to
clear it and earn points. The game ends when none of the remaining pieces fits anywhere.

Group: # 3 | **Section:** BSE-5A  
**Members:**  
* Asadullah Nasir – 24L-3089 – [Syed-Asadullah-Nasir](https://github.com/Syed-Asadullah-Nasir)  
* Mahrukh Rehman – 24L-3037 – [Mahrukh-Rehman-Khan](https://github.com/Mahrukh-Rehman-Khan)  
* Hamna Faisal – 24L-2551 – [HamnaFAISAL28](https://github.com/HamnaFAISAL28)

---

## 1. Requirements

| Tool | Version |
|------|---------|
| JDK  | 17 or newer |
| IDE (optional) | IntelliJ IDEA |

No external libraries are used, only the standard JDK (Swing, Java 2D, `javax.sound.sampled`).

## 2. Installation

```bash
git clone https://github.com/Syed-Asadullah-Nasir/BlockedExe.git
cd BlockedExe
```

Project layout:

```
BlockedExe/
├── data/        best score file (created automatically)
├── logs/        game.log (created automatically)
├── resources/   images/, fonts/, sounds/   (this folder is the resources root)
├── src/         Main.java and the packages model, logic, view, effects, input, audio, util
├── README.md
└── .gitignore
```

## 3. Run

### In IntelliJ IDEA
1. Open the project folder.
2. Right-click `src` → *Mark Directory as* → **Sources Root**.
3. Right-click `resources` → *Mark Directory as* → **Resources Root**.
4. Set the project SDK to JDK 17+.
5. Open `src/Main.java` and press the green *Run* button.
   The working directory must be the project folder, so `data/` and `logs/` are created there
   (this is IntelliJ's default).

### From the command line (run inside the project folder)

Linux / macOS:
```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out:resources Main
```

Windows (PowerShell or cmd):
```bat
mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp "out;resources" Main
```

## 4. Run the tests

The tests are plain console programs (no framework). Each one prints `PASS` / `FAIL` lines
and a final `Passed: n   Failed: 0`. Run them after compiling:

```bash
java -cp out model.BoardTest
java -cp out logic.PieceGeneratorTest
java -cp out logic.GameTest
java -cp out logic.GamePauseTest
java -cp out logic.GameTimeTest
java -cp out logic.ScoreManagerFileTest
java -cp out logic.GameBestScoreTest
java -cp out logic.GameStressTest
java -cp out audio.SoundManagerTest
java -cp out effects.EffectsTest
java -cp out util.UncaughtErrorLoggerTest
```

## 5. How to play

**Goal:** fill complete rows or columns. Several lines cleared with one piece give bonus points.

| Input | Action |
|-------|--------|
| Mouse | Press a piece in the tray, drag it over the board (a green/red preview shows the result), release to place it |
| `1` `2` `3` | Select the piece in that tray slot |
| Arrow keys | Move the preview on the board |
| `Enter` / `Space` | Place the selected piece |
| `P` | Pause / Resume |
| `R` | Restart |
| `M` | Mute / Unmute |

**Modes**
- **Classic** – play until no piece fits. The elapsed time is shown.
- **Time Attack** – reach **500 points within 180 seconds** to win; otherwise the round fails.
  Both numbers are constants in `util/Constants.java` (`TARGET_SCORE`, `TIME_ATTACK_SECONDS`).

**Scoring:** 1 point per cell placed, 10 points per cleared line, plus 10 for every extra line
when two or more lines are cleared at once.

## 6. Implemented features

- Main menu (Play, Instructions, Exit), mode selection, instructions screen, gameplay screen,
  pause screen and game-over / win / fail screen – all with mouse-clickable buttons.
- 8 x 8 board and a tray of three random pieces; 20 shapes: single square, lines (2, 3, 4 –
  horizontal and vertical), 2 x 2 square, 3 x 2 and 2 x 3 rectangles, 4 L shapes, 4 T shapes, 2 S shapes.
- Placement preview, rejection of overlapping or out-of-board placement (the piece is not consumed).
- Detection of all full rows and columns after each placement; an intersection cell is cleared once.
- New tray when empty; game over when no remaining piece fits.
- Mouse drag and drop and full keyboard control.
- Pause stops game time and input; Resume continues without skipping time.
- Two game modes with timer, game states (enums `GameState`, `GameMode`).
- Best score per mode saved in `data/bestscore.txt`.
- Four sound effects (select, place, clear, invalid / game over) played with `javax.sound.sampled.Clip`,
  Mute toggle, and silent operation when audio is unavailable.
- Three animations drawn with Graphics2D: placement pop, row/column clear flash, score popup.
- Errors and missing assets (images, font, sounds, score file) never crash the game;
  they are written to `logs/game.log`.

## 7. Design overview

| Package | Responsibility |
|---------|----------------|
| `model`   | Data and rules of the board: `Board`, `BlockPiece`, `CellPosition`, enums `GameState`, `GameMode` |
| `logic`   | Game rules: `Game`, `PieceGenerator`, `ShapeLibrary`, `ScoreManager`, `PlacementResult` |
| `view`    | Everything drawn on screen: `GameFrame`, `GamePanel`, menu panels, renderers, `EffectFactory` |
| `input`   | `MouseInputHandler`, `KeyInputHandler` – translate events into calls on `GamePanel` |
| `effects` | Animations: interface `Drawable`, abstract class `VisualEffect` and its subclasses |
| `audio`   | `SoundManager`, enum `SoundEffect` |
| `util`    | `Constants`, `FileLogger`, `ResourceLoader`, `TimeFormat`, `UncaughtErrorLogger` |

- **Game loop:** a Swing `Timer` (16 ms) calls `update()` and then `repaint()`.
  `paintComponent()` only draws; game rules are never changed while painting.
- **Polymorphism:** `GamePanel` holds a list of effects and calls `draw()` on each through the
  `Drawable` interface. `PlacementPopEffect`, `ClearFlashEffect` and `ScorePopupEffect` extend
  `VisualEffect` and each draws itself differently.

## 8. Work division

| Member | GitHub | Contribution |
|--------|--------|--------------|
| `Asadullah Nasir` | `Syed-Asadullah-Nasir` | `model + logic: Board, BlockPiece, PieceGenerator, Game, ScoreManager, tests` |
| `Mahrukh Rehman` | `Mahrukh-Rehman-Khan` | `view + input: screens, renderers, mouse and keyboard handling` |
| `Hamna Faisal` | `HamnaFAISAL28` | `audio, effects, README, logging, integration testing` |

## 9. Assets

Images, font and sounds come from the free asset pack linked in the assignment (see
`resources/`). If an asset is missing, the game uses a simple drawn replacement and keeps running.
