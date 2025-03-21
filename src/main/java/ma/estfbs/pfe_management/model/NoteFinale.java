package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "NoteFinale")
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
    
    @Column(name = "note_rapport", nullable = false)
    private Integer noteRapport;
    
    @Column(name = "note_soutenance", nullable = false)
    private Integer noteSoutenance;
    
    // Note: The SQL has foreign key references to the 'note' columns in Rapport and NoteSoutenance
    // but in JPA this is better modeled with entity references rather than just the note values
    
    /*
    @ManyToOne
    @JoinColumn(name = "rapport_id", nullable = false)
    private Rapport rapport;
    
    @ManyToOne
    @JoinColumn(name = "note_soutenance_id", nullable = false)
    private NoteSoutenance noteSoutenanceObj;
    */
}