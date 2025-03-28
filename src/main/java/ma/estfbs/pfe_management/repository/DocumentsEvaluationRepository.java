package ma.estfbs.pfe_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.DocumentsEvaluation;

@RepositoryRestResource(path = "documents-evaluation")
public interface DocumentsEvaluationRepository extends JpaRepository<DocumentsEvaluation, Long> {
    List<DocumentsEvaluation> findByBinome(Binome binome);
    List<DocumentsEvaluation> findByBinomeAndAnneeScolaire(Binome binome, AnneeScolaire anneeScolaire);
    List<DocumentsEvaluation> findByAnneeScolaire(AnneeScolaire anneeScolaire);
    
    // Get the latest document for a binome
    DocumentsEvaluation findTopByBinomeOrderByIdDesc(Binome binome);
    DocumentsEvaluation findTopByBinomeAndAnneeScolaireOrderByIdDesc(Binome binome, AnneeScolaire anneeScolaire);
}