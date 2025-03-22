package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SoutenanceManagementDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SoutenanceDTO {
        private Long id;
        private LocalDate date;
        private LocalTime heure;
        private SalleDTO salle;
        private BinomeDTO binome;
        private JuryDTO jury1;
        private JuryDTO jury2;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SoutenanceAddRequest {
        private LocalDate date;
        private String heure;
        private Long salleId;
        private Long binomeId;
        private Long jury1Id;
        private Long jury2Id;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SoutenanceUpdateRequest {
        private Long id;
        private LocalDate date;
        private String heure;
        private Long salleId;
        private Long binomeId;
        private Long jury1Id;
        private Long jury2Id;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeDTO {
        private Long id;
        private StudentDTO etudiant1;
        private StudentDTO etudiant2;
        private EncadrantDTO encadrant;
        private SujetShortDTO sujet;
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
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EncadrantDTO {
        private Long id;
        private String nom;
        private String prenom;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JuryDTO {
        private Long id;
        private String nom;
        private String prenom;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SalleDTO {
        private Long id;
        private String nom;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetShortDTO {
        private Long id;
        private String titre;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidationResponse {
        private boolean valid;
        private List<ValidationError> errors;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
