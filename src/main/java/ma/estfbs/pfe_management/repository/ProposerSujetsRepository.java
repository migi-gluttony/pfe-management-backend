package ma.estfbs.pfe_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.ProposerSujets;
import ma.estfbs.pfe_management.model.ProposerSujets.Status;

@RepositoryRestResource(path = "proposer-sujets")
public interface ProposerSujetsRepository extends JpaRepository<ProposerSujets, Long> {
    List<ProposerSujets> findByBinomeProposerPar(Binome binome);
    List<ProposerSujets> findByStatus(Status status);
    List<ProposerSujets> findByTitreContainingIgnoreCase(String titre);
    List<ProposerSujets> findByThemeContainingIgnoreCase(String theme);
}
