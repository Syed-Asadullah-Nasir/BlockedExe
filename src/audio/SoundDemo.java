package audio;

// TEMPORARY manual test. Run it to hear your four sound files and to see which ones load.
// Delete it before the final submission, like ResourceTestWindow.
public class SoundDemo {

    public static void main(String[] args) throws InterruptedException {
        SoundManager manager = new SoundManager();
        manager.loadSounds();

        for (SoundEffect effect : SoundEffect.values()) {
            String status = manager.isLoaded(effect) ? "loaded" : "NOT LOADED (read logs/game.log)";
            System.out.println(effect + " (" + effect.getPath() + "): " + status);
        }

        for (SoundEffect effect : SoundEffect.values()) {
            if (manager.isLoaded(effect)) {
                System.out.println("Playing " + effect);
                manager.play(effect);
                Thread.sleep(1200);
            }
        }
        System.exit(0);
    }
}
