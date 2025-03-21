package ma.estfbs.pfe_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.Filiere;

@RepositoryRestResource(path = "filiere")
public interface FiliereRepository extends JpaRepository<Filiere, Long> {
    boolean existsByNom(String nom);
}
