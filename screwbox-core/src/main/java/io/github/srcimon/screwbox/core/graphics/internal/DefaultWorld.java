package io.github.srcimon.screwbox.core.graphics.internal;

import io.github.srcimon.screwbox.core.Bounds;
import io.github.srcimon.screwbox.core.Percent;
import io.github.srcimon.screwbox.core.Rotation;
import io.github.srcimon.screwbox.core.Vector;
import io.github.srcimon.screwbox.core.graphics.*;
import io.github.srcimon.screwbox.core.utils.MathUtil;

import static java.util.Objects.isNull;

public class DefaultWorld implements World {

    private final Screen screen;

    private Vector cameraPosition = Vector.zero();
    private double zoom = 1;
    private double wantedZoom = zoom;
    private double minZoom = 0.5;
    private double maxZoom = 10;

    private Bounds visibleArea = Bounds.atOrigin(
            -Double.MAX_VALUE / 2,
            -Double.MAX_VALUE / 2,
            Double.MAX_VALUE,
            Double.MAX_VALUE);

    public DefaultWorld(final Screen screen) {
        this.screen = screen;
    }

    public void restrictZoomRangeTo(final double min, final double max) {
        if (min <= 0) {
            throw new IllegalArgumentException("min zoom must be positive");
        }
        if (min > max) {
            throw new IllegalArgumentException("max zoom must not be lower than min zoom");
        }
        this.minZoom = min;
        this.maxZoom = max;
    }

    public double wantedZoom() {
        return wantedZoom;
    }

    public double updateCameraZoom(final double zoom) {
        this.wantedZoom = MathUtil.clamp(minZoom, zoom, maxZoom);
        final double actualZoomValue = Math.floor(wantedZoom * 16.0) / 16.0;
        this.zoom = actualZoomValue;
        recalculateVisibleArea();
        return actualZoomValue;
    }

    public void updateCameraPosition(final Vector position) {
        this.cameraPosition = position;
        recalculateVisibleArea();
    }

    public void recalculateVisibleArea() {
        this.visibleArea = Bounds.atPosition(cameraPosition,
                screen.size().width() / zoom,
                screen.size().height() / zoom);
    }

    public Vector cameraPosition() {
        return cameraPosition;
    }

    @Override
    public World drawSprite(final Sprite sprite, final Vector origin, final double scale, final Percent opacity,
                            final Rotation rotation, final Flip flip, final Bounds clip) {
        final var offset = toOffset(origin);
        final var windowClipArea = isNull(clip) ? null : toScreen(clip);
        final var x = offset.x() - ((scale - 1) * sprite.size().width());
        final var y = offset.y() - ((scale - 1) * sprite.size().height());
        screen.drawSprite(sprite, Offset.at(x, y), scale * zoom, opacity, rotation, flip, windowClipArea);
        return this;
    }

    @Override
    public Bounds visibleArea() {
        return visibleArea;
    }

    @Override
    public World drawRectangle(final Bounds bounds, final RectangleDrawOptions options) {
        final Offset offset = toOffset(bounds.origin());
        final Size size = toDimension(bounds.size());
        screen.drawRectangle(offset, size, options);
        return this;
    }

    public double cameraZoom() {
        return zoom;
    }

    public Offset toOffset(final Vector position) {
        final double x = (position.x() - cameraPosition.x()) * zoom + (screen.size().width() / 2.0);
        final double y = (position.y() - cameraPosition.y()) * zoom + (screen.size().height() / 2.0);
        return Offset.at(x, y);
    }

    public Vector toPosition(final Offset offset) {
        final double x = (offset.x() - (screen.size().width() / 2.0)) / zoom + cameraPosition.x();
        final double y = (offset.y() - (screen.size().height() / 2.0)) / zoom + cameraPosition.y();

        return Vector.of(x, y);
    }

    @Override
    public World drawText(final Vector offset, final String text, final Font font, final Color color) {
        final Offset windowOffset = toOffset(offset);
        screen.drawText(windowOffset, text, font, color);
        return this;
    }

    @Override
    public World drawTextCentered(final Vector position, final String text, final Font font, final Color color) {
        final Offset offset = toOffset(position);
        screen.drawTextCentered(offset, text, font, color);
        return this;
    }

    @Override
    public World drawLine(final Vector from, final Vector to, final Color color) {
        screen.drawLine(toOffset(from), toOffset(to), color);
        return this;
    }

    @Override
    public World fillCircle(final Vector position, final double diameter, final Color color) {
        final Offset offset = toOffset(position);
        screen.fillCircle(offset, (int) (diameter * zoom), color);
        return this;
    }

    @Override
    public World drawCircle(Vector position, double diameter, Color color, final int strokeWidth) {
        final Offset offset = toOffset(position);
        screen.drawCircle(offset, (int) (diameter * zoom), color, strokeWidth);
        return this;
    }

    @Override
    public World drawTextCentered(final Vector position, final String text, final Pixelfont font,
                                  final Percent opacity, final double scale) {
        final Offset offset = toOffset(position);
        screen.drawTextCentered(offset, text, font, opacity, scale * zoom);
        return this;
    }

    @Override
    public World drawSpriteBatch(final SpriteBatch spriteBatch, final Bounds clip) {
        for (final SpriteBatch.SpriteBatchEntry entry : spriteBatch.entriesInDrawOrder()) {
            drawSprite(entry.sprite(),
                    entry.position(),
                    entry.scale(),
                    entry.opacity(),
                    entry.rotation(),
                    entry.flip(),
                    clip);
        }
        return this;
    }

    private Size toDimension(final Vector size) {
        final long x = Math.round(size.x() * zoom);
        final long y = Math.round(size.y() * zoom);
        return Size.of(x, y);
    }

    public ScreenBounds toScreen(final Bounds bounds) {
        final var offset = toOffset(bounds.origin());
        final var size = toDimension(bounds.size());
        return new ScreenBounds(offset, size);
    }

    public int toDistance(double distance) {
        return (int) Math.round(distance * zoom);
    }

    @Override
    public World drawFadingCircle(final Vector position, double diameter, final Color color) {
        if (diameter > 0) {
            screen.drawFadingCircle(toOffset(position), toDistance(diameter), color);
        }
        return this;
    }
}