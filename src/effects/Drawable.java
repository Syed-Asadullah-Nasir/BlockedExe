package effects;

import java.awt.Graphics2D;

// Anything that can draw itself. GamePanel keeps a list of Drawables and calls draw() on each,
// without knowing which kind it is. That is polymorphism: the same call, different behaviour.
public interface Drawable {

    void draw(Graphics2D g2);
}
