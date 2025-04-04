package ma.estfbs.pfe_management.controller;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.EtudiantBinomeDTOs.*;
import ma.estfbs.pfe_management.service.EtudiantBinomeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etudiant")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
// MODIFICATION: Commenter l'annotation PreAuthorize pour déboguer
// @PreAuthorize("hasAuthority('ETUDIANT')")
public class EtudiantBinomeController {

    private final EtudiantBinomeService etudiantBinomeService;

    /**
     * Check the current student's binome status
     */
    @GetMapping("/binome/status")
    public ResponseEntity<BinomeStatusResponse> checkBinomeStatus() {
        return ResponseEntity.ok(etudiantBinomeService.checkBinomeStatus());
    }
    
    /**
     * Get pending binome requests for the current student
     */
    @GetMapping("/binome/requests")
    public ResponseEntity<BinomeRequestResponse> getPendingRequests() {
        return ResponseEntity.ok(etudiantBinomeService.getPendingRequests());
    }
    
    /**
     * Handle a binome request (accept or reject)
     */
    @PostMapping("/binome/requests/handle")
    public ResponseEntity<Void> handleBinomeRequest(@RequestBody BinomeRequestActionRequest request) {
        etudiantBinomeService.handleBinomeRequest(request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get available students for binome formation
     */
    @GetMapping("/binome/available-students")
    public ResponseEntity<AvailableStudentsResponse> getAvailableStudents() {
        return ResponseEntity.ok(etudiantBinomeService.getAvailableStudents());
    }
    
    /**
     * Send a binome request to another student
     */
    @PostMapping("/binome/requests/send")
    public ResponseEntity<Void> sendBinomeRequest(@RequestBody SendRequestRequest request) {
        etudiantBinomeService.sendBinomeRequest(request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Create a solo binome (student works alone)
     */
    @PostMapping("/binome/solo")
    public ResponseEntity<BinomeDTO> createSoloBinome() {
        return ResponseEntity.ok(etudiantBinomeService.createSoloBinome());
    }
    
    /**
     * Get available subjects for selection
     */
    @GetMapping("/sujets")
    public ResponseEntity<AvailableSujetsResponse> getAvailableSujets() {
        return ResponseEntity.ok(etudiantBinomeService.getAvailableSujets());
    }
    
    /**
     * Select a subject for a binome
     */
    @PostMapping("/sujets/select")
    public ResponseEntity<BinomeDTO> selectSujet(@RequestBody SelectSujetRequest request) {
        return ResponseEntity.ok(etudiantBinomeService.selectSujet(request));
    }
}