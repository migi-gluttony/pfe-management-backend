package ma.estfbs.pfe_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.Salle;

@RepositoryRestResource(path = "salle")
public interface SalleRepository extends JpaRepository<Salle, Long> {
    Optional<Salle> findByNom(String nom);
    boolean existsByNom(String nom);
}
