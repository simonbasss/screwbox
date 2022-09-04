package de.suzufa.screwbox.core.graphics;

import static de.suzufa.screwbox.core.graphics.internal.ImageUtil.applyFilter;
import static de.suzufa.screwbox.core.graphics.internal.ImageUtil.scale;
import static de.suzufa.screwbox.core.graphics.internal.ImageUtil.toBufferedImage;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import de.suzufa.screwbox.core.Duration;
import de.suzufa.screwbox.core.graphics.internal.AwtMapper;
import de.suzufa.screwbox.core.graphics.internal.ImageUtil;
import de.suzufa.screwbox.core.utils.Resources;

public final class Frame implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Frame INVISIBLE = new Frame(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));

    private final Duration duration;
    private final ImageIcon imageCont;

    /**
     * Returns an invisible {@link Frame}.
     */
    public static Frame invisible() {
        return INVISIBLE;
    }

    // TODO: javadoc and test
    public static Frame fromFile(final String fileName) {
        return new Frame(imageFromFile(fileName));
    }

    public Frame(final Image image) {
        this(image, Duration.none());
    }

    public Frame(final Image image, final Duration duration) {
        this.imageCont = new ImageIcon(image);
        this.duration = duration;
    }

    // TODO: javadoc and test
    public Frame subFrame(final Offset offset, final Dimension size) {
        final var image = ImageUtil.toBufferedImage(image());
        final var subImage = image.getSubimage(offset.x(), offset.y(), size.width(), size.height());
        return new Frame(subImage);
    }

    public Image image() {
        return imageCont.getImage();
    }

    public Duration duration() {
        return duration;
    }

    /**
     * Returns the size of the frames {@link #image()}.
     */
    public Dimension size() {
        return Dimension.of(imageCont.getIconWidth(), imageCont.getIconHeight());
    }

    /**
     * Returns the {@link Color} of the pixel at the given position.
     * 
     * @throws IllegalArgumentException when position is out of bounds.
     * @see #colorAt(int, int)
     */
    public Color colorAt(final Offset offset) {
        return colorAt(offset.x(), offset.y());
    }

    /**
     * Returns the {@link Color} of the pixel at the given position.
     * 
     * @throws IllegalArgumentException when position is out of bounds.
     * @see #colorAt(Dimension)
     */
    public Color colorAt(final int x, final int y) {
        final Image image = image();
        if (x < 0 || x > image.getWidth(null) || y < 0 || y > image.getHeight(null)) {
            throw new IllegalArgumentException(format("Position is out of bounds: %d:%d", x, y));
        }
        final BufferedImage bufferedImage = toBufferedImage(image);
        final int rgb = bufferedImage.getRGB(x, y);
        final java.awt.Color awtColor = new java.awt.Color(rgb, true);
        return AwtMapper.toColor(awtColor);
    }

    /**
     * Returns a new instance. The new {@link Frame}s old {@link Color} is replaced
     * with a new one. This method is quite slow.
     */
    public Frame replaceColor(final Color oldColor, final Color newColor) {
        final Image oldImage = imageCont.getImage();
        final Image newImage = applyFilter(oldImage, new ReplaceColorFilter(oldColor, newColor));
        return new Frame(newImage);
    }

    /**
     * Returns a scaled version of the current {@link Frame}.
     */
    public Frame scaled(final double scale) {
        return new Frame(scale(image(), scale));
    }

    static BufferedImage imageFromFile(final String fileName) {
        final byte[] imageData = Resources.loadBinary(fileName);
        try (var inputStream = new ByteArrayInputStream(imageData)) {
            final BufferedImage image = ImageIO.read(inputStream);
            if (isNull(image)) {
                throw new IllegalArgumentException("image cannot be read: " + fileName);
            }
            return image;

        } catch (final IOException e) {
            throw new IllegalArgumentException("error while reading image: " + fileName, e);
        }
    }
}