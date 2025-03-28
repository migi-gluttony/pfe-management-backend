package ma.estfbs.pfe_management.service;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.BatchImportRequest;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.BatchImportResponse;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteAddRequest;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteDTO;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteEditRequest;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteManagementResponse;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.ImportItemResult;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.model.AnneeScolaire;
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
    private final AcademicYearService academicYearService;
    
    // Characters used for random password generation
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Get accounts by role for the current academic year
     */
    public CompteManagementResponse getComptesByRole(Role role) {
        List<CompteDTO> comptes;
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        if (role == Role.ETUDIANT) {
            // For students, only return those from the current year
            List<Etudiant> currentYearStudents = etudiantRepository.findByAnneeScolaire(currentYear);
            comptes = currentYearStudents.stream()
                    .map(etudiant -> mapToCompteDTO(etudiant.getUtilisateur(), etudiant))
                    .collect(Collectors.toList());
        } else if (role != null) {
            // For other roles (ENCADRANT, JURY), return all regardless of year
            comptes = utilisateurRepository.findAll().stream()
                    .filter(user -> user.getRole() == role)
                    .map(user -> mapToCompteDTO(user, null))
                    .collect(Collectors.toList());
        } else {
            // If no role specified, return all users but filter students by current year
            List<Utilisateur> nonStudents = utilisateurRepository.findAll().stream()
                    .filter(user -> user.getRole() == Role.ENCADRANT || user.getRole() == Role.JURY)
                    .collect(Collectors.toList());
            
            List<CompteDTO> nonStudentDTOs = nonStudents.stream()
                    .map(user -> mapToCompteDTO(user, null))
                    .collect(Collectors.toList());
            
            List<Etudiant> currentYearStudents = etudiantRepository.findByAnneeScolaire(currentYear);
            List<CompteDTO> studentDTOs = currentYearStudents.stream()
                    .map(etudiant -> mapToCompteDTO(etudiant.getUtilisateur(), etudiant))
                    .collect(Collectors.toList());
            
            comptes = new ArrayList<>();
            comptes.addAll(nonStudentDTOs);
            comptes.addAll(studentDTOs);
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
     * Add a new account - if student, associate with current year
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
        
        // If the role is ETUDIANT, create an Etudiant entity with current year
        Etudiant etudiant = null;
        if (request.getRole() == Role.ETUDIANT) {
            Filiere filiere = filiereRepository.findById(request.getFiliereId())
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + request.getFiliereId()));
            
            AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
            
            etudiant = Etudiant.builder()
                    .utilisateur(utilisateur)
                    .filiere(filiere)
                    .anneeScolaire(currentYear)
                    .build();
            
            etudiantRepository.save(etudiant);
        }
        
        // TODO: In a real application, send the generated password to the user via email
        // For now, we'll just print it to the console for testing
        System.out.println("Generated email: " + email);
        System.out.println("Generated password: " + password);
        
        return mapToCompteDTO(utilisateur, etudiant);
    }
    
    /**
     * Edit an account
     */
    @Transactional
    public CompteDTO editCompte(Long id, CompteEditRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));
        
        // For students, verify that they are from current year
        if (utilisateur.getRole() == Role.ETUDIANT) {
            Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + id));
            
            AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
            if (!etudiant.getAnneeScolaire().getId().equals(currentYear.getId())) {
                throw new RuntimeException("Impossible de modifier un étudiant d'une année précédente");
            }
        }
        
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
        Etudiant etudiant = null;
        if (utilisateur.getRole() == Role.ETUDIANT && request.getFiliereId() != null) {
            etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + id));
            
            Filiere filiere = filiereRepository.findById(request.getFiliereId())
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + request.getFiliereId()));
            
            etudiant.setFiliere(filiere);
            etudiant = etudiantRepository.save(etudiant);
        }
        
        return mapToCompteDTO(utilisateur, etudiant);
    }
    
    /**
     * Delete an account
     */
    @Transactional
    public void deleteCompte(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));
        
        // If the role is ETUDIANT, we need to verify it's from current year and delete the Etudiant entity first
        if (utilisateur.getRole() == Role.ETUDIANT) {
            Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + id));
            
            // Verify student is from current year
            AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
            if (!etudiant.getAnneeScolaire().getId().equals(currentYear.getId())) {
                throw new RuntimeException("Impossible de supprimer un étudiant d'une année précédente");
            }
            
            etudiantRepository.delete(etudiant);
        }
        
        utilisateurRepository.deleteById(id);
    }
    
    /**
     * Import multiple accounts in batch
     * For students, automatically assign the current academic year
     */
    @Transactional
    public BatchImportResponse importComptes(BatchImportRequest request) {
        List<ImportItemResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        // Process each account request in the batch
        for (CompteAddRequest compteRequest : request.getComptes()) {
            try {
                // Validate request
                validateCompteRequest(compteRequest);
                
                // Generate email and password
                String email = generateEmail(compteRequest.getPrenom(), compteRequest.getNom());
                String password = generateRandomPassword(10);
                
                // Create user entity
                Utilisateur utilisateur = Utilisateur.builder()
                        .nom(compteRequest.getNom())
                        .prenom(compteRequest.getPrenom())
                        .email(email)
                        .cni(compteRequest.getRole() != Role.ETUDIANT ? compteRequest.getCni() : null)
                        .cne(compteRequest.getRole() == Role.ETUDIANT ? compteRequest.getCne() : null)
                        .dateNaissance(compteRequest.getDateNaissance())
                        .motDePasse(passwordEncoder.encode(password))
                        .role(compteRequest.getRole())
                        .build();
                
                utilisateur = utilisateurRepository.save(utilisateur);
                
                // If the role is ETUDIANT, create an Etudiant entity with current year
                if (compteRequest.getRole() == Role.ETUDIANT) {
                    Filiere filiere = filiereRepository.findById(compteRequest.getFiliereId())
                            .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + compteRequest.getFiliereId()));
                    
                    Etudiant etudiant = Etudiant.builder()
                            .utilisateur(utilisateur)
                            .filiere(filiere)
                            .anneeScolaire(currentYear) // Use current year
                            .build();
                    
                    etudiantRepository.save(etudiant);
                }
                
                // Log generated credentials (in production, would send via email)
                System.out.println("Generated email for [" + compteRequest.getPrenom() + " " + compteRequest.getNom() + "]: " + email);
                System.out.println("Generated password: " + password);
                
                // Add success result
                results.add(ImportItemResult.builder()
                        .success(true)
                        .message("Compte créé avec succès. Email: " + email)
                        .data(compteRequest)
                        .build());
                
                successCount++;
            } catch (Exception e) {
                // Add failure result
                results.add(ImportItemResult.builder()
                        .success(false)
                        .message("Erreur: " + e.getMessage())
                        .data(compteRequest)
                        .build());
                
                failedCount++;
            }
        }
        
        // Build and return response
        return BatchImportResponse.builder()
                .results(results)
                .totalCount(request.getComptes().size())
                .successCount(successCount)
                .failedCount(failedCount)
                .build();
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
    private CompteDTO mapToCompteDTO(Utilisateur utilisateur, Etudiant etudiant) {
        String filiereName = null;
        
        // If the role is ETUDIANT, get the filiere name
        if (utilisateur.getRole() == Role.ETUDIANT) {
            if (etudiant == null) {
                etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                        .orElse(null);
            }
            
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