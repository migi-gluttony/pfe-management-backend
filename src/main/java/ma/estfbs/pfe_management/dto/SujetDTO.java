package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SujetDTO {
    private Long id;
    private String titre;
    private String theme;
    private String description;
    private String filiereName;
}
