package audio;

import util.FileLogger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

// Loads the four sound effects as Clips and plays them. A Clip keeps the whole sound in memory,
// so playing it again is instant, which is right for short effects.
// The game must work without sound: a missing file, a damaged file or no speakers at all only
// writes a line to the log. No method of this class throws an exception.
public class SoundManager {

    private final Map<SoundEffect, Clip> clips = new EnumMap<>(SoundEffect.class);
    private boolean muted;

    public SoundManager() {
        this.muted = false;
    }

    // Loads every sound. Call it once at startup. Sounds that fail to load are simply left out.
    public void loadSounds() {
        for (Clip oldClip : clips.values()) {
            oldClip.close();
        }
        clips.clear();

        for (SoundEffect effect : SoundEffect.values()) {
            Clip clip = openClip(effect.getPath());
            if (clip != null) {
                clips.put(effect, clip);
            }
        }
    }

    // True if this sound was loaded and can be played.
    public boolean isLoaded(SoundEffect effect) {
        return clips.containsKey(effect);
    }

    // Plays the sound from the start. If it is still playing, it restarts.
    // Does nothing when muted, when the sound did not load, or when playing fails.
    public void play(SoundEffect effect) {
        if (muted) {
            return;
        }
        Clip clip = clips.get(effect);
        if (clip == null) {
            return;
        }

        try {
            clip.stop();
            clip.setFramePosition(0);   // rewind
            clip.start();
        } catch (RuntimeException e) {
            FileLogger.log("Could not play sound " + effect + ": " + e);
        }
    }

    // Switches the sound on or off. Muting also silences sounds that are playing right now.
    // Returns true if the sound is now muted.
    public boolean toggleMute() {
        muted = !muted;
        if (muted) {
            for (Clip clip : clips.values()) {
                clip.stop();
            }
        }
        return muted;
    }

    public boolean isMuted() {
        return muted;
    }

    // Opens one WAV file from the resources as a Clip. Returns null if anything goes wrong.
    // Protected so a test can replace it with a fake clip.
    protected Clip openClip(String path) {
        URL url = SoundManager.class.getResource("/" + path);
        if (url == null) {
            FileLogger.log("Sound not found: " + path);
            return null;
        }

        try (AudioInputStream stream = AudioSystem.getAudioInputStream(url)) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (UnsupportedAudioFileException e) {
            FileLogger.log("Sound format not supported (use a PCM .wav file): " + path);
        } catch (IOException e) {
            FileLogger.log("Could not read sound " + path + ": " + e);
        } catch (LineUnavailableException e) {
            FileLogger.log("Audio device is busy, no sound for " + path + ": " + e);
        } catch (RuntimeException e) {
            // Java reports "no sound card / no speakers" as an IllegalArgumentException,
            // and a blocked audio system as a SecurityException.
            FileLogger.log("No audio available, no sound for " + path + ": " + e);
        }
        return null;
    }
}
