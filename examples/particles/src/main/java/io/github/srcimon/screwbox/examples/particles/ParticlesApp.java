package io.github.srcimon.screwbox.examples.particles;

import io.github.srcimon.screwbox.core.Duration;
import io.github.srcimon.screwbox.core.Engine;
import io.github.srcimon.screwbox.core.ScrewBox;
import io.github.srcimon.screwbox.core.Vector;
import io.github.srcimon.screwbox.core.assets.ParticleDesignerBundle;
import io.github.srcimon.screwbox.core.environment.Archetype;
import io.github.srcimon.screwbox.core.environment.core.TransformComponent;
import io.github.srcimon.screwbox.core.environment.debug.LogFpsSystem;
import io.github.srcimon.screwbox.core.environment.particles.ParticleDebugSystem;
import io.github.srcimon.screwbox.core.environment.particles.ParticleEmitterComponent;
import io.github.srcimon.screwbox.core.keyboard.Key;

import static io.github.srcimon.screwbox.core.Duration.ofMillis;

public class ParticlesApp {

    public static void main(String[] args) {
        Engine screwBox = ScrewBox.createEngine("Particles");


        screwBox.environment()
                .addSystem(engine -> engine.window().setTitle("Particles: " + engine.particles().particleCount() + " / " + engine.particles().particlesSpawnCount()))
                .addSystem(engine -> {
                    engine.graphics().camera().move(engine.keyboard().wsadMovement(1000 * engine.loop().delta()));
                    if (engine.keyboard().isPressed(Key.SPACE)) {
                        engine.environment().toggleSystem(new ParticleDebugSystem());
                    }
                })
                .addSystem(engine -> {
                    if (engine.mouse().isPressedLeft()) {
                        engine.physics().searchAtPosition(engine.mouse().position())
                                .checkingFor(Archetype.of(TransformComponent.class, ParticleEmitterComponent.class))
                                .selectAny().ifPresent(e -> {
                                    e.get(ParticleEmitterComponent.class).isEnabled = !e.get(ParticleEmitterComponent.class).isEnabled;
                                });
                    }
                })
                //TODO nicer engine logo
                //TODO disable by distance
                .addEntity("particle emitter",
                        new TransformComponent(Vector.zero().addX(-200), 128, 128),
                        new ParticleEmitterComponent(ofMillis(40), ParticleEmitterComponent.SpawnMode.POSITION, ParticleDesignerBundle.CONFETTI.get()))
                .addEntity("particle emitter",
                        new TransformComponent(Vector.zero(), 128, 128),
                        new ParticleEmitterComponent(ofMillis(50), ParticleDesignerBundle.SMOKE))
                .addEntity("particle emitter",
                        new TransformComponent(Vector.zero().addX(200), 128, 128),
                        new ParticleEmitterComponent(ofMillis(50), ParticleDesignerBundle.SMOKE.get()
                                .startScale(2)
                                .chaoticMovement(5, Duration.ofMillis(20))))
                .addSystem(new LogFpsSystem())
                .enableRendering()
                .enablePhysics()
                .enableTweening()
                .enableParticles();

        screwBox.start();
    }
}