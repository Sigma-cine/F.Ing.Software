package sigmacine.dominio.entity;

public class Pelicula {
    private int id;
    private String titulo;
    private String genero;
    private String clasificacion;
    private int duracion;
    private String director;   
    private String reparto;
    private String trailer;
    private String sinopsis;
    private String estado;
    public String posterUrl;
    public Pelicula() {}

    public Pelicula(int id, String titulo, String genero, String clasificacion,int duracion, String director, String reparto,
                    String trailer, String sinopsis, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.genero = genero;
        this.clasificacion = clasificacion;
        this.duracion = duracion;
        this.director = director;
        this.reparto = reparto;
        this.trailer = trailer;
        this.sinopsis = sinopsis;
        this.estado = estado;
    }

    public Pelicula(int id, String titulo, String genero, String clasificacion,
                    int duracion, String director, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.genero = genero;
        this.clasificacion = clasificacion;
        this.duracion = duracion;
        this.director = director;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }

    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getReparto() { return reparto; }
    public void setReparto(String reparto) { this.reparto = reparto; }

    public String getTrailer() { return trailer; }
    public void setTrailer(String trailer) { this.trailer = trailer; }

    public String getSinopsis() { return sinopsis; }
    public void setSinopsis(String sinopsis) { this.sinopsis = sinopsis; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
}
