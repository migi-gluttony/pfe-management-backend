package ma.estfbs.pfe_management.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "binome")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Binome {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "etudiant1_id", nullable = false)
    private Utilisateur etudiant1;
    
    @ManyToOne
    @JoinColumn(name = "etudiant2_id")
    private Utilisateur etudiant2;
    
    @ManyToOne
    @JoinColumn(name = "encadrant_id", nullable = false)
    private Utilisateur encadrant;
    
    @ManyToOne
    @JoinColumn(name = "sujet_id", nullable = false)
    private Sujet sujet;
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
    
    // Existing relationships
    @OneToMany(mappedBy = "binome")
    private List<Rapport> rapports;
    
    @OneToMany(mappedBy = "binome")
    private List<DocumentsEvaluation> documentsEvaluations;
    
    @OneToOne(mappedBy = "binome")
    private Soutenance soutenance;
    
    @OneToMany(mappedBy = "binomeProposerPar")
    private List<ProposerSujets> propositionsSujets;
}