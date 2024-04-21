package io.github.srcimon.screwbox.core.scenes.internal;

import io.github.srcimon.screwbox.core.Engine;
import io.github.srcimon.screwbox.core.environment.internal.DefaultEnvironment;
import io.github.srcimon.screwbox.core.scenes.Scene;

public class SceneContainer {
    private final Scene scene;
    private final DefaultEnvironment environment;
    boolean isInitialized;

    SceneContainer(final Scene scene, final Engine engine) {
        this.scene = scene;
        this.environment = new DefaultEnvironment(engine);
    }

    void initialize() {
        scene.populate(environment);
        isInitialized = true;
    }

    public DefaultEnvironment environment() {
        return environment;
    }

    public boolean sameSceneAs(Class<? extends Scene> sceneClass) {
        return scene.getClass().equals(sceneClass);
    }

    public Scene scene() {
        return scene;
    }
}