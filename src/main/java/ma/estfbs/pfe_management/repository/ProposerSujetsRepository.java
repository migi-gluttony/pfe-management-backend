package ma.estfbs.pfe_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.ProposerSujets;
import ma.estfbs.pfe_management.model.ProposerSujets.Status;

@RepositoryRestResource(path = "proposer-sujets")
public interface ProposerSujetsRepository extends JpaRepository<ProposerSujets, Long> {
    List<ProposerSujets> findByBinomeProposerPar(Binome binome);
    List<ProposerSujets> findByBinomeProposerParAndAnneeScolaire(Binome binome, AnneeScolaire anneeScolaire);
    List<ProposerSujets> findByStatus(Status status);
    List<ProposerSujets> findByTitreContainingIgnoreCase(String titre);
    List<ProposerSujets> findByThemeContainingIgnoreCase(String theme);
    List<ProposerSujets> findByAnneeScolaire(AnneeScolaire anneeScolaire);
    List<ProposerSujets> findByStatusAndAnneeScolaire(Status status, AnneeScolaire anneeScolaire);
    List<ProposerSujets> findByTitreContainingIgnoreCaseAndAnneeScolaire(String titre, AnneeScolaire anneeScolaire);
    List<ProposerSujets> findByThemeContainingIgnoreCaseAndAnneeScolaire(String theme, AnneeScolaire anneeScolaire);
    
    @Query("SELECT ps FROM ProposerSujets ps WHERE ps.binomeProposerPar.id = :binomeId AND ps.anneeScolaire.id = :anneeScolaireId")
    List<ProposerSujets> findByBinomeProposerParIdAndAnneeScolaireId(@Param("binomeId") Long binomeId, @Param("anneeScolaireId") Long anneeScolaireId);
}