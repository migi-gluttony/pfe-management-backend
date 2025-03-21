package ma.estfbs.pfe_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.NoteSoutenance;
import ma.estfbs.pfe_management.model.Utilisateur;

@RepositoryRestResource(path = "note-soutenance")
public interface NoteSoutenanceRepository extends JpaRepository<NoteSoutenance, Long> {
    List<NoteSoutenance> findByJury(Utilisateur jury);
    
    // You might want to add a relationship to Soutenance and then add:
    // List<NoteSoutenance> findBySoutenance(Soutenance soutenance);
}
