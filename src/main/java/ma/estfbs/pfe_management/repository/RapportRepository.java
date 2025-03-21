package ma.estfbs.pfe_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Rapport;

@RepositoryRestResource(path = "rapport")
public interface RapportRepository extends JpaRepository<Rapport, Long> {
    List<Rapport> findByBinome(Binome binome);
    List<Rapport> findByTitreContainingIgnoreCase(String titre);
    
    // Find the latest report for a binome
    Rapport findTopByBinomeOrderByIdDesc(Binome binome);
}
