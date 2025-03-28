package ma.estfbs.pfe_management.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.BinomeAddRequest;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.BinomeDTO;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.BinomeEditRequest;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.BinomeManagementResponse;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.EncadrantDTO;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.StudentDTO;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.SujetDTO;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.repository.SujetRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;

@Service
@RequiredArgsConstructor
public class BinomeManagementService {

    private final BinomeRepository binomeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final FiliereRepository filiereRepository;
    private final SujetRepository sujetRepository;
    private final AcademicYearService academicYearService;

    /**
     * Get binomes, filieres, available students, encadrants and subjects for the current year
     */
    public BinomeManagementResponse getBinomeManagementData(Long filiereId) {
        List<BinomeDTO> binomes;
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        if (filiereId != null) {
            // Get binomes filtered by filiere for current year
            Filiere filiere = filiereRepository.findById(filiereId)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + filiereId));
            
            // Get students in this filiere
            List<Etudiant> filiereStudents = etudiantRepository.findByFiliere(filiere);
            List<Utilisateur> filiereUtilisateurs = filiereStudents.stream()
                    .map(Etudiant::getUtilisateur)
                    .collect(Collectors.toList());
            
            // Get binomes where at least one student is from this filiere AND binome is for current year
            binomes = binomeRepository.findAll().stream()
                    .filter(binome -> 
                        binome.getAnneeScolaire().getId().equals(currentYear.getId()) && 
                        (filiereUtilisateurs.contains(binome.getEtudiant1()) || 
                        (binome.getEtudiant2() != null && filiereUtilisateurs.contains(binome.getEtudiant2()))))
                    .map(this::mapToBinomeDTO)
                    .collect(Collectors.toList());
        } else {
            // Get all binomes for current year
            binomes = binomeRepository.findAll().stream()
                    .filter(binome -> binome.getAnneeScolaire().getId().equals(currentYear.getId()))
                    .map(this::mapToBinomeDTO)
                    .collect(Collectors.toList());
        }
        
        // Get all filieres
        List<FiliereDTO> filieres = filiereRepository.findAll().stream()
                .map(this::mapToFiliereDTO)
                .collect(Collectors.toList());
        
        // Get available students (those not in a binome for current year)
        List<StudentDTO> availableStudents = getAvailableStudents(currentYear);
        
        // Get all encadrants
        List<EncadrantDTO> encadrants = utilisateurRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ENCADRANT)
                .map(this::mapToEncadrantDTO)
                .collect(Collectors.toList());
        
        // Get available subjects (not assigned to any binome for current year)
        List<SujetDTO> availableSujets = sujetRepository.findByAnneeScolaire(currentYear)
                .stream()
                .filter(sujet -> sujet.getBinomes().stream()
                    .noneMatch(binome -> binome.getAnneeScolaire().getId().equals(currentYear.getId())))
                .map(this::mapToSujetDTO)
                .collect(Collectors.toList());
        
        return BinomeManagementResponse.builder()
                .binomes(binomes)
                .filieres(filieres)
                .availableStudents(availableStudents)
                .encadrants(encadrants)
                .availableSujets(availableSujets)
                .build();
    }
    
    /**
     * Add a new binome for the current academic year
     */
    @Transactional
    public BinomeDTO addBinome(BinomeAddRequest request) {
        // Validate etudiant1
        Utilisateur etudiant1 = utilisateurRepository.findById(request.getEtudiant1Id())
                .orElseThrow(() -> new RuntimeException("Étudiant 1 non trouvé"));
        
        // Get current year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        // Check if student is already in a binome for current year
        if (binomeRepository.findAll().stream()
                .filter(b -> b.getAnneeScolaire().getId().equals(currentYear.getId()))
                .anyMatch(b -> b.getEtudiant1().equals(etudiant1) || 
                        (b.getEtudiant2() != null && b.getEtudiant2().equals(etudiant1)))) {
            throw new RuntimeException("L'étudiant 1 fait déjà partie d'un binôme pour l'année courante");
        }
        
        // Validate etudiant2 if provided
        final Utilisateur etudiant2;
        if (request.getEtudiant2Id() != null) {
            etudiant2 = utilisateurRepository.findById(request.getEtudiant2Id())
                    .orElseThrow(() -> new RuntimeException("Étudiant 2 non trouvé"));
            
            // Check if student is already in a binome for current year
            if (binomeRepository.findAll().stream()
                    .filter(b -> b.getAnneeScolaire().getId().equals(currentYear.getId()))
                    .anyMatch(b -> b.getEtudiant1().equals(etudiant2) || 
                            (b.getEtudiant2() != null && b.getEtudiant2().equals(etudiant2)))) {
                throw new RuntimeException("L'étudiant 2 fait déjà partie d'un binôme pour l'année courante");
            }
        } else {
            etudiant2 = null;
        }
        
        // Validate encadrant
        Utilisateur encadrant = utilisateurRepository.findById(request.getEncadrantId())
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé"));
        
        if (encadrant.getRole() != Role.ENCADRANT) {
            throw new RuntimeException("L'utilisateur sélectionné n'est pas un encadrant");
        }
        
        // Validate sujet
        Sujet sujet = sujetRepository.findById(request.getSujetId())
                .orElseThrow(() -> new RuntimeException("Sujet non trouvé"));
        
        // Create and save binome with current year
        Binome binome = Binome.builder()
                .etudiant1(etudiant1)
                .etudiant2(etudiant2)
                .encadrant(encadrant)
                .sujet(sujet)
                .anneeScolaire(currentYear)
                .build();
        
        binome = binomeRepository.save(binome);
        
        return mapToBinomeDTO(binome);
    }
    /**
     * Edit a binome's encadrant
     */
    @Transactional
    public BinomeDTO editBinome(Long id, BinomeEditRequest request) {
        Binome binome = binomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Binôme non trouvé avec l'id: " + id));
        
        // Check if binome is from current year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        if (!binome.getAnneeScolaire().getId().equals(currentYear.getId())) {
            throw new RuntimeException("Impossible de modifier un binôme d'une année précédente");
        }
        
        // Validate encadrant
        Utilisateur encadrant = utilisateurRepository.findById(request.getEncadrantId())
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé"));
        
        if (encadrant.getRole() != Role.ENCADRANT) {
            throw new RuntimeException("L'utilisateur sélectionné n'est pas un encadrant");
        }
        
        // Update binome
        binome.setEncadrant(encadrant);
        binome = binomeRepository.save(binome);
        
        return mapToBinomeDTO(binome);
    }
    
    /**
     * Delete a binome
     */
    @Transactional
    public void deleteBinome(Long id) {
        Binome binome = binomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Binôme non trouvé avec l'id: " + id));
                
        // Check if binome is from current year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        if (!binome.getAnneeScolaire().getId().equals(currentYear.getId())) {
            throw new RuntimeException("Impossible de supprimer un binôme d'une année précédente");
        }
        
        binomeRepository.deleteById(id);
    }
    
    /**
     * Get students that are not in any binome for the current year
     */
    private List<StudentDTO> getAvailableStudents(AnneeScolaire currentYear) {
        // Get all students for current year
        List<Etudiant> allEtudiants = etudiantRepository.findByAnneeScolaire(currentYear);
        List<Utilisateur> allStudents = allEtudiants.stream()
                .map(Etudiant::getUtilisateur)
                .collect(Collectors.toList());
        
        // Get students already in binomes for current year
        List<Utilisateur> studentsInBinomes = new ArrayList<>();
        binomeRepository.findAll().stream()
                .filter(binome -> binome.getAnneeScolaire().getId().equals(currentYear.getId()))
                .forEach(binome -> {
                    studentsInBinomes.add(binome.getEtudiant1());
                    if (binome.getEtudiant2() != null) {
                        studentsInBinomes.add(binome.getEtudiant2());
                    }
                });
        
        // Filter out students already in binomes
        return allStudents.stream()
                .filter(student -> !studentsInBinomes.contains(student))
                .map(this::mapToStudentDTO)
                .collect(Collectors.toList());
    }
    
    // Existing mapper methods...
    // These would stay the same as they don't need to change
    
    /**
     * Map Binome entity to BinomeDTO
     */
    private BinomeDTO mapToBinomeDTO(Binome binome) {
        // Get filiere from etudiant1
        Etudiant etudiant1 = etudiantRepository.findByUtilisateur(binome.getEtudiant1())
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + binome.getEtudiant1().getId()));
        
        String filiereName = etudiant1.getFiliere().getNom();
        
        return BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToStudentDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2()) : null)
                .encadrant(mapToEncadrantDTO(binome.getEncadrant()))
                .sujet(mapToSujetDTO(binome.getSujet()))
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
                .email(utilisateur.getEmail())
                .cne(utilisateur.getCne())
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
                .email(utilisateur.getEmail())
                .build();
    }
    
    /**
     * Map Sujet entity to SujetDTO
     */
    private SujetDTO mapToSujetDTO(Sujet sujet) {
        return SujetDTO.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
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