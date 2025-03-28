package ma.estfbs.pfe_management.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "soutenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Soutenance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalTime heure;
    
    @ManyToOne
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;
    
    @OneToOne
    @JoinColumn(name = "binome_id", nullable = false)
    private Binome binome;
    
    @ManyToOne
    @JoinColumn(name = "jury1_id", nullable = false)
    private Utilisateur jury1;
    
    @ManyToOne
    @JoinColumn(name = "jury2_id", nullable = false)
    private Utilisateur jury2;
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
}