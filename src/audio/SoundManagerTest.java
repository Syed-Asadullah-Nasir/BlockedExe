package audio;

import util.Constants;

import javax.sound.sampled.Clip;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Tests the logic of SoundManager without needing speakers: every Clip is replaced by a fake
// that only writes down which methods were called on it.
public class SoundManagerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testEffectPaths();
        testAllSoundsLoad();
        testPlayRewindsAndStarts();
        testPlayingTwiceRestarts();
        testOnlyTheRequestedSoundPlays();
        testMute();
        testMuteBeforeLoading();
        testMissingSoundDoesNotCrash();
        testNoAudioAtAll();
        testBrokenClipDoesNotCrash();
        testPlayNull();
        testLoadingTwiceClosesOldClips();
        testRealManagerWithoutFilesOrSpeakers();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
    }

    // ----- tests -----

    private static void testEffectPaths() {
        check("there are 4 sound effects", SoundEffect.values().length == 4);
        check("SELECT uses the select path", SoundEffect.SELECT.getPath().equals(Constants.SOUND_SELECT));
        check("PLACE uses the place path", SoundEffect.PLACE.getPath().equals(Constants.SOUND_PLACE));
        check("CLEAR uses the clear path", SoundEffect.CLEAR.getPath().equals(Constants.SOUND_CLEAR));
        check("INVALID uses the invalid path", SoundEffect.INVALID.getPath().equals(Constants.SOUND_INVALID));
    }

    private static void testAllSoundsLoad() {
        FakeSoundManager manager = new FakeSoundManager();
        manager.loadSounds();
        boolean allLoaded = true;
        for (SoundEffect effect : SoundEffect.values()) {
            allLoaded = allLoaded && manager.isLoaded(effect);
        }
        check("all four sounds are loaded", allLoaded);
    }

    private static void testPlayRewindsAndStarts() {
        FakeSoundManager manager = new FakeSoundManager();
        manager.loadSounds();
        manager.play(SoundEffect.PLACE);
        check("play = stop, rewind to 0, start",
                manager.callsOf(SoundEffect.PLACE).equals(list("stop", "setFramePosition(0)", "start")));
    }

    private static void testPlayingTwiceRestarts() {
        FakeSoundManager manager = new FakeSoundManager();
        manager.loadSounds();
        manager.play(SoundEffect.SELECT);
        manager.play(SoundEffect.SELECT);
        check("playing twice restarts the sound from the beginning",
                manager.callsOf(SoundEffect.SELECT).equals(list(
                        "stop", "setFramePosition(0)", "start",
                        "stop", "setFramePosition(0)", "start")));
    }

    private static void testOnlyTheRequestedSoundPlays() {
        FakeSoundManager manager = new FakeSoundManager();
        manager.loadSounds();
        manager.play(SoundEffect.CLEAR);
        check("the other sounds are not touched",
                manager.callsOf(SoundEffect.SELECT).isEmpty()
                        && manager.callsOf(SoundEffect.PLACE).isEmpty()
                        && manager.callsOf(SoundEffect.INVALID).isEmpty());
    }

    private static void testMute() {
        FakeSoundManager manager = new FakeSoundManager();
        manager.loadSounds();
        check("sound starts unmuted", !manager.isMuted());

        manager.play(SoundEffect.PLACE);
        int callsBeforeMute = manager.callsOf(SoundEffect.PLACE).size();

        boolean nowMuted = manager.toggleMute();
        check("toggleMute returns true when muting", nowMuted && manager.isMuted());
        check("muting stops every clip", everyClipWasStopped(manager));

        int callsAfterMuting = manager.callsOf(SoundEffect.PLACE).size();
        manager.play(SoundEffect.PLACE);
        manager.play(SoundEffect.INVALID);
        check("nothing is played while muted",
                manager.callsOf(SoundEffect.PLACE).size() == callsAfterMuting
                        && !manager.callsOf(SoundEffect.INVALID).contains("start"));
        check("muting did not start anything", callsAfterMuting == callsBeforeMute + 1);

        boolean nowUnmuted = !manager.toggleMute();
        check("toggleMute returns false when unmuting", nowUnmuted && !manager.isMuted());
        manager.play(SoundEffect.INVALID);
        check("sound plays again after unmuting", manager.callsOf(SoundEffect.INVALID).contains("start"));
    }

    private static void testMuteBeforeLoading() {
        SoundManager manager = new SoundManager();
        check("toggleMute works before loading", manager.toggleMute() && manager.isMuted());
        check("toggleMute back works", !manager.toggleMute() && !manager.isMuted());
    }

    private static void testMissingSoundDoesNotCrash() {
        FakeSoundManager manager = new FakeSoundManager();
        manager.missingPaths.add(Constants.SOUND_INVALID);
        manager.loadSounds();
        check("a missing file is not loaded", !manager.isLoaded(SoundEffect.INVALID));
        check("the other files are loaded", manager.isLoaded(SoundEffect.PLACE)
                && manager.isLoaded(SoundEffect.SELECT) && manager.isLoaded(SoundEffect.CLEAR));

        boolean noCrash = true;
        try {
            manager.play(SoundEffect.INVALID);
        } catch (RuntimeException e) {
            noCrash = false;
        }
        check("playing a sound that did not load does nothing", noCrash);
        manager.play(SoundEffect.PLACE);
        check("the sounds that did load still play", manager.callsOf(SoundEffect.PLACE).contains("start"));
    }

    private static void testNoAudioAtAll() {
        FakeSoundManager manager = new FakeSoundManager();
        for (SoundEffect effect : SoundEffect.values()) {
            manager.missingPaths.add(effect.getPath());
        }
        manager.loadSounds();

        boolean noCrash = true;
        try {
            for (SoundEffect effect : SoundEffect.values()) {
                manager.play(effect);
            }
            manager.toggleMute();
            manager.toggleMute();
        } catch (RuntimeException e) {
            noCrash = false;
        }
        check("the game runs silently when no sound could be loaded", noCrash);
    }

    private static void testBrokenClipDoesNotCrash() {
        FakeSoundManager manager = new FakeSoundManager();
        manager.brokenPaths.add(Constants.SOUND_CLEAR);
        manager.loadSounds();

        boolean noCrash = true;
        try {
            manager.play(SoundEffect.CLEAR);
        } catch (RuntimeException e) {
            noCrash = false;
        }
        check("a clip that fails to start does not crash the game", noCrash);
        manager.play(SoundEffect.PLACE);
        check("other sounds still work after a failure", manager.callsOf(SoundEffect.PLACE).contains("start"));
    }

    private static void testPlayNull() {
        FakeSoundManager manager = new FakeSoundManager();
        manager.loadSounds();
        boolean noCrash = true;
        try {
            manager.play(null);
        } catch (RuntimeException e) {
            noCrash = false;
        }
        check("play(null) does nothing", noCrash);
    }

    private static void testLoadingTwiceClosesOldClips() {
        FakeSoundManager manager = new FakeSoundManager();
        manager.loadSounds();
        manager.loadSounds();
        check("8 clips were opened in total", manager.allRecorders.size() == 8);
        boolean oldOnesClosed = true;
        for (int i = 0; i < 4; i++) {
            oldOnesClosed = oldOnesClosed && manager.allRecorders.get(i).calls.contains("close");
        }
        check("loading again closes the old clips", oldOnesClosed);
        check("the new clips are not closed", !manager.allRecorders.get(4).calls.contains("close"));
        check("all four sounds are loaded after reloading", manager.isLoaded(SoundEffect.SELECT)
                && manager.isLoaded(SoundEffect.INVALID));
    }

    // The real SoundManager must survive missing files and a computer without speakers.
    // It is muted first so this test makes no noise even if your sound files are found.
    private static void testRealManagerWithoutFilesOrSpeakers() {
        boolean noCrash = true;
        try {
            SoundManager manager = new SoundManager();
            manager.loadSounds();
            manager.toggleMute();
            for (SoundEffect effect : SoundEffect.values()) {
                manager.play(effect);
            }
        } catch (RuntimeException e) {
            noCrash = false;
        }
        check("the real SoundManager does not crash (files or speakers may be missing)", noCrash);
    }

    // ----- fakes and helpers -----

    // Remembers the names of the methods called on a clip.
    private static class ClipRecorder implements InvocationHandler {

        private final List<String> calls = new ArrayList<>();
        private final boolean failOnStart;

        ClipRecorder(boolean failOnStart) {
            this.failOnStart = failOnStart;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                return handleObjectMethod(proxy, name, args);
            }

            if (name.equals("setFramePosition")) {
                calls.add(name + "(" + args[0] + ")");
            } else {
                calls.add(name);
            }
            if (failOnStart && name.equals("start")) {
                throw new IllegalStateException("broken audio line");
            }
            return defaultValue(method.getReturnType());
        }

        private Object handleObjectMethod(Object proxy, String name, Object[] args) {
            if (name.equals("equals")) {
                return proxy == args[0];
            }
            if (name.equals("hashCode")) {
                return System.identityHashCode(proxy);
            }
            return "FakeClip";
        }

        private Object defaultValue(Class<?> type) {
            if (type == boolean.class) {
                return Boolean.FALSE;
            }
            if (type == int.class) {
                return 0;
            }
            if (type == long.class) {
                return 0L;
            }
            if (type == float.class) {
                return 0f;
            }
            if (type == double.class) {
                return 0d;
            }
            return null;
        }
    }

    // A SoundManager that creates fake clips instead of opening real files.
    private static class FakeSoundManager extends SoundManager {

        private final Set<String> missingPaths = new HashSet<>();
        private final Set<String> brokenPaths = new HashSet<>();
        private final List<ClipRecorder> allRecorders = new ArrayList<>();
        private final List<String> openedPaths = new ArrayList<>();

        @Override
        protected Clip openClip(String path) {
            if (missingPaths.contains(path)) {
                return null;
            }
            ClipRecorder recorder = new ClipRecorder(brokenPaths.contains(path));
            allRecorders.add(recorder);
            openedPaths.add(path);
            return (Clip) Proxy.newProxyInstance(
                    Clip.class.getClassLoader(), new Class<?>[]{Clip.class}, recorder);
        }

        // The calls made on the newest clip of this sound.
        List<String> callsOf(SoundEffect effect) {
            for (int i = openedPaths.size() - 1; i >= 0; i--) {
                if (openedPaths.get(i).equals(effect.getPath())) {
                    return allRecorders.get(i).calls;
                }
            }
            return new ArrayList<>();
        }
    }

    private static boolean everyClipWasStopped(FakeSoundManager manager) {
        for (SoundEffect effect : SoundEffect.values()) {
            if (!manager.callsOf(effect).contains("stop")) {
                return false;
            }
        }
        return true;
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
