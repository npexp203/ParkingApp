package be.esi.prj.model.repository;

/**
 * Exception personnalisé pour les erreurs liées aux opérations du repo.
 *
 */
public class RepositoryException extends RuntimeException {
    public RepositoryException(String message) {
        super(message);
    }
}

