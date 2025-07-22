package org.kamatech.chess;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight image‑utility class using only standard JDK APIs.
 * Supports animations and mock mode for testing.
 */
public class Img implements Cloneable {
    private BufferedImage img;

    // Animation support
    private List<BufferedImage> frames;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private long frameDuration = 100; // milliseconds per frame

    // Mock mode for testing
    private boolean mockMode = false;
    private String lastLoadedPath;
    private List<String> operations;

    public Img() {
        this.img = null;
        this.frames = new ArrayList<>();
        this.operations = new ArrayList<>();
    }

    public Img(boolean mockMode) {
        this();
        this.mockMode = mockMode;
    }

    public BufferedImage getImage() {
        updateAnimation();
        return img;
    }

    /**
     * Set the image directly
     */
    public void setImage(BufferedImage image) {
        this.img = image;
        if (mockMode) {
            operations.add("SET_IMAGE: " + (image != null ? image.getWidth() + "x" + image.getHeight() : "null"));
        }
    }

    /* ----------- Animation methods ----------- */
    public void addFrame(BufferedImage frame) {
        frames.add(frame);
        if (img == null) {
            img = frame; // Set first frame as current
        }
    }

    public void addFrame(String path) {
        if (mockMode) {
            operations.add("addFrame:" + path);
            return;
        }
        try {
            BufferedImage frame = ImageIO.read(new File(path));
            if (frame != null) {
                addFrame(frame);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load frame: " + path);
        }
    }

    public void setFrameDuration(long duration) {
        this.frameDuration = duration;
    }

    private void updateAnimation() {
        if (frames.size() > 1) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameTime > frameDuration) {
                currentFrame = (currentFrame + 1) % frames.size();
                img = frames.get(currentFrame);
                lastFrameTime = currentTime;
            }
        }
    }

    /* ----------- Mock mode methods ----------- */
    public boolean isMockMode() {
        return mockMode;
    }

    public List<String> getOperations() {
        return new ArrayList<>(operations);
    }

    public String getLastLoadedPath() {
        return lastLoadedPath;
    }

    /* ----------- load & optional resize ----------- */
    public Img read(String path,
            Dimension targetSize,
            boolean keepAspect,
            Object interpolation /* ignored */) {
        if (mockMode) {
            operations.add("read:" + path + " target:" + targetSize + " keepAspect:" + keepAspect);
            lastLoadedPath = path;
            // Create a dummy image for mock mode
            img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            return this;
        }

        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load image: " + path);
        }
        if (img == null)
            throw new IllegalArgumentException("Unsupported image: " + path);

        lastLoadedPath = path;

        if (targetSize != null) {
            int tw = targetSize.width, th = targetSize.height;
            int w = img.getWidth(), h = img.getHeight();

            int nw, nh;
            if (keepAspect) {
                double s = Math.min(tw / (double) w, th / (double) h);
                nw = (int) Math.round(w * s);
                nh = (int) Math.round(h * s);
            } else {
                nw = tw;
                nh = th;
            }

            BufferedImage dst = new BufferedImage(
                    nw, nh,
                    img.getColorModel().hasAlpha()
                            ? BufferedImage.TYPE_INT_ARGB
                            : BufferedImage.TYPE_INT_RGB);

            Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, nw, nh, null);
            g.dispose();
            img = dst;
        }
        return this;
    }

    public Img read(String path) {
        return read(path, null, false, null);
    }

    /* ----------- draw this image onto another ----------- */
    public void drawOn(Img other, int x, int y) {
        if (mockMode || other.mockMode) {
            operations.add("drawOn: x=" + x + " y=" + y + " other=" + other.hashCode());
            if (other.mockMode) {
                other.operations.add("receiveDraw: from=" + this.hashCode() + " x=" + x + " y=" + y);
            }
            return;
        }

        if (img == null || other.img == null)
            throw new IllegalStateException("Both images must be loaded.");

        if (x + img.getWidth() > other.img.getWidth()
                || y + img.getHeight() > other.img.getHeight())
            throw new IllegalArgumentException("Patch exceeds destination bounds.");

        Graphics2D g = other.img.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(img, x, y, null);
        g.dispose();
    }

    /* ----------- annotate with text ----------- */
    public void putText(String txt, int x, int y, float fontSize,
            Color color, int thickness /* unused in Java2D */) {
        if (mockMode) {
            operations.add("putText: '" + txt + "' x=" + x + " y=" + y + " size=" + fontSize + " color=" + color);
            return;
        }

        if (img == null)
            throw new IllegalStateException("Image not loaded.");

        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(color);
        g.setFont(img.getGraphics().getFont().deriveFont(fontSize * 12));
        g.drawString(txt, x, y);
        g.dispose();
    }

    /* ----------- display in a Swing window ----------- */
    public void show() {
        if (mockMode) {
            operations.add("show: displayed window");
            return;
        }

        if (img == null)
            throw new IllegalStateException("Image not loaded.");

        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Image");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new JLabel(new ImageIcon(img)));
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    @Override
    public Img clone() {
        try {
            Img cloned = (Img) super.clone();

            // Clone the main image
            if (this.img != null) {
                ColorModel cm = this.img.getColorModel();
                boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
                WritableRaster raster = this.img.copyData(null);
                cloned.img = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
            }

            // Clone animation frames
            cloned.frames = new ArrayList<>();
            for (BufferedImage frame : this.frames) {
                if (frame != null) {
                    ColorModel cm = frame.getColorModel();
                    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
                    WritableRaster raster = frame.copyData(null);
                    cloned.frames.add(new BufferedImage(cm, raster, isAlphaPremultiplied, null));
                }
            }

            // Clone operations list for mock mode
            cloned.operations = new ArrayList<>(this.operations);

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
}
