package de.suzufa.screwbox.examples.gameoflife.systems;

import de.suzufa.screwbox.core.Duration;
import de.suzufa.screwbox.core.Engine;
import de.suzufa.screwbox.core.Grid;
import de.suzufa.screwbox.core.Grid.Node;
import de.suzufa.screwbox.core.entities.Archetype;
import de.suzufa.screwbox.core.entities.EntitySystem;
import de.suzufa.screwbox.core.utils.Timer;
import de.suzufa.screwbox.examples.gameoflife.components.GridComponent;

public class GridUpdateSystem implements EntitySystem {

    private static final Archetype GRID_HOLDER = Archetype.of(GridComponent.class);
    private final Timer timer = Timer.withInterval(Duration.ofMillis(100));

    @Override
    public void update(final Engine engine) {
        engine.loop().setTargetFps(9999);
        if (timer.isTick()) {
            final var gridComponent = engine.entities().forcedFetch(GRID_HOLDER).get(GridComponent.class);
            Grid oldGrid = gridComponent.grid;
            final Grid grid = oldGrid.cleared();

            for (final Node node : oldGrid.nodes()) {
                final int count = oldGrid.blockedNeighbors(node).size();
                if (oldGrid.isFree(node)) {
                    if (count == 3) {
                        grid.block(node);
                    }
                } else if (count == 2 || count == 3) {
                    grid.block(node);
                }
            }
            gridComponent.grid = grid;
        }
    }

}
