package io.github.srcimon.screwbox.core.assets;

import io.github.srcimon.screwbox.core.Duration;
import io.github.srcimon.screwbox.core.Percent;
import io.github.srcimon.screwbox.core.environment.tweening.TweenMode;
import io.github.srcimon.screwbox.core.graphics.Offset;
import io.github.srcimon.screwbox.core.graphics.Screen;
import io.github.srcimon.screwbox.core.graphics.Sprite;
import io.github.srcimon.screwbox.core.graphics.SpriteDrawOptions;
import io.github.srcimon.screwbox.core.scenes.SceneTransition;

public enum SceneTransitionBundle implements AssetBundle<SceneTransition> {

    FADEOUT(SceneTransition.noExtro().intro(new SceneTransition.IntroAnimation() {
        @Override
        public void draw(Screen screen, Percent progress, Sprite screenshot) {
            screen.drawSprite(screenshot, Offset.origin(), SpriteDrawOptions.originalSize().opacity(progress));
        }
    }, Duration.ofMillis(500))),
    SLIDE_UP(SceneTransition.noExtro()
            .intro(new SceneTransition.IntroAnimation() {
        @Override
        public void draw(Screen screen, Percent progress, Sprite screenshot) {
            screen.drawSprite(screenshot, Offset.origin().addY((int) (screen.size().height() * (1 -progress.value()))), SpriteDrawOptions.originalSize());
        }
    }, Duration.ofMillis(1000)));//TODO simplify duration changes

    private final SceneTransition sceneTransition;

    SceneTransitionBundle(final SceneTransition sceneTransition) {
        this.sceneTransition = sceneTransition;
    }

    @Override
    public Asset<SceneTransition> asset() {
        return Asset.asset(() -> sceneTransition);
    }
}
