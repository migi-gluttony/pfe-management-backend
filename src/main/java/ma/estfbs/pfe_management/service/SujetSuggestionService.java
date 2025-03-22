package ma.estfbs.pfe_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.SujetSuggestionDTO;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.ProposerSujets;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.model.ProposerSujets.Status;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.ProposerSujetsRepository;
import ma.estfbs.pfe_management.repository.SujetRepository;

@Service
@RequiredArgsConstructor
public class SujetSuggestionService {

    private final ProposerSujetsRepository proposerSujetsRepository;
    private final SujetRepository sujetRepository;
    private final BinomeRepository binomeRepository;
    private final EtudiantRepository etudiantRepository;

    /**
     * Get all sujet suggestions
     */
    public List<SujetSuggestionDTO> getAllSuggestions() {
        return proposerSujetsRepository.findAll().stream()
                .map(this::mapToSujetSuggestionDTO)
                .collect(Collectors.toList());
    }

    /**
     * Accept a sujet suggestion and create a new sujet
     */
    @Transactional
    public void acceptSuggestion(Long id) {
        ProposerSujets suggestion = findSuggestionById(id);
        
        // Update suggestion status
        suggestion.setStatus(Status.ACCEPTER);
        proposerSujetsRepository.save(suggestion);
        
        // Get the binome that suggested the sujet
        Binome binome = suggestion.getBinomeProposerPar();
        
        // Get the filiere from the etudiant1 (must get Etudiant entity first)
        Utilisateur etudiant1User = binome.getEtudiant1();
        Etudiant etudiant1 = etudiantRepository.findByUtilisateur(etudiant1User)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + etudiant1User.getId()));
        Filiere filiere = etudiant1.getFiliere();
        
        // Create a new sujet based on the suggestion
        Sujet sujet = Sujet.builder()
                .titre(suggestion.getTitre())
                .theme(suggestion.getTheme())
                .description(suggestion.getDescription())
                .filiere(filiere)
                .build();
        
        sujet = sujetRepository.save(sujet);
        
        // Assign the sujet to the binome
        binome.setSujet(sujet);
        binomeRepository.save(binome);
    }

    /**
     * Reject a sujet suggestion
     */
    @Transactional
    public void rejectSuggestion(Long id) {
        ProposerSujets suggestion = findSuggestionById(id);
        
        // Update suggestion status
        suggestion.setStatus(Status.REFUSER);
        proposerSujetsRepository.save(suggestion);
    }

    /**
     * Find suggestion by ID
     */
    private ProposerSujets findSuggestionById(Long id) {
        return proposerSujetsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suggestion de sujet non trouvée avec l'id: " + id));
    }

    /**
     * Map ProposerSujets entity to SujetSuggestionDTO
     */
    private SujetSuggestionDTO mapToSujetSuggestionDTO(ProposerSujets suggestion) {
        Binome binome = suggestion.getBinomeProposerPar();
        
        // Get etudiant1 filiere
        Utilisateur etudiant1User = binome.getEtudiant1();
        Etudiant etudiant1 = etudiantRepository.findByUtilisateur(etudiant1User)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + etudiant1User.getId()));
        String filiereName = etudiant1.getFiliere().getNom();
        
        SujetSuggestionDTO.BinomeDTO binomeDTO = SujetSuggestionDTO.BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1Name(binome.getEtudiant1().getNom() + " " + binome.getEtudiant1().getPrenom())
                .etudiant2Name(binome.getEtudiant2() != null 
                        ? binome.getEtudiant2().getNom() + " " + binome.getEtudiant2().getPrenom() 
                        : null)
                .encadrantName(binome.getEncadrant().getNom() + " " + binome.getEncadrant().getPrenom())
                .filiereName(filiereName)
                .build();
        
        return SujetSuggestionDTO.builder()
                .id(suggestion.getId())
                .titre(suggestion.getTitre())
                .theme(suggestion.getTheme())
                .description(suggestion.getDescription())
                .status(suggestion.getStatus().toString())
                .binome(binomeDTO)
                .build();
    }
}