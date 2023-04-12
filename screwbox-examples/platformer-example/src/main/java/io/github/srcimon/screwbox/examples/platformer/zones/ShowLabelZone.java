package io.github.srcimon.screwbox.examples.platformer.zones;

import io.github.srcimon.screwbox.core.entities.Archetype;
import io.github.srcimon.screwbox.core.entities.Entity;
import io.github.srcimon.screwbox.core.entities.SourceImport.Converter;
import io.github.srcimon.screwbox.core.entities.components.SignalComponent;
import io.github.srcimon.screwbox.core.entities.components.TransformComponent;
import io.github.srcimon.screwbox.core.entities.components.TriggerAreaComponent;
import io.github.srcimon.screwbox.examples.platformer.components.LabelComponent;
import io.github.srcimon.screwbox.examples.platformer.components.PlayerMarkerComponent;
import io.github.srcimon.screwbox.tiled.GameObject;

public class ShowLabelZone implements Converter<GameObject> {

    @Override
    public Entity convert(GameObject object) {
        String label = object.properties().force("label");
        Integer size = object.properties().getInt("size").orElse(16);
        return new Entity().add(
                new SignalComponent(),
                new LabelComponent(label, size),
                new TriggerAreaComponent(Archetype.of(PlayerMarkerComponent.class, TransformComponent.class)),
                new TransformComponent(object.bounds()));
    }

}
