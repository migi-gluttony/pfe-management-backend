package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.estfbs.pfe_management.model.ProposerSujets;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SujetSuggestionDTO {
    private Long id;
    private String titre;
    private String theme;
    private String description;
    private String status;
    private BinomeDTO binome;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeDTO {
        private Long id;
        private String etudiant1Name;
        private String etudiant2Name;
        private String encadrantName;
        private String filiereName;
    }
}
