package ma.estfbs.pfe_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.model.Utilisateur;

@RepositoryRestResource(path = "binome")
public interface BinomeRepository extends JpaRepository<Binome, Long> {
    List<Binome> findByEtudiant1(Utilisateur etudiant);
    List<Binome> findByEtudiant2(Utilisateur etudiant);
    List<Binome> findByEncadrant(Utilisateur encadrant);
    List<Binome> findBySujet(Sujet sujet);
    List<Binome> findByAnneeScolaire(AnneeScolaire anneeScolaire);
    
    // Find binomes where user is either etudiant1 or etudiant2
    List<Binome> findByEtudiant1OrEtudiant2(Utilisateur etudiant1, Utilisateur etudiant2);
    
    // Find binomes where user is either etudiant1 or etudiant2 for specific year
    List<Binome> findByEtudiant1OrEtudiant2AndAnneeScolaire(Utilisateur etudiant1, Utilisateur etudiant2, AnneeScolaire anneeScolaire);
    
    // Check if a student is already part of a binome
    boolean existsByEtudiant1OrEtudiant2(Utilisateur etudiant1, Utilisateur etudiant2);
    
    // Check if a student is already part of a binome for specific year
    boolean existsByEtudiant1OrEtudiant2AndAnneeScolaire(Utilisateur etudiant1, Utilisateur etudiant2, AnneeScolaire anneeScolaire);
    
    // Find binome by both students
    Optional<Binome> findByEtudiant1AndEtudiant2(Utilisateur etudiant1, Utilisateur etudiant2);
    
    // Find binome by both students for specific year
    Optional<Binome> findByEtudiant1AndEtudiant2AndAnneeScolaire(Utilisateur etudiant1, Utilisateur etudiant2, AnneeScolaire anneeScolaire);
}