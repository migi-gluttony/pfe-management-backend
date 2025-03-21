package ma.estfbs.pfe_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.estfbs.pfe_management.dto.RegisterRequest;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;

@Service
public class UtilisateurService {
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Find a user by email
     */
    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    /**
     * Create a new user
     */
    @Transactional
    public Utilisateur createUser(Utilisateur utilisateur) {
        // Check if email already exists
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        
        // Encode password
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        
        // Save user
        return utilisateurRepository.save(utilisateur);
    }
    
    /**
     * Update a user
     */
    @Transactional
    public Utilisateur updateUser(Long id, Utilisateur utilisateur) {
        Utilisateur existingUser = utilisateurRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Update user properties
        existingUser.setNom(utilisateur.getNom());
        existingUser.setPrenom(utilisateur.getPrenom());
        
        // Only update password if provided
        if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
            existingUser.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }
        
        // Save updated user
        return utilisateurRepository.save(existingUser);
    }
    
    /**
     * Delete a user
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        utilisateurRepository.deleteById(id);
    }
    
    /**
     * Change user's password
     */
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        Utilisateur utilisateur = findByEmail(email);
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, utilisateur.getMotDePasse())) {
            throw new RuntimeException("Invalid old password");
        }
        
        // Set new password
        utilisateur.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(utilisateur);
    }
}