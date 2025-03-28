package ma.estfbs.pfe_management.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "annee_scolaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnneeScolaire {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String annee;
    
    @Column(nullable = false)
    private Boolean courante;
    
    // Relationships
    @OneToMany(mappedBy = "anneeScolaire")
    private List<Etudiant> etudiants;
    
    @OneToMany(mappedBy = "anneeScolaire")
    private List<Sujet> sujets;
    
    @OneToMany(mappedBy = "anneeScolaire")
    private List<Binome> binomes;
    
    @OneToMany(mappedBy = "anneeScolaire")
    private List<Soutenance> soutenances;
    
    @OneToMany(mappedBy = "anneeScolaire")
    private List<Rapport> rapports;
    
    @OneToMany(mappedBy = "anneeScolaire")
    private List<DocumentsEvaluation> documentsEvaluations;
    
    @OneToMany(mappedBy = "anneeScolaire")
    private List<ProposerSujets> proposerSujets;
    
    @OneToMany(mappedBy = "anneeScolaire")
    private List<DemandeBinome> demandeBinomes;
    
    @OneToOne(mappedBy = "anneeScolaire")
    private Pourcentage pourcentage;
}