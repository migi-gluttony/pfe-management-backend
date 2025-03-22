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
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.repository.SujetRepository;

@Service
@RequiredArgsConstructor
public class SujetManagementService {

    private final SujetRepository sujetRepository;
    private final FiliereRepository filiereRepository;

    /**
     * Get all subjects and filieres
     */
    public SujetManagementResponse getAllSujetsAndFilieres() {
        List<SujetDTO> sujets = sujetRepository.findAll().stream()
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
     * Add a new subject
     */
    @Transactional
    public SujetDTO addSujet(SujetAddRequest request) {
        Filiere filiere = filiereRepository.findById(request.getFiliereId())
                .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + request.getFiliereId()));

        Sujet sujet = Sujet.builder()
                .titre(request.getTitre())
                .theme(request.getTheme())
                .description(request.getDescription())
                .filiere(filiere)
                .build();

        Sujet savedSujet = sujetRepository.save(sujet);
        return mapToSujetDTO(savedSujet);
    }

    /**
     * Edit a subject (only title, theme, description)
     */
    @Transactional
    public SujetDTO editSujet(Long id, SujetEditRequest request) {
        Sujet sujet = sujetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sujet non trouvé avec l'id: " + id));

        // Update only title, theme, and description
        sujet.setTitre(request.getTitre());
        sujet.setTheme(request.getTheme());
        sujet.setDescription(request.getDescription());

        Sujet updatedSujet = sujetRepository.save(sujet);
        return mapToSujetDTO(updatedSujet);
    }

    /**
     * Delete a subject
     */
    @Transactional
    public void deleteSujet(Long id) {
        if (!sujetRepository.existsById(id)) {
            throw new RuntimeException("Sujet non trouvé avec l'id: " + id);
        }
        
        // Check if the subject is linked to any binomes
        if (!sujetRepository.findById(id).get().getBinomes().isEmpty()) {
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
