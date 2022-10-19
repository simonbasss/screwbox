package de.suzufa.screwbox.core.graphics;

import java.util.EventListener;

public interface GraphicsConfigurationListener extends EventListener {

    enum ConfigurationProperty {
        RESOLUTION,
        WINDOW_MODE,
        ANTIALIASING,
        LIGHTMAP_BLUR,
        LIGHTMAP_RESOLUTION
    }

    void configurationChanged(ConfigurationProperty changedProperty);
}
