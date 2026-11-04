package logic;

import model.GameMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// Tests the best score file. It only uses a temporary folder, never your real data/ folder.
public class ScoreManagerFileTest {

    private static int passed = 0;
    private static int failed = 0;
    private static File testFolder;

    public static void main(String[] args) throws IOException {
        testFolder = Files.createTempDirectory("bestscore_test").toFile();

        testMissingFileIsCreatedWithZeros();
        testMissingFolderIsCreated();
        testSaveAndLoadRoundTrip();
        testFileFormat();
        testEmptyFile();
        testGarbageFile();
        testNonNumericScore();
        testNegativeAndHugeScores();
        testUnknownModeAndExtraLines();
        testSpacesAndLowerCase();
        testDuplicateLineLastWins();
        testReloadForgetsOldValues();
        testOnlyOneModeInFile();
        testSaveOnlyWhenChanged();
        testFileIsADirectory();
        testBeatingTheBestAndRestarting();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    // ----- tests -----

    private static void testMissingFileIsCreatedWithZeros() {
        String path = newPath("missing.txt");
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("missing file: classic best is 0", manager.getBestScore(GameMode.CLASSIC) == 0);
        check("missing file: time attack best is 0", manager.getBestScore(GameMode.TIME_ATTACK) == 0);
        check("missing file is created", new File(path).exists());
        check("created file holds zeros", readLines(path).equals(list("CLASSIC=0", "TIME_ATTACK=0")));
        check("nothing unsaved after loading", !manager.hasUnsavedBestScores());
    }

    private static void testMissingFolderIsCreated() {
        String path = new File(new File(testFolder, "newfolder"), "scores.txt").getPath();
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("missing data folder and file are created", new File(path).exists());
    }

    private static void testSaveAndLoadRoundTrip() {
        String path = newPath("roundtrip.txt");
        ScoreManager first = new ScoreManager(path);
        first.setBestScore(GameMode.CLASSIC, 1200);
        first.setBestScore(GameMode.TIME_ATTACK, 640);
        first.saveBestScores();

        ScoreManager second = new ScoreManager(path);
        second.loadBestScores();
        check("round trip: classic is 1200", second.getBestScore(GameMode.CLASSIC) == 1200);
        check("round trip: time attack is 640", second.getBestScore(GameMode.TIME_ATTACK) == 640);
    }

    private static void testFileFormat() {
        String path = newPath("format.txt");
        ScoreManager manager = new ScoreManager(path);
        manager.setBestScore(GameMode.CLASSIC, 7);
        manager.setBestScore(GameMode.TIME_ATTACK, 9);
        manager.saveBestScores();
        check("file has one NAME=value line per mode",
                readLines(path).equals(list("CLASSIC=7", "TIME_ATTACK=9")));
    }

    private static void testEmptyFile() {
        String path = writeFile("empty.txt", "");
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("empty file gives zeros", manager.getBestScore(GameMode.CLASSIC) == 0
                && manager.getBestScore(GameMode.TIME_ATTACK) == 0);
    }

    private static void testGarbageFile() {
        String path = writeFile("garbage.txt", "abc\n\n%%%%\nhello world\n");
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("garbage text gives zeros and no crash", manager.getBestScore(GameMode.CLASSIC) == 0
                && manager.getBestScore(GameMode.TIME_ATTACK) == 0);
    }

    private static void testNonNumericScore() {
        String path = writeFile("nonnumeric.txt", "CLASSIC=abc\nTIME_ATTACK=55\n");
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("non-numeric score becomes 0", manager.getBestScore(GameMode.CLASSIC) == 0);
        check("the other line is still read", manager.getBestScore(GameMode.TIME_ATTACK) == 55);

        String emptyValue = writeFile("novalue.txt", "CLASSIC=\nTIME_ATTACK=3.5\n");
        ScoreManager second = new ScoreManager(emptyValue);
        second.loadBestScores();
        check("empty value becomes 0", second.getBestScore(GameMode.CLASSIC) == 0);
        check("decimal value becomes 0", second.getBestScore(GameMode.TIME_ATTACK) == 0);
    }

    private static void testNegativeAndHugeScores() {
        String path = writeFile("range.txt", "CLASSIC=-5\nTIME_ATTACK=99999999999\n");
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("negative score becomes 0", manager.getBestScore(GameMode.CLASSIC) == 0);
        check("number too big for int becomes 0", manager.getBestScore(GameMode.TIME_ATTACK) == 0);
    }

    private static void testUnknownModeAndExtraLines() {
        String path = writeFile("unknown.txt", "FOO=99\nCLASSIC=12=3\nTIME_ATTACK=8\njust text\n");
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("unknown mode is ignored", manager.getBestScore(GameMode.CLASSIC) == 0);
        check("line with two equal signs is ignored", manager.getBestScore(GameMode.CLASSIC) == 0);
        check("valid line between bad lines is read", manager.getBestScore(GameMode.TIME_ATTACK) == 8);
    }

    private static void testSpacesAndLowerCase() {
        String path = writeFile("spaces.txt", "  classic = 40  \n\tTIME_ATTACK=\t41\n");
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("spaces and lower case are accepted (classic)", manager.getBestScore(GameMode.CLASSIC) == 40);
        check("tabs are accepted (time attack)", manager.getBestScore(GameMode.TIME_ATTACK) == 41);
    }

    private static void testDuplicateLineLastWins() {
        String path = writeFile("duplicate.txt", "CLASSIC=10\nCLASSIC=20\n");
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("a repeated mode uses the last value", manager.getBestScore(GameMode.CLASSIC) == 20);
    }

    private static void testReloadForgetsOldValues() {
        String path = writeFile("reload.txt", "CLASSIC=5\n");
        ScoreManager manager = new ScoreManager(path);
        manager.setBestScore(GameMode.TIME_ATTACK, 300);
        manager.loadBestScores();
        check("reload keeps what the file says", manager.getBestScore(GameMode.CLASSIC) == 5);
        check("reload clears values that are not in the file", manager.getBestScore(GameMode.TIME_ATTACK) == 0);
    }

    private static void testOnlyOneModeInFile() {
        String path = writeFile("onemode.txt", "TIME_ATTACK=77\n");
        ScoreManager manager = new ScoreManager(path);
        manager.loadBestScores();
        check("the mode missing from the file is 0", manager.getBestScore(GameMode.CLASSIC) == 0);
        check("the mode in the file is read", manager.getBestScore(GameMode.TIME_ATTACK) == 77);
    }

    private static void testSaveOnlyWhenChanged() {
        String path = newPath("changed.txt");
        ScoreManager manager = new ScoreManager(path);
        manager.saveBestScoresIfChanged();
        check("nothing changed: the file is not created", !new File(path).exists());

        manager.addPoints(30);
        check("a better score is reported as new best", manager.updateBestScore(GameMode.CLASSIC));
        check("a new best is marked as unsaved", manager.hasUnsavedBestScores());
        manager.saveBestScoresIfChanged();
        check("saving writes the file", new File(path).exists());
        check("saving clears the unsaved mark", !manager.hasUnsavedBestScores());

        check("the same score again is not a new best", !manager.updateBestScore(GameMode.CLASSIC));
        check("a lower score is not a new best", lowerScoreIsNotBest(manager));
        check("per mode: time attack is still 0", manager.getBestScore(GameMode.TIME_ATTACK) == 0);
    }

    private static boolean lowerScoreIsNotBest(ScoreManager manager) {
        manager.resetScore();
        manager.addPoints(10);
        return !manager.updateBestScore(GameMode.CLASSIC) && manager.getBestScore(GameMode.CLASSIC) == 30;
    }

    private static void testFileIsADirectory() {
        File folder = new File(testFolder, "iamafolder");
        folder.mkdirs();
        ScoreManager manager = new ScoreManager(folder.getPath());
        manager.loadBestScores();
        check("a folder instead of a file: load does not crash", manager.getBestScore(GameMode.CLASSIC) == 0);
        manager.addPoints(5);
        manager.updateBestScore(GameMode.CLASSIC);
        manager.saveBestScores();
        check("a folder instead of a file: save does not crash", true);
        check("failed save keeps the 'unsaved' mark", manager.hasUnsavedBestScores());
        check("the score is still usable in memory", manager.getBestScore(GameMode.CLASSIC) == 5);
    }

    private static void testBeatingTheBestAndRestarting() {
        String path = newPath("restart.txt");
        ScoreManager firstRun = new ScoreManager(path);
        firstRun.loadBestScores();
        firstRun.addPoints(250);
        firstRun.updateBestScore(GameMode.TIME_ATTACK);
        firstRun.saveBestScoresIfChanged();

        ScoreManager secondRun = new ScoreManager(path);
        secondRun.loadBestScores();
        check("after 'restarting the app' the best score is still there",
                secondRun.getBestScore(GameMode.TIME_ATTACK) == 250);
        check("the other mode was not touched", secondRun.getBestScore(GameMode.CLASSIC) == 0);
    }

    // ----- helpers -----

    private static String newPath(String name) {
        return new File(testFolder, name).getPath();
    }

    private static String writeFile(String name, String text) {
        File file = new File(testFolder, name);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file.getPath();
    }

    private static List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    private static List<String> list(String... values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }
}
