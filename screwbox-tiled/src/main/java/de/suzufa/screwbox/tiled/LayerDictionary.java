package de.suzufa.screwbox.tiled;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.suzufa.screwbox.tiled.internal.entity.LayerEntity;
import de.suzufa.screwbox.tiled.internal.entity.MapEntity;

public class LayerDictionary {

    private final List<Layer> layers = new ArrayList<>();

    LayerDictionary(final MapEntity mapEntity) {
        int order = 0;
        for (final LayerEntity layerEntity : mapEntity.getLayers()) {
            layers.add(new Layer(layerEntity, order));
            order++;
        }
    }

    public List<Layer> allLayers() {
        return layers;
    }

    public Optional<Layer> findByName(String name) {
        return layers.stream()
                .filter(l -> "name".equals(l.name()))
                .findFirst();
    }
}
