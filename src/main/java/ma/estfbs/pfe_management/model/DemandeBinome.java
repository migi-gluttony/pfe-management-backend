package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DemandeBinome")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeBinome {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "demandeur_id", nullable = false)
    private Utilisateur demandeur;
    
    @ManyToOne
    @JoinColumn(name = "demande_id", nullable = false)
    private Utilisateur demande;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.EN_ATTENTE;
    
    // Enum for the status
    public enum Statut {
        EN_ATTENTE, ACCEPTER, REFUSER;
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}