package ma.estfbs.pfe_management.service;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteAddRequest;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteDTO;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteEditRequest;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteManagementResponse;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;

@Service
@RequiredArgsConstructor
public class CompteManagementService {

    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final FiliereRepository filiereRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Characters used for random password generation
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Get accounts by role
     */
    public CompteManagementResponse getComptesByRole(Role role) {
        List<CompteDTO> comptes;
        
        if (role != null) {
            comptes = utilisateurRepository.findAll().stream()
                    .filter(user -> user.getRole() == role)
                    .map(this::mapToCompteDTO)
                    .collect(Collectors.toList());
        } else {
            comptes = utilisateurRepository.findAll().stream()
                    .filter(user -> 
                        user.getRole() == Role.ETUDIANT || 
                        user.getRole() == Role.ENCADRANT || 
                        user.getRole() == Role.JURY)
                    .map(this::mapToCompteDTO)
                    .collect(Collectors.toList());
        }
        
        List<FiliereDTO> filieres = filiereRepository.findAll().stream()
                .map(this::mapToFiliereDTO)
                .collect(Collectors.toList());
        
        return CompteManagementResponse.builder()
                .comptes(comptes)
                .filieres(filieres)
                .build();
    }
    
    /**
     * Add a new account
     */
    @Transactional
    public CompteDTO addCompte(CompteAddRequest request) {
        validateCompteRequest(request);
        
        // Generate email from nom and prenom
        String email = generateEmail(request.getPrenom(), request.getNom());
        
        // Generate random password
        String password = generateRandomPassword(10);
        
        // Create user entity
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(email)
                .cni(request.getRole() != Role.ETUDIANT ? request.getCni() : null)
                .cne(request.getRole() == Role.ETUDIANT ? request.getCne() : null)
                .dateNaissance(request.getDateNaissance())
                .motDePasse(passwordEncoder.encode(password))
                .role(request.getRole())
                .build();
        
        utilisateur = utilisateurRepository.save(utilisateur);
        
        // If the role is ETUDIANT, create an Etudiant entity
        if (request.getRole() == Role.ETUDIANT) {
            Filiere filiere = filiereRepository.findById(request.getFiliereId())
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + request.getFiliereId()));
            
            Etudiant etudiant = Etudiant.builder()
                    .utilisateur(utilisateur)
                    .filiere(filiere)
                    .build();
            
            etudiantRepository.save(etudiant);
        }
        
        // TODO: In a real application, send the generated password to the user via email
        // For now, we'll just print it to the console for testing
        System.out.println("Generated email: " + email);
        System.out.println("Generated password: " + password);
        
        return mapToCompteDTO(utilisateur);
    }
    
    /**
     * Edit an account
     */
    @Transactional
    public CompteDTO editCompte(Long id, CompteEditRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));
        
        // Validate CNI uniqueness if changed
        if (request.getCni() != null && !request.getCni().equals(utilisateur.getCni()) && 
            utilisateur.getRole() != Role.ETUDIANT) {
            if (utilisateurRepository.existsByCni(request.getCni())) {
                throw new RuntimeException("CNI déjà utilisé");
            }
        }
        
        // Validate CNE uniqueness if changed
        if (request.getCne() != null && !request.getCne().equals(utilisateur.getCne()) && 
            utilisateur.getRole() == Role.ETUDIANT) {
            if (utilisateurRepository.existsByCne(request.getCne())) {
                throw new RuntimeException("CNE déjà utilisé");
            }
        }
        
        // Update user properties
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        
        if (utilisateur.getRole() == Role.ETUDIANT) {
            utilisateur.setCne(request.getCne());
        } else {
            utilisateur.setCni(request.getCni());
        }
        
        utilisateur.setDateNaissance(request.getDateNaissance());
        
        utilisateur = utilisateurRepository.save(utilisateur);
        
        // If the role is ETUDIANT and filiereId is provided, update the Etudiant entity
        if (utilisateur.getRole() == Role.ETUDIANT && request.getFiliereId() != null) {
            Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + id));
            
            Filiere filiere = filiereRepository.findById(request.getFiliereId())
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + request.getFiliereId()));
            
            etudiant.setFiliere(filiere);
            etudiantRepository.save(etudiant);
        }
        
        return mapToCompteDTO(utilisateur);
    }
    
    /**
     * Delete an account
     */
    @Transactional
    public void deleteCompte(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));
        
        // If the role is ETUDIANT, we need to delete the Etudiant entity first
        if (utilisateur.getRole() == Role.ETUDIANT) {
            Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + id));
            
            etudiantRepository.delete(etudiant);
        }
        
        utilisateurRepository.deleteById(id);
    }
    
    /**
     * Validate compte request fields
     */
    private void validateCompteRequest(CompteAddRequest request) {
        if (request.getNom() == null || request.getNom().trim().isEmpty()) {
            throw new RuntimeException("Le nom est obligatoire");
        }
        
        if (request.getPrenom() == null || request.getPrenom().trim().isEmpty()) {
            throw new RuntimeException("Le prénom est obligatoire");
        }
        
        if (request.getRole() == null) {
            throw new RuntimeException("Le rôle est obligatoire");
        }
        
        if (request.getDateNaissance() == null) {
            throw new RuntimeException("La date de naissance est obligatoire");
        }
        
        // Validate role-specific fields
        if (request.getRole() == Role.ETUDIANT) {
            if (request.getCne() == null || request.getCne().trim().isEmpty()) {
                throw new RuntimeException("Le CNE est obligatoire pour les étudiants");
            }
            
            if (utilisateurRepository.existsByCne(request.getCne())) {
                throw new RuntimeException("CNE déjà utilisé");
            }
            
            if (request.getFiliereId() == null) {
                throw new RuntimeException("La filière est obligatoire pour les étudiants");
            }
        } else {
            if (request.getCni() == null || request.getCni().trim().isEmpty()) {
                throw new RuntimeException("Le CNI est obligatoire pour les encadrants et jurés");
            }
            
            if (utilisateurRepository.existsByCni(request.getCni())) {
                throw new RuntimeException("CNI déjà utilisé");
            }
        }
    }
    
    /**
     * Generate email from first and last name
     */
    private String generateEmail(String prenom, String nom) {
        // Remove accents and special characters
        String normalizedPrenom = Normalizer.normalize(prenom.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        String normalizedNom = Normalizer.normalize(nom.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        
        // Remove spaces
        normalizedPrenom = normalizedPrenom.replaceAll("\\s+", "");
        normalizedNom = normalizedNom.replaceAll("\\s+", "");
        
        String email = normalizedPrenom + normalizedNom + ".efb@usms.ac.ma";
        
        // Check if email already exists, add number if needed
        int counter = 1;
        String baseEmail = email;
        while (utilisateurRepository.findByEmail(email).isPresent()) {
            email = normalizedPrenom + normalizedNom + counter + ".efb@usms.ac.ma";
            counter++;
        }
        
        return email;
    }
    
    /**
     * Generate random password
     */
    private String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
    
    /**
     * Map Utilisateur entity to CompteDTO
     */
    private CompteDTO mapToCompteDTO(Utilisateur utilisateur) {
        String filiereName = null;
        
        // If the role is ETUDIANT, get the filiere name
        if (utilisateur.getRole() == Role.ETUDIANT) {
            Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                    .orElse(null);
            
            if (etudiant != null && etudiant.getFiliere() != null) {
                filiereName = etudiant.getFiliere().getNom();
            }
        }
        
        return CompteDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .cni(utilisateur.getCni())
                .cne(utilisateur.getCne())
                .dateNaissance(utilisateur.getDateNaissance())
                .role(utilisateur.getRole())
                .filiereName(filiereName)
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
