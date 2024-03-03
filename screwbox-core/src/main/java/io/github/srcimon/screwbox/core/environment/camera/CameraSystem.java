package io.github.srcimon.screwbox.core.environment.camera;

import io.github.srcimon.screwbox.core.Bounds;
import io.github.srcimon.screwbox.core.Engine;
import io.github.srcimon.screwbox.core.Vector;
import io.github.srcimon.screwbox.core.environment.Archetype;
import io.github.srcimon.screwbox.core.environment.EntitySystem;
import io.github.srcimon.screwbox.core.environment.Order;
import io.github.srcimon.screwbox.core.environment.SystemOrder;
import io.github.srcimon.screwbox.core.environment.core.TransformComponent;

import static io.github.srcimon.screwbox.core.utils.MathUtil.clamp;

@Order(SystemOrder.PREPARATION)
public class CameraSystem implements EntitySystem {

    private static final Archetype TARGET = Archetype.of(CameraTargetComponent.class, TransformComponent.class);

    @Override
    public void update(final Engine engine) {
        engine.environment().tryFetchSingleton(TARGET).ifPresent(targetEntity -> {
            final var cameraPosition = engine.graphics().camera().position();
            final var targetBounds = targetEntity.get(TransformComponent.class).bounds;

            final var target = targetEntity.get(CameraTargetComponent.class);
            final var configuration = engine.environment().tryFetchSingletonComponent(CameraBoundsComponent.class);
            if (target.allowJumping && targetBounds.position().distanceTo(cameraPosition) > engine.graphics().world().visibleArea().width() / 2.0) {
                if(configuration.isPresent()) {
                    if(configuration.get().cameraBounds.expand(-targetBounds.extents().length()).contains(targetBounds.position())) {
                        engine.graphics().camera().setPosition(targetBounds.position());
                    }
                }
                return;
            }

            final Vector distance = cameraPosition
                    .substract(targetBounds.position())
                    .substract(target.shift);

            final Vector cameraMovement = distance.multiply(clamp(-1, engine.loop().delta(-1 * target.followSpeed), 1));

            if (configuration.isPresent()) {
                engine.graphics().camera().moveWithinVisualBounds(cameraMovement, configuration.get().cameraBounds);
            } else {
                engine.graphics().camera().move(cameraMovement);
            }
        });
    }
}
