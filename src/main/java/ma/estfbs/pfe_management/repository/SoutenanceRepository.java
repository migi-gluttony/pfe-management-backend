package ma.estfbs.pfe_management.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Salle;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Utilisateur;

@RepositoryRestResource(path = "soutenance")
public interface SoutenanceRepository extends JpaRepository<Soutenance, Long> {
    Optional<Soutenance> findByBinome(Binome binome);
    Optional<Soutenance> findByBinomeId(Long binomeId);
    List<Soutenance> findByDate(LocalDate date);
    List<Soutenance> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<Soutenance> findByDateAndHeure(LocalDate date, LocalTime heure);
    List<Soutenance> findBySalle(Salle salle);
    List<Soutenance> findByJury1OrJury2(Utilisateur jury1, Utilisateur jury2);
    
    // Check if a salle is already booked for a specific date and time
    boolean existsBySalleAndDateAndHeure(Salle salle, LocalDate date, LocalTime heure);
    
    // Check if a jury is already assigned for a specific date and time
    boolean existsByJury1AndDateAndHeure(Utilisateur jury, LocalDate date, LocalTime heure);
    boolean existsByJury2AndDateAndHeure(Utilisateur jury, LocalDate date, LocalTime heure);
    
    // Check if any soutenance is using a specific salle
    @Query("SELECT COUNT(s) > 0 FROM Soutenance s WHERE s.salle.id = :salleId")
    boolean existsBySalleId(@Param("salleId") Long salleId);
    
    // Count soutenances using a specific salle
    @Query("SELECT COUNT(s) FROM Soutenance s WHERE s.salle.id = :salleId")
    long countBySalleId(@Param("salleId") Long salleId);
}