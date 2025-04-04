package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proposer_sujets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposerSujets {
    
    public enum Status { // Renommé de Statut à Status pour correspondre aux services existants
        EN_ATTENTE,
        ACCEPTER,
        REFUSER
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(nullable = false)
    private String theme;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;
    
    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Utilisateur etudiant;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // Renommé de statut à status
    
    @ManyToOne
    @JoinColumn(name = "binome_id")
    private Binome binomeProposerPar;
    
    // Getters et setters pour binomeProposerPar
    public Binome getBinomeProposerPar() {
        return binomeProposerPar;
    }

    public void setBinomeProposerPar(Binome binomeProposerPar) {
        this.binomeProposerPar = binomeProposerPar;
    }
}