package io.github.srcimon.screwbox.core.environment.tweening;

import io.github.srcimon.screwbox.core.Engine;
import io.github.srcimon.screwbox.core.environment.Archetype;
import io.github.srcimon.screwbox.core.environment.EntitySystem;

//TODO Tests and javadoc
public class TweenDestroySystem implements EntitySystem {

    private static final Archetype DESTROYABLES = Archetype.of(TweenDestroyComponent.class);

    @Override
    public void update(Engine engine) {
        for (final var destroyable : engine.environment().fetchAll(DESTROYABLES)) {
            if (!destroyable.hasComponent(TweenStateComponent.class)) {
                engine.environment().remove(destroyable);
            }
        }
    }
}
