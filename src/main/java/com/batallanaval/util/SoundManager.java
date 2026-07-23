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
 * Punto unico para la musica de fondo y los efectos de sonido cortos
 * (hover/clic de botones) de toda la aplicacion. El reproductor de musica
 * es estatico a proposito: debe sobrevivir al cambio de pantalla (menu -&gt;
 * juego -&gt; menu), algo que un MediaPlayer ligado a una sola Scene no
 * podria hacer por si solo.
 *
 * Si un archivo de audio todavia no existe (por ejemplo, mientras se
 * consiguen los recursos), cada metodo falla en silencio con un aviso en el
 * log: el sonido es un extra, nunca debe impedir que el juego funcione.
 */
public final class SoundManager {

    private static final Logger LOG = Logger.getLogger(SoundManager.class.getName());
    private static final String SOUNDS_PATH = "/com/batallanaval/view/sounds/";

    private static MediaPlayer musicPlayer;
    private static AudioClip hoverClip;
    private static AudioClip clickClip;

    private SoundManager() {
    }

    /** Arranca la musica de fondo en bucle (volumen bajo, para no tapar los efectos). Es un no-op si ya estaba sonando. */
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
     * Engancha el sonido de hover y de clic a cualquier {@link Button} de la
     * Scene dada, sin tener que tocar cada boton del FXML uno por uno: un
     * solo filtro de eventos a nivel de Scene basta para los botones
     * presentes y los que se agreguen despues.
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
