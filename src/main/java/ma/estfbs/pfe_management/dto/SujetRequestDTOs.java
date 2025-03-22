package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SujetRequestDTOs {
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetAddRequest {
        private String titre;
        private String theme;
        private String description;
        private Long filiereId;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetEditRequest {
        private String titre;
        private String theme;
        private String description;
    }
}
