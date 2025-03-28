package ma.estfbs.pfe_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.SujetDTO;
import ma.estfbs.pfe_management.dto.SujetManagementResponse;
import ma.estfbs.pfe_management.dto.SujetRequestDTOs.SujetAddRequest;
import ma.estfbs.pfe_management.dto.SujetRequestDTOs.SujetEditRequest;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.repository.SujetRepository;

@Service
@RequiredArgsConstructor
public class SujetManagementService {

    private final SujetRepository sujetRepository;
    private final FiliereRepository filiereRepository;
    private final AcademicYearService academicYearService;

    /**
     * Get all subjects and filieres for the current academic year
     */
    public SujetManagementResponse getAllSujetsAndFilieres() {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        List<SujetDTO> sujets = sujetRepository.findByAnneeScolaire(currentYear).stream()
                .map(this::mapToSujetDTO)
                .collect(Collectors.toList());

        List<FiliereDTO> filieres = filiereRepository.findAll().stream()
                .map(this::mapToFiliereDTO)
                .collect(Collectors.toList());

        return SujetManagementResponse.builder()
                .sujets(sujets)
                .filieres(filieres)
                .build();
    }

    /**
     * Add a new subject for the current academic year
     */
    @Transactional
    public SujetDTO addSujet(SujetAddRequest request) {
        Filiere filiere = filiereRepository.findById(request.getFiliereId())
                .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + request.getFiliereId()));

        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        Sujet sujet = Sujet.builder()
                .titre(request.getTitre())
                .theme(request.getTheme())
                .description(request.getDescription())
                .filiere(filiere)
                .anneeScolaire(currentYear)
                .build();

        Sujet savedSujet = sujetRepository.save(sujet);
        return mapToSujetDTO(savedSujet);
    }

    /**
     * Edit a subject (only title, theme, description) for the current year
     */
    @Transactional
    public SujetDTO editSujet(Long id, SujetEditRequest request) {
        Sujet sujet = sujetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sujet non trouvé avec l'id: " + id));

        // Check if the subject is from the current year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        if (!sujet.getAnneeScolaire().getId().equals(currentYear.getId())) {
            throw new RuntimeException("Impossible de modifier un sujet d'une année précédente");
        }
        
        // Update only title, theme, and description
        sujet.setTitre(request.getTitre());
        sujet.setTheme(request.getTheme());
        sujet.setDescription(request.getDescription());

        Sujet updatedSujet = sujetRepository.save(sujet);
        return mapToSujetDTO(updatedSujet);
    }

    /**
     * Delete a subject for the current year
     */
    @Transactional
    public void deleteSujet(Long id) {
        Sujet sujet = sujetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sujet non trouvé avec l'id: " + id));
        
        // Check if the subject is from the current year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        if (!sujet.getAnneeScolaire().getId().equals(currentYear.getId())) {
            throw new RuntimeException("Impossible de supprimer un sujet d'une année précédente");
        }
        
        // Check if the subject is linked to any binomes
        if (!sujet.getBinomes().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un sujet lié à des binômes");
        }
        
        sujetRepository.deleteById(id);
    }

    /**
     * Map Sujet entity to SujetDTO
     */
    private SujetDTO mapToSujetDTO(Sujet sujet) {
        return SujetDTO.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .theme(sujet.getTheme())
                .description(sujet.getDescription())
                .filiereName(sujet.getFiliere().getNom())
                .build();
    }

    /**
     * Map Filiere entity to FiliereDTO
     */
    private FiliereDTO mapToFiliereDTO(Filiere filiere) {
        return FiliereDTO.builder()
                .id(filiere.getId())
                .nom(filiere.getNom())
                .build();
    }
}