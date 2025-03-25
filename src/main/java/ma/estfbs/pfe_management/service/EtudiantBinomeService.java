package ma.estfbs.pfe_management.service;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.EtudiantBinomeDTOs.*;
import ma.estfbs.pfe_management.dto.SujetDTO;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.DemandeBinome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.model.DemandeBinome.Statut;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.DemandeBinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.SujetRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtudiantBinomeService {

    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final BinomeRepository binomeRepository;
    private final DemandeBinomeRepository demandeBinomeRepository;
    private final SujetRepository sujetRepository;

    /**
     * Get the current status of a student
     * @param etudiantId ID of the authenticated student
     * @return EtudiantStatusDTO with current status information
     */
    public EtudiantStatusDTO getEtudiantStatus(Long etudiantId) {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
                
        // Check if student is part of a binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2(etudiant, etudiant);
        boolean hasBinome = !binomes.isEmpty();
        
        // Check if student has any pending incoming binome requests
        List<DemandeBinome> pendingIncomingRequests = demandeBinomeRepository.findByDemandeAndStatut(etudiant, Statut.EN_ATTENTE);
        boolean hasPendingRequests = !pendingIncomingRequests.isEmpty();
        
        // Check if student has sent any binome requests
        List<DemandeBinome> outgoingRequests = demandeBinomeRepository.findByDemandeurAndStatut(etudiant, Statut.EN_ATTENTE);
        boolean hasRequestSent = !outgoingRequests.isEmpty();
        
        EtudiantStatusDTO status = EtudiantStatusDTO.builder()
            .hasBinome(hasBinome)
            .hasPendingRequests(hasPendingRequests)
            .hasRequestSent(hasRequestSent)
            .build();
        
        if (hasBinome) {
            Binome binome = binomes.get(0);
            status.setBinomeId(binome.getId());
            
            if (binome.getSujet() != null) {
                status.setSujetId(binome.getSujet().getId());
                status.setStatusMessage("Vous avez déjà un binôme et un sujet assignés.");
            } else {
                status.setStatusMessage("Vous avez un binôme, mais vous devez choisir un sujet.");
            }
        } else if (hasPendingRequests) {
            status.setStatusMessage("Vous avez des demandes de binôme en attente.");
        } else if (hasRequestSent) {
            status.setStatusMessage("Vous avez envoyé une demande de binôme. Attendez la réponse.");
        } else {
            status.setStatusMessage("Vous n'avez pas encore de binôme.");
        }
        
        return status;
    }
    
    /**
     * Get list of available students for binome creation
     * @param etudiantId ID of the authenticated student
     * @return BinomeSearchDTO with available students and pending requests
     */
    public BinomeSearchDTO getBinomeSearchData(Long etudiantId) {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        // Get the student's filiere
        Etudiant currentEtudiantEntity = etudiantRepository.findByUtilisateur(etudiant)
                .orElseThrow(() -> new RuntimeException("Données étudiant non trouvées"));
        
        Filiere currentFiliere = currentEtudiantEntity.getFiliere();
        
        // Get all students from the same filiere who don't have a binome yet
        List<Etudiant> filiereEtudiants = etudiantRepository.findByFiliere(currentFiliere);
        
        List<AvailableStudentDTO> availableStudents = new ArrayList<>();
        
        for (Etudiant filiereEtudiant : filiereEtudiants) {
            Utilisateur student = filiereEtudiant.getUtilisateur();
            
            // Skip the current student
            if (student.getId().equals(etudiantId)) {
                continue;
            }
            
            // Skip students who already have a binome
            if (binomeRepository.existsByEtudiant1OrEtudiant2(student, student)) {
                continue;
            }
            
            // Check if a request has been sent to this student before and was rejected
            boolean canRequest = true;
            boolean alreadyRequested = false;
            
            Optional<DemandeBinome> previousRequest = demandeBinomeRepository.findByDemandeurAndDemande(etudiant, student);
            if (previousRequest.isPresent()) {
                DemandeBinome request = previousRequest.get();
                if (request.getStatut() == Statut.REFUSER) {
                    canRequest = false;
                } else if (request.getStatut() == Statut.EN_ATTENTE) {
                    alreadyRequested = true;
                }
            }
            
            // Add student to available list
            availableStudents.add(AvailableStudentDTO.builder()
                    .id(student.getId())
                    .nom(student.getNom())
                    .prenom(student.getPrenom())
                    .email(student.getEmail())
                    .filiereName(currentFiliere.getNom())
                    .alreadyRequested(alreadyRequested)
                    .canRequest(canRequest)
                    .build());
        }
        
        // Get pending requests
        List<DemandeBinome> incomingRequests = demandeBinomeRepository.findByDemandeAndStatut(etudiant, Statut.EN_ATTENTE);
        List<DemandeBinome> outgoingRequests = demandeBinomeRepository.findByDemandeurAndStatut(etudiant, Statut.EN_ATTENTE);
        
        // Convert to DTOs
        List<BinomeRequestDTO> incomingRequestDTOs = incomingRequests.stream()
                .map(this::mapToBinomeRequestDTO)
                .collect(Collectors.toList());
        
        List<BinomeRequestDTO> outgoingRequestDTOs = outgoingRequests.stream()
                .map(this::mapToBinomeRequestDTO)
                .collect(Collectors.toList());
        
        return BinomeSearchDTO.builder()
                .availableStudents(availableStudents)
                .incomingRequests(incomingRequestDTOs)
                .outgoingRequests(outgoingRequestDTOs)
                .build();
    }
    
    /**
     * Create a binome request
     * @param etudiantId ID of the authenticated student
     * @param request contains the ID of the student to send the request to
     * @return BinomeRequestResponseDTO with success status and message
     */
    @Transactional
    public BinomeRequestResponseDTO createBinomeRequest(Long etudiantId, BinomeRequestCreateDTO request) {
        Utilisateur demandeur = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant demandeur non trouvé"));
        
        Utilisateur demande = utilisateurRepository.findById(request.getDemandeId())
                .orElseThrow(() -> new RuntimeException("Étudiant cible non trouvé"));
        
        // Check if either student already has a binome
        if (binomeRepository.existsByEtudiant1OrEtudiant2(demandeur, demandeur)) {
            return BinomeRequestResponseDTO.builder()
                    .success(false)
                    .message("Vous avez déjà un binôme.")
                    .build();
        }
        
        if (binomeRepository.existsByEtudiant1OrEtudiant2(demande, demande)) {
            return BinomeRequestResponseDTO.builder()
                    .success(false)
                    .message("Cet étudiant a déjà un binôme.")
                    .build();
        }
        
        // Check if a request has already been sent
        if (demandeBinomeRepository.existsByDemandeurAndDemande(demandeur, demande)) {
            return BinomeRequestResponseDTO.builder()
                    .success(false)
                    .message("Vous avez déjà envoyé une demande à cet étudiant.")
                    .build();
        }
        
        // Check if there's a request from the other student
        Optional<DemandeBinome> reverseRequest = demandeBinomeRepository.findByDemandeurAndDemande(demande, demandeur);
        if (reverseRequest.isPresent()) {
            // Auto-accept the request and create a binome
            DemandeBinome existingRequest = reverseRequest.get();
            existingRequest.setStatut(Statut.ACCEPTER);
            demandeBinomeRepository.save(existingRequest);
            
            Binome newBinome = Binome.builder()
                    .etudiant1(demandeur)
                    .etudiant2(demande)
                    .build();
            binomeRepository.save(newBinome);
            
            return BinomeRequestResponseDTO.builder()
                    .success(true)
                    .message("Binôme créé automatiquement car une demande réciproque existait.")
                    .build();
        }
        
        // Create the new request
        DemandeBinome newRequest = DemandeBinome.builder()
                .demandeur(demandeur)
                .demande(demande)
                .statut(Statut.EN_ATTENTE)
                .build();
        
        demandeBinomeRepository.save(newRequest);
        
        return BinomeRequestResponseDTO.builder()
                .success(true)
                .message("Demande de binôme envoyée avec succès.")
                .build();
    }
    
    /**
     * Respond to a binome request
     * @param etudiantId ID of the authenticated student
     * @param requestId ID of the binome request
     * @param accept true to accept, false to reject
     * @return BinomeRequestResponseDTO with success status and message
     */
    @Transactional
    public BinomeRequestResponseDTO respondToBinomeRequest(Long etudiantId, Long requestId, boolean accept) {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        DemandeBinome request = demandeBinomeRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande de binôme non trouvée"));
        
        // Verify this request is for the current student
        if (!request.getDemande().getId().equals(etudiantId)) {
            return BinomeRequestResponseDTO.builder()
                    .success(false)
                    .message("Cette demande ne vous concerne pas.")
                    .build();
        }
        
        // Check if either student already has a binome
        if (binomeRepository.existsByEtudiant1OrEtudiant2(etudiant, etudiant)) {
            return BinomeRequestResponseDTO.builder()
                    .success(false)
                    .message("Vous avez déjà un binôme.")
                    .build();
        }
        
        Utilisateur demandeur = request.getDemandeur();
        if (binomeRepository.existsByEtudiant1OrEtudiant2(demandeur, demandeur)) {
            request.setStatut(Statut.REFUSER);
            demandeBinomeRepository.save(request);
            
            return BinomeRequestResponseDTO.builder()
                    .success(false)
                    .message("Cet étudiant a déjà un binôme.")
                    .build();
        }
        
        if (accept) {
            // Accept the request and create a binome
            request.setStatut(Statut.ACCEPTER);
            demandeBinomeRepository.save(request);
            
            // Create the binome
            Binome newBinome = Binome.builder()
                    .etudiant1(demandeur)
                    .etudiant2(etudiant)
                    .build();
            binomeRepository.save(newBinome);
            
            // Cancel any other pending requests for both students
            List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByStatut(Statut.EN_ATTENTE);
            for (DemandeBinome pendingRequest : pendingRequests) {
                if (pendingRequest.getDemandeur().getId().equals(etudiantId) || 
                    pendingRequest.getDemande().getId().equals(etudiantId) ||
                    pendingRequest.getDemandeur().getId().equals(demandeur.getId()) ||
                    pendingRequest.getDemande().getId().equals(demandeur.getId())) {
                    
                    if (!pendingRequest.getId().equals(requestId)) {
                        pendingRequest.setStatut(Statut.REFUSER);
                        demandeBinomeRepository.save(pendingRequest);
                    }
                }
            }
            
            return BinomeRequestResponseDTO.builder()
                    .success(true)
                    .message("Vous avez accepté la demande. Binôme créé avec succès.")
                    .build();
        } else {
            // Reject the request
            request.setStatut(Statut.REFUSER);
            demandeBinomeRepository.save(request);
            
            return BinomeRequestResponseDTO.builder()
                    .success(true)
                    .message("Vous avez refusé la demande.")
                    .build();
        }
    }
    
    /**
     * Cancel a binome request that was sent
     * @param etudiantId ID of the authenticated student
     * @param requestId ID of the binome request
     * @return BinomeRequestResponseDTO with success status and message
     */
    @Transactional
    public BinomeRequestResponseDTO cancelBinomeRequest(Long etudiantId, Long requestId) {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        DemandeBinome request = demandeBinomeRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande de binôme non trouvée"));
        
        // Verify this request was created by the current student
        if (!request.getDemandeur().getId().equals(etudiantId)) {
            return BinomeRequestResponseDTO.builder()
                    .success(false)
                    .message("Vous ne pouvez pas annuler cette demande.")
                    .build();
        }
        
        // Check if the request is still pending
        if (request.getStatut() != Statut.EN_ATTENTE) {
            return BinomeRequestResponseDTO.builder()
                    .success(false)
                    .message("Cette demande n'est plus en attente.")
                    .build();
        }
        
        // Delete the request
        demandeBinomeRepository.delete(request);
        
        return BinomeRequestResponseDTO.builder()
                .success(true)
                .message("Demande de binôme annulée avec succès.")
                .build();
    }
    
    /**
     * Choose to continue solo
     * @param etudiantId ID of the authenticated student
     * @return ContinueSoloDTO with success status and message
     */
    @Transactional
    public ContinueSoloDTO continueSolo(Long etudiantId) {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        // Check if student already has a binome
        if (binomeRepository.existsByEtudiant1OrEtudiant2(etudiant, etudiant)) {
            return ContinueSoloDTO.builder()
                    .success(false)
                    .message("Vous avez déjà un binôme.")
                    .build();
        }
        
        // Create a solo binome
        Binome soloBinome = Binome.builder()
                .etudiant1(etudiant)
                .etudiant2(null) // No partner
                .build();
        
        binomeRepository.save(soloBinome);
        
        // Cancel any pending requests
        List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByStatut(Statut.EN_ATTENTE);
        for (DemandeBinome pendingRequest : pendingRequests) {
            if (pendingRequest.getDemandeur().getId().equals(etudiantId) || 
                pendingRequest.getDemande().getId().equals(etudiantId)) {
                
                pendingRequest.setStatut(Statut.REFUSER);
                demandeBinomeRepository.save(pendingRequest);
            }
        }
        
        return ContinueSoloDTO.builder()
                .success(true)
                .message("Vous avez choisi de continuer en solo.")
                .build();
    }
    
    // Helper methods
    
    private BinomeRequestDTO mapToBinomeRequestDTO(DemandeBinome demande) {
        Utilisateur demandeur = demande.getDemandeur();
        
        // Get filiere for the student
        Etudiant etudiantEntity = etudiantRepository.findByUtilisateur(demandeur)
                .orElse(null);
        
        String filiereName = etudiantEntity != null && etudiantEntity.getFiliere() != null
                ? etudiantEntity.getFiliere().getNom()
                : "Non assigné";
        
        return BinomeRequestDTO.builder()
                .id(demande.getId())
                .demandeurId(demandeur.getId())
                .demandeurNom(demandeur.getNom())
                .demandeurPrenom(demandeur.getPrenom())
                .demandeurEmail(demandeur.getEmail())
                .filiereName(filiereName)
                .status(demande.getStatut().toString())
                .build();
    }
}