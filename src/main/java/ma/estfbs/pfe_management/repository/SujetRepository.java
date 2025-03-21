package ma.estfbs.pfe_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Sujet;

@RepositoryRestResource(path = "sujet")
public interface SujetRepository extends JpaRepository<Sujet, Long> {
    List<Sujet> findByFiliere(Filiere filiere);
    List<Sujet> findByThemeContainingIgnoreCase(String theme);
    List<Sujet> findByTitreContainingIgnoreCase(String titre);
    
    // Find subjects that don't have any binome assigned yet
    List<Sujet> findByBinomesIsEmpty();
}
