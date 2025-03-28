package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "note_soutenance")
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
    
    @ManyToOne
    @JoinColumn(name = "soutenance_id", nullable = false)
    private Soutenance soutenance;
    
    @Column(nullable = false)
    private Integer note;
    
    @Column
    private String commentaire;
    
    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation = LocalDateTime.now();
}