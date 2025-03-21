package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Etudiant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etudiant {
    
    @Id
    private Long id;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Utilisateur utilisateur;
    
    @ManyToOne
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;
    
    // Inverse relationships can be added as needed
    // For example, to get the binomes that this student is a part of:
    /*
    @OneToMany(mappedBy = "etudiant1")
    private List<Binome> binomesAsPrimary;
    
    @OneToMany(mappedBy = "etudiant2")
    private List<Binome> binomesAsSecondary;
    */
}