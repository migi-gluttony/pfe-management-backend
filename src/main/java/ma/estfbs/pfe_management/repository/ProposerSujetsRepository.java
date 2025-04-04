package ma.estfbs.pfe_management.repository;

import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.ProposerSujets;
import ma.estfbs.pfe_management.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposerSujetsRepository extends JpaRepository<ProposerSujets, Long> {
    List<ProposerSujets> findByEtudiant(Utilisateur etudiant);
    List<ProposerSujets> findByFiliere(Filiere filiere);
    List<ProposerSujets> findByStatus(ProposerSujets.Status status); // Modifié: findByStatut -> findByStatus, Statut -> Status
}