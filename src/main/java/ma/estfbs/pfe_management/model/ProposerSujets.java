package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ProposerSujets")
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
    
    // Enum for the status
    public enum Status {
        EN_ATTENTE, ACCEPTER, REFUSER;
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}