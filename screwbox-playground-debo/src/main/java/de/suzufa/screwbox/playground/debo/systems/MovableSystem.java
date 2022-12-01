package de.suzufa.screwbox.playground.debo.systems;

import static de.suzufa.screwbox.core.utils.MathUtil.modifier;

import java.util.Optional;

import de.suzufa.screwbox.core.Engine;
import de.suzufa.screwbox.core.Vector;
import de.suzufa.screwbox.core.entities.Archetype;
import de.suzufa.screwbox.core.entities.Entity;
import de.suzufa.screwbox.core.entities.EntitySystem;
import de.suzufa.screwbox.core.entities.Order;
import de.suzufa.screwbox.core.entities.SystemOrder;
import de.suzufa.screwbox.core.entities.components.PhysicsBodyComponent;
import de.suzufa.screwbox.core.entities.components.TransformComponent;
import de.suzufa.screwbox.core.physics.Borders;
import de.suzufa.screwbox.playground.debo.components.MovableComponent;
import de.suzufa.screwbox.playground.debo.components.PlayerMarkerComponent;

@Order(SystemOrder.SIMULATION_BEGIN)
public class MovableSystem implements EntitySystem {

    private static final Archetype PLAYER = Archetype.of(PlayerMarkerComponent.class, TransformComponent.class);
    private static final Archetype MOVABLES = Archetype.of(MovableComponent.class, PhysicsBodyComponent.class,
            TransformComponent.class);

    @Override
    public void update(Engine engine) {
        Entity player = engine.entities().forcedFetch(PLAYER);
        var playerMomentum = player.get(PhysicsBodyComponent.class).momentum;
        var playerPosition = player.get(TransformComponent.class).bounds.position();

        Optional<Entity> playerMovingBlock = engine.physics()
                .raycastFrom(playerPosition)
                .checkingFor(MOVABLES)
                .checkingBorders(Borders.VERTICAL_ONLY)
                .castingHorizontal(10 * modifier(playerMomentum.x()))
                .selectAnyEntity();

        if (playerMovingBlock.isPresent()) {
            Entity entity = playerMovingBlock.get();
            var physicsBody = entity.get(PhysicsBodyComponent.class);
            var movable = entity.get(MovableComponent.class);
            physicsBody.momentum = Vector.of(movable.maxSpeed * modifier(playerMomentum.x()), physicsBody.momentum.y());
        }
    }
}
