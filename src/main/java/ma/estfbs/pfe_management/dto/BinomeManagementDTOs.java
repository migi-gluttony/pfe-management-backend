package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class BinomeManagementDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeDTO {
        private Long id;
        private StudentDTO etudiant1;
        private StudentDTO etudiant2;
        private EncadrantDTO encadrant;
        private SujetDTO sujet;
        private String filiereName;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String cne;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EncadrantDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetDTO {
        private Long id;
        private String titre;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeAddRequest {
        private Long etudiant1Id;
        private Long etudiant2Id;
        private Long encadrantId;
        private Long sujetId;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeEditRequest {
        private Long encadrantId;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeManagementResponse {
        private List<BinomeDTO> binomes;
        private List<FiliereDTO> filieres;
        private List<StudentDTO> availableStudents;
        private List<EncadrantDTO> encadrants;
        private List<SujetDTO> availableSujets;
    }
}
