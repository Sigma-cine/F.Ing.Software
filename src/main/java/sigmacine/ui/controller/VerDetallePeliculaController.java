package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.beans.value.ChangeListener;
import sigmacine.dominio.entity.Pelicula;

public class VerDetallePeliculaController extends ContenidoCarteleraController {
   
    @FXML private StackPane trailerContainer;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Label timeLabel;
    private boolean isUpdatingSlider = false;
    
    // Variables estáticas para control global de trailers
    private static MediaPlayer currentGlobalPlayer = null;
    @Override
    public void setPelicula(Pelicula p) {
        // Detener cualquier trailer global que se esté reproduciendo
        stopCurrentGlobalPlayer();
        
        super.setPelicula(p);
        
        if (p != null) {
            cargarTrailer(p);
        }
    }
    
    private void cargarTrailer(Pelicula pelicula) {
        if (trailerContainer == null) return;
        
        try {
            trailerContainer.getChildren().clear();
            
            // Limpiar MediaPlayer anterior si existe
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
            
            String trailerUrl = pelicula.getTrailer();
            
            if (trailerUrl == null || trailerUrl.trim().isEmpty()) {
                mostrarMensajeNoTrailer();
                return;
            }
            
            java.net.URL resourceUrl = getClass().getResource(trailerUrl);
            
            if (resourceUrl == null) {
                mostrarMensajeNoTrailer();
                return;
            }
            
            Media media = new Media(resourceUrl.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);
            
            // Configurar el MediaPlayer para que siempre inicie desde el principio
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.seek(javafx.util.Duration.ZERO);
                mediaPlayer.stop();
                // Configurar el slider de progreso
                progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
            });
            
            // Listener para actualizar la barra de progreso
            mediaPlayer.currentTimeProperty().addListener((ChangeListener<javafx.util.Duration>) (observable, oldValue, newValue) -> {
                if (!isUpdatingSlider && progressSlider != null) {
                    isUpdatingSlider = true;
                    progressSlider.setValue(newValue.toSeconds());
                    updateTimeLabel(newValue, mediaPlayer.getTotalDuration());
                    isUpdatingSlider = false;
                }
            });
            
            mediaView.setFitWidth(600);
            mediaView.setFitHeight(360);
            mediaView.setPreserveRatio(true);
            
            VBox videoContainer = crearVideoConControles();
            trailerContainer.getChildren().add(videoContainer);
            
        } catch (Exception e) {
            mostrarMensajeNoTrailer();
        }
    }
    
    private VBox crearVideoConControles() {
        VBox container = new VBox(10);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        
        container.getChildren().add(mediaView);
        
        // Barra de progreso
        progressSlider = new Slider(0, 100, 0);
        progressSlider.setPrefWidth(580);
        progressSlider.setStyle("-fx-accent: #8A2F24;");
        
        // Listener para cuando el usuario mueve la barra de progreso
        progressSlider.setOnMousePressed(e -> isUpdatingSlider = true);
        progressSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(javafx.util.Duration.seconds(progressSlider.getValue()));
            }
            isUpdatingSlider = false;
        });
        
        // Etiqueta de tiempo
        timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        // Controles de reproducción
        HBox controles = new HBox(10);
        controles.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button playBtn = new Button("▶");
        Button pauseBtn = new Button("⏸");
        Button stopBtn = new Button("⏹");
        
        // Estilo para los botones con color rojo del proyecto
        String buttonStyle = "-fx-font-size: 16px; -fx-padding: 8 12 8 12; -fx-background-color: #8A2F24; -fx-text-fill: white; -fx-background-radius: 5;";
        playBtn.setStyle(buttonStyle);
        pauseBtn.setStyle(buttonStyle);
        stopBtn.setStyle(buttonStyle);
        
        // Control de volumen
        HBox volumeBox = new HBox(5);
        volumeBox.setAlignment(javafx.geometry.Pos.CENTER);
        Label volumeLabel = new Label("🔊");
        volumeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(100);
        volumeSlider.setStyle("-fx-accent: #8A2F24;");
        
        // Listener para el control de volumen
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue());
            }
        });
        
        volumeBox.getChildren().addAll(volumeLabel, volumeSlider);
        
        playBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                // Detener cualquier otro player y establecer este como actual
                stopCurrentGlobalPlayer();
                currentGlobalPlayer = mediaPlayer;
                mediaPlayer.play();
            }
        });
        
        pauseBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });
        
        stopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.seek(javafx.util.Duration.ZERO);
                if (currentGlobalPlayer == mediaPlayer) {
                    currentGlobalPlayer = null;
                }
            }
        });
        
        controles.getChildren().addAll(playBtn, pauseBtn, stopBtn);
        
        container.getChildren().addAll(progressSlider, timeLabel, controles, volumeBox);
        
        return container;
    }
    
    private void updateTimeLabel(javafx.util.Duration currentTime, javafx.util.Duration totalTime) {
        if (timeLabel != null && currentTime != null && totalTime != null) {
            String current = formatTime(currentTime);
            String total = formatTime(totalTime);
            timeLabel.setText(current + " / " + total);
        }
    }
    
    private String formatTime(javafx.util.Duration duration) {
        if (duration == null) return "00:00";
        
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private void mostrarMensajeNoTrailer() {
        if (trailerContainer == null) return;
        
        trailerContainer.getChildren().clear();
        Label lblNoTrailer = new Label("No hay trailer disponible para esta película");
        lblNoTrailer.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        trailerContainer.getChildren().add(lblNoTrailer);
    }
    
    // Método estático para detener cualquier trailer global
    public static void stopCurrentGlobalPlayer() {
        if (currentGlobalPlayer != null) {
            try {
                if (currentGlobalPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    currentGlobalPlayer.pause();
                }
            } catch (Exception e) {
                System.err.println("Error deteniendo MediaPlayer: " + e.getMessage());
            }
            currentGlobalPlayer = null;
        }
    }
}