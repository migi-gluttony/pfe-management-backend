package ma.estfbs.pfe_management.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequest {
    private String email;
    private String cni; // For jury and encadrants
    private String cne; // For students
    private Date dateNaissance;
}