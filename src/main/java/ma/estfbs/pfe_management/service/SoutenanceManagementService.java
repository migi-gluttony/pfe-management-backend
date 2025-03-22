package ma.estfbs.pfe_management.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.BinomeDTO;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.EncadrantDTO;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.JuryDTO;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.SalleDTO;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.SoutenanceAddRequest;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.SoutenanceDTO;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.SoutenanceUpdateRequest;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.StudentDTO;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.SujetShortDTO;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.ValidationError;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.ValidationResponse;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Salle;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.SalleRepository;
import ma.estfbs.pfe_management.repository.SoutenanceRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;

@Service
@RequiredArgsConstructor
public class SoutenanceManagementService {

    private final SoutenanceRepository soutenanceRepository;
    private final BinomeRepository binomeRepository;
    private final SalleRepository salleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Get all soutenances
     */
    public List<SoutenanceDTO> getAllSoutenances() {
        return soutenanceRepository.findAll().stream()
                .map(this::mapToSoutenanceDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get soutenance by ID
     */
    public SoutenanceDTO getSoutenanceById(Long id) {
        Soutenance soutenance = soutenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Soutenance non trouvée avec l'id: " + id));
        
        return mapToSoutenanceDTO(soutenance);
    }
    
    /**
     * Add a new soutenance
     */
    @Transactional
    public SoutenanceDTO addSoutenance(SoutenanceAddRequest request) {
        // Validate the request
        ValidationResponse validation = validateSoutenanceRequest(request, null);
        if (!validation.isValid()) {
            throw new RuntimeException(buildErrorMessage(validation.getErrors()));
        }
        
        // Get entities
        Binome binome = binomeRepository.findById(request.getBinomeId())
                .orElseThrow(() -> new RuntimeException("Binôme non trouvé avec l'id: " + request.getBinomeId()));
        
        Salle salle = salleRepository.findById(request.getSalleId())
                .orElseThrow(() -> new RuntimeException("Salle non trouvée avec l'id: " + request.getSalleId()));
        
        Utilisateur jury1 = utilisateurRepository.findById(request.getJury1Id())
                .orElseThrow(() -> new RuntimeException("Jury 1 non trouvé avec l'id: " + request.getJury1Id()));
        
        Utilisateur jury2 = utilisateurRepository.findById(request.getJury2Id())
                .orElseThrow(() -> new RuntimeException("Jury 2 non trouvé avec l'id: " + request.getJury2Id()));
        
        // Parse time
        LocalTime heure = LocalTime.parse(request.getHeure(), timeFormatter);
        
        // Create and save soutenance
        Soutenance soutenance = Soutenance.builder()
                .date(request.getDate())
                .heure(heure)
                .salle(salle)
                .binome(binome)
                .jury1(jury1)
                .jury2(jury2)
                .build();
        
        soutenance = soutenanceRepository.save(soutenance);
        
        return mapToSoutenanceDTO(soutenance);
    }
    
    /**
     * Update an existing soutenance
     */
    @Transactional
    public SoutenanceDTO updateSoutenance(Long id, SoutenanceUpdateRequest request) {
        Soutenance soutenance = soutenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Soutenance non trouvée avec l'id: " + id));
        
        // Validate the request
        ValidationResponse validation = validateSoutenanceRequest(request, id);
        if (!validation.isValid()) {
            throw new RuntimeException(buildErrorMessage(validation.getErrors()));
        }
        
        // Get entities
        Binome binome = binomeRepository.findById(request.getBinomeId())
                .orElseThrow(() -> new RuntimeException("Binôme non trouvé avec l'id: " + request.getBinomeId()));
        
        Salle salle = salleRepository.findById(request.getSalleId())
                .orElseThrow(() -> new RuntimeException("Salle non trouvée avec l'id: " + request.getSalleId()));
        
        Utilisateur jury1 = utilisateurRepository.findById(request.getJury1Id())
                .orElseThrow(() -> new RuntimeException("Jury 1 non trouvé avec l'id: " + request.getJury1Id()));
        
        Utilisateur jury2 = utilisateurRepository.findById(request.getJury2Id())
                .orElseThrow(() -> new RuntimeException("Jury 2 non trouvé avec l'id: " + request.getJury2Id()));
        
        // Parse time
        LocalTime heure = LocalTime.parse(request.getHeure(), timeFormatter);
        
        // Update soutenance
        soutenance.setDate(request.getDate());
        soutenance.setHeure(heure);
        soutenance.setSalle(salle);
        soutenance.setBinome(binome);
        soutenance.setJury1(jury1);
        soutenance.setJury2(jury2);
        
        soutenance = soutenanceRepository.save(soutenance);
        
        return mapToSoutenanceDTO(soutenance);
    }
    
    /**
     * Delete a soutenance
     */
    @Transactional
    public void deleteSoutenance(Long id) {
        if (!soutenanceRepository.existsById(id)) {
            throw new RuntimeException("Soutenance non trouvée avec l'id: " + id);
        }
        
        soutenanceRepository.deleteById(id);
    }
    
    /**
     * Validate a soutenance request to prevent conflicts
     */
    public ValidationResponse validateSoutenanceRequest(Object requestObj, Long soutenanceId) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Extract common fields from request (which can be either AddRequest or UpdateRequest)
        LocalDate date;
        String heureStr;
        Long salleId, binomeId, jury1Id, jury2Id;
        
        if (requestObj instanceof SoutenanceAddRequest) {
            SoutenanceAddRequest request = (SoutenanceAddRequest) requestObj;
            date = request.getDate();
            heureStr = request.getHeure();
            salleId = request.getSalleId();
            binomeId = request.getBinomeId();
            jury1Id = request.getJury1Id();
            jury2Id = request.getJury2Id();
        } else if (requestObj instanceof SoutenanceUpdateRequest) {
            SoutenanceUpdateRequest request = (SoutenanceUpdateRequest) requestObj;
            date = request.getDate();
            heureStr = request.getHeure();
            salleId = request.getSalleId();
            binomeId = request.getBinomeId();
            jury1Id = request.getJury1Id();
            jury2Id = request.getJury2Id();
        } else {
            throw new IllegalArgumentException("Request object must be either SoutenanceAddRequest or SoutenanceUpdateRequest");
        }
        
        // Validate required fields
        if (date == null) {
            errors.add(new ValidationError("date", "La date est obligatoire"));
        } else if (date.isBefore(LocalDate.now())) {
            errors.add(new ValidationError("date", "La date ne peut pas être antérieure à aujourd'hui"));
        }
        
        if (heureStr == null || heureStr.isEmpty()) {
            errors.add(new ValidationError("heure", "L'heure est obligatoire"));
        }
        
        LocalTime heure = null;
        if (heureStr != null && !heureStr.isEmpty()) {
            try {
                heure = LocalTime.parse(heureStr, timeFormatter);
            } catch (Exception e) {
                errors.add(new ValidationError("heure", "Format d'heure invalide. Utilisez HH:MM"));
            }
        }
        
        if (salleId == null) {
            errors.add(new ValidationError("salleId", "La salle est obligatoire"));
        }
        
        if (binomeId == null) {
            errors.add(new ValidationError("binomeId", "Le binôme est obligatoire"));
        }
        
        if (jury1Id == null) {
            errors.add(new ValidationError("jury1Id", "Le premier membre du jury est obligatoire"));
        }
        
        if (jury2Id == null) {
            errors.add(new ValidationError("jury2Id", "Le second membre du jury est obligatoire"));
        }
        
        // Check if jury1 and jury2 are the same
        if (jury1Id != null && jury2Id != null && jury1Id.equals(jury2Id)) {
            errors.add(new ValidationError("jury2Id", "Les deux membres du jury doivent être différents"));
        }
        
        // If there are basic validation errors, return them now
        if (!errors.isEmpty()) {
            return ValidationResponse.builder()
                    .valid(false)
                    .errors(errors)
                    .build();
        }
        
        // Check for scheduling conflicts
        if (date != null && heure != null) {
            // Get all soutenances on the same date and time
            List<Soutenance> existingSoutenances = soutenanceRepository.findByDateAndHeure(date, heure);
            
            // Filter out the current soutenance if we're updating
            if (soutenanceId != null) {
                existingSoutenances = existingSoutenances.stream()
                        .filter(s -> !s.getId().equals(soutenanceId))
                        .collect(Collectors.toList());
            }
            
            // Check for salle conflict
            if (salleId != null) {
                boolean salleConflict = existingSoutenances.stream()
                        .anyMatch(s -> s.getSalle().getId().equals(salleId));
                
                if (salleConflict) {
                    errors.add(new ValidationError("salleId", 
                            "Cette salle est déjà réservée à cette date et heure"));
                }
            }
            
            // Check for jury conflicts
            if (jury1Id != null) {
                boolean jury1Conflict = existingSoutenances.stream()
                        .anyMatch(s -> s.getJury1().getId().equals(jury1Id) || s.getJury2().getId().equals(jury1Id));
                
                if (jury1Conflict) {
                    errors.add(new ValidationError("jury1Id", 
                            "Ce membre du jury est déjà assigné à une autre soutenance à cette date et heure"));
                }
            }
            
            if (jury2Id != null) {
                boolean jury2Conflict = existingSoutenances.stream()
                        .anyMatch(s -> s.getJury1().getId().equals(jury2Id) || s.getJury2().getId().equals(jury2Id));
                
                if (jury2Conflict) {
                    errors.add(new ValidationError("jury2Id", 
                            "Ce membre du jury est déjà assigné à une autre soutenance à cette date et heure"));
                }
            }
            
            // Check for binome conflict (a binome can only have one soutenance)
            if (binomeId != null && soutenanceId == null) { // Only for new soutenances
                Optional<Soutenance> existingSoutenance = soutenanceRepository.findByBinomeId(binomeId);
                if (existingSoutenance.isPresent()) {
                    errors.add(new ValidationError("binomeId", 
                            "Ce binôme a déjà une soutenance programmée"));
                }
            }
        }
        
        return ValidationResponse.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .build();
    }
    
    /**
     * Map Soutenance entity to SoutenanceDTO
     */
    private SoutenanceDTO mapToSoutenanceDTO(Soutenance soutenance) {
        return SoutenanceDTO.builder()
                .id(soutenance.getId())
                .date(soutenance.getDate())
                .heure(soutenance.getHeure())
                .salle(mapToSalleDTO(soutenance.getSalle()))
                .binome(mapToBinomeDTO(soutenance.getBinome()))
                .jury1(mapToJuryDTO(soutenance.getJury1()))
                .jury2(mapToJuryDTO(soutenance.getJury2()))
                .build();
    }
    
    /**
     * Map Binome entity to BinomeDTO
     */
    private BinomeDTO mapToBinomeDTO(Binome binome) {
        String filiereName = null;
        
        // Get filiere name from etudiant1
        Etudiant etudiant1 = etudiantRepository.findByUtilisateur(binome.getEtudiant1())
                .orElse(null);
        if (etudiant1 != null && etudiant1.getFiliere() != null) {
            filiereName = etudiant1.getFiliere().getNom();
        }
        
        return BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToStudentDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2()) : null)
                .encadrant(mapToEncadrantDTO(binome.getEncadrant()))
                .sujet(mapToSujetShortDTO(binome.getSujet()))
                .filiereName(filiereName)
                .build();
    }
    
    /**
     * Map Utilisateur entity to StudentDTO
     */
    private StudentDTO mapToStudentDTO(Utilisateur utilisateur) {
        return StudentDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }
    
    /**
     * Map Utilisateur entity to EncadrantDTO
     */
    private EncadrantDTO mapToEncadrantDTO(Utilisateur utilisateur) {
        return EncadrantDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }
    
    /**
     * Map Utilisateur entity to JuryDTO
     */
    private JuryDTO mapToJuryDTO(Utilisateur utilisateur) {
        return JuryDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }
    
    /**
     * Map Salle entity to SalleDTO
     */
    private SalleDTO mapToSalleDTO(Salle salle) {
        return SalleDTO.builder()
                .id(salle.getId())
                .nom(salle.getNom())
                .build();
    }
    
    /**
     * Map Sujet entity to SujetShortDTO
     */
    private SujetShortDTO mapToSujetShortDTO(Sujet sujet) {
        return SujetShortDTO.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .build();
    }
    
    /**
     * Build error message from validation errors
     */
    private String buildErrorMessage(List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Validation error";
        }
        
        return errors.stream()
                .map(error -> error.getMessage())
                .collect(Collectors.joining(", "));
    }
}
