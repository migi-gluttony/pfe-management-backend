package ma.estfbs.pfe_management.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Salle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nom;
    
    // Relationships
    @OneToMany(mappedBy = "salle")
    private List<Soutenance> soutenances;
}