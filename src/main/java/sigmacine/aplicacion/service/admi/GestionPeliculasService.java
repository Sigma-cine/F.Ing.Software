package sigmacine.aplicacion.service.admi;

import sigmacine.aplicacion.data.PeliculaDTO;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.dominio.repository.PeliculaRepository;
import sigmacine.dominio.repository.admi.PeliculaAdminRepository;

import java.util.ArrayList;
import java.util.List;

public class GestionPeliculasService {

    private final PeliculaRepository      peliculaConsultaRepo;   // lecturas
    private final PeliculaAdminRepository peliculaAdminRepo;      // escrituras

    public GestionPeliculasService(PeliculaRepository peliculaConsultaRepo,
                                   PeliculaAdminRepository peliculaAdminRepo) {
        if (peliculaConsultaRepo == null) throw new IllegalArgumentException("peliculaConsultaRepo nulo");
        if (peliculaAdminRepo == null)   throw new IllegalArgumentException("peliculaAdminRepo nulo");
        this.peliculaConsultaRepo = peliculaConsultaRepo;
        this.peliculaAdminRepo    = peliculaAdminRepo;
    }

    // ===== Lecturas =====
    public List<PeliculaDTO> obtenerTodasLasPeliculas() {
        List<Pelicula> lista = peliculaConsultaRepo.buscarTodas();
        List<PeliculaDTO> salida = new ArrayList<PeliculaDTO>();
        for (int i = 0; i < lista.size(); i++) salida.add(convertirA_DTO(lista.get(i)));
        return salida;
    }

    public List<PeliculaDTO> buscarPeliculasPorTitulo(String textoBuscado) {
        List<Pelicula> lista = peliculaConsultaRepo.buscarPorTitulo(textoBuscado);
        List<PeliculaDTO> salida = new ArrayList<PeliculaDTO>();
        for (int i = 0; i < lista.size(); i++) salida.add(convertirA_DTO(lista.get(i)));
        return salida;
    }

    public List<PeliculaDTO> buscarPeliculasPorGenero(String generoBuscado) {
        List<Pelicula> lista = peliculaConsultaRepo.buscarPorGenero(generoBuscado);
        List<PeliculaDTO> salida = new ArrayList<PeliculaDTO>();
        for (int i = 0; i < lista.size(); i++) salida.add(convertirA_DTO(lista.get(i)));
        return salida;
    }

    // ===== Escrituras =====
    public PeliculaDTO crearNuevaPelicula(PeliculaDTO datosNuevaPelicula) {
        validarCamposPelicula(datosNuevaPelicula);
        Pelicula creada = peliculaAdminRepo.crearPelicula(convertirA_Entidad(datosNuevaPelicula));
        return convertirA_DTO(creada);
    }

    public PeliculaDTO actualizarPeliculaExistente(int idPelicula, PeliculaDTO datosActualizados) {
        validarCamposPelicula(datosActualizados);
        Pelicula actualizada = peliculaAdminRepo.actualizarPelicula(idPelicula, convertirA_Entidad(datosActualizados));
        return convertirA_DTO(actualizada);
    }

    public boolean eliminarPeliculaPorId(int idPelicula) {
        return peliculaAdminRepo.eliminarPelicula(idPelicula); // false si hay funciones futuras
    }

    // ===== Mapeos =====
    private PeliculaDTO convertirA_DTO(Pelicula entidad) {
        PeliculaDTO dto = new PeliculaDTO();
        dto.setIdPelicula(entidad.getId());
        dto.setTituloPelicula(entidad.getTitulo());
        dto.setGeneroPelicula(entidad.getGenero());
        dto.setClasificacionPelicula(entidad.getClasificacion());
        dto.setDuracionMinutos(entidad.getDuracion());
        dto.setDirectorPelicula(entidad.getDirector());
        dto.setRepartoPelicula(entidad.getReparto());
        dto.setUrlTrailer(entidad.getTrailer());
        dto.setSinopsisPelicula(entidad.getSinopsis());
        dto.setEstadoPelicula(entidad.getEstado());
        dto.setUrlPoster(entidad.getPosterUrl());
        return dto;
    }

    private Pelicula convertirA_Entidad(PeliculaDTO dto) {
        Pelicula entidad = new Pelicula();
        if (dto.getIdPelicula() != null) entidad.setId(dto.getIdPelicula());
        entidad.setTitulo(dto.getTituloPelicula());
        entidad.setGenero(dto.getGeneroPelicula());
        entidad.setClasificacion(dto.getClasificacionPelicula());
        if (dto.getDuracionMinutos() != null) entidad.setDuracion(dto.getDuracionMinutos());
        entidad.setDirector(dto.getDirectorPelicula());
        entidad.setReparto(dto.getRepartoPelicula());
        entidad.setTrailer(dto.getUrlTrailer());
        entidad.setSinopsis(dto.getSinopsisPelicula());
        entidad.setEstado(dto.getEstadoPelicula());
        entidad.setPosterUrl(dto.getUrlPoster());
        return entidad;
    }

    // ===== Validaciones =====
    private void validarCamposPelicula(PeliculaDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Datos de película nulos");
        if (dto.getTituloPelicula() == null || dto.getTituloPelicula().trim().isEmpty())
            throw new IllegalArgumentException("El título es obligatorio");
        if (dto.getClasificacionPelicula() == null || dto.getClasificacionPelicula().trim().isEmpty())
            throw new IllegalArgumentException("La clasificación es obligatoria");
        if (dto.getDuracionMinutos() == null || dto.getDuracionMinutos().intValue() <= 0)
            throw new IllegalArgumentException("La duración debe ser positiva");
        if (dto.getEstadoPelicula() == null || dto.getEstadoPelicula().trim().isEmpty())
            dto.setEstadoPelicula("ACTIVA");
    }
}
