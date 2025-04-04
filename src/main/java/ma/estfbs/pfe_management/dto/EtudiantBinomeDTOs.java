package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.estfbs.pfe_management.model.DemandeBinome.Statut;

import java.util.List;

public class EtudiantBinomeDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeStatusResponse {
        private boolean hasBinome;
        private boolean selectedSujet;
        private boolean hasPendingRequests;
        private BinomeDTO binome;
        private SujetDTO selectedSubject;
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
        private SujetDTO sujet;
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
        private String filiereName;
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
        private String theme;
        private String description;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PendingRequestDTO {
        private Long id;
        private StudentDTO demandeur;
        private Statut statut;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailableStudentsResponse {
        private List<StudentDTO> availableStudents;
        private boolean hasRejectedRequests;
        private List<Long> rejectedStudentIds;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateBinomeRequest {
        private Long etudiant2Id; // If solo, this will be null
        private boolean soloMode;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeRequestResponse {
        private List<PendingRequestDTO> pendingRequests;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeRequestActionRequest {
        private Long requestId;
        private boolean accept;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SendRequestRequest {
        private Long receiverId;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailableSujetsResponse {
        private List<SujetDTO> availableSujets;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SelectSujetRequest {
        private Long sujetId;
    }
}
