package ma.estfbs.pfe_management.service;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.EtudiantBinomeDTOs.*;
import ma.estfbs.pfe_management.model.*;
import ma.estfbs.pfe_management.model.DemandeBinome.Statut;
import ma.estfbs.pfe_management.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtudiantBinomeService {

    private final BinomeRepository binomeRepository;
    private final DemandeBinomeRepository demandeBinomeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final SujetRepository sujetRepository;
    private final JwtService jwtService;

    /**
     * Check the binome status for the current student
     */
    public BinomeStatusResponse checkBinomeStatus() {
        // Get current user from JWT token
        String token = jwtService.getTokenFromRequest();
        Long userId = jwtService.extractUserId(token);
        
        Utilisateur currentUser = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Check if student is in a binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2(currentUser, currentUser);
        boolean hasBinome = !binomes.isEmpty();
        
        // Check for pending requests
        List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByDemandeAndStatut(currentUser, Statut.EN_ATTENTE);
        boolean hasPendingRequests = !pendingRequests.isEmpty();
        
        // Create response
        BinomeStatusResponse response = BinomeStatusResponse.builder()
                .hasBinome(hasBinome)
                .hasPendingRequests(hasPendingRequests)
                .selectedSujet(false)
                .build();
        
        // If student has a binome, add details
        if (hasBinome) {
            Binome binome = binomes.get(0);
            response.setBinome(mapToBinomeDTO(binome));
            response.setSelectedSujet(binome.getSujet() != null);
            if (binome.getSujet() != null) {
                response.setSelectedSubject(mapToSujetDTO(binome.getSujet()));
            }
        }
        
        return response;
    }
    
    /**
     * Get pending binome requests for the current student
     */
    public BinomeRequestResponse getPendingRequests() {
        try {
            // Get current user from JWT token
            String token = jwtService.getTokenFromRequest();
            Long userId = jwtService.extractUserId(token);
            System.out.println("Getting pending requests for user ID: " + userId);
            
            Utilisateur currentUser = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Get pending requests where current user is the demande (receiver)
            List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByDemandeAndStatut(currentUser, Statut.EN_ATTENTE);
            System.out.println("Found " + pendingRequests.size() + " pending requests for user: " + currentUser.getEmail());
            
            // Map to DTOs
            List<PendingRequestDTO> requestDTOs = pendingRequests.stream()
                    .map(this::mapToPendingRequestDTO)
                    .collect(Collectors.toList());
            
            // Debug output
            for (PendingRequestDTO request : requestDTOs) {
                System.out.println("Pending request from: " + request.getDemandeur().getPrenom() + " " + 
                        request.getDemandeur().getNom() + " (ID: " + request.getId() + ")");
            }
            
            return BinomeRequestResponse.builder()
                    .pendingRequests(requestDTOs)
                    .build();
        } catch (Exception e) {
            System.err.println("Error in getPendingRequests: " + e.getMessage());
            e.printStackTrace();
            return BinomeRequestResponse.builder()
                    .pendingRequests(new ArrayList<>())
                    .build();
        }
    }
    
   /**
    * Handle a binome request (accept or reject)
    */
    @Transactional
    public void handleBinomeRequest(BinomeRequestActionRequest request) {
        // Get current user from JWT token
        String token = jwtService.getTokenFromRequest();
        Long userId = jwtService.extractUserId(token);
        
        Utilisateur currentUser = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Find the request
        DemandeBinome demande = demandeBinomeRepository.findById(request.getRequestId())
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
        
        // Verify this request is for the current user
        if (!demande.getDemande().getId().equals(currentUser.getId()) || demande.getStatut() != Statut.EN_ATTENTE) {
            throw new RuntimeException("Demande invalide");
        }
        
        // If accepted, create a binome
        if (request.isAccept()) {
            // Update request status
            demande.setStatut(Statut.ACCEPTER);
            demandeBinomeRepository.save(demande);
            
            // Find an encadrant (choose any encadrant for now)
            Utilisateur encadrant = utilisateurRepository.findAll().stream()
                    .filter(user -> user.getRole() == Utilisateur.Role.ENCADRANT)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Aucun encadrant disponible"));
            
            // Create binome with encadrant
            Binome binome = Binome.builder()
                    .etudiant1(demande.getDemandeur())
                    .etudiant2(currentUser)
                    .encadrant(encadrant) // Add an encadrant
                    .build();
            
            binomeRepository.save(binome);
            
            // Reject all other pending requests for both students
            rejectAllOtherRequests(demande.getDemandeur(), currentUser);
        } else {
            // Reject the request
            demande.setStatut(Statut.REFUSER);
            demandeBinomeRepository.save(demande);
        }
    }
    
    /**
     * Get available students for binome formation
     */
    public AvailableStudentsResponse getAvailableStudents() {
        try {
            // Get current user from JWT token
            String token = jwtService.getTokenFromRequest();
            Long userId = jwtService.extractUserId(token);
            System.out.println("Current user ID: " + userId);
            
            Utilisateur currentUser = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            System.out.println("Found user: " + currentUser.getEmail());
            
            // Get all ETUDIANT users except current user
            List<Utilisateur> allStudents = utilisateurRepository.findAll().stream()
                    .filter(user -> user.getRole() == Utilisateur.Role.ETUDIANT)
                    .filter(user -> !user.getId().equals(userId))
                    .collect(Collectors.toList());
            System.out.println("Found " + allStudents.size() + " total students excluding current user");
            
            // Get all students in binomes (either as etudiant1 or etudiant2)
            List<Utilisateur> studentsInBinomes = new ArrayList<>();
            binomeRepository.findAll().forEach(binome -> {
                if (binome.getEtudiant1() != null) {
                    studentsInBinomes.add(binome.getEtudiant1());
                }
                if (binome.getEtudiant2() != null) {
                    studentsInBinomes.add(binome.getEtudiant2());
                }
            });
            System.out.println("Found " + studentsInBinomes.size() + " students already in binomes");
            
            // Filter available students (not in binomes)
            List<StudentDTO> availableStudents = allStudents.stream()
                    .filter(user -> !studentsInBinomes.contains(user))
                    .map(this::mapToStudentDTO)
                    .collect(Collectors.toList());
            
            System.out.println("Final available students count: " + availableStudents.size());
            for (StudentDTO student : availableStudents) {
                System.out.println("Available student: " + student.getPrenom() + " " + student.getNom() + 
                    " (ID: " + student.getId() + ", Email: " + student.getEmail() + ")");
            }
            
            // Get rejected students
            List<DemandeBinome> rejectedRequests = demandeBinomeRepository.findByDemandeurAndStatut(currentUser, Statut.REFUSER);
            List<Long> rejectedStudentIds = rejectedRequests.stream()
                    .map(demande -> demande.getDemande().getId())
                    .collect(Collectors.toList());
            
            return AvailableStudentsResponse.builder()
                    .availableStudents(availableStudents)
                    .hasRejectedRequests(!rejectedRequests.isEmpty())
                    .rejectedStudentIds(rejectedStudentIds)
                    .build();
        } catch (Exception e) {
            System.err.println("Error in getAvailableStudents: " + e.getMessage());
            e.printStackTrace();
            return AvailableStudentsResponse.builder()
                    .availableStudents(new ArrayList<>())
                    .hasRejectedRequests(false)
                    .rejectedStudentIds(new ArrayList<>())
                    .build();
        }
    }
    
    /**
     * Send a binome request to another student
     */
    @Transactional
    public void sendBinomeRequest(SendRequestRequest request) {
        try {
            // Get current user from JWT token
            String token = jwtService.getTokenFromRequest();
            Long userId = jwtService.extractUserId(token);
            System.out.println("Sending binome request from user ID: " + userId + " to user ID: " + request.getReceiverId());
            
            Utilisateur currentUser = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Check if current user is already in a binome
            if (binomeRepository.existsByEtudiant1OrEtudiant2(currentUser, currentUser)) {
                throw new RuntimeException("Vous faites déjà partie d'un binôme");
            }
            
            // Get the receiver
            Utilisateur receiver = utilisateurRepository.findById(request.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Destinataire non trouvé"));
            
            // Check if receiver is already in a binome
            if (binomeRepository.existsByEtudiant1OrEtudiant2(receiver, receiver)) {
                throw new RuntimeException("Le destinataire fait déjà partie d'un binôme");
            }
            
            // Check if there's already a pending request between these users
            Optional<DemandeBinome> existingRequest = demandeBinomeRepository.findByDemandeurAndDemande(currentUser, receiver);
            if (existingRequest.isPresent()) {
                Statut status = existingRequest.get().getStatut();
                if (status == Statut.EN_ATTENTE) {
                    throw new RuntimeException("Vous avez déjà envoyé une demande à cet étudiant");
                } else if (status == Statut.REFUSER) {
                    throw new RuntimeException("Votre demande a déjà été refusée par cet étudiant");
                }
            }
            
            // Create the request
            DemandeBinome demande = DemandeBinome.builder()
                    .demandeur(currentUser)
                    .demande(receiver)
                    .statut(Statut.EN_ATTENTE)
                    .build();
            
            demandeBinomeRepository.save(demande);
            System.out.println("Successfully saved binome request with ID: " + demande.getId());
        } catch (Exception e) {
            System.err.println("Error in sendBinomeRequest: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Create a solo binome (student works alone)
     */
    @Transactional
    public BinomeDTO createSoloBinome() {
        try {
            // Get current user from JWT token
            String token = jwtService.getTokenFromRequest();
            Long userId = jwtService.extractUserId(token);
            System.out.println("Creating solo binome for user ID: " + userId);
            
            Utilisateur currentUser = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Check if current user is already in a binome
            if (binomeRepository.existsByEtudiant1OrEtudiant2(currentUser, currentUser)) {
                throw new RuntimeException("Vous faites déjà partie d'un binôme");
            }
            
            // Find an encadrant (choose any encadrant for now)
            Utilisateur encadrant = utilisateurRepository.findAll().stream()
                    .filter(user -> user.getRole() == Utilisateur.Role.ENCADRANT)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Aucun encadrant disponible"));
            
            // Create solo binome with encadrant (still without subject)
            Binome binome = Binome.builder()
                    .etudiant1(currentUser)
                    .etudiant2(null) // No partner
                    .encadrant(encadrant) // Set an encadrant
                    .build();
            
            binome = binomeRepository.save(binome);
            
            // Reject all pending requests for this student
            List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByDemandeAndStatut(currentUser, Statut.EN_ATTENTE);
            pendingRequests.forEach(request -> {
                request.setStatut(Statut.REFUSER);
                demandeBinomeRepository.save(request);
            });
            
            return mapToBinomeDTO(binome);
        } catch (Exception e) {
            System.err.println("Error in createSoloBinome: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Get available subjects for selection
     */
    public AvailableSujetsResponse getAvailableSujets() {
        // Get current user from JWT token
        String token = jwtService.getTokenFromRequest();
        Long userId = jwtService.extractUserId(token);
        
        Utilisateur currentUser = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Get etudiant info to get filiere
        Etudiant currentEtudiant = etudiantRepository.findByUtilisateur(currentUser)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        // Get all subjects for this filiere
        List<Sujet> filiereSujets = sujetRepository.findByFiliere(currentEtudiant.getFiliere());
        
        // Get subjects not assigned to any binome
        List<SujetDTO> availableSujets = filiereSujets.stream()
                .filter(sujet -> sujet.getBinomes() == null || sujet.getBinomes().isEmpty())
                .map(this::mapToSujetDTO)
                .collect(Collectors.toList());
        
        return AvailableSujetsResponse.builder()
                .availableSujets(availableSujets)
                .build();
    }
    
    /**
     * Select a subject for a binome
     */
    @Transactional
    public BinomeDTO selectSujet(SelectSujetRequest request) {
        // Get current user from JWT token
        String token = jwtService.getTokenFromRequest();
        Long userId = jwtService.extractUserId(token);
        
        Utilisateur currentUser = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Get user's binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2(currentUser, currentUser);
        if (binomes.isEmpty()) {
            throw new RuntimeException("Vous ne faites pas partie d'un binôme");
        }
        
        Binome binome = binomes.get(0);
        
        // Check if binome already has a subject
        if (binome.getSujet() != null) {
            throw new RuntimeException("Votre binôme a déjà choisi un sujet");
        }
        
        // Get the subject
        Sujet sujet = sujetRepository.findById(request.getSujetId())
                .orElseThrow(() -> new RuntimeException("Sujet non trouvé"));
        
        // Update binome with subject
        binome.setSujet(sujet);
        binome = binomeRepository.save(binome);
        
        return mapToBinomeDTO(binome);
    }
    
    /**
     * Reject all pending requests for both students
     */
    private void rejectAllOtherRequests(Utilisateur student1, Utilisateur student2) {
        // Reject all pending requests where these students are receivers
        List<DemandeBinome> pendingRequestsForStudent1 = demandeBinomeRepository.findByDemandeAndStatut(student1, Statut.EN_ATTENTE);
        List<DemandeBinome> pendingRequestsForStudent2 = demandeBinomeRepository.findByDemandeAndStatut(student2, Statut.EN_ATTENTE);
        
        pendingRequestsForStudent1.forEach(request -> {
            request.setStatut(Statut.REFUSER);
            demandeBinomeRepository.save(request);
        });
        
        pendingRequestsForStudent2.forEach(request -> {
            request.setStatut(Statut.REFUSER);
            demandeBinomeRepository.save(request);
        });
        
        // Reject all pending requests where these students are senders
        List<DemandeBinome> pendingRequestsFromStudent1 = demandeBinomeRepository.findByDemandeurAndStatut(student1, Statut.EN_ATTENTE);
        List<DemandeBinome> pendingRequestsFromStudent2 = demandeBinomeRepository.findByDemandeurAndStatut(student2, Statut.EN_ATTENTE);
        
        pendingRequestsFromStudent1.forEach(request -> {
            request.setStatut(Statut.REFUSER);
            demandeBinomeRepository.save(request);
        });
        
        pendingRequestsFromStudent2.forEach(request -> {
            request.setStatut(Statut.REFUSER);
            demandeBinomeRepository.save(request);
        });
    }
    
    // Mapping methods
    
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
    
    private PendingRequestDTO mapToPendingRequestDTO(DemandeBinome demande) {
        return PendingRequestDTO.builder()
                .id(demande.getId())
                .demandeur(mapToStudentDTO(demande.getDemandeur()))
                .statut(demande.getStatut())
                .build();
    }
}