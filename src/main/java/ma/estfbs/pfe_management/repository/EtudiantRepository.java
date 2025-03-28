package ma.estfbs.pfe_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Utilisateur;

@RepositoryRestResource(path = "etudiant")
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    Optional<Etudiant> findByUtilisateur(Utilisateur utilisateur);
    Optional<Etudiant> findByUtilisateurAndAnneeScolaire(Utilisateur utilisateur, AnneeScolaire anneeScolaire);
    List<Etudiant> findByFiliere(Filiere filiere);
    List<Etudiant> findByAnneeScolaire(AnneeScolaire anneeScolaire);
    List<Etudiant> findByFiliereAndAnneeScolaire(Filiere filiere, AnneeScolaire anneeScolaire);
    List<Etudiant> findByUtilisateurIn(List<Utilisateur> utilisateurs);
    List<Etudiant> findByUtilisateurInAndAnneeScolaire(List<Utilisateur> utilisateurs, AnneeScolaire anneeScolaire);
}