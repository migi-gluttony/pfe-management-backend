package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Rapport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rapport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "binome_id", nullable = false)
    private Binome binome;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(name = "localisation_rapport", nullable = false)
    private String localisationRapport;
    
    @Column(nullable = false)
    private Integer note;
    
    @Column
    private String commentaire;
}