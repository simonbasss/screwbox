package io.github.simonbas.screwbox.core.test;

import io.github.simonbas.screwbox.core.Engine;
import io.github.simonbas.screwbox.core.entities.internal.DefaultEntities;
import io.github.simonbas.screwbox.core.graphics.Graphics;
import io.github.simonbas.screwbox.core.graphics.Screen;
import io.github.simonbas.screwbox.core.graphics.World;
import io.github.simonbas.screwbox.core.keyboard.Keyboard;
import io.github.simonbas.screwbox.core.log.Log;
import io.github.simonbas.screwbox.core.loop.Loop;
import io.github.simonbas.screwbox.core.physics.Physics;
import io.github.simonbas.screwbox.core.window.Window;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntitiesExtension implements Extension, BeforeEachCallback, ParameterResolver {

    private final Map<Class<?>, Object> parameters = new HashMap<>();
    private DefaultEntities entities;

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        final var gameLoop = Mockito.mock(Loop.class);
        final var engine = Mockito.mock(Engine.class);
        final var graphics = Mockito.mock(Graphics.class);
        final var world = Mockito.mock(World.class);
        final var log = Mockito.mock(Log.class);
        final var physics = mock(Physics.class);
        final var keyboard = Mockito.mock(Keyboard.class);
        final var window = Mockito.mock(Window.class);
        final var screen = Mockito.mock(Screen.class);
        entities = new DefaultEntities(engine);

        // resolve a real entity engine with many mocked subsystems
        when(engine.entities()).thenReturn(entities);

        // resolve mocks for any subsystem
        when(engine.graphics()).thenReturn(graphics);
        when(engine.log()).thenReturn(log);
        when(engine.physics()).thenReturn(physics);
        when(engine.loop()).thenReturn(gameLoop);
        when(engine.keyboard()).thenReturn(keyboard);
        when(graphics.world()).thenReturn(world);
        when(graphics.window()).thenReturn(window);
        when(graphics.screen()).thenReturn(screen);

        // resolve test method parameters
        parameters.put(Loop.class, gameLoop);
        parameters.put(Graphics.class, graphics);
        parameters.put(Screen.class, screen);
        parameters.put(World.class, world);
        parameters.put(Window.class, window);
        parameters.put(Log.class, log);
        parameters.put(Physics.class, physics);
        parameters.put(Keyboard.class, keyboard);
        parameters.put(Engine.class, engine);
        parameters.put(DefaultEntities.class, entities);
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final var type = parameterContext.getParameter().getType();
        return parameters.containsKey(type);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final var type = parameterContext.getParameter().getType();
        return parameters.get(type);
    }

}
