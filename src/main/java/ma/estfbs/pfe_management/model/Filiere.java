package ma.estfbs.pfe_management.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Filiere")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Filiere {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String nom;
    
    // Relationships
    @OneToMany(mappedBy = "filiere")
    private List<Etudiant> etudiants;
    
    @OneToMany(mappedBy = "filiere")
    private List<Sujet> sujets;
}