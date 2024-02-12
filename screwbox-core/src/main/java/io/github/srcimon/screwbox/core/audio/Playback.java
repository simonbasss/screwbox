package io.github.srcimon.screwbox.core.audio;

import io.github.srcimon.screwbox.core.Duration;
import io.github.srcimon.screwbox.core.Percent;
import io.github.srcimon.screwbox.core.Time;
import io.github.srcimon.screwbox.core.Vector;

import java.util.Optional;

//TODO Test and javadoc
public class Playback {

    private final Sound sound;
    private final SoundOptions options;
    private final boolean isMusic;
    private final Vector position;
    private final Time start = Time.now();

    public Playback(final Sound sound, final SoundOptions options, final boolean isMusic, final Vector position) {
        this.sound = sound;
        this.options = options;
        this.isMusic = isMusic;
        this.position = position;
            }

            public Percent done() {
        return  Percent.of(Duration.between(start, Time.now()).nanos() * 1.0 / sound.duration().nanos()  );
            }
    public boolean isEffect() {
        return !isMusic;
    }

    public Sound sound() {
        return sound;
    }

    public SoundOptions options() {
        return options;
    }

    public boolean isMusic() {
        return isMusic;
    }

    public Optional<Vector> position() {
        return Optional.ofNullable(position);
    }
}