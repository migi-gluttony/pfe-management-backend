package ma.estfbs.pfe_management.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.estfbs.pfe_management.model.Utilisateur;

public class CompteManagementDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompteDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String cni;
        private String cne;
        private Date dateNaissance;
        private Utilisateur.Role role;
        private String filiereName; // Only for students
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompteAddRequest {
        private String nom;
        private String prenom;
        private String cni; // For ENCADRANT and JURY
        private String cne; // For ETUDIANT
        private Date dateNaissance;
        private Utilisateur.Role role;
        private Long filiereId; // Only for students
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompteEditRequest {
        private String nom;
        private String prenom;
        private String cni; // For ENCADRANT and JURY
        private String cne; // For ETUDIANT
        private Date dateNaissance;
        private Long filiereId; // Only for students
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompteManagementResponse {
        private List<CompteDTO> comptes;
        private List<FiliereDTO> filieres;
    }
}
