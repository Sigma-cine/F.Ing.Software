package sigmacine.dominio.entity;

public class PeliculaTrailer {
    private long id;
    private long peliculaId;
    private String url;

    public PeliculaTrailer() {}

    public PeliculaTrailer(long id, long peliculaId, String url) {
        this.id = id;
        this.peliculaId = peliculaId;
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPeliculaId() {
        return peliculaId;
    }

    public void setPeliculaId(long peliculaId) {
        this.peliculaId = peliculaId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}