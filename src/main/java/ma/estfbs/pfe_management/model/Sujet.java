package ma.estfbs.pfe_management.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Sujet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sujet {
    
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
    
    // Relationships
    @OneToMany(mappedBy = "sujet")
    private List<Binome> binomes;
}