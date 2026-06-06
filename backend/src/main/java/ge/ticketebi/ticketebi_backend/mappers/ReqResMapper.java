package ge.ticketebi.ticketebi_backend.mappers;

public interface ReqResMapper<E, REQ, RES> {
    E toEntity (REQ req);
    RES toResponseDto (E entity);
}
