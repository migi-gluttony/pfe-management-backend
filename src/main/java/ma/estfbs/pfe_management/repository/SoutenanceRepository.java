package ma.estfbs.pfe_management.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Salle;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Utilisateur;

@RepositoryRestResource(path = "soutenance")
public interface SoutenanceRepository extends JpaRepository<Soutenance, Long> {
    Optional<Soutenance> findByBinome(Binome binome);
    List<Soutenance> findByDate(LocalDate date);
    List<Soutenance> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<Soutenance> findBySalle(Salle salle);
    List<Soutenance> findByJury1OrJury2(Utilisateur jury1, Utilisateur jury2);
    
    // Check if a salle is already booked for a specific date and time range
    boolean existsBySalleAndDate(Salle salle, LocalDate date);
}
