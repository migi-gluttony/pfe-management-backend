package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DocumentsEvaluation")
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
}