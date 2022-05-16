package de.suzufa.screwbox.core.audio.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import de.suzufa.screwbox.core.Percentage;
import de.suzufa.screwbox.core.audio.Audio;
import de.suzufa.screwbox.core.audio.Sound;
import de.suzufa.screwbox.core.audio.SoundPool;

public class DefaultAudio implements Audio, LineListener {

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Set<Clip> activeClips = new HashSet<>();
    private Percentage effectVolume = Percentage.max();
    private Percentage musicVolume = Percentage.max();

    @Override
    public Audio playMusic(final Sound sound) {
        playClip(sound.getClip(), musicVolume, true);
        return this;
    }

    @Override
    public Audio playEffect(final Sound sound) {
        if (!sound.isActive()) {
            playClip(sound.getClip(), effectVolume, false);
        }
        return this;
    }

    @Override
    public Sound playEffectLooped(final Sound sound) {
        if (!sound.isActive()) {
            playClip(sound.getClip(), effectVolume, true);
        }
        return sound;
    }

    @Override
    public Sound playEffectLooped(final SoundPool soundPool) {
        return playEffectLooped(soundPool.next());
    }

    @Override
    public Audio setEffectVolume(final Percentage volume) {
        this.effectVolume = volume;
        return this;
    }

    @Override
    public Audio setMusicVolume(final Percentage volume) {
        this.musicVolume = volume;
        return this;
    }

    public void shutdown() {
        synchronized (this) {
            executorService.shutdown();
        }
    }

    @Override
    public Audio stopAllAudio() {
        if (!executorService.isShutdown()) {
            executorService.execute(() -> {
                final List<Clip> clipsToStop = new ArrayList<>(activeClips);
                for (final Clip clip : clipsToStop) {
                    clip.stop();
                }
                activeClips.clear();
            });
        }
        return this;
    }

    @Override
    public Audio resume(final Sound sound) {
        executorService.execute(() -> start(sound.getClip(), sound.isLooped()));
        return this;
    }

    @Override
    public Audio stop(final Sound sound) {
        executorService.execute(() -> sound.getClip().stop());
        return this;
    }

    @Override
    public void update(final LineEvent event) {
        if (event.getType().equals(LineEvent.Type.STOP)) {
            activeClips.remove(event.getSource());
        } else if (event.getType().equals(LineEvent.Type.START)) {
            activeClips.add((Clip) event.getSource());
        }
    }

    private void playClip(final Clip clip, final Percentage volume, final boolean looped) {
        activeClips.add(clip);
        clip.setFramePosition(0);
        clip.addLineListener(this);
        final FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20f * (float) Math.log10(volume.value()));
        executorService.execute(() -> start(clip, looped));
    }

    private void start(final Clip clip, final boolean looped) {
        if (looped) {
            clip.loop(Integer.MAX_VALUE);
        } else {
            clip.start();
        }
    }

    @Override
    public Audio playEffect(final SoundPool soundPool) {
        final Sound sound = soundPool.next();
        playEffect(sound);
        return this;
    }

}
