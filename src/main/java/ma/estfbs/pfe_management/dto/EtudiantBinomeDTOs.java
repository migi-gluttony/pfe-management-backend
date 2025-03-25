package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class EtudiantBinomeDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EtudiantStatusDTO {
        private boolean hasBinome;
        private boolean hasPendingRequests;
        private boolean hasRequestSent;
        private Long binomeId;
        private Long sujetId;
        private String statusMessage;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeRequestDTO {
        private Long id;
        private Long demandeurId;
        private String demandeurNom;
        private String demandeurPrenom;
        private String demandeurEmail;
        private String filiereName;
        private String status;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailableStudentDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String filiereName;
        private boolean alreadyRequested;
        private boolean canRequest; // false if student was rejected before
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeRequestResponseDTO {
        private boolean success;
        private String message;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeRequestCreateDTO {
        private Long demandeId;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetListDTO {
        private List<SujetDTO> sujets;
        private Long binomeId;
        private boolean isSoloMode;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetSelectDTO {
        private Long sujetId;
        private Long encadrantId;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetSelectionResponseDTO {
        private boolean success;
        private String message;
        private SujetDTO selectedSujet;
        private UserDTO encadrant;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PendingRequestsDTO {
        private List<BinomeRequestDTO> incomingRequests;
        private List<BinomeRequestDTO> outgoingRequests;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContinueSoloDTO {
        private boolean success;
        private String message;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeSearchDTO {
        private List<AvailableStudentDTO> availableStudents;
        private List<BinomeRequestDTO> incomingRequests;
        private List<BinomeRequestDTO> outgoingRequests;
    }
}
