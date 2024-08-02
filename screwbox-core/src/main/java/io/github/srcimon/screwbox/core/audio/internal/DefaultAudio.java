package io.github.srcimon.screwbox.core.audio.internal;

import io.github.srcimon.screwbox.core.Percent;
import io.github.srcimon.screwbox.core.Vector;
import io.github.srcimon.screwbox.core.audio.Audio;
import io.github.srcimon.screwbox.core.audio.AudioConfiguration;
import io.github.srcimon.screwbox.core.audio.AudioConfigurationEvent;
import io.github.srcimon.screwbox.core.audio.AudioConfigurationListener;
import io.github.srcimon.screwbox.core.audio.Playback;
import io.github.srcimon.screwbox.core.audio.Sound;
import io.github.srcimon.screwbox.core.audio.SoundOptions;
import io.github.srcimon.screwbox.core.graphics.Camera;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static io.github.srcimon.screwbox.core.audio.AudioConfigurationEvent.ConfigurationProperty.EFFECTS_VOLUME;
import static io.github.srcimon.screwbox.core.audio.AudioConfigurationEvent.ConfigurationProperty.MUSIC_VOLUME;
import static io.github.srcimon.screwbox.core.utils.MathUtil.modifier;
import static java.util.Objects.requireNonNull;

public class DefaultAudio implements Audio, AudioConfigurationListener {

    private final ExecutorService executor;
    private final AudioAdapter audioAdapter;
    private final Camera camera;
    private final Map<Clip, Playback> playbacks = new ConcurrentHashMap<>();
    private final AudioConfiguration configuration = new AudioConfiguration().addListener(this);
    private final VolumeMonitor volumeMonitor;
    private final DataLinePool dataLinePool = new DataLinePool();

    public DefaultAudio(final ExecutorService executor, final AudioAdapter audioAdapter, final Camera camera) {
        this.executor = executor;
        this.audioAdapter = audioAdapter;
        this.camera = camera;
        this.volumeMonitor = new VolumeMonitor(executor, audioAdapter, configuration);
    }

    @Override
    public Audio stopAllSounds() {
        if (!executor.isShutdown()) {
            executor.execute(() -> {
                final List<ActivePlayback> playbacksToStop = new ArrayList<>(activePlayBacks.values());
                for (final ActivePlayback playback : playbacksToStop) {
                    playback.isShutdown = true;
                }
            });
        }
        return this;
    }

    @Override
    public Percent microphoneLevel() {
        return volumeMonitor.level();
    }

    @Override
    public boolean isMicrophoneActive() {
        return volumeMonitor.isActive();
    }

    @Override
    public Audio playSound(final Sound sound, final Vector position) {
        requireNonNull(position, "position must not be null");
        final var distance = camera.position().distanceTo(position);
        final var direction = modifier(position.x() - camera.position().x());
        final var quotient = distance / configuration.soundRange();
        final var options = SoundOptions.playOnce()
                .pan(direction * quotient)
                .volume(Percent.of(1 - quotient));

        playSound(sound, options, position);
        return this;
    }

    @Override
    public List<Playback> activePlaybacks() {
        final List<Playback> playbacks = new ArrayList<>();
        for(var playback : activePlayBacks.values()) {
            playbacks.add(playback.playback);
        }
        return playbacks;
    }

    @Override
    public Audio playSound(final Sound sound, final SoundOptions options) {
        playSound(sound, options, null);
        return this;
    }

    @Override
    public Audio stopSound(final Sound sound) {
        for(final  var playback : fetchActivePlaybacksFor(sound)) {
            playback.isShutdown = true;
        }
        return this;
    }

    private class ActivePlayback {
        private boolean isShutdown = false;
        private Playback playback;
        private SourceDataLine line;

        public ActivePlayback(Playback playback, SourceDataLine line) {
            this.playback = playback;
            this.line = line;
        }

    }

    private final Map<UUID, ActivePlayback> activePlayBacks = new ConcurrentHashMap<>();

    private void playSound(final Sound sound, final SoundOptions options, final Vector position) {
        requireNonNull(sound, "sound must not be null");
        requireNonNull(options, "options must not be null");
        final Percent configVolume = options.isMusic() ? musicVolume() : effectVolume();
        final Percent volume = configVolume.multiply(options.volume().value());
        if (!volume.isZero()) {
            var id = UUID.randomUUID();

            executor.execute(() -> {

                try (var stream = AudioAdapter.getAudioInputStream(sound.content())) {
                    var format = stream.getFormat();
                    var line = dataLinePool.getLine(format);

                    ActivePlayback activePlayback = new ActivePlayback(new Playback(sound, options, position), line);
                    activePlayBacks.put(id, activePlayback);

                    audioAdapter.setVolume(line, volume);
                    audioAdapter.setBalance(line, options.balance());
                    audioAdapter.setPan(line, options.pan());
                    byte[] bufferBytes = new byte[4096];
                    int readBytes = -1;
                    while ((readBytes = stream.read(bufferBytes)) != -1 && !activePlayback.isShutdown) {
                        line.write(bufferBytes, 0, readBytes);
                    }
                    dataLinePool.freeLine(line);
                    //TODO implement looping
                    //TODO implement stopping
                    //TODO implemnt audio change while playing
                    //TODO preload soundbundle into pool?
                    //TODO implement  playbacks.put(clip, new Playback(sound, options, position));
                    activePlayBacks.remove(id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        }
    }

    @Override
    public int activeCount(final Sound sound) {
        return fetchActivePlaybacksFor(sound).size();
    }

    @Override
    public boolean isActive(final Sound sound) {
        for (final var activeSound : activePlayBacks.values()) {
            if (activeSound.playback.sound().equals(sound)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int activeCount() {
        return activePlayBacks.size();
    }

    @Override
    public AudioConfiguration configuration() {
        return configuration;
    }

    @Override
    public void configurationChanged(final AudioConfigurationEvent event) {
        if (MUSIC_VOLUME.equals(event.changedProperty())) {
            for(final var playback : activePlayBacks.values()) {
                if(playback.playback.options().isMusic()) {
                    audioAdapter.setVolume(playback.line, musicVolume().multiply(playback.playback.options().volume().value()));
                }
            }

        } else if (EFFECTS_VOLUME.equals(event.changedProperty())) {
            for(final var playback : activePlayBacks.values()) {
                if(playback.playback.options().isEffect()) {
                    audioAdapter.setVolume(playback.line, effectVolume().multiply(playback.playback.options().volume().value()));
                }
            }
        }
    }

    private List<ActivePlayback> fetchActivePlaybacksFor(final Sound sound) {
        final List<ActivePlayback> playbacks = new ArrayList<>();
        for (final var playback : activePlayBacks.values()) {
            if (playback.playback.sound().equals(sound)) {
                playbacks.add(playback);
            }
        }
        return playbacks;
    }

    private Percent musicVolume() {
        return configuration.isMusicMuted() ? Percent.zero() : configuration.musicVolume();
    }

    private Percent effectVolume() {
        return configuration.areEffectsMuted() ? Percent.zero() : configuration.effectVolume();
    }

}