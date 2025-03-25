package ma.estfbs.pfe_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.EtudiantBinomeDTOs.BinomeRequestCreateDTO;
import ma.estfbs.pfe_management.dto.EtudiantBinomeDTOs.BinomeRequestResponseDTO;
import ma.estfbs.pfe_management.dto.EtudiantBinomeDTOs.BinomeSearchDTO;
import ma.estfbs.pfe_management.dto.EtudiantBinomeDTOs.ContinueSoloDTO;
import ma.estfbs.pfe_management.dto.EtudiantBinomeDTOs.EtudiantStatusDTO;
import ma.estfbs.pfe_management.dto.SujetDTO;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.EtudiantBinomeService;
import ma.estfbs.pfe_management.service.JwtService;

@RestController
@RequestMapping("/api/etudiant")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ETUDIANT')")
public class EtudiantController {

    private final EtudiantBinomeService etudiantBinomeService;
    private final JwtService jwtService;
    
    /**
     * Helper method to get the current authenticated user's ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Utilisateur) {
            return ((Utilisateur) auth.getPrincipal()).getId();
        } else {
            // Fall back to extracting from JWT token
            String token = jwtService.getTokenFromRequest();
            if (token != null) {
                return jwtService.extractUserId(token);
            }
        }
        throw new RuntimeException("Utilisateur non authentifié");
    }
    
    /**
     * Get the current status of the authenticated student
     */
    @GetMapping("/status")
    public ResponseEntity<EtudiantStatusDTO> getStatus() {
        Long etudiantId = getCurrentUserId();
        return ResponseEntity.ok(etudiantBinomeService.getEtudiantStatus(etudiantId));
    }
    
    /**
     * Get binome search data (available students and pending requests)
     */
    @GetMapping("/binome-search")
    public ResponseEntity<BinomeSearchDTO> getBinomeSearchData() {
        Long etudiantId = getCurrentUserId();
        return ResponseEntity.ok(etudiantBinomeService.getBinomeSearchData(etudiantId));
    }
    
    /**
     * Send a binome request to another student
     */
    @PostMapping("/binome-request")
    public ResponseEntity<BinomeRequestResponseDTO> createBinomeRequest(@RequestBody BinomeRequestCreateDTO request) {
        Long etudiantId = getCurrentUserId();
        return ResponseEntity.ok(etudiantBinomeService.createBinomeRequest(etudiantId, request));
    }
    
    /**
     * Accept a binome request
     */
    @PostMapping("/binome-request/{requestId}/accept")
    public ResponseEntity<BinomeRequestResponseDTO> acceptBinomeRequest(@PathVariable Long requestId) {
        Long etudiantId = getCurrentUserId();
        return ResponseEntity.ok(etudiantBinomeService.respondToBinomeRequest(etudiantId, requestId, true));
    }
    
    /**
     * Reject a binome request
     */
    @PostMapping("/binome-request/{requestId}/reject")
    public ResponseEntity<BinomeRequestResponseDTO> rejectBinomeRequest(@PathVariable Long requestId) {
        Long etudiantId = getCurrentUserId();
        return ResponseEntity.ok(etudiantBinomeService.respondToBinomeRequest(etudiantId, requestId, false));
    }
    
    /**
     * Cancel a sent binome request
     */
    @DeleteMapping("/binome-request/{requestId}")
    public ResponseEntity<BinomeRequestResponseDTO> cancelBinomeRequest(@PathVariable Long requestId) {
        Long etudiantId = getCurrentUserId();
        return ResponseEntity.ok(etudiantBinomeService.cancelBinomeRequest(etudiantId, requestId));
    }
    
    /**
     * Choose to continue solo
     */
    @PostMapping("/continue-solo")
    public ResponseEntity<ContinueSoloDTO> continueSolo() {
        Long etudiantId = getCurrentUserId();
        return ResponseEntity.ok(etudiantBinomeService.continueSolo(etudiantId));
    }
}
