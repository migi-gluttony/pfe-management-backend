package ma.estfbs.pfe_management.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SujetManagementResponse {
    private List<SujetDTO> sujets;
    private List<FiliereDTO> filieres;
}
