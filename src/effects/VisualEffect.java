package effects;

// Base class of every animation. It only knows about time: how long the effect lasts and how
// much of that time has passed. Each subclass decides what to draw (the abstract draw method
// from Drawable). An effect never touches the game; it is only decoration.
public abstract class VisualEffect implements Drawable {

    private final long durationMillis;
    private long elapsedMillis;

    protected VisualEffect(long durationMillis) {
        if (durationMillis <= 0) {
            throw new IllegalArgumentException("duration must be positive");
        }
        this.durationMillis = durationMillis;
    }

    // Moves the effect forward in time. Called once per game tick.
    public void update(long deltaMillis) {
        if (deltaMillis > 0) {
            elapsedMillis = Math.min(durationMillis, elapsedMillis + deltaMillis);
        }
    }

    public boolean isFinished() {
        return elapsedMillis >= durationMillis;
    }

    // 0.0 when the effect starts, 1.0 when it is over.
    public double getProgress() {
        return (double) elapsedMillis / durationMillis;
    }

    public long getDurationMillis() {
        return durationMillis;
    }
}
