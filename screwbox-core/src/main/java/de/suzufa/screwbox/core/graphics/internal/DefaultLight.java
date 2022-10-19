package de.suzufa.screwbox.core.graphics.internal;

import static de.suzufa.screwbox.core.graphics.Offset.origin;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

import de.suzufa.screwbox.core.Angle;
import de.suzufa.screwbox.core.Bounds;
import de.suzufa.screwbox.core.Percentage;
import de.suzufa.screwbox.core.Vector;
import de.suzufa.screwbox.core.graphics.Color;
import de.suzufa.screwbox.core.graphics.GraphicsConfiguration;
import de.suzufa.screwbox.core.graphics.GraphicsConfigurationListener;
import de.suzufa.screwbox.core.graphics.Light;
import de.suzufa.screwbox.core.graphics.Offset;
import de.suzufa.screwbox.core.graphics.Sprite;
import de.suzufa.screwbox.core.graphics.Window;
import de.suzufa.screwbox.core.loop.internal.Updatable;

public class DefaultLight implements Light, Updatable, GraphicsConfigurationListener {

    private final ExecutorService executor; // TODO: use for async preperation
    private final Window window;
    private final LightPhysics lightPhysics;

    private Percentage ambientLight = Percentage.min();

    private Lightmap lightmap;
    private final DefaultWorld world;
    private Function<BufferedImage, BufferedImage> postFilter = new BlurImageFilter(3);
    private final List<Runnable> drawingTasks = new ArrayList<>();
    Future<Sprite> sprite = null;
    private final GraphicsConfiguration configuration;

    public DefaultLight(final Window window, final DefaultWorld world, final GraphicsConfiguration configuration,
            final ExecutorService executor) {
        this.executor = executor;
        this.window = window;
        this.world = world;
        this.configuration = configuration;
        this.lightPhysics = new LightPhysics(world);
        configuration.registerListener(this);
        initializeLightmap();
    }

    @Override
    public Light updateObstacles(final List<Bounds> obstacles) {
        lightPhysics.setObstacles(obstacles);
        return this;
    }

    @Override
    public List<Bounds> obstacles() {
        return lightPhysics.obstacles();
    }

    @Override
    public Light addPointLight(final Vector position, final double range, final Color color) {
        final Bounds lightBox = Bounds.atPosition(position, range, range);
        if (isVisible(lightBox)) {
            drawingTasks.add(() -> {
                final List<Offset> area = lightPhysics.calculateArea(lightBox);
                lightmap.addPointLight(world.toOffset(position), world.toDistance(range), area, color);
            });

        }
        return this;
    }

    @Override
    public Light addSpotLight(final Vector position, final double range, final Color color) {
        final Bounds lightBox = Bounds.atPosition(position, range, range);
        if (isVisible(lightBox)) {
            drawingTasks.add(() -> lightmap.addSpotLight(world.toOffset(position), world.toDistance(range), color));

        }
        return this;
    }

    @Override
    public void update() {
        initializeLightmap();
    }

    private void initializeLightmap() {
        if (lightmap != null) {
            lightmap.close();
        }
        lightmap = new Lightmap(window.size(), configuration.lightmapResolution(), configuration.isUseAntialising());
    }

    @Override
    public Light drawLightmap() {
        if (sprite == null) {
            throw new IllegalStateException("lightmap has not been sealed yet");
        }
        // TODO: if not seal create but log warning on first time
        try {
            window.drawSprite(sprite.get(), origin(), lightmap.resolution(), ambientLight.invert(), Angle.none());
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("error receiving lightmap sprite", e);
        }
        return this;
    }

    @Override
    public Light seal() {
        sprite = executor.submit(() -> {
            for (final var drawingTask : drawingTasks) {
                drawingTask.run();
            }
            drawingTasks.clear();
            final var image = lightmap.createSprite();
            final var filtered = postFilter.apply(image);
            return Sprite.fromImage(filtered);
        });
        return this;
    }

    @Override
    public Light setAmbientLight(final Percentage ambientLight) {
        // TODO: non null
        this.ambientLight = ambientLight;
        return this;
    }

    @Override
    public Percentage ambientLight() {
        return ambientLight;
    }

    @Override
    public void configurationChanged(final ConfigurationProperty changedProperty) {
        if (ConfigurationProperty.LIGHTMAP_BLUR.equals(changedProperty)) {
            postFilter = configuration.lightmapBlur() == 0
                    ? doNothing -> doNothing
                    : new BlurImageFilter(configuration.lightmapBlur());
        }

    }

    private boolean isVisible(final Bounds lightBox) {
        return window.isVisible(world.toWindowBounds(lightBox));
    }

}
