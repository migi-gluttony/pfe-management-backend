package ma.estfbs.pfe_management.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documents_evaluation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentsEvaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "binome_id", nullable = false)
    private Binome binome;
    
    @Column(name = "localisation_doc", nullable = false)
    private String localisationDoc;
    
    @Column(columnDefinition = "TEXT")
    private String commentaire;
    
    @Column(name = "date_soumission", nullable = false)
    private LocalDateTime dateSoumission = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
}