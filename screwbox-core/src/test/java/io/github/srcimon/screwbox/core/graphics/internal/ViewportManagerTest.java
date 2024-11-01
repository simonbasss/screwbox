package io.github.srcimon.screwbox.core.graphics.internal;

import io.github.srcimon.screwbox.core.graphics.Camera;
import io.github.srcimon.screwbox.core.graphics.ScreenBounds;
import io.github.srcimon.screwbox.core.graphics.SplitScreenOptions;
import io.github.srcimon.screwbox.core.graphics.Viewport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.github.srcimon.screwbox.core.Vector.$;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ViewportManagerTest {

    @Mock
    Renderer renderer;

    ViewportManager viewportManager;

    @BeforeEach
    void setUp() {
        DefaultCanvas canvas = new DefaultCanvas(renderer, new ScreenBounds(20, 20, 640, 480));
        Camera camera = new DefaultCamera(canvas);
        camera.setZoom(3);
        camera.setPosition($(20, 90));
        camera.setZoomRestriction(0.1, 10);
        Viewport defaultViewport = new DefaultViewport(canvas, camera);
        viewportManager = new ViewportManager(defaultViewport, renderer);
    }

    @Test
    void enableSplitScreen_oneScreen_setsSplitscreenModeEnabled() {
        viewportManager.enableSplitscreenMode(SplitScreenOptions.viewports(2));

        assertThat(viewportManager.isSplitscreenModeEnabled()).isTrue();
    }

    @Test
    void disableSplitScreen_splitScreenEnabled_setsSplitscreenModeEnabledFalse() {
        viewportManager.enableSplitscreenMode(SplitScreenOptions.viewports(2));

        viewportManager.disableSplitscreenMode();

        assertThat(viewportManager.isSplitscreenModeEnabled()).isFalse();
    }

    @Test
    void disableSplitScreen_splitscreenModeEnabled_appliesCameraChangesOfFirstViewportCameraToDefaultViewport() {
        viewportManager.enableSplitscreenMode(SplitScreenOptions.viewports(2));

        viewportManager.viewport(0).ifPresent(viewport -> viewport.camera()
                .setPosition($(100, 100)).setZoomRestriction(1, 4).setZoom(3.0));

        viewportManager.disableSplitscreenMode();

        assertThat(viewportManager.defaultViewport().camera().position()).isEqualTo($(100, 100));
        assertThat(viewportManager.defaultViewport().camera().zoom()).isEqualTo(3.0);
        assertThat(viewportManager.defaultViewport().camera().minZoom()).isEqualTo(1.0);
        assertThat(viewportManager.defaultViewport().camera().maxZoom()).isEqualTo(4.0);
    }
}
