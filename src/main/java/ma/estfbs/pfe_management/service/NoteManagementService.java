package ma.estfbs.pfe_management.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.NoteManagementDTOs.EtudiantDTO;
import ma.estfbs.pfe_management.dto.NoteManagementDTOs.NoteDTO;
import ma.estfbs.pfe_management.dto.NoteManagementDTOs.NoteManagementResponse;
import ma.estfbs.pfe_management.dto.NoteManagementDTOs.PourcentageDTO;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.NoteFinale;
import ma.estfbs.pfe_management.model.NoteSoutenance;
import ma.estfbs.pfe_management.model.Pourcentage;
import ma.estfbs.pfe_management.model.Rapport;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.repository.NoteFinaleRepository;
import ma.estfbs.pfe_management.repository.NoteSoutenanceRepository;
import ma.estfbs.pfe_management.repository.PourcentageRepository;
import ma.estfbs.pfe_management.repository.RapportRepository;

@Service
@RequiredArgsConstructor
public class NoteManagementService {

    private final NoteFinaleRepository noteFinaleRepository;
    private final RapportRepository rapportRepository;
    private final NoteSoutenanceRepository noteSoutenanceRepository;
    private final BinomeRepository binomeRepository;
    private final EtudiantRepository etudiantRepository;
    private final FiliereRepository filiereRepository;
    private final PourcentageRepository pourcentageRepository;

    /**
     * Get all student notes with filières and percentages
     */
    public NoteManagementResponse getAllNotesWithFilieres() {
        List<NoteDTO> notes = new ArrayList<>();
        
        // Get all filières
        List<FiliereDTO> filieres = filiereRepository.findAll().stream()
                .map(this::mapToFiliereDTO)
                .collect(Collectors.toList());
        
        // Get the latest grade percentages configuration
        Pourcentage pourcentage = pourcentageRepository.findTopByOrderByIdDesc();
        PourcentageDTO pourcentageDTO = mapToPourcentageDTO(pourcentage);
        
        // Get all students
        List<Etudiant> allStudents = etudiantRepository.findAll();
        
        for (Etudiant etudiant : allStudents) {
            Utilisateur utilisateur = etudiant.getUtilisateur();
            
            // Find the student's binôme (as etudiant1 or etudiant2)
            List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2(utilisateur, utilisateur);
            if (binomes.isEmpty()) {
                continue; // Skip students without a binôme
            }
            
            Binome binome = binomes.get(0); // Get the first binôme if multiple
            
            // Get rapport note
            Integer noteRapport = null;
            Rapport rapport = rapportRepository.findTopByBinomeOrderByIdDesc(binome);
            if (rapport != null) {
                noteRapport = rapport.getNote();
            }
            
            // Get soutenance note
            Integer noteSoutenance = null;
            List<NoteSoutenance> noteSoutenances = noteSoutenanceRepository.findByJury(binome.getEncadrant());
            if (!noteSoutenances.isEmpty()) {
                // Calculate average if multiple jury evaluations
                noteSoutenance = (int) noteSoutenances.stream()
                        .mapToInt(NoteSoutenance::getNote)
                        .average()
                        .orElse(0);
            }
            
            // Get encadrant evaluation - in a real application, this should come from a specific entity
            // For now using a placeholder value
            Integer noteEncadrant = 15; // Placeholder value
            
            // Get the filière info
            Filiere filiere = etudiant.getFiliere();
            
            // Create note DTO
            NoteDTO noteDTO = NoteDTO.builder()
                    .id(utilisateur.getId()) // Using user ID as note ID for now
                    .etudiant(mapToEtudiantDTO(utilisateur))
                    .noteRapport(noteRapport)
                    .noteSoutenance(noteSoutenance)
                    .noteEncadrant(noteEncadrant)
                    .filiereId(filiere.getId())
                    .filiereName(filiere.getNom())
                    .build();
            
            notes.add(noteDTO);
        }
        
        return NoteManagementResponse.builder()
                .notes(notes)
                .filieres(filieres)
                .pourcentages(pourcentageDTO)
                .build();
    }
    
    /**
     * Get notes filtered by filière
     */
    public List<NoteDTO> getNotesByFiliere(Long filiereId) {
        Filiere filiere = filiereRepository.findById(filiereId)
                .orElseThrow(() -> new RuntimeException("Filière not found with id: " + filiereId));
        
        List<Etudiant> filiereStudents = etudiantRepository.findByFiliere(filiere);
        List<NoteDTO> notes = new ArrayList<>();
        
        for (Etudiant etudiant : filiereStudents) {
            Utilisateur utilisateur = etudiant.getUtilisateur();
            
            // Get the student's binôme
            List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2(utilisateur, utilisateur);
            if (binomes.isEmpty()) {
                continue; // Skip students without a binôme
            }
            
            Binome binome = binomes.get(0);
            
            // Get rapport note
            Integer noteRapport = null;
            Rapport rapport = rapportRepository.findTopByBinomeOrderByIdDesc(binome);
            if (rapport != null) {
                noteRapport = rapport.getNote();
            }
            
            // Get soutenance note
            Integer noteSoutenance = null;
            List<NoteSoutenance> noteSoutenances = noteSoutenanceRepository.findByJury(binome.getEncadrant());
            if (!noteSoutenances.isEmpty()) {
                noteSoutenance = (int) noteSoutenances.stream()
                        .mapToInt(NoteSoutenance::getNote)
                        .average()
                        .orElse(0);
            }
            
            // Get encadrant evaluation (placeholder)
            Integer noteEncadrant = 15;
            
            // Create note DTO
            NoteDTO noteDTO = NoteDTO.builder()
                    .id(utilisateur.getId())
                    .etudiant(mapToEtudiantDTO(utilisateur))
                    .noteRapport(noteRapport)
                    .noteSoutenance(noteSoutenance)
                    .noteEncadrant(noteEncadrant)
                    .filiereId(filiere.getId())
                    .filiereName(filiere.getNom())
                    .build();
            
            notes.add(noteDTO);
        }
        
        return notes;
    }
    
    /**
     * Map Utilisateur entity to EtudiantDTO
     */
    private EtudiantDTO mapToEtudiantDTO(Utilisateur utilisateur) {
        return EtudiantDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .cne(utilisateur.getCne())
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
    
    /**
     * Map Pourcentage entity to PourcentageDTO
     */
    private PourcentageDTO mapToPourcentageDTO(Pourcentage pourcentage) {
        if (pourcentage == null) {
            // Default percentages if none configured
            return PourcentageDTO.builder()
                    .pourcentageRapport(40)
                    .pourcentageSoutenance(40)
                    .pourcentageEncadrant(20)
                    .build();
        }
        
        return PourcentageDTO.builder()
                .pourcentageRapport(pourcentage.getPourcentageRapport())
                .pourcentageSoutenance(pourcentage.getPourcentageSoutenance())
                .pourcentageEncadrant(pourcentage.getPourcentageEncadrant())
                .build();
    }
    
    /**
     * Get all filières
     */
    public List<FiliereDTO> getAllFilieres() {
        return filiereRepository.findAll().stream()
                .map(this::mapToFiliereDTO)
                .collect(Collectors.toList());
    }
}