package io.github.srcimon.screwbox.core.environment.rendering;

import io.github.srcimon.screwbox.core.Bounds;
import io.github.srcimon.screwbox.core.Engine;
import io.github.srcimon.screwbox.core.Vector;
import io.github.srcimon.screwbox.core.environment.Archetype;
import io.github.srcimon.screwbox.core.environment.Entity;
import io.github.srcimon.screwbox.core.environment.EntitySystem;
import io.github.srcimon.screwbox.core.environment.Order;
import io.github.srcimon.screwbox.core.environment.SystemOrder;
import io.github.srcimon.screwbox.core.environment.core.TransformComponent;
import io.github.srcimon.screwbox.core.graphics.Offset;
import io.github.srcimon.screwbox.core.graphics.Screen;
import io.github.srcimon.screwbox.core.graphics.Size;
import io.github.srcimon.screwbox.core.graphics.Sprite;
import io.github.srcimon.screwbox.core.graphics.SpriteDrawOptions;
import io.github.srcimon.screwbox.core.graphics.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Order(SystemOrder.PRESENTATION_WORLD)
public class RenderSystem implements EntitySystem {

    public class Batch {

        private final List<BatchEntry> entries = new ArrayList<>();

        public record BatchEntry(Sprite sprite, Vector position, SpriteDrawOptions options, int drawOrder)
                implements Comparable<BatchEntry> {

            @Override
            public int compareTo(final BatchEntry o) {
                return Integer.compare(drawOrder, o.drawOrder);
            }
        }

        public void addEntry(final Sprite sprite, final Vector position, SpriteDrawOptions options, final int drawOrder) {
            this.entries.add(new BatchEntry(sprite, position, options, drawOrder));
        }

        public List<BatchEntry> entriesInDrawOrder() {
            Collections.sort(entries);
            return entries;
        }

    }

    private static final Archetype RENDERS = Archetype.of(RenderComponent.class, TransformComponent.class);

    @Override
    public void update(final Engine engine) {
        final Batch batch = new Batch();
        final World world = engine.graphics().world();
        final Bounds visibleArea = world.visibleArea();

        for (final Entity entity : engine.environment().fetchAll(RENDERS)) {
            final RenderComponent render = entity.get(RenderComponent.class);
            final double width = render.sprite.size().width() * render.options.scale();
            final double height = render.sprite.size().height() * render.options.scale();
            final var spriteBounds = Bounds.atPosition(entity.position(), width, height);

            if (spriteBounds.intersects(visibleArea)) {
                batch.addEntry(render.sprite, spriteBounds.origin(), render.options, render.drawOrder);
            }
        }
        Screen screen = engine.graphics().screen();
        double zoom = engine.graphics().camera().zoom();
        Vector cameraPosition = engine.graphics().camera().position();

        for (final var entry : batch.entriesInDrawOrder()) {
            final SpriteDrawOptions scaledOptions = entry.options().scale(entry.options().scale() * zoom);
            screen.drawSprite(entry.sprite, toOffset(entry.position, zoom, cameraPosition, screen.size()), scaledOptions);
        }
    }

    private Offset toOffset(final Vector position, final  double zoom, final Vector cameraPosition, Size screenSize) {
        final double x = (position.x() - cameraPosition.x()) * zoom + (screenSize.width() / 2.0);
        final double y = (position.y() - cameraPosition.y()) * zoom + (screenSize.height() / 2.0);
        return Offset.at(x, y);
    }
}
