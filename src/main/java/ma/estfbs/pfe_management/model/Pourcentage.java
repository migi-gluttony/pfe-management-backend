package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Pourcentage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pourcentage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pourcentage_rapport", nullable = false)
    private Integer pourcentageRapport;
    
    @Column(name = "pourcentage_soutenance", nullable = false)
    private Integer pourcentageSoutenance;
    
    @Column(name = "pourcentage_encadrant", nullable = false)
    private Integer pourcentageEncadrant;
}