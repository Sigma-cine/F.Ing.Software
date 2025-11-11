package sigmacine.aplicacion.data;

public class PeliculaDTO {
    private Integer idPelicula;
    private String  tituloPelicula;
    private String  generoPelicula;
    private String  clasificacionPelicula;
    private Integer duracionMinutos;
    private String  directorPelicula;
    private String  repartoPelicula;
    private String  urlTrailer;
    private String  sinopsisPelicula;
    private String  estadoPelicula;   // "ACTIVA" | "INACTIVA"
    private String  urlPoster;

    public Integer getIdPelicula() { return idPelicula; }
    public String  getTituloPelicula() { return tituloPelicula; }
    public String  getGeneroPelicula() { return generoPelicula; }
    public String  getClasificacionPelicula() { return clasificacionPelicula; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public String  getDirectorPelicula() { return directorPelicula; }
    public String  getRepartoPelicula() { return repartoPelicula; }
    public String  getUrlTrailer() { return urlTrailer; }
    public String  getSinopsisPelicula() { return sinopsisPelicula; }
    public String  getEstadoPelicula() { return estadoPelicula; }
    public String  getUrlPoster() { return urlPoster; }

    public void setIdPelicula(Integer idPelicula) { this.idPelicula = idPelicula; }
    public void setTituloPelicula(String tituloPelicula) { this.tituloPelicula = tituloPelicula; }
    public void setGeneroPelicula(String generoPelicula) { this.generoPelicula = generoPelicula; }
    public void setClasificacionPelicula(String clasificacionPelicula) { this.clasificacionPelicula = clasificacionPelicula; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public void setDirectorPelicula(String directorPelicula) { this.directorPelicula = directorPelicula; }
    public void setRepartoPelicula(String repartoPelicula) { this.repartoPelicula = repartoPelicula; }
    public void setUrlTrailer(String urlTrailer) { this.urlTrailer = urlTrailer; }
    public void setSinopsisPelicula(String sinopsisPelicula) { this.sinopsisPelicula = sinopsisPelicula; }
    public void setEstadoPelicula(String estadoPelicula) { this.estadoPelicula = estadoPelicula; }
    public void setUrlPoster(String urlPoster) { this.urlPoster = urlPoster; }
}
