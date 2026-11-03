package util;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

public final class ResourceLoader {

    private ResourceLoader() {
    }

    // Returns the image, or null if it cannot be loaded.
    public static BufferedImage loadImage(String path) {
        try {
            URL url = ResourceLoader.class.getResource("/" + path);
            if (url == null) {
                FileLogger.log("Image not found: " + path);
                return null;
            }
            BufferedImage image = ImageIO.read(url);
            if (image == null) {
                FileLogger.log("Image could not be decoded: " + path);
            }
            return image;
        } catch (Exception e) {
            FileLogger.log("Failed to load image " + path + ": " + e);
            return null;
        }
    }

    // Returns the custom font at the given size, or a default font if loading fails.
    public static Font loadFont(String path, float size) {
        try (InputStream stream = ResourceLoader.class.getResourceAsStream("/" + path)) {
            if (stream == null) {
                FileLogger.log("Font not found: " + path);
                return createDefaultFont(size);
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, stream);
            return baseFont.deriveFont(size);
        } catch (Exception e) {
            FileLogger.log("Failed to load font " + path + ": " + e);
            return createDefaultFont(size);
        }
    }

    private static Font createDefaultFont(float size) {
        return new Font("SansSerif", Font.PLAIN, Math.round(size));
    }
}
