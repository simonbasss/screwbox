package de.suzufa.screwbox.core.audio.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.suzufa.screwbox.core.audio.Sound;

public class AudioAdapter {

    public Clip createClip(final Sound sound) {
        try (InputStream inputStream = new ByteArrayInputStream(sound.content())) {
            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)) {
                final Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                return clip;
            }
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            throw new IllegalStateException("could not create sound", e);
        }
    }
}
