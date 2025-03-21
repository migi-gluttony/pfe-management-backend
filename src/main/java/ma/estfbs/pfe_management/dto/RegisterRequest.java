package ma.estfbs.pfe_management.dto;

import java.util.Date;

import lombok.Data;
import ma.estfbs.pfe_management.model.Utilisateur;

@Data
public class RegisterRequest {
    private String nom;
    private String prenom;
    private String email;
    private String cni;
    private String cne;
    private Date dateNaissance;
    private String motDePasse;
    private Utilisateur.Role role;
}