package ma.estfbs.pfe_management.model;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NoteSoutenanceId implements Serializable {
    private Long juryId;
    private Long soutenanceId;
}