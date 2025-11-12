package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import sigmacine.dominio.entity.Pelicula;

public class VerDetallePeliculaController extends ContenidoCarteleraController {
   
    @FXML private StackPane trailerContainer;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    
    @Override
    public void setPelicula(Pelicula p) {
        super.setPelicula(p);
        
        if (p != null) {
            cargarTrailer(p);
        }
    }
    
    private void cargarTrailer(Pelicula pelicula) {
        if (trailerContainer == null) return;
        
        try {
            trailerContainer.getChildren().clear();
            
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
        
        HBox controles = new HBox(10);
        controles.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button playBtn = new Button("Play");
        Button pauseBtn = new Button("Pausa");
        Button stopBtn = new Button("Stop");
        
        playBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
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
            }
        });
        
        controles.getChildren().addAll(playBtn, pauseBtn, stopBtn);
        container.getChildren().add(controles);
        
        return container;
    }
    
    private void mostrarMensajeNoTrailer() {
        if (trailerContainer == null) return;
        
        trailerContainer.getChildren().clear();
        Label lblNoTrailer = new Label("No hay trailer disponible para esta película");
        lblNoTrailer.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        trailerContainer.getChildren().add(lblNoTrailer);
    }
}