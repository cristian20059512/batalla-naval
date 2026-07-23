package com.batallanaval.util;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Single point for the background music and the short sound effects
 * (button hover/click) of the whole application. The music player is
 * static on purpose: it must survive switching screens (menu -&gt; game -&gt;
 * menu), something a MediaPlayer tied to a single Scene could not do on
 * its own.
 *
 * If an audio file does not exist yet (for example, while assets are still
 * being gathered), each method fails silently with a log warning: sound is
 * an extra, it must never keep the game from working.
 */
public final class SoundManager {

    private static final Logger LOG = Logger.getLogger(SoundManager.class.getName());
    private static final String SOUNDS_PATH = "/com/batallanaval/view/sounds/";

    private static MediaPlayer musicPlayer;
    private static AudioClip hoverClip;
    private static AudioClip clickClip;

    private SoundManager() {
    }

    /** Starts the background music on loop (low volume, so it doesn't drown out the effects). No-op if it was already playing. */
    public static void playBackgroundMusic() {
        if (musicPlayer != null) {
            return;
        }
        URL resource = SoundManager.class.getResource(SOUNDS_PATH + "musica-fondo.mp3");
        if (resource == null) {
            LOG.warning("No se encontro musica-fondo.mp3 en resources/.../view/sounds; sigue sin musica.");
            return;
        }
        musicPlayer = new MediaPlayer(new Media(resource.toExternalForm()));
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.setVolume(0.35);
        musicPlayer.play();
    }

    /**
     * Attaches the hover and click sounds to any {@link Button} in the given
     * Scene, without having to touch every button in the FXML one by one: a
     * single Scene-level event filter is enough for the buttons already
     * present and the ones added later.
     */
    public static void attachButtonSounds(Scene scene) {
        scene.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, event -> {
            if (event.getTarget() instanceof Button) {
                playEffect(hoverClip, "hover.wav", clip -> hoverClip = clip);
            }
        });
        scene.addEventFilter(ActionEvent.ACTION, event -> {
            if (event.getTarget() instanceof Button) {
                playEffect(clickClip, "click.wav", clip -> clickClip = clip);
            }
        });
    }

    private static void playEffect(AudioClip cached, String fileName, java.util.function.Consumer<AudioClip> cacheSetter) {
        AudioClip clip = cached;
        if (clip == null) {
            URL resource = SoundManager.class.getResource(SOUNDS_PATH + fileName);
            if (resource == null) {
                LOG.log(Level.WARNING, "No se encontro el efecto de sonido: {0}", fileName);
                return;
            }
            clip = new AudioClip(resource.toExternalForm());
            cacheSetter.accept(clip);
        }
        clip.play();
    }
}
