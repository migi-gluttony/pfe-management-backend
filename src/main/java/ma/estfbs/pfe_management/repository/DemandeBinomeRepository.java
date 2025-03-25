package ma.estfbs.pfe_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.DemandeBinome;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.model.DemandeBinome.Statut;

@RepositoryRestResource(path = "demande-binome")
public interface DemandeBinomeRepository extends JpaRepository<DemandeBinome, Long> {
    List<DemandeBinome> findByDemandeur(Utilisateur demandeur);
    List<DemandeBinome> findByDemande(Utilisateur demande);
    List<DemandeBinome> findByStatut(Statut statut);
    
    // Find requests between two specific students
// Find requests sent by a specific student with a specific status
List<DemandeBinome> findByDemandeurAndStatut(Utilisateur demandeur, Statut statut);    
    // Find pending requests for a specific student
    List<DemandeBinome> findByDemandeAndStatut(Utilisateur demande, Statut statut);
    
    // Check if there is already a request between two students
    boolean existsByDemandeurAndDemande(Utilisateur demandeur, Utilisateur demande);

/**
 * Find a specific request between two students
 */
Optional<DemandeBinome> findByDemandeurAndDemande(Utilisateur demandeur, Utilisateur demande);

    
}
