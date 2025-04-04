package ma.estfbs.pfe_management.controller;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.EtudiantBinomeDTOs.*;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.DemandeBinome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.DemandeBinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.SujetRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;
import ma.estfbs.pfe_management.service.EtudiantBinomeService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PublicTestController {

    private final EtudiantBinomeService etudiantBinomeService;
    private final UtilisateurRepository utilisateurRepository;
    private final BinomeRepository binomeRepository;
    private final DemandeBinomeRepository demandeBinomeRepository;
    private final EtudiantRepository etudiantRepository;
    private final SujetRepository sujetRepository;

    @GetMapping("/test")
    public String test() {
        return "Test endpoint is working!";
    }
    
    @PostMapping("/binome/solo")
    public ResponseEntity<BinomeDTO> createSoloBinome() {
        System.out.println("Public solo binome endpoint called");
        try {
            BinomeDTO result = etudiantBinomeService.createSoloBinome();
            System.out.println("Solo binome created successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error in public createSoloBinome endpoint: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @PostMapping("/direct-solo-binome")
    public ResponseEntity<String> createDirectSoloBinome(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        try {
            System.out.println("Creating direct solo binome for email: " + email);
            
            // Get user by email
            Utilisateur student = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));
            
            // Check if student already has a binome
            boolean hasExistingBinome = binomeRepository.existsByEtudiant1OrEtudiant2(student, student);
            if (hasExistingBinome) {
                return ResponseEntity.badRequest().body("L'étudiant a déjà un binôme");
            }
            
            // Get any encadrant
            Utilisateur encadrant = utilisateurRepository.findAll().stream()
                .filter(user -> user.getRole() == Utilisateur.Role.ENCADRANT)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun encadrant disponible"));
            
            // Create solo binome
            Binome binome = Binome.builder()
                .etudiant1(student)
                .etudiant2(null) // Solo
                .encadrant(encadrant)
                .build();
            
            binomeRepository.save(binome);
            
            return ResponseEntity.ok("Binome solo créé pour " + email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }
    
    @PostMapping("/binome/status")
    public ResponseEntity<Map<String, Object>> getPublicBinomeStatus(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        try {
            System.out.println("Checking binome status for email: " + email);
            
            Utilisateur currentUser = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Check if student is in a binome
            List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2(currentUser, currentUser);
            boolean hasBinome = !binomes.isEmpty();
            
            // Check for pending requests
            List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByDemandeAndStatut(
                currentUser, DemandeBinome.Statut.EN_ATTENTE);
            boolean hasPendingRequests = !pendingRequests.isEmpty();
            
            Map<String, Object> response = new HashMap<>();
            response.put("hasBinome", hasBinome);
            response.put("hasPendingRequests", hasPendingRequests);
            response.put("selectedSujet", false);
            
            // If student has a binome, add details
            if (hasBinome) {
                Binome binome = binomes.get(0);
                BinomeDTO binomeDTO = mapToBinomeDTO(binome);
                response.put("binome", binomeDTO);
                response.put("selectedSujet", binome.getSujet() != null);
                if (binome.getSujet() != null) {
                    response.put("selectedSubject", mapToSujetDTO(binome.getSujet()));
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/binome/available-students")
    public ResponseEntity<Map<String, Object>> getPublicAvailableStudents(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        try {
            System.out.println("Getting available students for email: " + email);
            
            Utilisateur currentUser = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Get etudiant info to get filiere
            Optional<Etudiant> etudiantOpt = etudiantRepository.findByUtilisateur(currentUser);
            if (!etudiantOpt.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "availableStudents", new ArrayList<>(),
                    "hasRejectedRequests", false,
                    "rejectedStudentIds", new ArrayList<>()
                ));
            }
            
            Etudiant currentEtudiant = etudiantOpt.get();
            if (currentEtudiant.getFiliere() == null) {
                return ResponseEntity.ok(Map.of(
                    "availableStudents", new ArrayList<>(),
                    "hasRejectedRequests", false,
                    "rejectedStudentIds", new ArrayList<>()
                ));
            }
            
            // Get all students in the same filiere
            List<Etudiant> filiereStudents = etudiantRepository.findByFiliere(currentEtudiant.getFiliere());
            System.out.println("Found " + filiereStudents.size() + " students in the same filiere");
            
            // Get students already in binomes
            List<Utilisateur> studentsInBinomes = new ArrayList<>();
            binomeRepository.findAll().forEach(binome -> {
                studentsInBinomes.add(binome.getEtudiant1());
                if (binome.getEtudiant2() != null) {
                    studentsInBinomes.add(binome.getEtudiant2());
                }
            });
            System.out.println("Found " + studentsInBinomes.size() + " students already in binomes");
            
            // Get students who rejected current user's request
            List<DemandeBinome> rejectedRequests = demandeBinomeRepository.findByDemandeurAndStatut(
                currentUser, DemandeBinome.Statut.REFUSER);
            List<Long> rejectedStudentIds = rejectedRequests.stream()
                .map(demande -> demande.getDemande().getId())
                .collect(Collectors.toList());
            System.out.println("Found " + rejectedStudentIds.size() + " students who rejected this user's requests");
            
            // Filter available students
            List<StudentDTO> availableStudents = filiereStudents.stream()
                .map(Etudiant::getUtilisateur)
                .filter(user -> !user.getId().equals(currentUser.getId())) // Exclude self
                .filter(user -> !studentsInBinomes.contains(user)) // Exclude students already in binomes
                .filter(user -> !rejectedStudentIds.contains(user.getId())) // Exclude students who rejected
                .map(this::mapToStudentDTO)
                .collect(Collectors.toList());
            
            System.out.println("Final available students count: " + availableStudents.size());
            
            return ResponseEntity.ok(Map.of(
                "availableStudents", availableStudents,
                "hasRejectedRequests", !rejectedRequests.isEmpty(),
                "rejectedStudentIds", rejectedStudentIds
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/binome/requests")
    public ResponseEntity<Map<String, Object>> getPublicPendingRequests(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        try {
            System.out.println("Getting pending requests for email: " + email);
            
            Utilisateur currentUser = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Get pending requests
            List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByDemandeAndStatut(
                currentUser, DemandeBinome.Statut.EN_ATTENTE);
            
            System.out.println("Found " + pendingRequests.size() + " pending requests");
            
            // Map to DTOs
            List<Map<String, Object>> requestDTOs = pendingRequests.stream()
                .map(this::mapToPendingRequestDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("pendingRequests", requestDTOs));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    // Helper methods for mapping entities to DTOs
    
    private BinomeDTO mapToBinomeDTO(Binome binome) {
        return BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToStudentDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2()) : null)
                .encadrant(binome.getEncadrant() != null ? mapToEncadrantDTO(binome.getEncadrant()) : null)
                .sujet(binome.getSujet() != null ? mapToSujetDTO(binome.getSujet()) : null)
                .build();
    }
    
    private StudentDTO mapToStudentDTO(Utilisateur utilisateur) {
        // Get student's filiere name
        String filiereName = null;
        Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur).orElse(null);
        if (etudiant != null && etudiant.getFiliere() != null) {
            filiereName = etudiant.getFiliere().getNom();
        }
        
        return StudentDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .cne(utilisateur.getCne())
                .filiereName(filiereName)
                .build();
    }
    
    private EncadrantDTO mapToEncadrantDTO(Utilisateur utilisateur) {
        return EncadrantDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .build();
    }
    
    private SujetDTO mapToSujetDTO(Sujet sujet) {
        return SujetDTO.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .theme(sujet.getTheme())
                .description(sujet.getDescription())
                .build();
    }
    
    private Map<String, Object> mapToPendingRequestDTO(DemandeBinome demande) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", demande.getId());
        map.put("demandeur", mapToStudentDTO(demande.getDemandeur()));
        map.put("statut", demande.getStatut().toString());
        return map;
    }
    /**
 * Add a simple test endpoint to check if students exist
 */
@GetMapping("/test-students")
public ResponseEntity<List<Map<String, Object>>> getAllStudents() {
    List<Utilisateur> students = utilisateurRepository.findAll().stream()
            .filter(user -> user.getRole() == Utilisateur.Role.ETUDIANT)
            .collect(Collectors.toList());
    
    List<Map<String, Object>> result = new ArrayList<>();
    for (Utilisateur student : students) {
        Map<String, Object> studentMap = new HashMap<>();
        studentMap.put("id", student.getId());
        studentMap.put("nom", student.getNom());
        studentMap.put("prenom", student.getPrenom());
        studentMap.put("email", student.getEmail());
        
        // Check if in binome
        boolean inBinome = binomeRepository.existsByEtudiant1OrEtudiant2(student, student);
        studentMap.put("inBinome", inBinome);
        
        result.add(studentMap);
    }
    
    return ResponseEntity.ok(result);
}
}