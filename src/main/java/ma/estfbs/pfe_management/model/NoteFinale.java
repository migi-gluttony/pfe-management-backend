package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "note_finale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteFinale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Utilisateur etudiant;
    
    @ManyToOne
    @JoinColumn(name = "binome_id", nullable = false)
    private Binome binome;
    
    @Column(name = "note_rapport", nullable = false)
    private Integer noteRapport;
    
    @Column(name = "note_soutenance", nullable = false)
    private Integer noteSoutenance;
    
    @Column(name = "note_encadrant", nullable = false)
    private Integer noteEncadrant;
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
    
    // For backward compatibility
    public NoteFinale(Utilisateur etudiant, Integer noteRapport, Integer noteSoutenance) {
        this.etudiant = etudiant;
        this.noteRapport = noteRapport;
        this.noteSoutenance = noteSoutenance;
        this.noteEncadrant = 0; // Default value
    }
}