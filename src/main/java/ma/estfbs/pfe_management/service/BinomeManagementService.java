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

    /**
     * Get binomes, filieres, available students, encadrants and subjects
     */
    public BinomeManagementResponse getBinomeManagementData(Long filiereId) {
        List<BinomeDTO> binomes;
        
        if (filiereId != null) {
            // Get binomes filtered by filiere
            Filiere filiere = filiereRepository.findById(filiereId)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + filiereId));
            
            // Get students in this filiere
            List<Etudiant> filiereStudents = etudiantRepository.findByFiliere(filiere);
            List<Utilisateur> filiereUtilisateurs = filiereStudents.stream()
                    .map(Etudiant::getUtilisateur)
                    .collect(Collectors.toList());
            
            // Get binomes where at least one student is from this filiere
            binomes = binomeRepository.findAll().stream()
                    .filter(binome -> 
                        filiereUtilisateurs.contains(binome.getEtudiant1()) || 
                        (binome.getEtudiant2() != null && filiereUtilisateurs.contains(binome.getEtudiant2())))
                    .map(this::mapToBinomeDTO)
                    .collect(Collectors.toList());
        } else {
            // Get all binomes
            binomes = binomeRepository.findAll().stream()
                    .map(this::mapToBinomeDTO)
                    .collect(Collectors.toList());
        }
        
        // Get all filieres
        List<FiliereDTO> filieres = filiereRepository.findAll().stream()
                .map(this::mapToFiliereDTO)
                .collect(Collectors.toList());
        
        // Get available students (those not in a binome)
        List<StudentDTO> availableStudents = getAvailableStudents();
        
        // Get all encadrants
        List<EncadrantDTO> encadrants = utilisateurRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ENCADRANT)
                .map(this::mapToEncadrantDTO)
                .collect(Collectors.toList());
        
        // Get available subjects (not assigned to any binome)
        List<SujetDTO> availableSujets = sujetRepository.findByBinomesIsEmpty().stream()
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
     * Add a new binome
     */
    @Transactional
    public BinomeDTO addBinome(BinomeAddRequest request) {
        // Validate etudiant1
        Utilisateur etudiant1 = utilisateurRepository.findById(request.getEtudiant1Id())
                .orElseThrow(() -> new RuntimeException("Étudiant 1 non trouvé"));
        
        if (binomeRepository.existsByEtudiant1OrEtudiant2(etudiant1, etudiant1)) {
            throw new RuntimeException("L'étudiant 1 fait déjà partie d'un binôme");
        }
        
        // Validate etudiant2 if provided
        Utilisateur etudiant2 = null;
        if (request.getEtudiant2Id() != null) {
            etudiant2 = utilisateurRepository.findById(request.getEtudiant2Id())
                    .orElseThrow(() -> new RuntimeException("Étudiant 2 non trouvé"));
            
            if (binomeRepository.existsByEtudiant1OrEtudiant2(etudiant2, etudiant2)) {
                throw new RuntimeException("L'étudiant 2 fait déjà partie d'un binôme");
            }
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
        
        // Create and save binome
        Binome binome = Binome.builder()
                .etudiant1(etudiant1)
                .etudiant2(etudiant2)
                .encadrant(encadrant)
                .sujet(sujet)
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
        if (!binomeRepository.existsById(id)) {
            throw new RuntimeException("Binôme non trouvé avec l'id: " + id);
        }
        
        binomeRepository.deleteById(id);
    }
    
    /**
     * Get students that are not in any binome
     */
    private List<StudentDTO> getAvailableStudents() {
        // Get all students
        List<Utilisateur> allStudents = utilisateurRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ETUDIANT)
                .collect(Collectors.toList());
        
        // Get students already in binomes
        List<Utilisateur> studentsInBinomes = new ArrayList<>();
        binomeRepository.findAll().forEach(binome -> {
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
