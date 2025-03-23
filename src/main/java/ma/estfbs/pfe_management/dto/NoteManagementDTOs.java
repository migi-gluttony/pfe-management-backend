package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class NoteManagementDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteDTO {
        private Long id;
        private EtudiantDTO etudiant;
        private Integer noteRapport;
        private Integer noteSoutenance;
        private Integer noteEncadrant;
        private Long filiereId;
        private String filiereName;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EtudiantDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String cne;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PourcentageDTO {
        private Integer pourcentageRapport;
        private Integer pourcentageSoutenance;
        private Integer pourcentageEncadrant;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteManagementResponse {
        private List<NoteDTO> notes;
        private List<FiliereDTO> filieres;
        private PourcentageDTO pourcentages;
    }
}