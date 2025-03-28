package ma.estfbs.pfe_management.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "demande_binome")
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
    
    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
    
    // Enum for the status - updated to match new schema
    public enum Statut {
        EN_ATTENTE, ACCEPTER, REFUSER;
        
        @Override
        public String toString() {
            return name();
        }
    }
}