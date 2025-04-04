package ma.estfbs.pfe_management.controller;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.SujetDTOs.*;
import ma.estfbs.pfe_management.service.SujetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etudiant")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ETUDIANT')") // Add this line
public class SujetController {

    private final SujetService sujetService;

    /**
     * Get available subjects for the current student's binome
     */
    @GetMapping("/sujets-choix") // URL modifiée
    public ResponseEntity<AvailableSujetsResponse> getAvailableSujets() {
        return ResponseEntity.ok(sujetService.getAvailableSujets());
    }

    /**
     * Select a subject for the current student's binome
     */
    @PostMapping("/sujets-choix/select") // URL modifiée
    public ResponseEntity<BinomeSujetResponse> selectSujet(@RequestBody SelectSujetRequest request) {
        return ResponseEntity.ok(sujetService.selectSujet(request));
    }

    /**
     * Get a random subject for the current student's binome
     */
    @GetMapping("/sujets-choix/random") // URL modifiée
    public ResponseEntity<SujetDTO> getRandomSujet() {
        return ResponseEntity.ok(sujetService.getRandomSujet());
    }

    /**
     * Propose a new subject
     */
    @PostMapping("/proposer-sujet")
    public ResponseEntity<SujetDTO> proposerSujet(@RequestBody ProposerSujetRequest request) {
        return ResponseEntity.ok(sujetService.proposerSujet(request));
    }
}