package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "NoteSoutenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteSoutenance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "jury_id", nullable = false)
    private Utilisateur jury;
    
    @Column(nullable = false)
    private Integer note;
    
    // You might want to add a relationship to the Soutenance
    // @ManyToOne
    // @JoinColumn(name = "soutenance_id", nullable = false)
    // private Soutenance soutenance;
}