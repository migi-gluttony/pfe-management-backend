package ma.estfbs.pfe_management.service;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.SujetDTOs.*;
import ma.estfbs.pfe_management.model.*;
import ma.estfbs.pfe_management.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SujetService {

    private final SujetRepository sujetRepository;
    private final BinomeRepository binomeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final ProposerSujetsRepository proposerSujetsRepository;
    private final FiliereRepository filiereRepository;
    private final JwtService jwtService;

    /**
     * Get available subjects for the student's binome
     */
    public AvailableSujetsResponse getAvailableSujets() {
        // Get current user from JWT token
        String token = jwtService.getTokenFromRequest();
        Long userId = jwtService.extractUserId(token);
        
        System.out.println("Récupération des sujets pour l'utilisateur ID: " + userId);
        
        Utilisateur currentUser = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        System.out.println("Utilisateur trouvé: " + currentUser.getEmail());
        
        // Get student's binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2(currentUser, currentUser);
        if (binomes.isEmpty()) {
            System.out.println("ERROR: L'utilisateur ne fait pas partie d'un binôme");
            throw new RuntimeException("Vous ne faites pas partie d'un binôme");
        }
        
        System.out.println("Binôme trouvé avec ID: " + binomes.get(0).getId());
        
        // Get student's filiere
        Etudiant etudiant = etudiantRepository.findByUtilisateur(currentUser)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        if (etudiant.getFiliere() == null) {
            System.out.println("ERROR: L'étudiant n'a pas de filière");
            throw new RuntimeException("Étudiant sans filière");
        }
        
        System.out.println("Filière trouvée: " + etudiant.getFiliere().getNom() + " (ID: " + etudiant.getFiliere().getId() + ")");
        
        // Get all subjects for this filiere
        List<Sujet> filiereSujets = sujetRepository.findByFiliere(etudiant.getFiliere());
        System.out.println("Nombre de sujets trouvés pour la filière: " + filiereSujets.size());
        
        // Get subjects not assigned to any binome
        List<SujetDTO> availableSujets = filiereSujets.stream()
                .filter(sujet -> {
                    boolean available = sujet.getBinomes() == null || sujet.getBinomes().isEmpty();
                    if (!available) {
                        System.out.println("Sujet " + sujet.getId() + " (" + sujet.getTitre() + ") est déjà assigné à un binôme");
                    }
                    return available;
                })
                .map(this::mapToSujetDTO)
                .collect(Collectors.toList());
        
        System.out.println("Nombre de sujets disponibles après filtrage: " + availableSujets.size());
        if (availableSujets.size() > 0) {
            System.out.println("Premier sujet disponible: " + availableSujets.get(0).getTitre());
        }
        
        return AvailableSujetsResponse.builder()
                .availableSujets(availableSujets)
                .build();
    }
    
    /**
     * Select a subject for the student's binome
     */
    @Transactional
    public BinomeSujetResponse selectSujet(SelectSujetRequest request) {
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
        
        return BinomeSujetResponse.builder()
                .binome(mapToBinomeDTO(binome))
                .sujet(mapToSujetDTO(sujet))
                .build();
    }
    
    /**
     * Get a random available subject
     */
    public SujetDTO getRandomSujet() {
        // Get current user from JWT token
        String token = jwtService.getTokenFromRequest();
        Long userId = jwtService.extractUserId(token);
        
        Utilisateur currentUser = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Get student's filiere
        Etudiant etudiant = etudiantRepository.findByUtilisateur(currentUser)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        if (etudiant.getFiliere() == null) {
            throw new RuntimeException("Étudiant sans filière");
        }
        
        // Get all subjects for this filiere
        List<Sujet> filiereSujets = sujetRepository.findByFiliere(etudiant.getFiliere());
        
        // Filter available subjects
        List<Sujet> availableSujets = filiereSujets.stream()
                .filter(sujet -> sujet.getBinomes() == null || sujet.getBinomes().isEmpty())
                .collect(Collectors.toList());
        
        if (availableSujets.isEmpty()) {
            throw new RuntimeException("Aucun sujet disponible");
        }
        
        // Select a random subject
        Random random = new Random();
        Sujet randomSujet = availableSujets.get(random.nextInt(availableSujets.size()));
        
        return mapToSujetDTO(randomSujet);
    }
    
    /**
     * Propose a new subject
     */
    @Transactional
    public SujetDTO proposerSujet(ProposerSujetRequest request) {
        // Get current user from JWT token
        String token = jwtService.getTokenFromRequest();
        Long userId = jwtService.extractUserId(token);
        
        Utilisateur currentUser = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Get student's binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2(currentUser, currentUser);
        if (binomes.isEmpty()) {
            throw new RuntimeException("Vous ne faites pas partie d'un binôme");
        }
        
        Binome binome = binomes.get(0);
        
        // Get student's filiere
        Etudiant etudiant = etudiantRepository.findByUtilisateur(currentUser)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        if (etudiant.getFiliere() == null) {
            throw new RuntimeException("Étudiant sans filière");
        }
        
        // Create the new proposal
        ProposerSujets proposition = ProposerSujets.builder()
                .titre(request.getTitre())
                .theme(request.getTheme())
                .description(request.getDescription())
                .filiere(etudiant.getFiliere())
                .etudiant(currentUser)
                .status(ProposerSujets.Status.EN_ATTENTE)  // Modifié: statut -> status
                .binomeProposerPar(binome)  // Ajouté: pour établir la relation avec le binôme
                .build();
        
        proposition = proposerSujetsRepository.save(proposition);
        
        return PropositionDTO.builder()
                .id(proposition.getId())
                .titre(proposition.getTitre())
                .theme(proposition.getTheme())
                .description(proposition.getDescription())
                .statut(proposition.getStatus().toString())  // Modifié: getStatut -> getStatus
                .build();
    }
    
    // Mapping methods
    
    private SujetDTO mapToSujetDTO(Sujet sujet) {
        return SujetDTO.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .theme(sujet.getTheme())
                .description(sujet.getDescription())
                .build();
    }
    
    private BinomeDTO mapToBinomeDTO(Binome binome) {
        return BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToEtudiantDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToEtudiantDTO(binome.getEtudiant2()) : null)
                .encadrant(binome.getEncadrant() != null ? mapToEncadrantDTO(binome.getEncadrant()) : null)
                .sujet(binome.getSujet() != null ? mapToSujetDTO(binome.getSujet()) : null)
                .build();
    }
    
    private EtudiantDTO mapToEtudiantDTO(Utilisateur utilisateur) {
        return EtudiantDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
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
}