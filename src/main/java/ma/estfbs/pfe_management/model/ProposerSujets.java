package ma.estfbs.pfe_management.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proposer_sujets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposerSujets {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "binome_proposer_par_id", nullable = false)
    private Binome binomeProposerPar;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(nullable = false)
    private String theme;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.EN_ATTENTE;
    
    @Column(name = "date_proposition", nullable = false)
    private LocalDateTime dateProposition = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
    
    // Enum for the status
    public enum Status {
        EN_ATTENTE, ACCEPTER, REFUSER;
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}