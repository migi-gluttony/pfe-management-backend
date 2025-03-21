package ma.estfbs.pfe_management.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur,Long> {
    Optional<Utilisateur> findByEmail(String email);
    
    Optional<Utilisateur> findByEmailAndDateNaissanceAndCni(String email, Date dateNaissance, String cni);
    Optional<Utilisateur> findByEmailAndDateNaissanceAndCne(String email, Date dateNaissance, String cne);
    boolean existsByCni(String cni);  
    boolean existsByCne(String cne);
}
